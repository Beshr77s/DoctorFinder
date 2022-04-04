package com.elmobarmij.chatting.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.elmobarmij.chatting.R;
import com.elmobarmij.chatting.databinding.ActivitySignUpBinding;
import com.elmobarmij.chatting.utiles.Constants;
import com.elmobarmij.chatting.utiles.ReferencesManger;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private String encodeImage;
    private ReferencesManger referencesManger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        referencesManger = new ReferencesManger(getApplicationContext());

        setListeners();

        if (referencesManger.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }
    private void setListeners(){
        binding.btnSignUp.setOnClickListener(v -> signUp());
        binding.selectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }
    private void signUp(){
        if (isValidSignUpDetails()){
            loading(true);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String , Object> user = new HashMap<>();
            user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
            user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
            user.put(Constants.KEY_PASSWORD,binding.inputPass.getText().toString());
            user.put(Constants.KEY_IMAGE,encodeImage);
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        referencesManger.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        referencesManger.putString(Constants.KEY_USER_ID,documentReference.getId());
                        referencesManger.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                        referencesManger.putString(Constants.KEY_IMAGE,encodeImage);
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        showToast("Sign Up Successfully !");
                    })
                    .addOnFailureListener(e -> {
                        loading(false);
                        showToast("Please Try again later !");
                    });
        }
    }
    private Boolean isValidSignUpDetails(){
        if (encodeImage.equals(null)){
            showToast("Please select profile image !");
            return false;
        }else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            binding.inputEmail.setError("Required failed");
            return false;
        }else if (binding.inputName.getText().toString().trim().isEmpty()){
            binding.inputName.setError("Required failed");
            return false;
        }else if (binding.inputPass.getText().toString().trim().isEmpty()){
            binding.inputPass.setError("Required failed");
            return false;
        }else if (!binding.inputPass.getText().toString().equals(binding.inputConfirmPass.getText().toString())){
            binding.inputConfirmPass.setError("Required failed");
            return false;
        }else{
            return true;
        }

    }
    private void showToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK){
                            if (result.getData() !=null){
                                Uri imageUri = result.getData().getData();
                                try {
                                    InputStream  inputStream = getContentResolver().openInputStream(imageUri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    binding.image.setImageBitmap(bitmap);
                                    encodeImage = encodeImage(bitmap);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private void loading(boolean isLoad){
        if (isLoad){
            binding.btnSignUp.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
        }else {
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
        }
    }

}
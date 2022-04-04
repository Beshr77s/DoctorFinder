package com.elmobarmij.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.elmobarmij.chatting.R;
import com.elmobarmij.chatting.databinding.ActivitySignInBinding;
import com.elmobarmij.chatting.utiles.Constants;
import com.elmobarmij.chatting.utiles.ReferencesManger;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private ReferencesManger referencesManger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        referencesManger = new ReferencesManger(getApplicationContext());
        startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        setListeners();

    }

    private void setListeners(){
        binding.btnSignIn.setOnClickListener(v ->{
            if (isValidSignInDetails()){
                signIn();
            }
        });

    }
    private void showToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails(){
      if (binding.inputEmail.getText().toString().trim().isEmpty()){
            binding.inputEmail.setError("Required failed !");
            return false;
        }else if (binding.inputPass.getText().toString().trim().isEmpty()){
            binding.inputPass.setError("Required failed !");
            return false;
        }else{
            return true;
        }

    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() !=null
                    && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                        referencesManger.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        referencesManger.putString(Constants.KEY_USER_ID,snapshot.getId());
                        referencesManger.putString(Constants.KEY_NAME,snapshot.getString(Constants.KEY_NAME));
                        referencesManger.putString(Constants.KEY_EMAIL,snapshot.getString(Constants.KEY_EMAIL));
                        referencesManger.putString(Constants.KEY_IMAGE,snapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        showToast("Sign Up Successfully !");                    }
                    else {
                        loading(false);
                        showToast("Email or Password are failed !");
                    }
                });
    }
    private void loading(boolean isLoading){
        if (isLoading){
            binding.btnSignIn.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
        }else {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
        }
    }

}
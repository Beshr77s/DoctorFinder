package com.elmobarmij.chatting.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.elmobarmij.chatting.databinding.ActivityPreviewBinding;
import com.elmobarmij.chatting.utiles.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PreviewActivity extends AppCompatActivity {
    private ActivityPreviewBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadImage(getIntent().getStringExtra(Constants.KEY_IMAGE));

        binding.imageSave.setOnClickListener(v -> {
            pickImage.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        });

    }

    private void loadImage(String encodeImage) {
        binding.imageView.setImageBitmap(encodeString(encodeImage));
        }
        private Bitmap encodeString(String encodeImage){
            if (encodeImage !=null) {
                byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            }else {
                return null;
            }
        }
    private final ActivityResultLauncher<String> pickImage =
          registerForActivityResult(
                  new ActivityResultContracts.RequestPermission(),
                  new ActivityResultCallback<Boolean>() {
                      @Override
                      public void onActivityResult(Boolean result) {
                        if (result){
                            saveImage(encodeString(getIntent().getStringExtra(Constants.KEY_IMAGE)));
                        }
                      }
                  }
          );

    private void saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + System.currentTimeMillis()+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(getApplicationContext(), "Image Saved !", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
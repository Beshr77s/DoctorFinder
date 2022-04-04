package com.elmobarmij.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.elmobarmij.chatting.R;
import com.elmobarmij.chatting.databinding.ActivityBaseBinding;
import com.elmobarmij.chatting.utiles.Constants;
import com.elmobarmij.chatting.utiles.ReferencesManger;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    private ActivityBaseBinding binding;
    private ReferencesManger referencesManger;
    private DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        referencesManger = new ReferencesManger(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(referencesManger.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,getReadAbleDate());
    }

    private String getReadAbleDate() {
        return new SimpleDateFormat("dd, -hh:mm a", Locale.US).format(new Date());

    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY,"Online");

    }
}
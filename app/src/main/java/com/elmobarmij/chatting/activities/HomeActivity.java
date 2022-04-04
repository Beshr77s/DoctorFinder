package com.elmobarmij.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.elmobarmij.chatting.R;
import com.elmobarmij.chatting.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
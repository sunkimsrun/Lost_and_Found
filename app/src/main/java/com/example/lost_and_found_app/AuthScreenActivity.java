package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lost_and_found_app.databinding.ActivityAuthScreenBinding;

public class AuthScreenActivity extends AppCompatActivity {

    private ActivityAuthScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submitBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AuthScreenActivity.this, HomeActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

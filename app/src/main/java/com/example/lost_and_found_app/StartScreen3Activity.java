package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.example.lost_and_found_app.databinding.ActivityStartScreen3Binding;

public class StartScreen3Activity extends BaseActivity {

    ActivityStartScreen3Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityStartScreen3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView3.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen3Activity.this, StartScreen4Activity.class);
            startActivity(intent);
        });

        binding.textView2.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen3Activity.this, LoginActivity.class);
            startActivity(intent);
        });

        }
    }
package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lost_and_found_app.databinding.ActivityStartScreen4Binding;

public class StartScreen4Activity extends AppCompatActivity {

    ActivityStartScreen4Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityStartScreen4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView3.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen4Activity.this, LoginActivity.class);
            startActivity(intent);
        });

        binding.textView3.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen4Activity.this, StartScreen5Activity.class);
            startActivity(intent);
        });

        binding.prev.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen4Activity.this, StartScreen3Activity.class);
            startActivity(intent);
        });

    }
}
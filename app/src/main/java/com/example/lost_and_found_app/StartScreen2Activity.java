package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.example.lost_and_found_app.databinding.ActivityStartScreen2Binding;

public class StartScreen2Activity extends BaseActivity {

    ActivityStartScreen2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityStartScreen2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.selectedEnglish.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen2Activity.this, StartScreen3Activity.class);
            startActivity(intent);
        });
    }
}

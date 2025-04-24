package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lost_and_found_app.databinding.ActivityStartScreen3Binding;
import com.example.lost_and_found_app.databinding.ActivityStartScreen5Binding;

public class StartScreen5Activity extends AppCompatActivity {

    ActivityStartScreen5Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityStartScreen5Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.prev.setOnClickListener(v->{
            Intent intent = new Intent(this, StartScreen4Activity.class);
            startActivity(intent);
        });

        binding.textView3.setOnClickListener(v->{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
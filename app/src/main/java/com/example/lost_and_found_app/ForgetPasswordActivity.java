package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.lost_and_found_app.databinding.ActivityForgetPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends BaseActivity {

    ActivityForgetPasswordBinding binding;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean account = getIntent().getBooleanExtra("openFromAccount", false);

        if(account){
            binding.tvBack.setText("Back");
            binding.tvBack.setOnClickListener(v -> {
                onBackPressed();
            });
        }else {
            binding.tvBack.setOnClickListener(v -> {
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        binding.btnReset.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                binding.etEmail.setError("Email is required!");
                return;
            }

            binding.loadingBar.setVisibility(View.VISIBLE);

            binding.btnGoLogin.setOnClickListener(view -> {
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });


            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        binding.loadingBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            binding.showingMessage.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Reset email sent to " + email, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }
}
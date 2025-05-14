package com.example.lost_and_found_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lost_and_found_app.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupPasswordToggle(binding.etPassword, true);

        binding.tvSkipForNow.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        });

        binding.tvForgetPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
        });

        binding.tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.btnLogIn.setOnClickListener(view -> {
            String userInput = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(userInput)) {
                binding.etEmail.setError("Please enter a valid email!!");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                binding.etPassword.setError("Password is required!");
                return;
            }

            loginWithEmail(userInput, password);
        });
    }

    private void loginWithEmail(String email, String password) {
        binding.loadingBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.loadingBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AuthScreenActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " +
                                Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText editText, boolean isMainPassword) {
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    if (isMainPassword) {
                        isPasswordVisible = !isPasswordVisible;
                        updatePasswordVisibility(editText, isPasswordVisible);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private void updatePasswordVisibility(EditText editText, boolean visible) {
        if (visible) {
            editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_lock, 0, R.drawable.ico_eye_closed, 0);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_lock, 0, R.drawable.ico_eye_open, 0);
        }
        editText.setSelection(editText.getText().length());
    }

}

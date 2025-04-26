package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lost_and_found_app.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        binding.skipForNow.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        });

        binding.forgotPass.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            FirebaseAuth.getInstance().sendPasswordResetEmail("sunkimsrun123@gmail.com");
        });

        binding.signUp.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.frame3.setOnClickListener(view -> {
            String userInput = binding.rectangleUser.getText().toString().trim();
            String password = binding.rectanglePassword.getText().toString().trim();

            if (TextUtils.isEmpty(userInput)) {
                binding.rectangleUser.setError("Username or Email is required!");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                binding.rectanglePassword.setError("Password is required!");
                return;
            }

            if (userInput.contains("@")) {
                loginWithEmail(userInput, password);
            } else {
                fetchEmailByUsername(userInput, password);
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchEmailByUsername(String username, String password) {
        usersRef.orderByChild("username").equalTo(username)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            String email = snapshot.child("email").getValue(String.class);
                            if (email != null) {
                                loginWithEmail(email, password);
                                return;
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "No account found with this username", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lost_and_found_app.databinding.ActivityRegisterBinding;
import com.example.lost_and_found_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    //private RelativeLayout loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        binding.signUp.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        binding.frame3.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = binding.usernameAuth.getText().toString().trim();
        String email = binding.emailAuth.getText().toString().trim();
        String password = binding.passwordAuth.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordAuth.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.usernameAuth.setError("Username is required!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.emailAuth.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passwordAuth.setError("Password is required!");
            return;
        }
        if (password.length() < 8) {
            binding.passwordAuth.setError("Password must be at least 8 characters!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordAuth.setError("Passwords do not match!");
            return;
        }

        usersRef.orderByChild("username").equalTo(username)
                .get().addOnCompleteListener(checkTask -> {
                    if (checkTask.isSuccessful() && checkTask.getResult().exists()) {
                        binding.usernameAuth.setError("Username already taken!");
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            saveUserToDatabase(user.getUid(), username, email);
                                        }
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration failed: "
                                                + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    private void saveUserToDatabase(String userId, String username, String email) {
        User newUser = new User(userId, username, email);

        usersRef.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save your data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

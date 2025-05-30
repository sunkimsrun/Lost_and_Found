package com.example.lost_and_found_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.lost_and_found_app.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Event Listeners
        binding.btnGoLogin.setOnClickListener(v -> signInWithGoogle());
        binding.tvSkipForNow.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, HomeActivity.class)));
        binding.tvForgetPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));
        binding.tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        binding.btnLogIn.setOnClickListener(v -> handleEmailLogin());

        setupPasswordToggle(binding.etPassword, true);
    }

    private void handleEmailLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Please enter email!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Invalid email format!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required!");
            return;
        }

        loginWithEmail(email, password);
    }

    private void loginWithEmail(String email, String password) {
        binding.loadingBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.loadingBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserProfile(user);
                    } else {
                        Toast.makeText(this, "Login failed: " +
                                Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        binding.loadingBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    binding.loadingBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

                        userRef.get().addOnCompleteListener(snapshotTask -> {
                            if (snapshotTask.isSuccessful()) {
                                DataSnapshot snapshot = snapshotTask.getResult();
                                if (!snapshot.exists() || task.getResult().getAdditionalUserInfo().isNewUser()) {
                                    HashMap<String, Object> userData = new HashMap<>();
                                    userData.put("isProfileComplete", false);

                                    userRef.setValue(userData);

                                    Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                    intent.putExtra("continueWithGoogle", true);
                                    intent.putExtra("username", user.getDisplayName());
                                    intent.putExtra("email", user.getEmail());

                                    startActivity(intent);
                                } else {
                                    checkUserProfile(user);
                                }
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserProfile(FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                String gender = snapshot.child("gender").getValue(String.class);
                String phone = snapshot.child("phoneNumber").getValue(String.class);
                String profileImage = snapshot.child("profileImageUrl").getValue(String.class);

                boolean isComplete = gender != null && !gender.isEmpty()
                        && phone != null && !phone.isEmpty()
                        && profileImage != null && !profileImage.isEmpty();

                if (isComplete) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, AuthScreenActivity.class));
                } else {
                    Toast.makeText(this, "Please complete your profile to continue.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
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

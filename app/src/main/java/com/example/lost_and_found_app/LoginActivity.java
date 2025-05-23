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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

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

        binding.btnGoLogin.setOnClickListener(v -> signInWithGoogle());

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
                binding.etEmail.setError("Please enter email or phone number!");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.etPassword.setError("Password is required!");
                return;
            }

            if (android.util.Patterns.EMAIL_ADDRESS.matcher(userInput).matches()) {
                loginWithEmail(userInput, password);
            } else if (userInput.matches("^\\+?[0-9]{10,13}$")) {
                loginWithPhone(userInput, password);
            } else {
                binding.etEmail.setError("Invalid email or phone number format!");
            }
        });
    }

    public void loginWithPhone(String phoneNumber, String password) {
        binding.loadingBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(phoneNumber + "@phoneuser.com", password)
                .addOnCompleteListener(task -> {
                    binding.loadingBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AuthScreenActivity.class));
                        finish();
                    } else {
                        String errorMsg = "Login failed";
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMsg = "Invalid phone or password!";
                        } else if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
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
                        Toast.makeText(this, "Google Sign-In Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AuthScreenActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

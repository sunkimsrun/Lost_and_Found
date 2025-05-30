package com.example.lost_and_found_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lost_and_found_app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    private static final int REQUEST_CODE_IMAGE_PICK = 1001;
    private static final int REQUEST_PERMISSION_CODE = 2001;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.profileImage.setImageURI(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.phoneNumberAuth.setText("+855 ");
        binding.phoneNumberAuth.setSelection(binding.phoneNumberAuth.getText().length());

        binding.phoneNumberAuth.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString();

                if (!input.startsWith("+855 ")) {
                    String cleanInput = input.replace("+855 ", "").replace("+855", "");
                    binding.phoneNumberAuth.setText("+855 " + cleanInput);
                    binding.phoneNumberAuth.setSelection(binding.phoneNumberAuth.getText().length());
                } else if (input.length() > 6) {
                    char firstDigit = input.charAt(6);
                    if (firstDigit == '0') {
                        Toast.makeText(ProfileActivity.this, "First number after +855 cannot be 0", Toast.LENGTH_SHORT).show();
                        binding.phoneNumberAuth.setText("+855 ");
                        binding.phoneNumberAuth.setSelection(binding.phoneNumberAuth.getText().length());
                    }
                }

                isFormatting = false;
            }
        });


        binding.inputPerson.setOnClickListener(v -> {
            if (hasGalleryPermission()) {
                openGallery();
            } else {
                requestGalleryPermission();
            }
        });

        binding.button.setOnClickListener(v -> uploadProfile());
    }

    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Permission required to pick an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfile() {
        binding.loadingBar.setVisibility(View.VISIBLE);

        boolean google = getIntent().getBooleanExtra("continueWithGoogle", false);
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        String gender = binding.genderAuthSpinner.getSelectedItem().toString();
        String phone = binding.phoneNumberAuth.getText().toString();

        if (google) {
            FirebaseUser user = auth.getCurrentUser();

            if (phone.length() > 6 && phone.charAt(5) == '0') {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Phone number after +855 cannot start with 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user != null) {
                Log.d("phone6", "Character at 6 : " + phone.charAt(6));
                saveProfileToFirebase(user.getUid(), user.getEmail(), username, gender, phone);
            } else {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
        else {

            if (username == null || username.isEmpty() || phone.isEmpty() || imageUri == null) {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.length() > 6 && phone.charAt(5) == '0') {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Phone number after +855 cannot start with 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(registerTask -> {
                if (registerTask.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        saveProfileToFirebase(user.getUid(), email, username, gender, phone);
                    } else {
                        binding.loadingBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Registration failed: " + registerTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfileToFirebase(String userId, String email, String username, String gender, String phone) {
        StorageReference imageRef = storage.getReference().child("profile_images/" + userId + ".jpg");

        imageRef.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                HashMap<String, Object> profileData = new HashMap<>();
                profileData.put("username", username);
                profileData.put("email", email);
                profileData.put("gender", gender);
                profileData.put("phoneNumber", phone);
                profileData.put("profileImageUrl", downloadUri.toString());
                profileData.put("isProfileComplete", true);

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                userRef.setValue(profileData)
                        .addOnSuccessListener(unused -> {
                            binding.loadingBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, AuthScreenActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            binding.loadingBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to upload image: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

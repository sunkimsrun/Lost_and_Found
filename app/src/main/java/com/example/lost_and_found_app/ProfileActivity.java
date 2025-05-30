package com.example.lost_and_found_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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
    private FirebaseDatabase firebaseDatabase;
    private static final int REQUEST_PERMISSIONS = 1003;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            binding.profileImage.setImageURI(imageUri);
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
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
        firebaseDatabase = FirebaseDatabase.getInstance();

        setupPhoneNumberInput();
        setupImagePicker();
        setupSaveButton();
    }

    private void setupPhoneNumberInput() {
        binding.phoneNumberAuth.setText("+855 ");
        binding.phoneNumberAuth.setSelection(binding.phoneNumberAuth.getText().length());

        binding.phoneNumberAuth.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
    }

    private void setupImagePicker() {
        binding.inputPerson.setOnClickListener(v -> {
            if (hasPermissions()) {
                showImagePickerDialog();
            } else {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS);
            }
        });
    }

    private void setupSaveButton() {
        binding.button.setOnClickListener(v -> uploadProfile());
    }

    private void showImagePickerDialog() {
        if (!hasGalleryPermission()) {
            requestGalleryPermission();
            return;
        }

        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(cameraIntent);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intent);
            }
        });
        builder.show();
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
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 2001);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showImagePickerDialog();
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
                saveProfileToFirebase(user.getUid(), user.getEmail(), username, gender, phone);
            } else {
                binding.loadingBar.setVisibility(View.GONE);
                Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
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

    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void saveProfileToFirebase(String userId, String email, String username, String gender, String phone) {
        if (imageUri == null) {
            binding.loadingBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference imageRef = storage.getReference("profile_images/" + userId + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            HashMap<String, Object> profileData = new HashMap<>();
                            profileData.put("username", username);
                            profileData.put("email", email);
                            profileData.put("gender", gender);
                            profileData.put("phoneNumber", phone);
                            profileData.put("profileImageUrl", uri.toString());
                            profileData.put("isProfileComplete", true);

                            DatabaseReference userRef = firebaseDatabase.getReference("Users").child(userId);
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
                        }))
                .addOnFailureListener(e -> {
                    binding.loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
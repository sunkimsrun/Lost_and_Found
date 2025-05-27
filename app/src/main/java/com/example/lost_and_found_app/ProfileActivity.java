package com.example.lost_and_found_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        String gender = binding.genderAuthSpinner.getSelectedItem().toString();
        String phone = binding.phoneNumberAuth.getText().toString();

        if (imageUri == null || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(registerTask -> {
                    if (registerTask.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String userId = user.getUid();
                        StorageReference imageRef = storage.getReference().child("profile_images/" + userId + ".jpg");

                        imageRef.putFile(imageUri).continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
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
                                            auth.signInWithEmailAndPassword(email, password)
                                                    .addOnSuccessListener(authResult -> {
                                                        binding.loadingBar.setVisibility(View.GONE);
                                                        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(this, AuthScreenActivity.class));
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        binding.loadingBar.setVisibility(View.GONE);
                                                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            binding.loadingBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
                    } else {
                        binding.loadingBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration failed: " + registerTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.lost_and_found_app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private Uri imageUri;
    private HomeActivity homeActivity;

    public AccountFragment() {}
    // Launch image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            uploadImageToFirebase();
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeActivity = (HomeActivity) getActivity();

        updateUserDisplay();

        // Click listeners for navigation
        binding.accountInfoItem.setOnClickListener(v -> loadFragment(new AccountInformationFragment()));
        binding.managePostItem.setOnClickListener(v -> loadFragment(new ManagePostFragment()));
        binding.changePasswordItem.setOnClickListener(v -> loadFragment(new ChangePasswordFragment()));

        // Profile image edit icon click
        binding.editIcon.setOnClickListener(v -> openGallery());

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (!isAdded() || getActivity() == null) return;
            Fragment currentFragment = getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof AccountInformationFragment) {
                updateUserDisplay();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Image"));
    }

    // Upload image to Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri == null) return;

        showProgressBar();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            hideProgressBar();
            return;
        }

        StorageReference imageRef = firebaseStorage.getReference("profile_images/" + user.getUid() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            saveImageUrlToDatabase(uri.toString());
                            Glide.with(requireContext()).load(uri.toString()).into(binding.profileImage);
                            hideProgressBar();
                            Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Save image URL in Realtime Database
    private void saveImageUrlToDatabase(String url) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(user.getUid());
        userRef.child("profileImageUrl").setValue(url);
    }

    public void updateUserDisplay() {
        showProgressBar();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            getUserDataFromDatabase(userID);
        } else {
            hideProgressBar();
        }
    }

    private void getUserDataFromDatabase(String userID) {
        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(userID);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) {
                    hideProgressBar();
                    return;
                }

                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    if (username != null) {
                        binding.nameText.setText(username);
                    }

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .centerCrop()
                                .into(binding.profileImage);
                    }
                }

                hideProgressBar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch user data: " + error.getMessage());
                hideProgressBar();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showProgressBar() {
        if (homeActivity != null) homeActivity.showProgressBar();
    }

    private void hideProgressBar() {
        if (homeActivity != null) homeActivity.hideProgressBar();
    }
}

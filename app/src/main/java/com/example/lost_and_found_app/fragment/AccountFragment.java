package com.example.lost_and_found_app.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.adapter.PostCardAdapter;
import com.example.lost_and_found_app.databinding.FragmentAccountBinding;
import com.example.lost_and_found_app.model.PostCard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.*;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private PostCardAdapter adapter;
    private Uri imageUri;
    private HomeActivity homeActivity;
    private String userIdToUse;
    private static final int CAMERA_PERMISSION_CODE = 1001;
    private static final int GALLERY_PERMISSION_CODE = 1002;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            uploadImageToFirebase();
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        if (currentUserId == null && (getArguments() == null || !getArguments().containsKey("userId"))) {
            Toast.makeText(getContext(), "No user found.", Toast.LENGTH_SHORT).show();
            return;
        }

        userIdToUse = getArguments() != null && getArguments().containsKey("userId")
                ? getArguments().getString("userId")
                : currentUserId;

        boolean isViewingOwnAccount = currentUser != null && currentUser.getUid().equals(userIdToUse);

        binding.tvBack.setVisibility(isViewingOwnAccount ? View.GONE : View.VISIBLE);
        binding.editIcon.setVisibility(isViewingOwnAccount ? View.VISIBLE : View.GONE);
        binding.button2.setVisibility(isViewingOwnAccount ? View.VISIBLE : View.GONE);

        updateUserDisplay(userIdToUse);

        setupRecyclerView(view);
        setupNavigationBack(view);
        setupRadioGroup(view);

        binding.editIcon.setOnClickListener(v -> showImagePickerDialog());

        binding.button2.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.LoadFragment(new AccountInformationFragment());
            }
        });

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof AccountInformationFragment)) {
                updateUserDisplay(userIdToUse);
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermission();
            } else {
                checkGalleryPermission();
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            launchGallery();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGallery();
            } else {
                Toast.makeText(getContext(), "Storage permission is required to select images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchCamera() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            imageUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rcv_list_post);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostCardAdapter(userIdToUse);
        recyclerView.setAdapter(adapter);
    }

    private void setupNavigationBack(View view) {
        if (homeActivity == null) return;

        int selectedId = Objects.requireNonNull(homeActivity.binding.navigationView.getCheckedItem()).getItemId();
        String backText;
        Fragment destinationFragment;

        if (selectedId == R.id.nav_found) {
            backText = "View found items";
            destinationFragment = new ViewFoundFragment();
        } else if (selectedId == R.id.nav_lost) {
            backText = "View lost items";
            destinationFragment = new ViewLostFragment();
        } else {
            backText = "Home";
            destinationFragment = new HomeFragment();
        }

        binding.tvBack.setText(backText);
        binding.tvBack.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.LoadFragment(destinationFragment);
            }
        });
    }

    private void setupRadioGroup(View view) {
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId != -1) {
            RadioButton selected = view.findViewById(selectedId);
            handleRadioSelection(selected.getText().toString());
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selected = view.findViewById(checkedId);
                handleRadioSelection(selected.getText().toString());
            }
        });
    }

    private void handleRadioSelection(String selectedText) {
        String lower = selectedText.toLowerCase(Locale.ROOT);
        if ("lost".equals(lower)) {
            loadPosts(userIdToUse, "lostitems");
        } else if ("found".equals(lower)) {
            loadPosts(userIdToUse, "founditems");
        }
    }

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
                            Glide.with(requireContext()).load(uri).into(binding.profileImage);
                            hideProgressBar();
                            Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveImageUrlToDatabase(String url) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        firebaseDatabase.getReference("Users").child(user.getUid()).child("profileImageUrl").setValue(url);
    }

    public void updateUserDisplay(String userId) {
        showProgressBar();
        if (userId != null) {
            getUserDataFromDatabase(userId);
        } else {
            hideProgressBar();
        }
    }

    private void getUserDataFromDatabase(String userID) {
        firebaseDatabase.getReference("Users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded() || binding == null) {
                            hideProgressBar();
                            return;
                        }

                        if (snapshot.exists()) {
                            binding.nameText.setText(snapshot.child("username").getValue(String.class));
                            binding.email.setText(snapshot.child("email").getValue(String.class));
                            binding.phoneNumber.setText(snapshot.child("phoneNumber").getValue(String.class));
                            binding.gender.setText(snapshot.child("gender").getValue(String.class));

                            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(requireContext()).load(imageUrl).centerCrop().into(binding.profileImage);
                            }
                        }

                        hideProgressBar();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideProgressBar();
                    }
                });
    }

    private void loadPosts(String userId, String ref) {
        firebaseDatabase.getReference(ref).orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<PostCard> posts = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PostCard post = snapshot.getValue(PostCard.class);
                            if (post != null) posts.add(post);
                        }
                        binding.noFound.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                        adapter.setPostCards(posts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserDisplay(userIdToUse);
    }

    private void showProgressBar() {
        if (homeActivity != null) homeActivity.showProgressBar();
    }

    private void hideProgressBar() {
        if (homeActivity != null) homeActivity.hideProgressBar();
    }
}
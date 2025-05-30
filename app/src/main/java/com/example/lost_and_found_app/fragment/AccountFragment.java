package com.example.lost_and_found_app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private PostCardAdapter adapter;
    private Uri imageUri;
    private HomeActivity homeActivity;
    private String userIdToUse;

    public AccountFragment() {}

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        userIdToUse = getArguments() != null && getArguments().containsKey("userId")
                ? getArguments().getString("userId")
                : currentUserId;

        if (userIdToUse == null) {
            Toast.makeText(getContext(), "No user found.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null && !currentUser.getUid().equals(userIdToUse) || currentUserId == null ) {
            binding.tvBack.setVisibility(View.VISIBLE);
            binding.editIcon.setVisibility(View.GONE);
            binding.button2.setVisibility(View.GONE);
        }

        updateUserDisplay(userIdToUse);

        RecyclerView recyclerView = view.findViewById(R.id.rcv_list_post);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostCardAdapter(userIdToUse);
        recyclerView.setAdapter(adapter);

        if (homeActivity == null) return;

        int selectedId = Objects.requireNonNull(homeActivity.binding.navigationView.getCheckedItem()).getItemId();
        String backText = "Back";
        Fragment destinationFragment = null;

        if (selectedId == R.id.nav_found) {
            backText = "View found items";
            destinationFragment = new ViewFoundFragment();
        } else if (selectedId == R.id.nav_lost) {
            backText = "View lost items";
            destinationFragment = new ViewLostFragment();
        } else if (selectedId == R.id.nav_home) {
            backText = "Home";
            destinationFragment = new HomeFragment();
        }

        binding.tvBack.setText(backText);

        Fragment finalDestination = destinationFragment;
        binding.tvBack.setOnClickListener(v -> {
            if (homeActivity != null && finalDestination != null) {
                homeActivity.LoadFragment(finalDestination);
            }
        });

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        int selectedRadioId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioId != -1) {
            RadioButton selectedRadioButton = view.findViewById(selectedRadioId);
            String selectedText = selectedRadioButton.getText().toString().toLowerCase(Locale.ROOT);
            if ("lost".equals(selectedText)) {
                loadPosts(userIdToUse, "lostitems");
            } else if ("found".equals(selectedText)) {
                loadPosts(userIdToUse, "founditems");
            }
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selectedRadioButton = view.findViewById(checkedId);
                String selectedText = selectedRadioButton.getText().toString().toLowerCase(Locale.ROOT);
                if ("lost".equals(selectedText)) {
                    loadPosts(userIdToUse, "lostitems");
                } else if ("found".equals(selectedText)) {
                    loadPosts(userIdToUse, "founditems");
                }
            }
        });

        binding.editIcon.setOnClickListener(v -> openGallery());

        binding.button2.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.LoadFragment(new AccountInformationFragment());
            }
        });

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = requireActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof AccountInformationFragment)) {
                updateUserDisplay(userIdToUse);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Image"));
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
                            Glide.with(requireContext()).load(uri.toString()).into(binding.profileImage);
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
        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(user.getUid());
        userRef.child("profileImageUrl").setValue(url);
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
                    String email = snapshot.child("email").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    if (username != null || email != null || phoneNumber != null || gender != null) {
                        binding.nameText.setText(username);
                        binding.email.setText(email);
                        binding.phoneNumber.setText(phoneNumber);
                        binding.gender.setText(gender);
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

    private void loadPosts(String currentUserId, String ref) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference(ref);

        itemsRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<PostCard> posts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    PostCard post = postSnapshot.getValue(PostCard.class);
                    if (post != null) {
                        posts.add(post);
                    }
                }
                if(posts.isEmpty()){
                    binding.noFound.setVisibility(View.VISIBLE);
                }else{
                    binding.noFound.setVisibility(View.GONE);
                }

                adapter.setPostCards(posts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
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

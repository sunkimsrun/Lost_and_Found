package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;

    HomeActivity homeActivity;

    public AccountFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeActivity = (HomeActivity) getActivity();

        updateUserDisplay();

        binding.accountInfoItem.setOnClickListener(v -> loadFragment(new AccountInformationFragment()));
        binding.managePostItem.setOnClickListener(v -> loadFragment(new ManagePostFragment()));
        binding.changePasswordItem.setOnClickListener(v -> loadFragment(new ChangePasswordFragment()));

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = requireActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof AccountInformationFragment) {
                updateUserDisplay();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserDisplay();
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

    private void loadFragment(Fragment fragment) {
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void getUserDataFromDatabase(String userID) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userID);

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

    private void showProgressBar() {
        if (homeActivity != null) {
            homeActivity.showProgressBar();
        }
    }

    private void hideProgressBar() {
        if (homeActivity != null) {
            homeActivity.hideProgressBar();
        }
    }
}

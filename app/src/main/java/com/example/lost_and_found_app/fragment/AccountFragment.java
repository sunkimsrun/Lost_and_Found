package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

    public AccountFragment() {
        // Required empty public constructor
    }

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

        updateUserDisplay();

        binding.accountInfoItem.setOnClickListener(v ->
                loadFragment(new AccountInformationFragment()));

        binding.managePostItem.setOnClickListener(v ->
                loadFragment(new ManagePostFragment()));

        binding.changePasswordItem.setOnClickListener(v ->
                loadFragment(new ChangePasswordFragment()));

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = requireActivity().getSupportFragmentManager()
                    .findFragmentById(com.example.lost_and_found_app.R.id.fragment_container);
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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            getUsernameFromDatabase(userID);
        }
    }

    private void loadFragment(Fragment fragment) {
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void getUsernameFromDatabase(String userID) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userID);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded() || binding == null) return;

                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.getValue(String.class);
                    if (username != null) {
                        binding.nameText.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to fetch username: " + databaseError.getMessage());
            }
        });
    }
}

package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.model.User;

public class AccountFragment extends Fragment {

    private TextView nameText, detailsText;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.nameText);
        detailsText = view.findViewById(R.id.detailsText);

        updateUserDisplay();

        view.findViewById(R.id.account_info_item).setOnClickListener(v ->
                loadFragment(new AccountInformationFragment()));

        view.findViewById(R.id.manage_post_item).setOnClickListener(v ->
                loadFragment(new ManagePostFragment()));

        view.findViewById(R.id.change_password_item).setOnClickListener(v ->
                loadFragment(new ChangePasswordFragment()));

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
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

    private void updateUserDisplay() {
        User user = User.currentUser;
        if (user != null) {
            nameText.setText(user.name);
            detailsText.setText("Gender: " + user.gender + " | DOB: " + user.dob);
        }
    }

    private void loadFragment(Fragment fragment) {
        FrameLayout container = requireActivity().findViewById(R.id.fragment_container);
        container.setVisibility(View.VISIBLE);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

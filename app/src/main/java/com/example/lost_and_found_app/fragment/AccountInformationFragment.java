package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.model.User;

public class AccountInformationFragment extends Fragment {

    private TextView usernameText, emailText, genderText, phoneText, dobText, passwordText;

    private RelativeLayout backButton, topPanel;
    private ImageView editUsername, editEmail, editGender, editPhoneNumber, editDOB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account_information, container, false);

        usernameText = view.findViewById(R.id.username);
        emailText = view.findViewById(R.id.email);
        genderText = view.findViewById(R.id.gender);
        phoneText = view.findViewById(R.id.phone);
        dobText = view.findViewById(R.id.birth_of_date);

        User user = User.currentUser;
        usernameText.setText(user.name);
        emailText.setText(user.email);
        genderText.setText(user.gender);
        phoneText.setText(user.phone);
        dobText.setText(user.dob);

        backButton = view.findViewById(R.id.back_button);
        topPanel = view.findViewById(R.id.top_panel);
        editUsername = view.findViewById(R.id.edit_username);
        editEmail = view.findViewById(R.id.edit_email);
        editGender = view.findViewById(R.id.edit_gender);
        editPhoneNumber = view.findViewById(R.id.edit_phnumber);
        editDOB = view.findViewById(R.id.edit_dob);

        View.OnClickListener closeFragment = v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        };

        backButton.setOnClickListener(closeFragment);
        topPanel.setOnClickListener(closeFragment);

        // Open the appropriate fragment when an edit icon is clicked
        editUsername.setOnClickListener(v -> openFragment(ChangeFieldFragment.newInstance(
                "Change Username", "name", "Enter new username", R.drawable.user)));

        editEmail.setOnClickListener(v -> openFragment(ChangeFieldFragment.newInstance(
                "Change Email", "email", "Enter new email", R.drawable.email)));

        editGender.setOnClickListener(v -> openFragment(new ChangeGenderFragment()));

        editPhoneNumber.setOnClickListener(v -> openFragment(ChangeFieldFragment.newInstance(
                "Change PhoneNumber", "phone", "Enter new phone number", R.drawable.phone)));

        editDOB.setOnClickListener(v -> openFragment(new ChangeBirthDateFragment()));

        return view;
    }

    void updateUserData(String newName, String newGender, String newDOB, String newPassword) {
        User.currentUser.name = newName;
        User.currentUser.gender = newGender;
        User.currentUser.dob = newDOB;
        User.currentUser.password = newPassword;

        usernameText.setText(newName);
        genderText.setText(newGender);
        dobText.setText(newDOB);
        passwordText.setText(newDOB);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof AccountFragment) {
            ((AccountFragment) parentFragment).updateUserDisplay();
        }
    }

    private void openFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

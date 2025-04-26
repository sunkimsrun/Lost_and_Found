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

    private TextView usernameText, emailText, genderText, phoneText, passwordText;
    private RelativeLayout backButton, topPanel;
    private ImageView editUsername, editEmail, editGender, editPhoneNumber;

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

        User user = User.currentUser;
        usernameText.setText(user.name);
        emailText.setText(user.email);
        genderText.setText(user.gender);
        phoneText.setText(user.phone);

        backButton = view.findViewById(R.id.back_button);
        topPanel = view.findViewById(R.id.top_panel);
        editUsername = view.findViewById(R.id.edit_username);
        editEmail = view.findViewById(R.id.edit_email);
        editGender = view.findViewById(R.id.edit_gender);
        editPhoneNumber = view.findViewById(R.id.edit_phnumber);

        View.OnClickListener closeFragment = v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        };

        backButton.setOnClickListener(closeFragment);
        topPanel.setOnClickListener(closeFragment);

        editUsername.setOnClickListener(v -> openEditUsername());
        editEmail.setOnClickListener(v -> openEditEmail());
        editGender.setOnClickListener(v -> openEditGender());
        editPhoneNumber.setOnClickListener(v -> openEditPhoneNumber());

        return view;
    }

    public void updateUserData(String name, String gender, String password) {
        usernameText.setText(name);
        genderText.setText(gender);
    }

    private void openEditUsername() { }
    private void openEditEmail() { }
    private void openEditGender() { }
    private void openEditPhoneNumber() { }
}

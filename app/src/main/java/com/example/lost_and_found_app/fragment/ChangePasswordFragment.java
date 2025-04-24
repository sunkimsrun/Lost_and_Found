package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.model.User;

public class ChangePasswordFragment extends Fragment {

    private EditText currentPasswordInput, newPasswordInput, confirmPasswordInput;
    private ImageView currentEyeIcon, newEyeIcon, confirmEyeIcon;
    private RelativeLayout confirmButton, topPanel;

    private boolean showCurrent = false;
    private boolean showNew = false;
    private boolean showConfirm = false;

    private String savedPassword = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentPasswordInput = view.findViewById(R.id.current_password_input);
        newPasswordInput = view.findViewById(R.id.new_password_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);

        currentEyeIcon = view.findViewById(R.id.current_eye_icon);
        newEyeIcon = view.findViewById(R.id.new_eye_icon);
        confirmEyeIcon = view.findViewById(R.id.confirm_eye_icon);

        confirmButton = view.findViewById(R.id.confirm_button);
        topPanel = view.findViewById(R.id.top_panel);

        if (User.currentUser != null) {
            savedPassword = User.currentUser.password;
        } else {
            showToast("User not logged in");
            closeFragment();
            return;
        }

        currentEyeIcon.setOnClickListener(v -> {
            showCurrent = !showCurrent;
            toggleVisibility(currentPasswordInput, showCurrent);
        });

        newEyeIcon.setOnClickListener(v -> {
            showNew = !showNew;
            toggleVisibility(newPasswordInput, showNew);
        });

        confirmEyeIcon.setOnClickListener(v -> {
            showConfirm = !showConfirm;
            toggleVisibility(confirmPasswordInput, showConfirm);
        });

        topPanel.setOnClickListener(v -> closeFragment());

        confirmButton.setOnClickListener(v -> updatePassword());

        TextView resetPassText = view.findViewById(R.id.reset_passw);
        resetPassText.setOnClickListener(v -> openForgotPasswordFragment());
    }

    private void toggleVisibility(EditText field, boolean visible) {
        if (visible) {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        field.setSelection(field.getText().length());
    }

    private void updatePassword() {
        String current = currentPasswordInput.getText().toString().trim();
        String newPass = newPasswordInput.getText().toString().trim();
        String confirm = confirmPasswordInput.getText().toString().trim();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showToast("All fields are required");
        } else if (!current.equals(savedPassword)) {
            showToast("Incorrect password");
        } else if (newPass.length() < 8) {
            showToast("Password must be at least 8 characters");
        } else if (!newPass.equals(confirm)) {
            showToast("Passwords do not match");
        } else {
            User.currentUser.password = newPass;
            showToast("Password changed successfully");
            closeFragment();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void closeFragment() {
        requireActivity().getSupportFragmentManager().popBackStack(); // Better UX
        View container = requireActivity().findViewById(R.id.fragment_container);
        if (container != null) {
            container.setVisibility(View.GONE);
        }
    }

    private void openForgotPasswordFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ForgotPasswordFragment())
                .addToBackStack(null)
                .commit();
    }
}

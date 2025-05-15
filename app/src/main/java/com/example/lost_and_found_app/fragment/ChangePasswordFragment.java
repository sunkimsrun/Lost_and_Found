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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {

    private EditText currentPasswordInput, newPasswordInput, confirmPasswordInput;
    private ImageView currentEyeIcon, newEyeIcon, confirmEyeIcon;
    private RelativeLayout confirmButton, topPanel;
    private boolean showCurrent = false, showNew = false, showConfirm = false;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        currentPasswordInput = view.findViewById(R.id.current_password_input);
        newPasswordInput = view.findViewById(R.id.new_password_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);

        currentEyeIcon = view.findViewById(R.id.current_eye_icon);
        newEyeIcon = view.findViewById(R.id.new_eye_icon);
        confirmEyeIcon = view.findViewById(R.id.confirm_eye_icon);

        confirmButton = view.findViewById(R.id.confirm_button);
        topPanel = view.findViewById(R.id.top_panel);

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
            return;
        }

        if (newPass.length() < 8) {
            showToast("Password must be at least 8 characters");
            return;
        }

        if (!newPass.equals(confirm)) {
            showToast("Passwords do not match");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showToast("User not authenticated");
            return;
        }

        // Re-authenticate user before password change
        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), current))
                .addOnSuccessListener(unused -> {
                    user.updatePassword(newPass)
                            .addOnSuccessListener(unused1 -> {
                                showToast("Password changed successfully");
                                closeFragment();
                            })
                            .addOnFailureListener(e -> showToast("Failed to update password: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showToast("Incorrect current password"));
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void closeFragment() {
        requireActivity().getSupportFragmentManager().popBackStack();
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

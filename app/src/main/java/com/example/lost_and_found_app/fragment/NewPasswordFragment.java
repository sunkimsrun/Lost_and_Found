package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lostandfound.R;
import com.example.lostandfound.model.User;

public class NewPasswordFragment extends Fragment {

    private EditText newPasswordInput, confirmPasswordInput;
    private ImageView newEyeIcon, confirmEyeIcon;
    private RelativeLayout confirmButton, topPanel;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    public NewPasswordFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newPasswordInput = view.findViewById(R.id.new_password_input);
        confirmPasswordInput = view.findViewById(R.id.password_input);
        newEyeIcon = view.findViewById(R.id.new_eye_icon);
        confirmEyeIcon = view.findViewById(R.id.confirm_eye_icon);
        confirmButton = view.findViewById(R.id.confirm_button);
        topPanel = view.findViewById(R.id.top_panel);

        newEyeIcon.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            togglePasswordVisibility(newPasswordInput, isNewPasswordVisible);
        });

        confirmEyeIcon.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(confirmPasswordInput, isConfirmPasswordVisible);
        });

        topPanel.setOnClickListener(v -> closeFragment());

        confirmButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update password in User model
            User.currentUser.password = newPassword;
            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
            closeFragment();
        });
    }

    private void togglePasswordVisibility(EditText editText, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }

    private void closeFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(NewPasswordFragment.this)
                .commit();
        requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
    }
}

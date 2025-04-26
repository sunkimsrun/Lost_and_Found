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

public class ChangeFieldFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_FIELD = "field";
    private static final String ARG_HINT = "hint";
    private static final String ARG_ICON = "icon";

    private String title, field, hint;
    private int iconResId;

    public static ChangeFieldFragment newInstance(String title, String field, String hint, int iconResId) {
        ChangeFieldFragment fragment = new ChangeFieldFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_FIELD, field);
        args.putString(ARG_HINT, hint);
        args.putInt(ARG_ICON, iconResId);
        fragment.setArguments(args);
        return fragment;
    }

    private EditText inputField, passwordField;
    private ImageView iconImage, eyeIcon;
    private TextView titleText;
    private RelativeLayout confirmButton;
    private boolean showPassword = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_change_field, container, false);

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            field = getArguments().getString(ARG_FIELD);
            hint = getArguments().getString(ARG_HINT);
            iconResId = getArguments().getInt(ARG_ICON);
        }

        inputField = view.findViewById(R.id.input_field);
        passwordField = view.findViewById(R.id.password_input);
        iconImage = view.findViewById(R.id.input_icon);
        eyeIcon = view.findViewById(R.id.eye_icon);
        titleText = view.findViewById(R.id.title_text);
        confirmButton = view.findViewById(R.id.confirm_button);
        RelativeLayout topPanel = view.findViewById(R.id.top_panel);

        titleText.setText(title);
        inputField.setHint(hint);
        iconImage.setImageResource(iconResId);

        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        confirmButton.setOnClickListener(v -> updateField());

        topPanel.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    private void togglePasswordVisibility() {
        if (showPassword) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        showPassword = !showPassword;
        passwordField.setSelection(passwordField.getText().length());
    }

    private void updateField() {
        String newValue = inputField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (newValue.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

//        if (!password.equals(User.currentUser.password)) {
//            Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
//            return;
//        }

//        if (field.equals("name")) {
//            User.currentUser.name = newValue;
//        } else if (field.equals("email")) {
//            User.currentUser.email = newValue;
//        } else if (field.equals("phone")) {
//            User.currentUser.phone = newValue;
//        } else if (field.equals("password")) {
//            User.currentUser.password = newValue;
//        } else {
//            Toast.makeText(getContext(), "Unknown field", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();

        Fragment parentFragment = getParentFragment();
//        if (parentFragment instanceof com.example.lost_and_found_app.fragment.AccountFragment) {
//            ((com.example.lost_and_found_app.fragment.AccountFragment) parentFragment).updateUserDisplay();
//        } else if (getParentFragment() instanceof AccountInformationFragment) {
//            ((AccountInformationFragment) getParentFragment()).updateUserData(
//                    User.currentUser.name,
//                    User.currentUser.gender,
//                    User.currentUser.dob,
//                    User.currentUser.password
//            );
//        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }
}

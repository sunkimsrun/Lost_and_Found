package com.example.lost_and_found_app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeGenderFragment extends Fragment {

    private TextView genderDisplay;
    private EditText passwordField;
    private ImageView eyeToggle;
    private boolean showPassword = false;
    private String chosenGender = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_gender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        genderDisplay = view.findViewById(R.id.gender_text);
        passwordField = view.findViewById(R.id.password_input);
        eyeToggle = view.findViewById(R.id.eye_icon);

        chosenGender = User.currentUser.gender;
        genderDisplay.setText(chosenGender);

        view.findViewById(R.id.pick_gender).setOnClickListener(v -> openGenderOptions());
        view.findViewById(R.id.confirm_button).setOnClickListener(v -> saveChanges());
        view.findViewById(R.id.top_panel).setOnClickListener(v -> goBack());
        eyeToggle.setOnClickListener(v -> togglePasswordView());
    }

    private void openGenderOptions() {
        String[] options = {"Male", "Female", "Other"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Gender")
                .setItems(options, (dialog, index) -> {
                    chosenGender = options[index];
                    genderDisplay.setText(chosenGender);
                })
                .show();
    }

    private void togglePasswordView() {
        if (showPassword) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        showPassword = !showPassword;
        passwordField.setSelection(passwordField.getText().length());
    }

    private void saveChanges() {
        String password = passwordField.getText().toString().trim();

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(User.currentUser.password)) {
            Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId).update("gender", chosenGender)
                .addOnSuccessListener(aVoid -> {
                    User.currentUser.gender = chosenGender;
                    Toast.makeText(getContext(), "Gender updated successfully", Toast.LENGTH_SHORT).show();

                    Fragment parentFragment = getParentFragment();
                    if (parentFragment instanceof AccountInformationFragment) {
                        ((AccountInformationFragment) parentFragment).updateUserData(
                                User.currentUser.name,
                                User.currentUser.gender,
                                User.currentUser.password
                        );
                    }
                    goBack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating gender: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void goBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}

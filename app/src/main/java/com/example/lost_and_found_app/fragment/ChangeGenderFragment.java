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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ChangeGenderFragment extends Fragment {

    private TextView genderDisplay;
    private EditText passwordField;
    private ImageView eyeToggle;
    private boolean showPassword = false;
    private String chosenGender = "";

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

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

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        genderDisplay = view.findViewById(R.id.gender_text);
        passwordField = view.findViewById(R.id.password_input);
        eyeToggle = view.findViewById(R.id.eye_icon);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            genderDisplay.setText("Not specified");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        firestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        chosenGender = documentSnapshot.getString("gender");
                        genderDisplay.setText(Objects.requireNonNullElse(chosenGender, "Not specified"));
                    } else {
                        genderDisplay.setText("Not specified");
                    }
                })
                .addOnFailureListener(e -> {
                    genderDisplay.setText("Not specified");
                });

        view.findViewById(R.id.pick_gender).setOnClickListener(v -> openGenderOptions());
        view.findViewById(R.id.confirm_button).setOnClickListener(v -> saveChanges());
        eyeToggle.setOnClickListener(v -> togglePasswordView());
    }

    private void openGenderOptions() {
        String[] options = {"Male", "Female", "Non Binary"};
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
        String inputPassword = passwordField.getText().toString().trim();

        if (inputPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = mAuth.getCurrentUser().getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, inputPassword);

        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(unused -> updateGender())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show());
    }

    private void updateGender() {
        String userId = mAuth.getCurrentUser().getUid();

        firestore.collection("users").document(userId).update("gender", chosenGender)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Gender updated successfully", Toast.LENGTH_SHORT).show();

                    Bundle result = new Bundle();
                    result.putString("newGender", chosenGender);
                    getParentFragmentManager().setFragmentResult("gender_updated", result);

                    goBack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating gender: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void goBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}

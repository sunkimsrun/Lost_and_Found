package com.example.lost_and_found_app.fragment;

import android.app.AlertDialog;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountInformationFragment extends DialogFragment {


    private TextView usernameText, emailText, genderText, phoneText;

    private DatabaseReference userRef;

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

        RelativeLayout backButton = view.findViewById(R.id.back_button);
//        RelativeLayout topPanel = view.findViewById(R.id.top_panel);
        ImageView editUsername = view.findViewById(R.id.edit_username);
        ImageView editEmail = view.findViewById(R.id.edit_email);
        ImageView editGender = view.findViewById(R.id.edit_gender);
        ImageView editPhoneNumber = view.findViewById(R.id.edit_phnumber);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            loadUserData();
        }

        View.OnClickListener closeFragment = v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        };

        backButton.setOnClickListener(closeFragment);
//        topPanel.setOnClickListener(closeFragment);

        editUsername.setOnClickListener(v -> openEditField("username", "Username", usernameText));
        editEmail.setOnClickListener(v -> openEditField("email", "Email", emailText));
        editGender.setOnClickListener(v -> openEditField("gender", "Gender", genderText));
        editPhoneNumber.setOnClickListener(v -> openEditField("phoneNumber", "Phone Number", phoneText));

        return view;
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                usernameText.setText(snapshot.child("username").getValue(String.class));
                emailText.setText(snapshot.child("email").getValue(String.class));
                genderText.setText(snapshot.child("gender").getValue(String.class));
                phoneText.setText(snapshot.child("phoneNumber").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEditField(String key, String label, TextView targetView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit " + label);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(targetView.getText().toString());

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                userRef.child(key).setValue(value)
                        .addOnSuccessListener(unused -> {
                            targetView.setText(value);
                            Toast.makeText(getContext(), label + " updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to update " + label, Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), label + " cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}

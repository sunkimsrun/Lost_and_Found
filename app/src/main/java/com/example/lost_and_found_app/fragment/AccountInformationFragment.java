package com.example.lost_and_found_app.fragment;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.lost_and_found_app.HomeActivity;
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

        ImageView editUsername = view.findViewById(R.id.edit_username);
        ImageView editEmail = view.findViewById(R.id.edit_email);
        ImageView editGender = view.findViewById(R.id.edit_gender);
        ImageView editPhoneNumber = view.findViewById(R.id.edit_phnumber);
        ImageView editPassword = view.findViewById(R.id.edit_password);
        LinearLayout tvBack = view.findViewById(R.id.tvBack);

        tvBack.setOnClickListener(v->{
            if (getActivity() instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                homeActivity.LoadFragment(new AccountFragment());
                homeActivity.binding.navigationView.setCheckedItem(R.id.nav_account);
            }
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            loadUserData();
        }

        View.OnClickListener closeFragment = v -> {
            getParentFragmentManager().setFragmentResult("updateUserInfo", new Bundle());
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        };

        editUsername.setOnClickListener(v -> openEditField("username", "Username", usernameText));
        editEmail.setOnClickListener(v -> openEditField("email", "Email", emailText));
        editPhoneNumber.setOnClickListener(v -> openEditField("phoneNumber", "Phone Number", phoneText));
        editGender.setOnClickListener(v -> openGenderPicker());
        editPassword.setOnClickListener(v -> {
            ChangePasswordFragment dialog = new ChangePasswordFragment();
            dialog.show(getParentFragmentManager(), "ChangePassDialog");
        });

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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.MyDialogTheme);

        TextView customTitle = new TextView(requireContext());
        customTitle.setText("Edit " + label);
        customTitle.setTextSize(20);
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        customTitle.setPadding(45, 24, paddingPx, 16);
        customTitle.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        customTitle.setTypeface(Typeface.DEFAULT_BOLD);
        builder.setCustomTitle(customTitle);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(margin, margin, margin, margin);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(targetView.getText().toString());
        input.setBackgroundResource(R.drawable.bg_gray_rectangle);
        int editPadding = (int) (12 * getResources().getDisplayMetrics().density);
        input.setPadding(editPadding, editPadding, editPadding, editPadding);
        container.addView(input);

        LinearLayout buttonLayout = new LinearLayout(requireContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.END);
        buttonLayout.setPadding(0, margin, 0, 0);

        Button cancelBtn = new Button(requireContext());
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        cancelBtn.setBackgroundResource(R.drawable.edit_cancel);

        Button saveBtn = new Button(requireContext());
        saveBtn.setText("Save");
        saveBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        saveBtn.setBackgroundResource(R.drawable.edit_confirm);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(20, 0, 0, 0);

        buttonLayout.addView(cancelBtn);
        buttonLayout.addView(saveBtn, btnParams);

        container.addView(buttonLayout);
        builder.setView(container);

        AlertDialog dialog = builder.create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                userRef.child(key).setValue(value)
                        .addOnSuccessListener(unused -> {
                            targetView.setText(value);
                            Toast.makeText(getContext(), label + " updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update " + label, Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), label + " cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void openGenderPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.MyDialogTheme);
        builder.setTitle("Select Gender");
        final String[] genderOptions = {"Male", "Female", "Non Binary"};
        int currentSelection = -1;
        String currentGender = genderText.getText().toString();
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equalsIgnoreCase(currentGender)) {
                currentSelection = i;
                break;
            }
        }
        builder.setSingleChoiceItems(genderOptions, currentSelection, null);
        builder.setPositiveButton("Save", (dialog, which) -> {
            AlertDialog alertDialog = (AlertDialog) dialog;
            int selectedPosition = alertDialog.getListView().getCheckedItemPosition();
            if (selectedPosition >= 0) {
                String selectedGender = genderOptions[selectedPosition];
                userRef.child("gender").setValue(selectedGender)
                        .addOnSuccessListener(unused -> {
                            genderText.setText(selectedGender);
                            Toast.makeText(getContext(), "Gender updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update gender", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}

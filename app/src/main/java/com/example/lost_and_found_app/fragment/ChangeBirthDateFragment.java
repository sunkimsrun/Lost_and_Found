package com.example.lost_and_found_app.fragment;

import android.app.DatePickerDialog;
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

import java.util.Calendar;

public class ChangeBirthDateFragment extends Fragment {

    private TextView dobText;
    private EditText passwordInput;
    private ImageView eyeIcon, arrDownIcon;
    private RelativeLayout confirmButton, topPanel;
    private boolean passwordVisible = false;
    private String selectedDate = "";

    public ChangeBirthDateFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_birth_date, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dobText = view.findViewById(R.id.dob_text);
        passwordInput = view.findViewById(R.id.password_input);
        eyeIcon = view.findViewById(R.id.eye_icon);
        arrDownIcon = view.findViewById(R.id.arr_down_icon);
        confirmButton = view.findViewById(R.id.confirm_button);
        topPanel = view.findViewById(R.id.top_panel);

        selectedDate = User.currentUser.dob;
        dobText.setText(selectedDate);

        arrDownIcon.setOnClickListener(v -> showDatePicker());
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        confirmButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(User.currentUser.password)) {
                Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!selectedDate.isEmpty()) {
                User.currentUser.dob = selectedDate;
                Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();

                Fragment parentFragment = getParentFragment();
                if (parentFragment instanceof com.example.lost_and_found_app.fragment.AccountFragment) {
                    ((com.example.lost_and_found_app.fragment.AccountFragment) parentFragment).updateUserDisplay();
                }

                Fragment parent = getParentFragment();
                if (parent instanceof AccountInformationFragment) {
                    ((AccountInformationFragment) parent).updateUserData(
                            User.currentUser.name,
                            User.currentUser.gender,
                                User.currentUser.dob,
                                User.currentUser.password
                    );
                }

                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        topPanel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            selectedDate = d + " - " + (m + 1) + " - " + y;
            dobText.setText(selectedDate);
        }, year, month, day).show();
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        passwordVisible = !passwordVisible;
        passwordInput.setSelection(passwordInput.getText().length());
    }
}

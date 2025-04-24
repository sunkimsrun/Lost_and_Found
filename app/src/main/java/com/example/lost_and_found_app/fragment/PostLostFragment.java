package com.example.lost_and_found_app.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentPostLostBinding;

import java.util.Calendar;

public class PostLostFragment extends Fragment {

    FragmentPostLostBinding binding;

    public PostLostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostLostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCreate.setEnabled(false);
        binding.btnCreate.setBackgroundColor(getResources().getColor(R.color.gray));

        binding.checkBoxPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.btnCreate.setEnabled(true);
                binding.btnCreate.setBackgroundColor(getResources().getColor(R.color.blue));
            } else {
                binding.btnCreate.setEnabled(false);
                binding.btnCreate.setBackgroundColor(getResources().getColor(R.color.gray));
            }
        });

        binding.btnCreate.setOnClickListener(v1 -> {
            if (binding.checkBoxPolicy.isChecked()) {
                Log.d("CreatePostFragment", "Post Created: ");
            }
        });
        binding.tvSelectedDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        binding.tvSelectedDate.setText(formattedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });
        binding.tvSelectedTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view1, selectedHour, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        binding.tvSelectedTime.setText(formattedTime);
                    }, hour, minute, true);

            timePickerDialog.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
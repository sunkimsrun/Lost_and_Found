package com.example.lost_and_found_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lost_and_found_app.databinding.FragmentPrivacyPolicyBinding;

public class PrivacyPolicyFragment extends Fragment {

    private FragmentPrivacyPolicyBinding binding;

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout with binding
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Set up button click
        binding.readMoreButton.setOnClickListener(v -> {
            boolean isVisible = binding.moreContentText.getVisibility() == View.VISIBLE;
            binding.moreContentText.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            binding.readMoreButton.setText(isVisible ? "Read More" : "Show Less");
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leaks
    }
}
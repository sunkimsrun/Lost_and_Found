package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.databinding.FragmentSuccessfulBinding;

public class SuccessfulFragment extends Fragment {

    FragmentSuccessfulBinding binding;

    public SuccessfulFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSuccessfulBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        HomeActivity homeActivity = (HomeActivity) getActivity();

        binding.tvBack.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.LoadFragment(new HomeFragment());
            }
        });

        return view;
    }
}
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

    private FragmentSuccessfulBinding binding;

    public SuccessfulFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSuccessfulBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvBack.setOnClickListener(v -> {
            if (isAdded() && getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).LoadFragment(new HomeFragment());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.lost_and_found_app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.LoginActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    HomeActivity homeActivity;

    private final boolean userIsLoggedIn = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.cardCreate.setOnClickListener(v -> {
            if (!userIsLoggedIn) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            } else {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                homeActivity.LoadFragment(new PostItemFragment());
            }

        });

        binding.cardLost.setOnClickListener(v -> {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            if (homeActivity == null) return;

            homeActivity.binding.navigationView.setCheckedItem(R.id.nav_lost);
            homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_lost, 0);
        });

        binding.cardFound.setOnClickListener(v -> {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            if (homeActivity == null) return;

            homeActivity.binding.navigationView.setCheckedItem(R.id.nav_found);
            homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_found, 0);

        });

        return binding.getRoot();
    }
}
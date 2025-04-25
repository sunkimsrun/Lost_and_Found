package com.example.lost_and_found_app.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.LoginActivity;
import com.example.lost_and_found_app.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    HomeActivity homeActivity;

    private boolean userIsLoggedIn = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.cardCreate.setOnClickListener(v -> {
            if (!userIsLoggedIn){
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return;
            }else{
                HomeActivity homeActivity = (HomeActivity) getActivity();
                homeActivity.LoadFragment(new PostItemFragment());
            }

        });

        binding.cardLost.setOnClickListener(v -> {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            homeActivity.LoadFragment(new ViewLostFragment());
        });

        binding.cardFound.setOnClickListener(v -> {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            homeActivity.LoadFragment(new ViewFoundFragment());
        });

        return binding.getRoot();
    }
}
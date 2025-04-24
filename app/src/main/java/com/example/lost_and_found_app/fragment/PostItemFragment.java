package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.lost_and_found_app.databinding.FragmentPostItemBinding;

public class PostItemFragment extends Fragment {

    private FragmentPostItemBinding binding;

    public PostItemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostItemBinding.inflate(inflater, container, false);

        loadChildFragment(new PostLostFragment());

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.radioLost.getId()) {
                loadChildFragment(new PostLostFragment());
            } else if (checkedId == binding.radioFound.getId()) {
                loadChildFragment(new PostFoundFragment());
            }
        });

        return binding.getRoot();
    }

    private void loadChildFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(binding.lostfoundFrameContainer.getId(), fragment);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
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

        Bundle args = getArguments();
        if (args != null) {
            String action = args.getString("action");
            if ("delete".equals(action)) {
                binding.text.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                binding.text.setText("Delete Successful!!");
                binding.lottieView.setAnimation(R.raw.delete);
                binding.lottieView.playAnimation();
            }
        }

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvBack.setOnClickListener(v -> {
            if (isAdded() && getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).binding.navigationView.setCheckedItem(R.id.nav_home);
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

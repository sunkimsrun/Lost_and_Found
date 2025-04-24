package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.lost_and_found_app.R;

public class ForgotPasswordFragment extends Fragment {

    private RelativeLayout topPanel;
    private RelativeLayout nextButton;

    public ForgotPasswordFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        topPanel = view.findViewById(R.id.top_panel);
        nextButton = view.findViewById(R.id.next_button);

        View.OnClickListener closeFragment = v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(ForgotPasswordFragment.this)
                    .commit();
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        };

        topPanel.setOnClickListener(closeFragment);

        nextButton.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new NewPasswordFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
}

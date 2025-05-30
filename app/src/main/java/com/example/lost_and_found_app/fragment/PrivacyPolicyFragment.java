package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;

public class PrivacyPolicyFragment extends Fragment {

    private TextView moreContentText;
    private Button readMoreButton;
    private boolean isMoreContentVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_privacy_policy, container, false);

        moreContentText = root.findViewById(R.id.moreContentText);
        readMoreButton = root.findViewById(R.id.readMoreButton);

        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMoreContentVisible = !isMoreContentVisible;
                if (isMoreContentVisible) {
                    moreContentText.setVisibility(View.VISIBLE);
                    readMoreButton.setText(getString(R.string.read_less));
                } else {
                    moreContentText.setVisibility(View.GONE);
                    readMoreButton.setText(getString(R.string.read_more));
                }
            }
        });

        return root;
    }
}

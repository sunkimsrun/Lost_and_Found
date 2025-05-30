package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;

public class PolicyFragment extends Fragment {

    public PolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_policy, container, false);

        Button readMoreButton = view.findViewById(R.id.readMoreButton);
        TextView moreContentText = view.findViewById(R.id.moreContentText);

        readMoreButton.setOnClickListener(v -> {
            if (moreContentText.getVisibility() == View.GONE) {
                moreContentText.setVisibility(View.VISIBLE);
                readMoreButton.setText("Show Less");
            } else {
                moreContentText.setVisibility(View.GONE);
                readMoreButton.setText("Read More");
            }
        });

        return view;
    }
}

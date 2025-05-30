package com.example.lost_and_found_app.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;

public class ContactFragment extends Fragment {

    private static final String MAP_LOCATION_URI = "https://maps.app.goo.gl/HgJ3FMwsWrVknbYq6";
    private static final String CONTACT_PHONE_URI = "tel:+855966482541";
    private static final String CONTACT_EMAIL_URI = "mailto:contact@example.com?subject=Inquiry%20from%20FoundIt%20App";

    public ContactFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.tvLocation).setOnClickListener(v -> openMap());
        view.findViewById(R.id.tvCall).setOnClickListener(v -> dialPhoneNumber());
        view.findViewById(R.id.tvEmail).setOnClickListener(v -> sendEmail());
    }

    private void openMap() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MAP_LOCATION_URI));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No application can open the map link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialPhoneNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(CONTACT_PHONE_URI));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No application can handle this action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(CONTACT_EMAIL_URI));
        try {
            startActivity(Intent.createChooser(intent, "Send email using..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}

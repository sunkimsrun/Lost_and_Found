package com.example.lost_and_found_app;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.fragment.AccountInformationFragment;
import com.example.lost_and_found_app.fragment.ChangePasswordFragment;
import com.example.lost_and_found_app.fragment.ManagePostFragment;
import com.example.lost_and_found_app.model.User;

public class AccountActivity extends AppCompatActivity {

    private TextView nameText, detailsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        nameText = findViewById(R.id.nameText);
        detailsText = findViewById(R.id.detailsText);

        // Apply shared user values on start
        updateUserDisplay();

        // Set up click listeners for menu items
        findViewById(R.id.account_info_item).setOnClickListener(view ->
                loadFragment(new AccountInformationFragment()));

        findViewById(R.id.manage_post_item).setOnClickListener(view ->
                loadFragment(new ManagePostFragment()));

        findViewById(R.id.change_password_item).setOnClickListener(view ->
                loadFragment(new ChangePasswordFragment()));



        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof AccountInformationFragment) {
                updateUserDisplay();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserDisplay(); // Update the UI when the activity resumes
    }



    // In AccountActivity
    public void updateUserDisplay() {
        User user = User.currentUser;
        nameText.setText(user.name);
        detailsText.setText("Gender: " + user.gender + " | DOB: " + user.dob);
    }


    // Load a fragment into the container
    private void loadFragment(Fragment fragment) {
        FrameLayout container = findViewById(R.id.fragment_container);
        container.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Handle the back button press to manage the fragment back stack
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}

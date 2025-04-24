package com.example.lost_and_found_app;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.databinding.ActivityHomeBinding;
import com.example.lost_and_found_app.fragment.AccountFragment;
import com.example.lost_and_found_app.fragment.ContactFragment;
import com.example.lost_and_found_app.fragment.HomeFragment;
import com.example.lost_and_found_app.fragment.PolicyFragment;
import com.example.lost_and_found_app.fragment.ViewFoundFragment;
import com.example.lost_and_found_app.fragment.ViewLostFragment;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnHamburger.setOnClickListener(view -> {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        View headerView = binding.navigationView.getHeaderView(0);
        View btnClose = headerView.findViewById(R.id.btnDrawerClose);
        btnClose.setOnClickListener(v -> binding.drawerLayout.closeDrawer(GravityCompat.START));

        binding.navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                LoadFragment(new HomeFragment());
            } else if (itemId == R.id.nav_account) {
                LoadFragment(new AccountFragment());
            } else if (itemId == R.id.nav_lost) {
                LoadFragment(new ViewLostFragment());
            } else if (itemId == R.id.nav_found) {
                LoadFragment(new ViewFoundFragment());
            } else if (itemId == R.id.nav_privacy) {
                LoadFragment(new PolicyFragment());
            } else // TODO: Handle sign-out logic here
                if (itemId == R.id.nav_contact) {
                LoadFragment(new ContactFragment());
            } else return itemId == R.id.nav_signout;

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        binding.navigationView.setCheckedItem(R.id.nav_home);
        LoadFragment(new HomeFragment());

    }
    public void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }

}

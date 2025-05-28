package com.example.lost_and_found_app.fragment;

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

import com.bumptech.glide.Glide;
import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentPostDetailBinding;
import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.service.PostCardService;
import com.example.lost_and_found_app.util.RetrofitClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailFragment extends Fragment {

    private FragmentPostDetailBinding binding;
    private HomeActivity homeActivity;
    private boolean isPostLoaded = false;
    private boolean isUserLoaded = false;

    private static final String MAP_VIEW_BUNDLE_KEY = "unforgettable";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeActivity = (HomeActivity) getActivity();

        Bundle args = getArguments();
        if (args == null) return;

        String postId = args.getString("postId");
        String status = args.getString("status");

        if (postId != null && status != null) {
            loadPostById(postId, status);
        }

        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY) : null;
        binding.mapView.onCreate(mapViewBundle);

        binding.mapView.getMapAsync(googleMap -> {
            LatLng rupp = new LatLng(11.5673, 104.8885);
            googleMap.addMarker(new MarkerOptions().position(rupp).title("Royal University of Phnom Penh"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rupp, 16f));
        });


        binding.tvReturn.setOnClickListener(v -> {
            if (homeActivity == null) return;

            int selectedId = homeActivity.binding.navigationView.getCheckedItem().getItemId();

            if (selectedId == R.id.nav_found) {
                homeActivity.LoadFragment(new ViewFoundFragment());
            } else if (selectedId == R.id.nav_lost) {
                homeActivity.LoadFragment(new ViewLostFragment());
            } else if (selectedId == R.id.nav_account) {
                homeActivity.LoadFragment(new AccountFragment());
            } else if (selectedId == R.id.nav_home) {
                homeActivity.LoadFragment(new HomeFragment());
            }
        });


        binding.btnDelete.setOnClickListener(v -> {

            String postIdArg = args.getString("postId");
            String statusArg = args.getString("status");

            if (postIdArg == null || statusArg == null) return;

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String itemType = getItemType(statusArg);
                        if (itemType == null) return;

                        showProgressBar();

                        PostCardService postCardService = RetrofitClient.getClient().create(PostCardService.class);
                        Call<Void> call = postCardService.deleteCard(itemType, postIdArg);

                        call.enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                hideProgressBar();
                                if (!response.isSuccessful() || !isAdded()) return;

                                if (homeActivity != null) {
                                    new android.os.Handler().postDelayed(() -> {
                                        homeActivity.LoadFragment(new SuccessfulFragment());
                                    }, 1500);
                                }

                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                hideProgressBar();
                                Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    private void loadPostById(String postId, String status) {
        showProgressBar();
        isPostLoaded = false;
        isUserLoaded = false;

        String itemType = getItemType(status);
        if (itemType == null) {
            hideProgressBar();
            return;
        }

        PostCardService postCardService = RetrofitClient.getClient().create(PostCardService.class);
        Call<PostCard> call = postCardService.getCard(itemType, postId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PostCard> call, @NonNull Response<PostCard> response) {
                if (!response.isSuccessful() || response.body() == null || !isAdded()) {
                    hideProgressBar();
                    return;
                }

                PostCard card = response.body();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                updateUserDisplay(card.getUserId());

                if (card.getUserId() != null && card.getUserId().equals(mAuth.getUid())) {
                    binding.btnCall.setVisibility(View.GONE);
                    binding.btnDelete.setVisibility(View.VISIBLE);
                    binding.checkbox.setEnabled(true);
                    binding.checkbox.setClickable(true);
                }

                binding.displayTitle.setText(card.getTitle());
                binding.displayDate.setText(card.getDate());
                binding.displayInformation.setText(card.getInformation());
                binding.displayEmail.setText(card.getEmail());
                binding.displayPhone.setText(card.getPhone());
                binding.displayTime.setText(card.getPostTime());
                binding.checkbox.setText(card.getStatus());

                binding.btnCall.setOnClickListener(v -> {
                    String phone = card.getPhone();
                    if (phone != null && !phone.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                    }
                });

                if (card.getReward() != null) {
                    binding.reward.setText(card.getReward());
                } else {
                    binding.rewardContainer.setVisibility(View.GONE);
                }

                binding.checkbox.setChecked(
                        Objects.equals(card.getStatus(), "Found") || Objects.equals(card.getStatus(), "Returned")
                );

                Glide.with(requireContext())
                        .load(card.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.displayImage);

                binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String newStatus = getUpdatedStatus(card.getStatus(), isChecked);
                    if (newStatus != null) updatePostStatus(postId, newStatus);
                });

                isPostLoaded = true;
                tryHideProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<PostCard> call, @NonNull Throwable t) {
                hideProgressBar();
                Toast.makeText(requireContext(), "Failed to load post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getItemType(String status) {
        switch (status) {
            case "Not Found":
            case "Found":
                return "lostitems";
            case "Not Returned":
            case "Returned":
                return "founditems";
            default:
                return null;
        }
    }

    private String getUpdatedStatus(String currentStatus, boolean isChecked) {
        if (isChecked) {
            switch (currentStatus) {
                case "Not Found":
                    return "Found";
                case "Not Returned":
                    return "Returned";
                default:
                    return null;
            }
        } else {
            switch (currentStatus) {
                case "Found":
                    return "Not Found";
                case "Returned":
                    return "Not Returned";
                default:
                    return null;
            }
        }
    }

    public void updateUserDisplay(String userId) {
        if (userId != null) {
            getUserDataFromDatabase(userId);
        }
    }

    private void getUserDataFromDatabase(String userID) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    if (username != null) binding.userName.setText(username);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(binding.userImage);
                    }
                }

                isUserLoaded = true;
                tryHideProgressBar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isUserLoaded = true;
                tryHideProgressBar();
            }
        });
    }

    private void tryHideProgressBar() {
        if (isPostLoaded && isUserLoaded) {
            hideProgressBar();
        }
    }

    private void showProgressBar() {
        if (homeActivity != null) {
            homeActivity.showProgressBar();
        }
    }

    private void hideProgressBar() {
        if (homeActivity != null) {
            homeActivity.hideProgressBar();
        }
    }

    private void updatePostStatus(String postId, String newStatus) {
        PostCardService postCardService = RetrofitClient.getClient().create(PostCardService.class);
        String itemType = getItemType(newStatus);
        if (itemType == null) return;

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", newStatus);

        Call<PostCard> call = postCardService.updatePostStatus(itemType, postId, statusMap);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PostCard> call, @NonNull Response<PostCard> response) {
                if (response.isSuccessful()) {
                    binding.checkbox.setText(newStatus);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostCard> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (binding != null) binding.mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (binding != null) binding.mapView.onStop();
    }

    @Override
    public void onPause() {
        if (binding != null) binding.mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null) binding.mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        if (binding != null) binding.mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        if (binding != null) binding.mapView.onSaveInstanceState(mapViewBundle);
    }

}

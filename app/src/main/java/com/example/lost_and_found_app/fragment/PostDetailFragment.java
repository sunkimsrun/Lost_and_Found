package com.example.lost_and_found_app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    FragmentPostDetailBinding binding;
    HomeActivity homeActivity;
    private final boolean isBookmarked = false;
    private boolean isPostLoaded = false;
    private boolean isUserLoaded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeActivity = (HomeActivity) getActivity();
        String postId = getArguments() != null ? getArguments().getString("postId") : null;
        String status = getArguments() != null ? getArguments().getString("status") : null;
        String source = getArguments() != null ? getArguments().getString("source") : null;

        if (postId != null && status != null) {
            loadPostById(postId, status);
        }

        binding.tvReturn.setOnClickListener(v -> {
            if (homeActivity == null) return;

            if ("found".equals(source)) {
                homeActivity.binding.navigationView.setCheckedItem(R.id.nav_found);
                homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_found, 0);
            } else if ("lost".equals(source)) {
                homeActivity.binding.navigationView.setCheckedItem(R.id.nav_lost);
                homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_lost, 0);
            } else if ("manage".equals(source)) {
                homeActivity.binding.navigationView.setCheckedItem(R.id.nav_account);
                homeActivity.LoadFragment(new ManagePostFragment());
            }
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (getArguments() == null) return;

            String postIdArg = getArguments().getString("postId");
            String statusArg = getArguments() != null ? getArguments().getString("status") : null;

            if (postIdArg == null || statusArg == null) return;

            String itemType;
            if ("Not Found".equals(statusArg) || "Found".equals(statusArg)) {
                itemType = "lostitems";
            } else if ("Not Returned".equals(statusArg) || "Returned".equals(statusArg)) {
                itemType = "founditems";
            } else {
                return;
            }

            showProgressBar();

            PostCardService postCardService = RetrofitClient.getClient().create(PostCardService.class);
            Call<Void> call = postCardService.deleteCard(itemType, postIdArg);

            call.enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                    hideProgressBar();
                    if (!response.isSuccessful() || !isAdded()) return;

                    if (homeActivity != null) {
                        homeActivity.LoadFragment(new ManagePostFragment());
                        homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_account, 0);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    hideProgressBar();
                }
            });
        });
    }

    private void loadPostById(String postId, String status) {
        showProgressBar();
        isPostLoaded = false;
        isUserLoaded = false;

        String itemType;
        if ("Not Found".equals(status) || "Found".equals(status)) {
            itemType = "lostitems";
        } else if ("Not Returned".equals(status) || "Returned".equals(status)) {
            itemType = "founditems";
        } else {
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
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                        startActivity(intent);
                    }
                });

                binding.location.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/2kiK5XSakXmyMYy68"));
                    startActivity(intent);
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
                        .into(binding.displayImage);

                binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if ("Not Found".equals(card.getStatus())) {
                            updatePostStatus(postId, "Found");
                        } else if ("Not Returned".equals(card.getStatus())) {
                            updatePostStatus(postId, "Returned");
                        }
                    } else {
                        if ("Found".equals(card.getStatus())) {
                            updatePostStatus(postId, "Not Found");
                        } else if ("Returned".equals(card.getStatus())) {
                            updatePostStatus(postId, "Not Returned");
                        }
                    }
                });

                isPostLoaded = true;
                tryHideProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<PostCard> call, @NonNull Throwable t) {
                hideProgressBar();
            }
        });
    }

    public void updateUserDisplay(String userId) {
        if (userId != null) {
            getUserDataFromDatabase(userId);
        }
    }

    private void getUserDataFromDatabase(String userID) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userID);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    if (username != null) {
                        binding.userName.setText(username);
                    }

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .centerCrop()
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

        String itemType;
        if ("Not Found".equals(newStatus) || "Found".equals(newStatus)) {
            itemType = "lostitems";
        } else if ("Not Returned".equals(newStatus) || "Returned".equals(newStatus)) {
            itemType = "founditems";
        } else {
            return;
        }

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
            }
        });
    }

}

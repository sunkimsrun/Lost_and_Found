package com.example.lost_and_found_app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.LoginActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.adapter.ViewCardAdapter;
import com.example.lost_and_found_app.databinding.FragmentHomeBinding;
import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.model.ViewCardData;
import com.example.lost_and_found_app.repository.IApiCallback;
import com.example.lost_and_found_app.repository.PostCardRepository;
import com.example.lost_and_found_app.service.PostCardServiceHelper;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private final android.os.Handler autoScrollHandler = new android.os.Handler();
    private Runnable autoScrollRunnable;
    private int currentPage = 0;
    private FragmentHomeBinding binding;
    private PostCardRepository postCardRepository;
    private FirebaseAuth mAuth;

    HomeActivity homeActivity;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        postCardRepository = new PostCardRepository();
        PostCardServiceHelper serviceHelper = new PostCardServiceHelper(postCardRepository);

        homeActivity = (getActivity() instanceof HomeActivity) ? (HomeActivity) getActivity() : null;

        showProgressBar();
        serviceHelper.getTop3UsersWithMostLostPosts(new IApiCallback<List<Map.Entry<String, Integer>>>() {
            @Override
            public void onSuccess(List<Map.Entry<String, Integer>> result) {
                for (int i = 0; i < result.size(); i++) {
                    Map.Entry<String, Integer> entry = result.get(i);
                    String userId = entry.getKey();
                    int postCount = entry.getValue();

                    if (i == 0) {
                        binding.sadUserText1.setText(postCount + (postCount > 1 ? " losts" : " lost"));
                        loadUserImage(userId, binding.sadUserImage1);
                    } else if (i == 1) {
                        binding.sadUserText2.setText(postCount + (postCount > 1 ? " losts" : " lost"));
                        loadUserImage(userId, binding.sadUserImage2);
                    } else if (i == 2) {
                        binding.sadUserText3.setText(postCount + (postCount > 1 ? " losts" : " lost"));
                        loadUserImage(userId, binding.sadUserImage3);
                    }
                }
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HomeFragment", "Error fetching top users: " + errorMessage);
                hideProgressBar();
            }
        });

        List<ViewCardData> cardList = new ArrayList<>();
        cardList.add(new ViewCardData("Found Items", "Found Something?\nLet’s Return It!", R.raw.viewfoundcard));
        cardList.add(new ViewCardData("Lost Items", "Let's Help You Find\nWhat’s Lost!", R.raw.viewlostcard));

        ViewCardAdapter adapter = new ViewCardAdapter(cardList);

        binding.viewPager.setAdapter(adapter);
        startAutoScroll(cardList.size());

        binding.tvSkip.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                homeActivity.LoadFragment(new ViewFoundFragment());
            }
        });

        new TabLayoutMediator(binding.tabIndicator, binding.viewPager, (tab, position) -> {
        }).attach();

        binding.cardCreate.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            } else {
                if (getActivity() instanceof HomeActivity) {
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    homeActivity.LoadFragment(new PostItemFragment());
                }
            }
        });

        adapter.setOnCardClickListener(position -> {
            if (getActivity() instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                if (position == 0) {
                    homeActivity.binding.navigationView.setCheckedItem(R.id.nav_found);
                    homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_found, 0);
                } else {
                    homeActivity.binding.navigationView.setCheckedItem(R.id.nav_lost);
                    homeActivity.binding.navigationView.getMenu().performIdentifierAction(R.id.nav_lost, 0);
                }
            }
        });

        loadLatestCard();

        return binding.getRoot();
    }

    private void loadUserImage(String userId, androidx.appcompat.widget.AppCompatImageView imageView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.placeholder)
                                .centerCrop()
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.placeholder);
                    }
                } else {
                    imageView.setImageResource(R.drawable.placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                imageView.setImageResource(R.drawable.placeholder);
            }
        });
    }

    private void loadLatestCard() {
        showProgressBar();
        postCardRepository.getLatestPosts("lostitems", "date", 1, new IApiCallback<List<PostCard>>() {
            @Override
            public void onSuccess(List<PostCard> postCards) {
                if (postCards != null && !postCards.isEmpty()) {
                    PostCard post = postCards.get(0);

                    Glide.with(requireContext())
                            .load(post.getImageUrl())
                            .centerCrop()
                            .placeholder(R.drawable.placeholder)
                            .into(binding.postImage);

                    binding.latestCard.setOnClickListener(v -> {
                        PostDetailFragment fragment = new PostDetailFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString("postId", post.getPostId());

                        fragment.setArguments(bundle);

                    });

                    binding.postTitle.setText(post.getTitle());
                    binding.postInformation.setText(post.getInformation());
                    binding.date.setText(post.getDate());
                    binding.time.setText(post.getPostTime());
                    binding.userPhone.setText(post.getPhone());

                    boolean isChecked = "Found".equalsIgnoreCase(post.getStatus()) || "Returned".equalsIgnoreCase(post.getStatus());
                    binding.checkbox.setChecked(isChecked);
                    binding.checkbox.setText(post.getStatus());

                    loadUserData(post.getUserId(), requireContext());

                    binding.latestCard.setOnClickListener(v -> {
                        PostDetailFragment fragment = new PostDetailFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString("postId", post.getPostId());
                        bundle.putString("status", post.getStatus());

                        if (getActivity() instanceof HomeActivity) {
                            HomeActivity homeActivity = (HomeActivity) getActivity();
                            int selectedId = homeActivity.binding.navigationView.getCheckedItem().getItemId();

                            if (selectedId == R.id.nav_lost) {
                                bundle.putString("source", "lost");
                            } else if (selectedId == R.id.nav_found) {
                                bundle.putString("source", "found");
                            } else if (selectedId == R.id.nav_account) {
                                bundle.putString("source", "manage");
                            }

                            fragment.setArguments(bundle);
                            homeActivity.LoadFragment(fragment);
                        }
                    });


                }
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HomeFragment", "Failed to load latest post: " + errorMessage);
                hideProgressBar();
            }
        });
    }

    private void loadUserData(String userId, Context context) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    binding.userName.setText(username != null ? username : "Unknown");

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(context)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.placeholder)
                                .centerCrop()
                                .into(binding.userImage);
                    } else {
                        binding.userImage.setImageResource(R.drawable.placeholder);
                    }
                } else {
                    binding.userName.setText("Unknown");
                    binding.userImage.setImageResource(R.drawable.placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.userName.setText("Unknown");
                binding.userImage.setImageResource(R.drawable.placeholder);
            }
        });
    }
    private void startAutoScroll(int pageCount) {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && binding.viewPager != null && pageCount > 0) {
                    currentPage = (currentPage + 1) % pageCount;
                    binding.viewPager.setCurrentItem(currentPage, true);
                    autoScrollHandler.postDelayed(this, 3000);
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 1000);
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
        binding = null;
    }


    private void showProgressBar() {
        if (homeActivity != null) homeActivity.showProgressBar();
    }

    private void hideProgressBar() {
        if (homeActivity != null) homeActivity.hideProgressBar();
    }
}

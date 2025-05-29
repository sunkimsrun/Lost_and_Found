package com.example.lost_and_found_app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

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

    private static final int AUTO_SCROLL_DELAY = 3000;
    private static final int AUTO_SCROLL_INITIAL_DELAY = 1000;

    private final android.os.Handler autoScrollHandler = new android.os.Handler();
    private Runnable autoScrollRunnable;
    private int currentPage = 0;

    private FragmentHomeBinding binding;
    private PostCardRepository postCardRepository;
    private FirebaseAuth mAuth;
    private HomeActivity homeActivity;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        postCardRepository = new PostCardRepository();
        PostCardServiceHelper serviceHelper = new PostCardServiceHelper(postCardRepository);

        if (getActivity() instanceof HomeActivity) {
            homeActivity = (HomeActivity) getActivity();
        }

        showProgressBar();

        serviceHelper.getTop3UsersWithMostLostPosts(new IApiCallback<List<Map.Entry<String, Integer>>>() {
            @Override
            public void onSuccess(List<Map.Entry<String, Integer>> result) {
                for (int i = 0; i < result.size(); i++) {
                    Map.Entry<String, Integer> entry = result.get(i);
                    String userId = entry.getKey();
                    int postCount = entry.getValue();

                    String text = getResources().getQuantityString(R.plurals.lost_count, postCount, postCount);

                    if (i == 0) {
                        binding.sadUserText1.setText(text);
                        loadUserImage(userId, binding.sadUserImage1);
                    } else if (i == 1) {
                        binding.sadUserText2.setText(text);
                        loadUserImage(userId, binding.sadUserImage2);
                    } else if (i == 2) {
                        binding.sadUserText3.setText(text);
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

        setupCardViewPager();

        binding.tvSkip.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).binding.navigationView.setCheckedItem(R.id.nav_lost); ;
                ((HomeActivity) getActivity()).LoadFragment(new ViewLostFragment());
            }
        });

        binding.cardCreate.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            } else if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).LoadFragment(new PostItemFragment());
            }
        });

        loadLatestCard("lostitems");

        TextView tvLost = binding.getRoot().findViewById(R.id.tvLatestLost);
        TextView tvFound = binding.getRoot().findViewById(R.id.tvLatestFound);
        View indicator = binding.getRoot().findViewById(R.id.activeIndicator);

        tvLost.setOnClickListener(v -> {
            loadLatestCard("lostitems");

            binding.tvSkip.setOnClickListener(v1 -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).binding.navigationView.setCheckedItem(R.id.nav_lost); ;
                    ((HomeActivity) getActivity()).LoadFragment(new ViewLostFragment());
                }
            });

            tvLost.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
            tvFound.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            moveIndicatorTo(tvLost, indicator);
        });

        tvFound.setOnClickListener(v -> {
            loadLatestCard("founditems");

            binding.tvSkip.setOnClickListener(v1 -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).binding.navigationView.setCheckedItem(R.id.nav_found); ;
                    ((HomeActivity) getActivity()).LoadFragment(new ViewFoundFragment());
                }
            });

            tvFound.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
            tvLost.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            moveIndicatorTo(tvFound, indicator);
        });

        return binding.getRoot();
    }

    private void setupCardViewPager() {
        List<ViewCardData> cardList = new ArrayList<>();
        cardList.add(new ViewCardData("Found Items", "Found Something?\nLet’s Return It!", R.raw.viewfoundcard));
        cardList.add(new ViewCardData("Lost Items", "Let's Help You Find\nWhat’s Lost!", R.raw.viewlostcard));

        ViewCardAdapter adapter = new ViewCardAdapter(cardList);
        binding.viewPager.setAdapter(adapter);
        startAutoScroll(cardList.size());

        ImageView bgBlue = binding.swapButton.findViewById(R.id.bg_blue);
        ImageView bgDot2 = binding.swapButton.findViewById(R.id.bg_dot2);

        bgBlue.setImageResource(R.drawable.blue_dot);
        bgDot2.setImageResource(R.drawable.gray_dot);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bgBlue.setImageResource(position == 0 ? R.drawable.blue_dot : R.drawable.gray_dot);
                bgDot2.setImageResource(position == 0 ? R.drawable.gray_dot : R.drawable.blue_dot);
            }
        });

        bgBlue.setOnClickListener(v -> binding.viewPager.setCurrentItem(0, true));
        bgDot2.setOnClickListener(v -> binding.viewPager.setCurrentItem(1, true));

        adapter.setOnCardClickListener(position -> {
            if (getActivity() instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                int navId = (position == 0) ? R.id.nav_found : R.id.nav_lost;
                homeActivity.binding.navigationView.setCheckedItem(navId);
                homeActivity.binding.navigationView.getMenu().performIdentifierAction(navId, 0);
            }
        });
    }

    private void loadUserImage(String userId, androidx.appcompat.widget.AppCompatImageView imageView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.child("profileImageUrl").getValue(String.class);
                if (url != null && !url.isEmpty()) {
                    Glide.with(requireContext()).load(url).placeholder(R.drawable.placeholder).centerCrop().into(imageView);
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

    private void loadLatestCard(String source) {
        showProgressBar();
        postCardRepository.getLatestPosts(source, "date", 1, new IApiCallback<List<PostCard>>() {
            @Override
            public void onSuccess(List<PostCard> postCards) {
                if (postCards == null || postCards.isEmpty()) {
                    hideProgressBar();
                    return;
                }

                PostCard post = postCards.get(0);

                Glide.with(requireContext())
                        .load(post.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(binding.postImage);

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
                        int selectedId = ((HomeActivity) getActivity()).binding.navigationView.getCheckedItem().getItemId();

                        if (selectedId == R.id.nav_lost) {
                            bundle.putString("source", "lost");
                        } else if (selectedId == R.id.nav_found) {
                            bundle.putString("source", "found");
                        } else if (selectedId == R.id.nav_account) {
                            bundle.putString("source", "manage");
                        }

                        fragment.setArguments(bundle);
                        ((HomeActivity) getActivity()).LoadFragment(fragment);
                    }
                });

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
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                binding.userName.setText(username != null ? username : "Unknown");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(context).load(imageUrl).placeholder(R.drawable.placeholder).centerCrop().into(binding.userImage);
                } else {
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
                    autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INITIAL_DELAY);
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

    private void moveIndicatorTo(View target, View indicator) {
        indicator.animate().x(target.getX() + 50).setDuration(200).start();
    }
}
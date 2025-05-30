package com.example.lost_and_found_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.PostCardBinding;
import com.example.lost_and_found_app.fragment.PostDetailFragment;
import com.example.lost_and_found_app.model.PostCard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private final List<PostCard> postCards;
    private final String currentUserId;

    public PostCardAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
        this.postCards = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PostCardBinding binding = PostCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostCard post = postCards.get(position);

        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
                .into(holder.binding.postImage);

        holder.binding.postTitle.setText(post.getTitle());
        holder.binding.postInformation.setText(post.getInformation());
        holder.binding.date.setText(post.getDate());
        holder.binding.phone.setText(post.getPhone());
        holder.binding.time.setText(post.getPostTime());
        holder.binding.checkbox.setText(post.getStatus());

        holder.binding.checkbox.setChecked(Objects.equals(post.getStatus(), "Found") || Objects.equals(post.getStatus(), "Returned"));

        loadUserData(holder.binding, post.getUserId(), holder.itemView.getContext());

        String source;
        if (Objects.equals(post.getUserId(), currentUserId)) {
            source = "manage";
        } else if (post.getStatus().equals("Found") || post.getStatus().equals("Not Found")) {
            source = "lost";
        } else if (post.getStatus().equals("Returned") || post.getStatus().equals("Not Returned")) {
            source = "found";
        } else {
            source = "lost";
        }

        holder.itemView.setOnClickListener(view -> {
            PostDetailFragment fragment = new PostDetailFragment();
            Context context = view.getContext();

            if (context instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) context;
                int selectedId = homeActivity.binding.navigationView.getCheckedItem().getItemId();

                Bundle bundle = new Bundle();
                bundle.putString("userId", post.getUserId());
                bundle.putString("postId", post.getPostId());
                bundle.putString("status", post.getStatus());
                bundle.putString("source", source);

                if (selectedId == R.id.nav_lost) {
                    bundle.putString("back", "View lost items");
                } else if (selectedId == R.id.nav_found) {
                    bundle.putString("back", "View found items");
                }

                fragment.setArguments(bundle);
                homeActivity.LoadFragment(fragment);
            }
        });



    }

    private void loadUserData(PostCardBinding binding, String userId, Context context) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    if (username != null) {
                        binding.userName.setText(username);
                    } else {
                        binding.userName.setText("Unknown");
                    }

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(context)
                                .load(profileImageUrl)
                                .centerCrop()
                                .into(binding.userImage);
                    } else {
                        binding.userImage.setImageResource(android.R.color.darker_gray);
                    }
                } else {
                    binding.userName.setText("Unknown");
                    binding.userImage.setImageResource(android.R.color.darker_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.userName.setText("Unknown");
                binding.userImage.setImageResource(android.R.color.darker_gray);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postCards.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPostCards(List<PostCard> newCards) {
        postCards.clear();
        postCards.addAll(newCards);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PostCardBinding binding;

        public ViewHolder(@NonNull PostCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

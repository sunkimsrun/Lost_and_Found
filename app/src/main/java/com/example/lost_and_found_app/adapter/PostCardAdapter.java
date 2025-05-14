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
import com.example.lost_and_found_app.databinding.PostCardBinding;
import com.example.lost_and_found_app.fragment.PostDetailFragment;
import com.example.lost_and_found_app.model.PostCard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        holder.binding.checkbox.setChecked(Objects.equals(post.getStatus(), "Found") || Objects.equals(post.getStatus(), "Returned"));
        holder.binding.checkbox.setText(post.getStatus());

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

            Bundle bundle = new Bundle();
            bundle.putString("postId", post.getPostId());
            bundle.putString("status", post.getStatus());
            bundle.putString("source", source);

            fragment.setArguments(bundle);

            Context context = view.getContext();
            if (context instanceof HomeActivity) {
                ((HomeActivity) context).LoadFragment(fragment);
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

    public void addPostCards(List<PostCard> newCards) {
        int start = postCards.size();
        postCards.addAll(newCards);
        notifyItemRangeInserted(start, newCards.size());
    }

    public PostCard getPostCardAt(int position) {
        return postCards.get(position);
    }

    private String formatDate(String isoDate) {
        try {
            Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(isoDate);
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsedDate);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PostCardBinding binding;

        public ViewHolder(@NonNull PostCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

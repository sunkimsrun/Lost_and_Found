package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.adapter.PostCardAdapter;
import com.example.lost_and_found_app.model.PostCard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManagePostFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostCardAdapter adapter;

    public ManagePostFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_post, container, false);

        recyclerView = rootView.findViewById(R.id.rcv_list_post);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        adapter = new PostCardAdapter(currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadPosts(currentUserId);

        return rootView;
    }

    private void loadPosts(String currentUserId) {
        DatabaseReference lostItemsRef = FirebaseDatabase.getInstance().getReference("lostitems");
        DatabaseReference foundItemsRef = FirebaseDatabase.getInstance().getReference("founditems");

        lostItemsRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<PostCard> posts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    PostCard post = postSnapshot.getValue(PostCard.class);
                    if (post != null) {
                        posts.add(post);
                    }
                }
                adapter.setPostCards(posts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        foundItemsRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<PostCard> posts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    PostCard post = postSnapshot.getValue(PostCard.class);
                    if (post != null) {
                        posts.add(post);
                    }
                }

                adapter.addPostCards(posts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

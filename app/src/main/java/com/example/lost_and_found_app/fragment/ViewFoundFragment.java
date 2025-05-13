package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.adapter.PostCardAdapter;
import com.example.lost_and_found_app.model.PostCard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewFoundFragment extends Fragment {

    private PostCardAdapter adapter;
    private RecyclerView recyclerView;

    public ViewFoundFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_found, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rcv_list_post);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PostCardAdapter();
        recyclerView.setAdapter(adapter);

        loadDataFromFirebase();
    }

    private void loadDataFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("founditems");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PostCard> postCards = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    PostCard card = postSnapshot.getValue(PostCard.class);
                    postCards.add(card);
                }
                adapter.setPostCards(postCards);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}

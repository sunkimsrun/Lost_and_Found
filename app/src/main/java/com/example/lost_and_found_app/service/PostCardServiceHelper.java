package com.example.lost_and_found_app.service;

import android.util.Log;

import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.repository.IApiCallback;
import com.example.lost_and_found_app.repository.PostCardRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostCardServiceHelper {

    private final PostCardRepository repository;

    public PostCardServiceHelper(PostCardRepository repository) {
        this.repository = repository;
    }

    public void getTop3UsersWithMostLostPosts(final IApiCallback<List<Map.Entry<String, Integer>>> callback) {
        repository.getAllPosts("lostitems", new IApiCallback<List<PostCard>>() {
            @Override
            public void onSuccess(List<PostCard> postCards) {
                Map<String, Integer> userLostCount = new HashMap<>();
                for (PostCard post : postCards) {
                    if ("Found".equalsIgnoreCase(post.getStatus()) || "Not Found".equalsIgnoreCase(post.getStatus())) {
                        String userId = post.getUserId();
                        userLostCount.put(userId, userLostCount.getOrDefault(userId, 0) + 1);
                    }
                }
                List<Map.Entry<String, Integer>> top3 = userLostCount.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(3)
                        .collect(Collectors.toList());

                for (Map.Entry<String, Integer> entry : top3) {
                    Log.d("PostCardServiceHelper", "UserId: " + entry.getKey() + ", Lost Posts: " + entry.getValue());
                }

                callback.onSuccess(top3);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}

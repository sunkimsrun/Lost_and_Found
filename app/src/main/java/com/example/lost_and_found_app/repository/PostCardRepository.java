package com.example.lost_and_found_app.repository;

import androidx.annotation.NonNull;

import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.service.PostCardService;
import com.example.lost_and_found_app.util.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostCardRepository {
    private final PostCardService postCardService;

    public PostCardRepository() {
        postCardService = RetrofitClient.getClient().create(PostCardService.class);
    }

    public void getAllPosts(String itemType, final IApiCallback<List<PostCard>> callback) {
        postCardService.getCards(itemType).enqueue(new Callback<Map<String, PostCard>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, PostCard>> call, @NonNull Response<Map<String, PostCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PostCard> postCards = new ArrayList<>(response.body().values());
                    callback.onSuccess(postCards);
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, PostCard>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getPost(String itemType, String postId, final IApiCallback<PostCard> callback) {
        postCardService.getCard(itemType, postId).enqueue(new Callback<PostCard>() {
            @Override
            public void onResponse(@NonNull Call<PostCard> call, @NonNull Response<PostCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostCard> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createPost(String itemType, String postId, PostCard postCard, final IApiCallback<PostCard> callback) {
        postCardService.createCard(itemType, postId, postCard).enqueue(new Callback<PostCard>() {
            @Override
            public void onResponse(@NonNull Call<PostCard> call, @NonNull Response<PostCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostCard> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deletePost(String itemType, String postId, final IApiCallback<String> callback) {
        postCardService.deleteCard(itemType, postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Post deleted successfully");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return "Error: " + response.code() + " - " + response.errorBody().string();
            }
        } catch (IOException e) {
            return "Error: " + response.code() + " - (error parsing body)";
        }
        return "Error: " + response.code() + " - " + response.message();
    }
}

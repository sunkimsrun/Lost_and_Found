package com.example.lost_and_found_app.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.service.PostCardService;
import com.example.lost_and_found_app.util.RetrofitClient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    public void getLatestPosts(String itemType, String orderBy, int limit, final IApiCallback<List<PostCard>> callback) {
        String orderByParam = "\"" + orderBy + "\"";
        postCardService.getLatestCard(itemType, orderByParam, limit).enqueue(new Callback<Map<String, PostCard>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, PostCard>> call, @NonNull Response<Map<String, PostCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PostCard> postCards = new ArrayList<>(response.body().values());

                    postCards.sort((p1, p2) -> {

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date d1 = sdf.parse(p1.getCreatedDate());
                            Date d2 = sdf.parse(p2.getCreatedDate());
                            return d2.compareTo(d1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

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


    public void getCardsByDates(String itemType, String orderBy, String startDate, String endDate, final IApiCallback<List<PostCard>> callback) {

        String orderByParam = "\"" + orderBy + "\"";
        String startDateParam = "\"" + startDate + "\"";
        String endDateParam = "\"" + endDate + "\"";

        Log.d("Date", orderByParam + " - " + startDateParam + " - " + endDateParam);

        postCardService.getCardsByDateRange(itemType, orderByParam, startDateParam, endDateParam).enqueue(new Callback<Map<String, PostCard>>() {
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

    public void createPost(String itemType, String postId, PostCard postCard,
                           final IApiCallback<PostCard> callback) {
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

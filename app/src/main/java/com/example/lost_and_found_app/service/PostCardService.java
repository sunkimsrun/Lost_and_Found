package com.example.lost_and_found_app.service;

import com.example.lost_and_found_app.model.PostCard;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PostCardService {

    @GET("founditems.json")
    Call<Map<String, PostCard>> getCards();

    @GET("founditems/{id}.json")
    Call<PostCard> getCard(@Path("id") String id);

    @POST("founditems.json")
    Call<PostCard> createCard(@Body PostCard postCard);

    @DELETE("founditems/{id}.json")
    Call<Void> deleteCard(@Path("id") String cardId);
}

package com.example.lost_and_found_app.service;

import com.example.lost_and_found_app.model.PostCard;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostCardService {

    @GET("{type}.json")
    Call<Map<String, PostCard>> getCards(@Path("type") String type);

    @GET("{type}/{id}.json")
    Call<PostCard> getCard(@Path("type") String type, @Path("id") String id);

    @GET("{type}.json")
    Call<Map<String, PostCard>> getCardsByDateRange(
            @Path("type") String type,
            @Query("orderBy") String orderBy,
            @Query("startAt") String startDate,
            @Query("endAt") String endDate
    );


    @PUT("{type}/{id}.json")
    Call<PostCard> createCard(@Path("type") String type, @Path("id") String id, @Body PostCard postCard);

    @DELETE("{type}/{id}.json")
    Call<Void> deleteCard(@Path("type") String type, @Path("id") String id);

    @PATCH("{type}/{id}.json")
    Call<PostCard> updatePostStatus(@Path("type") String type, @Path("id") String id, @Body Map<String, String> status);
}

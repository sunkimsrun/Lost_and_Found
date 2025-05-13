package com.example.lost_and_found_app.repository;

public interface IApiCallback<T> {

    void onSuccess(T result);

    void onError(String errorMessage);
}

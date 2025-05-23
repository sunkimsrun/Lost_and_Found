package com.example.lost_and_found_app.model;

public class ViewCardData {
    private final String title;
    private final String subText;
    private final int lottieRes;

    public ViewCardData(String title, String subText, int lottieRes) {
        this.title = title;
        this.subText = subText;
        this.lottieRes = lottieRes;
    }

    public String getTitle() {
        return title;
    }

    public String getSubText() {
        return subText;
    }

    public int getLottieRes() {
        return lottieRes;
    }
}

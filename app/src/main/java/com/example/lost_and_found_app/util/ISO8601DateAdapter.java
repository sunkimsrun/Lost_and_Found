package com.example.lost_and_found_app.util;

import android.annotation.SuppressLint;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ISO8601DateAdapter extends TypeAdapter<Date> {
    private final SimpleDateFormat formatter;

    @SuppressLint("SimpleDateFormat")
    public ISO8601DateAdapter() {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void write(JsonWriter out, Date date) throws IOException {
        if (date == null) {
            out.nullValue();
        } else {
            out.value(formatter.format(date));
        }
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        try {
            String dateStr = in.nextString();
            return formatter.parse(dateStr);
        } catch (Exception e) {

            return null;
        }
    }
}

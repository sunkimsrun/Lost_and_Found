package com.example.lost_and_found_app.util;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DateConverter {
    public static Date convertToDate(String dateString, String timeString) {

        LocalDate date = LocalDate.parse(dateString);
        LocalTime time = LocalTime.parse(timeString);

        LocalDateTime dateTime = LocalDateTime.of(date, time);

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

package com.example.madcamp1st.diary;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Page implements Serializable {
    public Date date;
    public int weather;
    public float rating;
    public String comment;
    public String fid;

    public Page(Date date, int weather, float rating, String comment, String fid) {
        this.date = date;
        this.weather = weather;
        this.rating = rating;
        this.comment = comment;
        this.fid = fid;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-DD");
        return format.format(date);
    }

    public int getWeather() {
        return weather;
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}

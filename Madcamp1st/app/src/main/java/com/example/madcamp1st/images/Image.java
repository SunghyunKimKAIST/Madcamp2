package com.example.madcamp1st.images;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public class Image implements Comparable<Image>{
    @SerializedName("filename")
    public String name;
    public String fid;

    public transient File original;
    public transient File thumbnail;

    public Image(String name, File original, File thumbnail){
        this.name = name;
        this.original = original;
        this.thumbnail = thumbnail;
    }

    @Override
    public int compareTo(Image other) {
        return name.compareTo(other.name);
    }
}
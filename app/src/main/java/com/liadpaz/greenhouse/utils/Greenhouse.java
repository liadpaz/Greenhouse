package com.liadpaz.greenhouse.utils;

import android.annotation.SuppressLint;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

import java.io.Serializable;

@Keep
@SuppressWarnings({"unused"})
public class Greenhouse implements Serializable {

    private String id;
    private int width;
    private int height;
    //    public ArrayList<GreenhousePath> Paths;

    public Greenhouse(String id, int width, int height/*, @Nullable ArrayList<GreenhousePath> paths*/) {
        this.id = id;
        this.width = width;
        this.height = height;
        //        this.Paths = paths;
    }

    public Greenhouse() {}

    @Nullable
    @Exclude
    static Greenhouse parse(@NonNull String greenhouse) {
        return new Gson().fromJson(greenhouse, Greenhouse.class);
    }

    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    @Exclude
    public String toString() {
        return new Gson().toJson(this);
    }
}
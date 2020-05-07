package com.liadpaz.greenhouse.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Json {

    @SuppressWarnings("unused")
    private static final String TAG = "JSON";
    private static SharedPreferences bugs;
    private static SharedPreferences farm;

    public static void setJson(SharedPreferences bugs, SharedPreferences farm) {
        Json.bugs = bugs;
        Json.farm = farm;
    }

    public static void clear() {
        bugs.edit().clear().apply();
        farm.edit().clear().apply();
    }

    public static class JsonBugs {

        @NonNull
        public static ArrayList<Bug> getBugs(String greenhouse) {
            ArrayList<Bug> bugs = new Gson().fromJson(Json.bugs.getString(greenhouse, null), new TypeToken<ArrayList<Bug>>() {}.getType());
            if (bugs == null) {
                bugs = new ArrayList<>();
            }
            return bugs;
        }

        public static void setBugs(@NonNull Greenhouse greenhouse, ArrayList<Bug> bugs) {
            Json.bugs.edit().putString(greenhouse.toString(), new Gson().toJson(bugs)).apply();
        }

        @NonNull
        public static HashMap<Greenhouse, ArrayList<Bug>> getGreenhouses() {
            HashMap<Greenhouse, ArrayList<Bug>> greenhouses = new HashMap<>();
            bugs.getAll().forEach((greenhouse, bugsInGreenhouse) -> greenhouses.put(Greenhouse.parse(greenhouse), new Gson().fromJson(bugsInGreenhouse.toString(), new TypeToken<ArrayList<Bug>>() {}.getType())));
            return greenhouses;
        }
    }

    public static class JsonFarm {

        static void setString(@NonNull String key, @Nullable String value) {
            farm.edit().putString(key, value).apply();
        }

        static String getString(@NonNull String key) {
            return farm.getString(key, null);
        }

        @NonNull
        public static Date getLastUpdate() {
            return new Date(farm.getLong(Constants.SharedPrefConstants.LAST_UPDATE, 0));
        }

        public static void setLastUpdate(@NonNull Date date) {
            farm.edit().putLong(Constants.SharedPrefConstants.LAST_UPDATE, date.getTime()).apply();
        }
    }
}
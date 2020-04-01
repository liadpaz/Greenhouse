package com.liadpaz.greenhouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

class Utilities {

    @SuppressWarnings("unused")
    private static final String TAG = "UTILITIES";
    // TODO: check if name and farm is necessary
    @SuppressWarnings("FieldCanBeLocal")
    private static String farm;
    private static Role role;
    private static DocumentReference greenhousesRef;
    private static DatabaseReference bugsRef;

    static void setCurrentFarm(String farmId) {
        farm = farmId;
        greenhousesRef = FirebaseFirestore.getInstance().collection(Constants.FirebaseConstants.FARMS).document(farm);
        bugsRef = FirebaseDatabase.getInstance().getReference(Constants.FirebaseConstants.BUGS).child(farm);
    }

    static String getName() {
        return Json.JsonFarm.getString(Constants.SharedPrefConstants.NAME);
    }

    static void setRole(Context context, @NonNull String name) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Inspector;
            Json.JsonFarm.setString(Constants.SharedPrefConstants.NAME, name);
        }
    }

    static String getId() {
        return Json.JsonFarm.getString(Constants.SharedPrefConstants.FARM);
    }

    static void setId(String id) {
        Json.JsonFarm.setString(Constants.SharedPrefConstants.ID, id);
    }

    @Nullable
    static Role getRole() {
        return role;
    }

    static void setRole(Context context) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Exterminator;
        }
    }

    @NonNull
    static DocumentReference getGreenhousesRef() {
        return greenhousesRef;
    }

    @NonNull
    static DatabaseReference getBugsRef() {
        return bugsRef;
    }

    static CompletableFuture<Boolean> checkConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return InetAddress.getByName("google.com").isReachable(3000);
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    public enum Role {
        Inspector, Exterminator
    }

}

@SuppressWarnings("SpellCheckingInspection")
@SuppressLint("SimpleDateFormat")
class DateParser {
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSSSSSS");
}

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class Greenhouse implements Serializable {

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

    public Greenhouse() {
    }

    @Nullable
    @Exclude
    static Greenhouse parse(@NotNull String greenhouse) {
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

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class GreenhousePath implements Serializable {
    public Block Path;
    public int X;
    public int Y;
    public int Width;
    public int Height;

    public GreenhousePath(Block path, int x, int y, int width, int height) {
        Path = path;
        X = x;
        Y = y;
        Width = width;
        Height = height;
    }

    public GreenhousePath() {
    }

    enum Block {
        Entrance, Road
    }
}

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class Bug {

    private String greenhouse;
    private String time;
    private double x;
    private double y;

    public Bug(String greenhouse, Date time, double x, double y) {
        this.greenhouse = greenhouse;
        this.time = DateParser.dateFormat.format(time);
        this.x = Math.round(x * 100) / 100.0;
        this.y = Math.round(y * 100) / 100.0;
    }

    public Bug() {
    }

    public String getGreenhouse() {
        return greenhouse;
    }

    public void setGreenhouse(String greenhouse) {
        this.greenhouse = greenhouse;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @SuppressWarnings("ConstantConditions")
    @Exclude
    int getId() {
        try {
            return (int)DateParser.dateFormat.parse(time).getTime() + (int)x + (int)y;
        } catch (Exception ignored) {
            return 0;
        }
    }
}

class Json {

    @SuppressWarnings("unused")
    private static final String TAG = "JSON";
    private static SharedPreferences bugs;
    private static SharedPreferences farm;

    static void setJson(SharedPreferences bugs, SharedPreferences farm) {
        Json.bugs = bugs;
        Json.farm = farm;
    }

    static void clear() {
        bugs.edit().clear().apply();
        farm.edit().clear().apply();
    }

    static class JsonBugs {

        @NotNull
        static ArrayList<Bug> getBugs(String greenhouse) {
            ArrayList<Bug> bugs = new Gson().fromJson(Json.bugs.getString(greenhouse, null), new TypeToken<ArrayList<Bug>>() {}.getType());
            if (bugs == null) {
                bugs = new ArrayList<>();
            }
            return bugs;
        }

        static void setBugs(@NotNull Greenhouse greenhouse, ArrayList<Bug> bugs) {
            Json.bugs.edit().putString(greenhouse.toString(), new Gson().toJson(bugs)).apply();
        }

        @NotNull
        static HashMap<Greenhouse, ArrayList<Bug>> getGreenhouses() {
            HashMap<Greenhouse, ArrayList<Bug>> greenhouses = new HashMap<>();
            bugs.getAll().forEach((greenhouse, bugsInGreenhouse) -> greenhouses.put(Greenhouse.parse(greenhouse), new Gson().fromJson(bugsInGreenhouse.toString(), new TypeToken<ArrayList<Bug>>() {}.getType())));
            return greenhouses;
        }
    }

    static class JsonFarm {

        static void setString(@NonNull String key, @Nullable String value) {
            farm.edit().putString(key, value).apply();
        }

        static String getString(@NonNull String key) {
            return farm.getString(key, null);
        }

        @NotNull
        static Date getLastUpdate() {
            return new Date(farm.getLong(Constants.SharedPrefConstants.LAST_UPDATE, 0));
        }

        static void setLastUpdate(@NotNull Date date) {
            farm.edit().putLong(Constants.SharedPrefConstants.LAST_UPDATE, date.getTime()).apply();
        }
    }
}
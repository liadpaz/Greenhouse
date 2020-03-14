package com.liadpaz.greenhouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

class Utilities {

    // TODO: check if name and farm is necessary
    @SuppressWarnings("unused")
    private static String name;
    @SuppressWarnings("FieldCanBeLocal")
    private static String farm;
    private static Role role;
    private static DatabaseReference greenhousesRef;
    private static DatabaseReference bugsRef;

    static void setCurrentFarm(String farmId) {
        farm = farmId;
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference();
        greenhousesRef = mainRef.child("Farms/" + farm);
        bugsRef = mainRef.child("Bugs/" + farm);
    }

    static void setName(String name) {
        Utilities.name = name;
    }

    static void setRole(Context context, @NonNull String name) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Inspector;
            Utilities.name = name;
        }
    }

    @Contract(pure = true)
    @Nullable
    static Role getRole() {
        return role;
    }

    static void setRole(Context context) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Exterminator;
        }
    }

    @Contract(pure = true)
    @NonNull
    static DatabaseReference getGreenhousesRef() {
        return greenhousesRef;
    }

    @Contract(pure = true)
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

    public String Id;
    public int Width;
    public int Height;
    public ArrayList<GreenhousePath> Paths;

    public Greenhouse(String id, int width, int height, @Nullable ArrayList<GreenhousePath> paths) {
        this.Id = id;
        this.Width = width;
        this.Height = height;
        this.Paths = paths;
    }

    public Greenhouse() {
    }

    @Nullable
    static Greenhouse parse(@NotNull String greenhouse) {
        String[] parts = greenhouse.split("`");
        try {
            return new Greenhouse(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), null);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("%s`%d`%d", Id, Width, Height);
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

    public String Greenhouse;
    public String Time;
    public double X;
    public double Y;

    public Bug(String greenhouse, Date time, double x, double y) {
        this.Greenhouse = greenhouse;
        this.Time = DateParser.dateFormat.format(time);
        this.X = Math.round(x * 100) / 100.0;
        this.Y = Math.round(y * 100) / 100.0;
    }

    public Bug() {
    }

    @SuppressWarnings("ConstantConditions")
    int getId() {
        try {
            return (int)DateParser.dateFormat.parse(Time).getTime() + (int)X + (int)Y;
        } catch (Exception ignored) {
            return 0;
        }
    }
}

class JsonBug {

    @SuppressWarnings("unused")
    private static final String TAG = "JSON_BUG";
    private static SharedPreferences bugs;

    static void setJson(SharedPreferences bugs) {
        JsonBug.bugs = bugs;
    }

    @NotNull
    static ArrayList<Bug> getBugs(String greenhouse, @NotNull AtomicBoolean inTask) {
        inTask.set(true);
        ArrayList<Bug> bugs = new Gson().fromJson(JsonBug.bugs.getString(greenhouse, null), new TypeToken<ArrayList<Bug>>() {}.getType());
        if (bugs == null) {
            bugs = new ArrayList<>();
        }
        Log.d(TAG, "getBugs: " + bugs.size());
        inTask.set(false);
        return bugs;
    }

    @SuppressLint("ApplySharedPref")
    static void setBugs(Greenhouse greenhouse, ArrayList<Bug> bugs, AtomicBoolean inTask) {
        new Thread(() -> {
            inTask.set(true);
            JsonBug.bugs.edit().putString(greenhouse.toString(), new Gson().toJson(bugs)).commit();
            inTask.set(false);
        }).start();
    }

    @SuppressLint("ApplySharedPref")
    static void setBugs(Greenhouse greenhouse, ArrayList<Bug> bugs, Runnable runnable) {
        new Thread(() -> {
            JsonBug.bugs.edit().putString(greenhouse.toString(), new Gson().toJson(bugs)).commit();
            runnable.run();
        }).start();
    }

    @NotNull
    static HashMap<String, String> getGreenhouses() {
        HashMap<String, String> greenhouses = new HashMap<>();
        bugs.getAll().forEach((key, value) -> {
            if (!key.equals("last-update")) {
                greenhouses.put(key, value.toString());
            }
        });
        return greenhouses;
    }

    @SuppressLint("ApplySharedPref")
    static void clear(@NotNull AtomicBoolean inTask) {
        Log.d(TAG, "clear: CLEAR");
        inTask.set(true);
        new Thread(() -> {
            bugs.edit().clear().commit();
            inTask.set(false);
        }).start();
    }

    @SuppressLint("ApplySharedPref")
    static void setLastUpdate(@NotNull Date date, @NotNull AtomicBoolean inTask) {
        inTask.set(true);
        new Thread(() -> {
            bugs.edit().putString("last-update", DateParser.dateFormat.format(date)).commit();
            inTask.set(false);
        }).start();
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    static Date getLastUpdate() {
        try {
            return DateParser.dateFormat.parse(bugs.getString("last-update", null));
        } catch (Exception ignored) {
            return new Date();
        }
    }
}

// TODO: check if class is necessary
class JsonFarm {

    static SharedPreferences farm;

    static void setJson(SharedPreferences farm) {
        JsonFarm.farm = farm;
    }
}
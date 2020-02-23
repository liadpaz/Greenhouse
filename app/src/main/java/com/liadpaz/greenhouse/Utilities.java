package com.liadpaz.greenhouse;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class Utilities {

    private static String name;
    private static String farm;
    private static Role role;
    private static DatabaseReference greenhousesRef;
    private static DatabaseReference bugsRef;

    static String getFarm() {
        return farm;
    }

    static void setCurrentFarm(String farmId) {
        farm = farmId;
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference();
        greenhousesRef = mainRef.child("Farms/" + farm);
        bugsRef = mainRef.child("Bugs/" + farm);
    }

    static String getName() {
        return Utilities.name;
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

    static Role getRole() {
        return role;
    }

    static void setRole(Context context) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Exterminator;
        }
    }

    static DatabaseReference getGreenhousesRef() {
        return greenhousesRef;
    }

    static DatabaseReference getBugsRef() {
        return bugsRef;
    }

    public enum Role {
        Inspector,
        Exterminator
    }
}

@SuppressLint("SimpleDateFormat")
class DateParser {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSSSSSSX");

    static Date parseDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (Exception ignored) {
            return null;
        }
    }
}

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class Greenhouse implements Serializable {

    public String Id;
    public int Width;
    public int Height;
    public ArrayList<GreenhousePath> Paths;

    public Greenhouse(String id, int width, int height, ArrayList<GreenhousePath> paths) {
        this.Id = id;
        this.Width = width;
        this.Height = height;
        this.Paths = paths;
    }

    public Greenhouse() {
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
        Entrance,
        Road
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
        this.X = x;
        this.Y = y;
    }

    public Bug() {
    }
}

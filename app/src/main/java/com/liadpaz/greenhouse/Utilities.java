package com.liadpaz.greenhouse;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

class Utilities {

    private static String name;
    private static Role role;
    private static DatabaseReference greenhousesRef;
    private static DatabaseReference bugsRef;

    static void setReferences(String farmId) {
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference();
        greenhousesRef = mainRef.child("Farms/" + farmId);
        bugsRef = mainRef.child("Bugs/" + farmId);
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

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class Greenhouse {

    public String Id;
    public String Width;
    public String Height;

    public Greenhouse(String id, String width, String height) {
        this.Id = id;
        this.Width = width;
        this.Height = height;
    }

    public Greenhouse() {
    }
}

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class Bug {

    public String Greenhouse;
    public Date Time;
    public double X;
    public double Y;

    public Bug(String greenhouse, Date time, double x, double y) {
        this.Greenhouse = greenhouse;
        this.Time = time;
        this.X = x;
        this.Y = y;
    }

    public Bug() {
    }
}

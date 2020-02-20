package com.liadpaz.greenhouse;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class Utilities {

    public enum Role{
        Inspector,
        Exterminator
    }

    static void setReferences(String farmId) {
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference();
        greenhousesRef = mainRef.child("Farms/" + farmId);
        bugsRef = mainRef.child("Bugs/" + farmId);
    }

    static void setRole(Context context) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Exterminator;
        }
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

    static DatabaseReference getGreenhousesRef() {
        return greenhousesRef;
    }

    static DatabaseReference getBugsRef() {
        return bugsRef;
    }

    private static String name;

    private static Role role;

    private static DatabaseReference greenhousesRef;
    private static DatabaseReference bugsRef;
}

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
class Greenhouse {

    public Greenhouse(String id, String width, String height) {
        this.Id = id;
        this.Width = width;
        this.Height = height;
    }

    public Greenhouse() {
    }

    public String Id;
    public String Width;
    public String Height;
}

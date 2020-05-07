package com.liadpaz.greenhouse.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.liadpaz.greenhouse.activities.MainActivity;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class Utilities {

    @SuppressWarnings("unused")
    private static final String TAG = "UTILITIES";
    // TODO: check if name and farm is necessary
    @SuppressWarnings("FieldCanBeLocal")
    private static String farm;
    private static Role role;
    private static DocumentReference greenhousesRef;
    private static DatabaseReference bugsRef;

    public static void setCurrentFarm(String farmId) {
        farm = farmId;
        greenhousesRef = FirebaseFirestore.getInstance().collection(Constants.FirebaseConstants.FARMS).document(farm);
        bugsRef = FirebaseDatabase.getInstance().getReference(Constants.FirebaseConstants.BUGS).child(farm);
    }

    public static String getName() {
        return Json.JsonFarm.getString(Constants.SharedPrefConstants.NAME);
    }

    public static void setRole(Context context, @NonNull String name) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Inspector;
            Json.JsonFarm.setString(Constants.SharedPrefConstants.NAME, name);
        }
    }

    public static String getId() {
        return Json.JsonFarm.getString(Constants.SharedPrefConstants.FARM);
    }

    public static void setId(String id) {
        Json.JsonFarm.setString(Constants.SharedPrefConstants.ID, id);
    }

    @Nullable
    public static Role getRole() {
        return role;
    }

    public static void setRole(Context context) {
        if (context instanceof MainActivity) {
            Utilities.role = Role.Exterminator;
        }
    }

    @NonNull
    public static DocumentReference getGreenhousesRef() {
        return greenhousesRef;
    }

    @NonNull
    public static DatabaseReference getBugsRef() {
        return bugsRef;
    }

    @NonNull
    public static CompletableFuture<Boolean> checkConnection() {
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

package com.liadpaz.greenhouse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int FIREBASE_AUTH = 275;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_main_inspector).setOnClickListener((v) -> {
            if (auth.getCurrentUser() == null) {
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(false).build()))
                        .build(), FIREBASE_AUTH);
            } else {
                checkFarms();
            }
        });
        findViewById(R.id.btn_main_spray).setOnClickListener((v -> new FarmIdSelectDialog(MainActivity.this).show()));

        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().reload().addOnCompleteListener(task -> {
               if (auth.getCurrentUser() != null) {
                   userRef = FirebaseDatabase.getInstance().getReference("Users/" + auth.getCurrentUser().getUid());
                   checkFarms();
               }
            });
        }
    }

    /**
     * This function iterates over all the user's farms and if there are more than one, shows a select dialog
     */
    private void checkFarms() {
        ArrayList<AbstractMap.SimpleEntry<String, String>> farms = new ArrayList<>();
        farms.add(new AbstractMap.SimpleEntry<>(getString(R.string.no_farm), ""));
        userRef.child("Farms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot farm : dataSnapshot.getChildren()) {
                    farms.add(new AbstractMap.SimpleEntry<>(farm.getValue(String.class), farm.getKey()));
                }
                if (farms.size() > 2) {
                    new FarmSelectDialog(MainActivity.this, farms).show();
                } else {
                    startActivity(new Intent(MainActivity.this, GreenhouseSelectorActivity.class).putExtra("Farm", farms.get(1).getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FIREBASE_AUTH) {
            if (resultCode == RESULT_OK) {
                userRef = FirebaseDatabase.getInstance().getReference("Users/" + Objects.requireNonNull(auth.getCurrentUser()).getUid());
                checkFarms();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

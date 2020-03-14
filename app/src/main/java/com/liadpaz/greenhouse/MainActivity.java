package com.liadpaz.greenhouse;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liadpaz.greenhouse.databinding.ActivityMainBinding;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final int FIREBASE_AUTH = 275;

    private volatile AtomicBoolean inTask = new AtomicBoolean(false);

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarMain);

        auth = FirebaseAuth.getInstance();
        binding.btnMainInspector.setOnClickListener((v) -> {
            if (!inTask.get()) {
                try {
                    if (Utilities.checkConnection().get()) {
                        if (auth.getCurrentUser() == null) {
                            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(false).build())).build(), FIREBASE_AUTH);
                        } else {
                            Utilities.setRole(MainActivity.this, auth.getCurrentUser().getDisplayName());
                            checkFarms();
                        }
                    } else {
                        startActivity(new Intent(MainActivity.this, GreenhouseSelectActivity.class));
                    }
                } catch (Exception ignored) {
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            }
        });
        binding.btnMainSpray.setOnClickListener((v -> {
            Utilities.setRole(MainActivity.this);
            new FarmIdSelectDialog(MainActivity.this).show();
        }));

        JsonBug.setJson(getSharedPreferences("bugs", 0));
        JsonFarm.setJson(getSharedPreferences("farm", 0));

        try {
            if (Utilities.checkConnection().get()) {
                if (auth.getCurrentUser() != null) {
                    inTask.set(true);
                    auth.getCurrentUser().reload().addOnCompleteListener(task -> {
                        if (auth.getCurrentUser() != null) {
                            userRef = FirebaseDatabase.getInstance().getReference("Users/" + auth.getCurrentUser().getUid());
                            inTask.set(false);
                            checkFarms();
                        }
                    });
                }
            }
        } catch (Exception ignored) {
        }

        if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - JsonBug.getLastUpdate().getTime()) >= 12) {
            JsonBug.clear(inTask);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (auth.getCurrentUser() == null) {
            menu.findItem(R.id.menu_main_logout).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_main_logout) {
            if (!inTask.get()) {
                AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        item.setVisible(false);
                        Toast.makeText(MainActivity.this, R.string.logout_successful, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function iterates over all the user's farms and if there are more than one, shows a
     * select dialog
     */
    private void checkFarms() {
        inTask.set(true);
        HashMap<String, String> farms = new HashMap<>();
        farms.put(getString(R.string.no_farm), "");
        userRef.child("Farms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstKey = null;
                for (DataSnapshot farm : dataSnapshot.getChildren()) {
                    farms.put(farm.getValue(String.class), firstKey = farm.getKey());
                }
                if (farms.size() > 2) {
                    try {
                        Dialog farmDialog = new FarmSelectDialog(MainActivity.this, farms);
                        farmDialog.setOnDismissListener(dialog -> inTask.set(false));
                        farmDialog.show();
                    } catch (Exception ignored) {
                    }
                } else {
                    startActivity(new Intent(MainActivity.this, GreenhouseSelectActivity.class).putExtra("Farm", firstKey));
                    inTask.set(false);
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
                Toolbar toolbar = findViewById(R.id.toolbar_main);
                toolbar.getMenu().findItem(R.id.menu_main_logout).setVisible(true);
                Utilities.setRole(MainActivity.this, Objects.requireNonNull(auth.getCurrentUser().getDisplayName()));
                checkFarms();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

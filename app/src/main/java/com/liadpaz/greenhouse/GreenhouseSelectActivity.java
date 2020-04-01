package com.liadpaz.greenhouse;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.liadpaz.greenhouse.databinding.ActivityGreenhouseSelectBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.liadpaz.greenhouse.Constants.FirebaseConstants;
import static com.liadpaz.greenhouse.Constants.GreenhouseExtra;
import static com.liadpaz.greenhouse.Constants.GreenhouseSelectExtra;

public class GreenhouseSelectActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "ACTIVITY_GREENHOUSE_SELECT";
    private static final int GREENHOUSE_ACTIVITY = 768;

    private volatile AtomicBoolean inTask = new AtomicBoolean(true);

    private ListView lv_greenhouses;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGreenhouseSelectBinding binding = ActivityGreenhouseSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarGreenhouseSelect);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.greenhouse_select);

        String farm = getIntent().getStringExtra(GreenhouseSelectExtra.FARM);

        lv_greenhouses = binding.lvGreenhouses;

        lv_greenhouses.setAdapter(new GreenhousesAdapter(GreenhouseSelectActivity.this));
        lv_greenhouses.setOnItemClickListener((parent, view, position, id) -> {
            if (inTask.get()) {
                Toast.makeText(GreenhouseSelectActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            } else {
                startActivityForResult(new Intent(GreenhouseSelectActivity.this, GreenhouseActivity.class).putExtra(GreenhouseExtra.GREENHOUSE, (Greenhouse)parent.getItemAtPosition(position)), GREENHOUSE_ACTIVITY);
            }
        });

        if (farm != null) {
            Utilities.setCurrentFarm(farm);
            Utilities.setId(farm);
        }
        inTask.set(true);
        Utilities.checkConnection().thenApplyAsync(connection -> {
            inTask.set(false);
            if (connection) {
                if (Json.JsonBugs.getGreenhouses().size() == 0) {
                    downloadBugs();
                } else {
                    runOnUiThread(() -> new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.local_cloud).setMessage(R.string.local_cloud_message).setNegativeButton(R.string.cloud, ((dialog, which) -> downloadBugs())).setPositiveButton(R.string.local, (dialog, which) -> useLocalBugs()).show());
                }
            } else {
                useLocalBugs();
            }
            return null;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_greenhouse_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_try_upload: {
                Utilities.checkConnection().thenApplyAsync(connection -> {
                    if (connection) {
                        runOnUiThread(() -> new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.upload_to_cloud).setMessage(R.string.upload_to_cloud_message).setNegativeButton(R.string.dont, null).setPositiveButton(R.string.upload, (dialog, which) -> {
                            HashMap<String, ArrayList<Bug>> bugs = new HashMap<>();
                            Json.JsonBugs.getGreenhouses().forEach(((greenhouse, bugsList) -> bugs.put(greenhouse.getId(), bugsList)));
                            Utilities.getBugsRef().runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                    currentData.setValue(bugs);
                                    return Transaction.success(currentData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    Toast.makeText(GreenhouseSelectActivity.this, committed ? R.string.upload_successful : R.string.upload_fail, Toast.LENGTH_LONG).show();
                                }
                            });
                        }).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(GreenhouseSelectActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show());
                    }
                    return null;
                });
                break;
            }

            case R.id.menu_try_sync: {
                Utilities.checkConnection().thenApplyAsync(connection -> {
                    if (connection) {
                        runOnUiThread(() -> new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.sync_with_cloud).setMessage(R.string.sync_with_cloud_message).setNegativeButton(R.string.dont, null).setPositiveButton(R.string.sync, (dialog, which) -> downloadBugs()).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(GreenhouseSelectActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show());
                    }
                    return null;
                });
                break;
            }

            default: {
                return false;
            }
        }
        return true;
    }

    /**
     * This function is used to download bugs data from the cloud (Firebase Firestore)
     */
    @SuppressWarnings("ConstantConditions")
    private void downloadBugs() {
        ((GreenhousesAdapter)lv_greenhouses.getAdapter()).clear();
        inTask.set(true);
        Utilities.getGreenhousesRef().collection(FirebaseConstants.GREENHOUSES).get().addOnSuccessListener(documents -> documents.getDocuments().forEach(greenhouse -> {
            Greenhouse currentGreenhouse = new Greenhouse(greenhouse.getId(), greenhouse.get(FirebaseConstants.WIDTH, Integer.class), greenhouse.get(FirebaseConstants.HEIGHT, Integer.class));
            ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(currentGreenhouse, greenhouse.get(FirebaseConstants.BUG_COUNT, Integer.class));
            Utilities.getBugsRef().child(currentGreenhouse.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    inTask.set(true);
                    Json.JsonBugs.setBugs(currentGreenhouse, dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<Bug>>() {}));
                    inTask.set(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }));
        Json.JsonFarm.setLastUpdate(new Date());
    }

    /**
     * This function is used to load bugs data from local file (bugs in Shared Preferences)
     */
    private void useLocalBugs() {
        Json.JsonBugs.getGreenhouses().forEach((greenhouse, bugs) -> ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(greenhouse, bugs.size()));
        inTask.set(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GREENHOUSE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                try {
                    Json.JsonBugs.getGreenhouses().forEach((greenhouse, bugs) -> ((GreenhousesAdapter)lv_greenhouses.getAdapter()).updateGreenhouseBugs(greenhouse.getId(), bugs.size()));
                    Json.JsonFarm.setLastUpdate(new Date());
                } catch (Exception ignored) {
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

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
import com.liadpaz.greenhouse.Utilities.TaskFinished;
import com.liadpaz.greenhouse.databinding.ActivityGreenhouseSelectBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

        String farm = getIntent().getStringExtra("Farm");

        lv_greenhouses = binding.lvGreenhouses;

        lv_greenhouses.setAdapter(new GreenhousesAdapter(GreenhouseSelectActivity.this));
        lv_greenhouses.setOnItemClickListener((parent, view, position, id) -> {
            if (inTask.get()) {
                Toast.makeText(GreenhouseSelectActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            } else {
                startActivityForResult(new Intent(GreenhouseSelectActivity.this, GreenhouseActivity.class).putExtra("Greenhouse", (Greenhouse)parent.getItemAtPosition(position)), GREENHOUSE_ACTIVITY);
            }
        });

        Utilities.setCurrentFarm(farm);
        inTask.set(true);
        Utilities.checkConnection().thenApplyAsync(connection -> {
            if (connection) {
                if (JsonBug.getGreenhouses().size() != 0) {
                    runOnUiThread(() -> new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.local_cloud).setMessage(R.string.local_cloud_message).setNegativeButton(R.string.cloud, ((dialog, which) -> downloadBugs(new TaskFinished() {
                        @Override
                        public void Success() {
                            inTask.set(false);
                        }

                        @Override
                        public void Fail() {
                            inTask.set(false);
                        }
                    }))).setPositiveButton(R.string.local, (dialog, which) -> useLocalBugs()).show());
                } else {
                    downloadBugs(new TaskFinished() {
                        @Override
                        public void Success() {
                            inTask.set(false);
                        }

                        @Override
                        public void Fail() {
                            inTask.set(false);
                        }
                    });
                }
            } else {
                useLocalBugs();
                inTask.set(false);
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
                            JsonBug.getGreenhouses().forEach(((greenhouse, bugsList) -> bugs.put(greenhouse.Id, bugsList)));
                            Utilities.getBugsRef().runTransaction(new Transaction.Handler() {
                                @SuppressWarnings("ConstantConditions")
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                    HashMap<String, ArrayList<Bug>> databaseBugs = currentData.getValue(new GenericTypeIndicator<HashMap<String, ArrayList<Bug>>>() {});
                                    databaseBugs.forEach((greenhouse, bugsList) -> {
                                        if (!bugs.get(greenhouse).equals(bugsList)) {
                                            currentData.child(greenhouse).setValue(bugs.get(greenhouse));
                                        }
                                    });
                                    bugs.keySet().forEach(greenhouse -> {
                                        if (!databaseBugs.containsKey(greenhouse)) {
                                            currentData.child(greenhouse).setValue(bugs.get(greenhouse));
                                        }
                                    });
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
                        runOnUiThread(() -> new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.sync_with_cloud).setMessage(R.string.sync_with_cloud_message).setNegativeButton(R.string.dont, null).setPositiveButton(R.string.sync, (dialog, which) -> downloadBugs(new TaskFinished() {
                            @Override
                            public void Success() {
                                Toast.makeText(GreenhouseSelectActivity.this, R.string.sync_successful, Toast.LENGTH_LONG).show();
                                inTask.set(false);
                            }

                            @Override
                            public void Fail() {
                                Toast.makeText(GreenhouseSelectActivity.this, R.string.sync_fail, Toast.LENGTH_LONG).show();
                                inTask.set(false);
                            }
                        })).show());
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

    @SuppressWarnings("ConstantConditions")
    private void downloadBugs(@Nullable TaskFinished taskFinished) {
        ((GreenhousesAdapter)lv_greenhouses.getAdapter()).clear();
        Utilities.getGreenhousesRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final long count = snapshot.getChildrenCount();
                AtomicInteger current = new AtomicInteger(0);
                for (DataSnapshot greenhouse : snapshot.getChildren()) {
                    Utilities.getBugsRef().child(greenhouse.getValue(Greenhouse.class).Id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            JsonBug.setBugs(greenhouse.getValue(Greenhouse.class), snapshot.getValue(new GenericTypeIndicator<ArrayList<Bug>>() {}), inTask);
                            ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(greenhouse.getValue(Greenhouse.class), (int)snapshot.getChildrenCount());
                            if (current.incrementAndGet() == count) {
                                inTask.set(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
                if (taskFinished != null) {
                    taskFinished.Success();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (taskFinished != null) {
                    taskFinished.Fail();
                }
            }
        });
    }

    private void useLocalBugs() {
        runOnUiThread(() -> JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(greenhouse, bugs.size())));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GREENHOUSE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                try {
                    JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> ((GreenhousesAdapter)lv_greenhouses.getAdapter()).updateGreenhouseBugs(greenhouse.Id, bugs.size()));
                    JsonBug.setLastUpdate(new Date(), inTask);
                } catch (Exception ignored) {
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

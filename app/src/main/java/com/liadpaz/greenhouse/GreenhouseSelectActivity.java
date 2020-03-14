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
        Utilities.checkConnection().thenApply(connection -> {
            if (connection) {
                Utilities.getGreenhousesRef().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final long count = dataSnapshot.getChildrenCount();
                        AtomicInteger current = new AtomicInteger(0);
                        for (DataSnapshot greenhouse : dataSnapshot.getChildren()) {
                            Utilities.getBugsRef().child(greenhouse.getValue(Greenhouse.class).Id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    JsonBug.setBugs(greenhouse.getValue(Greenhouse.class), dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<Bug>>() {}), inTask);
                                    ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(greenhouse.getValue(Greenhouse.class), (int)dataSnapshot.getChildrenCount());
                                    if (current.incrementAndGet() == count) {
                                        inTask.set(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            } else {
                JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(Greenhouse.parse(greenhouse), bugs.size()));
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
                new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.upload_to_cloud).setMessage(R.string.upload_to_cloud_message).setNegativeButton(R.string.dont, null).setPositiveButton(R.string.upload, (dialog, which) -> Utilities.checkConnection().thenApply(connection -> {
                    if (connection) {
                        final HashMap<String, ArrayList<Bug>> bugs = JsonBug.getGreenhouses();
                        Utilities.getBugsRef().runTransaction(new Transaction.Handler() {
                            @SuppressWarnings("ConstantConditions")
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                HashMap<String, ArrayList<Bug>> databaseBugs = mutableData.getValue(new GenericTypeIndicator<HashMap<String, ArrayList<Bug>>>() {});
                                databaseBugs.forEach((greenhouse, bugsList) -> {
                                    if (bugs.get(greenhouse) != bugsList) {
                                        mutableData.setValue(bugs.get(greenhouse));
                                    }
                                });
                                bugs.keySet().forEach(greenhouse -> {
                                    if (!databaseBugs.containsKey(greenhouse)) {
                                        mutableData.setValue(bugs.get(greenhouse));
                                    }
                                });
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                Toast.makeText(GreenhouseSelectActivity.this, b ? R.string.upload_successful : R.string.upload_fail, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(GreenhouseSelectActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                    }
                    return null;
                })).show();
                break;
            }

            case R.id.menu_try_sync: {
//                new AlertDialog.Builder(GreenhouseSelectActivity.this).setTitle(R.string.).setMessage(R.string.).setNegativeButton(R.string., null).setPositiveButton(R.string., (dialog, which) -> {
//
//                }).show();
                break;
            }

            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GREENHOUSE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                try {
                    JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> ((GreenhousesAdapter)lv_greenhouses.getAdapter()).updateGreenhouseBugs(greenhouse, bugs.size()));
                    JsonBug.setLastUpdate(new Date(), inTask);
                } catch (Exception ignored) {
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

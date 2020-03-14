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
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
        try {
            inTask.set(true);
            if (Utilities.checkConnection().get()) {
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
                JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> {
                    ArrayList<Bug> bugArrayList = new Gson().fromJson(bugs, new TypeToken<ArrayList<Bug>>() {}.getType());
                    if (bugArrayList == null) {
                        bugArrayList = new ArrayList<>();
                    }
                    ((GreenhousesAdapter)lv_greenhouses.getAdapter()).addItem(Greenhouse.parse(greenhouse), bugArrayList.size());
                });
                inTask.set(false);
            }
        } catch (Exception ignored) {
            inTask.set(false);
        }
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
                        HashMap<String, ArrayList<Bug>> bugs = new HashMap<>();
                        Utilities.getBugsRef().setValue(null).addOnCompleteListener(task -> Toast.makeText(GreenhouseSelectActivity.this, task.isSuccessful() ? R.string.upload_successful : R.string.upload_fail, Toast.LENGTH_LONG).show());
                    }
                    return null;
                })).show();
                break;
            }

            case R.id.menu_try_sync:
//                new AlertDialog.Builder(GreenhouseSelectActivity.this)
                break;

            default:
                return false;
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GREENHOUSE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> {
                    ArrayList<Bug> bugArrayList = new Gson().fromJson(bugs, new TypeToken<ArrayList<Bug>>() {}.getType());
                    if (bugArrayList == null) {
                        bugArrayList = new ArrayList<>();
                    }
                    ((GreenhousesAdapter)lv_greenhouses.getAdapter()).updateGreenhouseBugs(Greenhouse.parse(greenhouse).Id, bugArrayList.size());
                });
                JsonBug.setLastUpdate(new Date(), inTask);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

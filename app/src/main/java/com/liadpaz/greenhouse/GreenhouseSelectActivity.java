package com.liadpaz.greenhouse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GreenhouseSelectActivity extends AppCompatActivity {

    private static final int GREENHOUSE_ACTIVITY = 768;
    private static final String TAG = "ACTIVITY_GREENHOUSE_SELECT";

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GREENHOUSE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                JsonBug.getGreenhouses().forEach((greenhouse, bugs) -> {
                    ArrayList<Bug> bugArrayList = new Gson().fromJson(bugs, new TypeToken<ArrayList<Bug>>() {}.getType());
                    if (bugArrayList == null) {
                        bugArrayList = new ArrayList<>();
                    }
                    ((GreenhousesAdapter)lv_greenhouses.getAdapter()).updateGreenhouseBugs(greenhouse, bugArrayList.size());
                });
                JsonBug.setLastUpdate(new Date(), inTask);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

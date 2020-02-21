package com.liadpaz.greenhouse;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class GreenhouseSelectActivity extends AppCompatActivity {

    private ArrayList<Greenhouse> greenhouses = new ArrayList<>();

    private volatile AtomicBoolean inTask = new AtomicBoolean(true);

    private ListView lv_greenhouses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouse_selector);

        String farm = Objects.requireNonNull(getIntent().getStringExtra("Farm"));

        lv_greenhouses = findViewById(R.id.lv_greenhouses);

        Utilities.setCurrentFarm(farm);
        Utilities.getGreenhousesRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inTask.set(true);
                greenhouses.clear();
                for (DataSnapshot greenhouse : dataSnapshot.getChildren()) {
                    greenhouses.add(greenhouse.getValue(Greenhouse.class));
                }
                Utilities.getBugsRef().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String, Integer> bugs = new HashMap<>();
                        dataSnapshot.getChildren().forEach(dst -> {
                            Bug bug = Objects.requireNonNull(dst.getValue(Bug.class));
                            if (bugs.containsKey(bug.Greenhouse)) {
                                bugs.replace(bug.Greenhouse, Objects.requireNonNull(bugs.get(bug.Greenhouse)) + 1);
                            } else {
                                bugs.put(bug.Greenhouse, 1);
                            }
                        });
                        lv_greenhouses.setAdapter(new GreenhousesAdapter(GreenhouseSelectActivity.this, greenhouses, bugs));
                        inTask.set(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}

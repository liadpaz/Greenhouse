package com.liadpaz.greenhouse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

        findViewById(R.id.btn_select_greenhouse).setOnClickListener(v -> {
            if (!inTask.get()) {
                if (greenhouses.size() == 1) {
                    startActivity(new Intent(GreenhouseSelectActivity.this, MainActivity.class).putExtra("Greenhouse", greenhouses.get(0).Id));
                    // TODO: implement the bug/greenhouse activity
                } else {
                    new GreenhouseSelectDialog(GreenhouseSelectActivity.this, greenhouses).show();
                }
            } else {
                Toast.makeText(GreenhouseSelectActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            }
        });

        lv_greenhouses.setAdapter(new GreenhousesAdapter(GreenhouseSelectActivity.this, R.layout.layout_greenhouse_item, new ArrayList<>()));

        Utilities.setReferences(farm);
        Utilities.getGreenhousesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inTask.set(true);
                greenhouses.clear();
                ((GreenhousesAdapter) lv_greenhouses.getAdapter()).clear();
                for (DataSnapshot greenhouse : dataSnapshot.getChildren()) {
                    greenhouses.add(greenhouse.getValue(Greenhouse.class));
                    ((GreenhousesAdapter) lv_greenhouses.getAdapter()).add(greenhouse.getValue(Greenhouse.class));
                }
                inTask.set(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}

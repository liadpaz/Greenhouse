package com.liadpaz.greenhouse;

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

public class GreenhouseSelectorActivity extends AppCompatActivity {

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

                // TODO: imlement select greenhouse

            } else {
                Toast.makeText(GreenhouseSelectorActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            }
        });

        lv_greenhouses.setAdapter(new GreenhousesAdapter(GreenhouseSelectorActivity.this, R.layout.layout_greenhouse_item, new ArrayList<>()));

        Utilities.setReferences(farm);

        Utilities.getGreenhousesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inTask.set(true);
                ((GreenhousesAdapter) lv_greenhouses.getAdapter()).clear();
                for (DataSnapshot greenhouse : dataSnapshot.getChildren()) {
                    ((GreenhousesAdapter) lv_greenhouses.getAdapter()).add(Objects.requireNonNull(greenhouse.getValue(Greenhouse.class)));
                }
                inTask.set(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}

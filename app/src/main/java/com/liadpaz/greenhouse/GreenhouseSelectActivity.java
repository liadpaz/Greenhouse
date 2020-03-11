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
import com.liadpaz.greenhouse.databinding.ActivityGreenhouseSelectorBinding;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GreenhouseSelectActivity extends AppCompatActivity {

    private volatile AtomicBoolean inTask = new AtomicBoolean(true);

    private ListView lv_greenhouses;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGreenhouseSelectorBinding binding = ActivityGreenhouseSelectorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String farm = getIntent().getStringExtra("Farm");

        lv_greenhouses = binding.lvGreenhouses;

        lv_greenhouses.setAdapter(new GreenhousesAdapter(GreenhouseSelectActivity.this));
        lv_greenhouses.setOnItemClickListener((parent, view, position, id) -> {
            if (inTask.get()) {
                Toast.makeText(GreenhouseSelectActivity.this, R.string.cant_do_this_now, Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(GreenhouseSelectActivity.this, GreenhouseActivity.class).putExtra("Greenhouse", (Greenhouse)parent.getItemAtPosition(position)));
            }
        });

        Utilities.setCurrentFarm(farm);
        Utilities.getGreenhousesRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inTask.set(true);
                final long count = dataSnapshot.getChildrenCount();
                AtomicInteger current = new AtomicInteger(0);
                for (DataSnapshot greenhouse : dataSnapshot.getChildren()) {
                    Utilities.getBugsRef().child(greenhouse.getValue(Greenhouse.class).Id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
    }
}

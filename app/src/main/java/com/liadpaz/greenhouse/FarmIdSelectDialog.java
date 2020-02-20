package com.liadpaz.greenhouse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

class FarmIdSelectDialog extends Dialog {

    FarmIdSelectDialog(Activity activity) {
        super(activity);
        setContentView(R.layout.dialog_farm_id);

        EditText et_farm_id = findViewById(R.id.et_farm_id);

        findViewById(R.id.btn_farm_id_select).setOnClickListener((v) -> FirebaseDatabase.getInstance().getReference("Farms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot farm : dataSnapshot.getChildren()) {
                    if (Objects.equals(et_farm_id.getText().toString(), Objects.requireNonNull(farm.getKey()).substring(0, 5))) {
                        activity.startActivity(new Intent(activity, GreenhouseSelectActivity.class).putExtra("Farm", farm.getKey()));
                        dismiss();
                        return;
                    }
                }
                Toast.makeText(activity, R.string.farm_id_not_found , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        }));

    }
}

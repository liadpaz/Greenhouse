package com.liadpaz.greenhouse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.HashMap;

class FarmSelectDialog extends Dialog {

    FarmSelectDialog(Activity activity, HashMap<String, String> farms) {
        super(activity);
        setContentView(R.layout.dialog_farm_selector);

        ((Spinner) findViewById(R.id.spinner_farms)).setAdapter(new ArrayAdapter<>(activity, R.layout.support_simple_spinner_dropdown_item, farms.keySet().toArray()));
        ((Spinner) findViewById(R.id.spinner_farms)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressWarnings("RedundantCast")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    activity.startActivity(new Intent(activity, GreenhouseSelectorActivity.class).putExtra("Farm", farms.get((String) parent.getAdapter().getItem(position))));
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}

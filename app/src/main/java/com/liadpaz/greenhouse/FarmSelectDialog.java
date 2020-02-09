package com.liadpaz.greenhouse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.AbstractMap;
import java.util.ArrayList;

class FarmSelectDialog extends Dialog {

    FarmSelectDialog(Activity activity, ArrayList<AbstractMap.SimpleEntry<String, String>> farms) {
        super(activity);
        setContentView(R.layout.dialog_farm_selector);

        ArrayList<String> strings = new ArrayList<>();
        farms.forEach(stringStringSimpleEntry -> strings.add(stringStringSimpleEntry.getKey()));

        ((Spinner) findViewById(R.id.spinner_farms)).setAdapter(new ArrayAdapter<>(activity, R.layout.support_simple_spinner_dropdown_item, strings));
        ((Spinner) findViewById(R.id.spinner_farms)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    activity.startActivity(new Intent(activity, GreenhouseSelectorActivity.class).putExtra("Farm", farms.get(position).getValue()));
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}

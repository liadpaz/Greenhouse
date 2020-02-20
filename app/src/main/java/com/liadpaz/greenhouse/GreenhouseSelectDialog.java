package com.liadpaz.greenhouse;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

class GreenhouseSelectDialog extends Dialog {

    GreenhouseSelectDialog(Context context, ArrayList<Greenhouse> greenhouses) {
        super(context);
        setContentView(R.layout.dialog_greenhouse_select);

        ArrayList<String> ids = new ArrayList<>();
        ids.add(context.getString(R.string.no_greenhouse));
        for (Greenhouse greenhouse :
                greenhouses) {
            ids.add(greenhouse.Id);
        }

        ((Spinner) findViewById(R.id.spinner_greenhouses)).setAdapter(new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, ids));
        ((Spinner) findViewById(R.id.spinner_greenhouses)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
//                    context.startActivity(new Intent(context, MainActivity.class).putExtra("Greenhouse", ids.get(position)));
                    // TODO: implement the bug/greenhouse activity
                    dismiss();
                    Toast.makeText(context, ids.get(position), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}

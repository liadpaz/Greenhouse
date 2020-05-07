package com.liadpaz.greenhouse.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.liadpaz.greenhouse.R;
import com.liadpaz.greenhouse.activities.GreenhouseSelectActivity;
import com.liadpaz.greenhouse.databinding.DialogFarmSelectBinding;
import com.liadpaz.greenhouse.utils.Constants;

import java.util.HashMap;

public class FarmSelectDialog extends Dialog {

    private static final String TAG = "FARM_SELECT_DIALOG";

    public FarmSelectDialog(@NonNull Activity activity, @NonNull HashMap<String, String> farms) {
        super(activity);
        DialogFarmSelectBinding binding = DialogFarmSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.spinnerFarms.setAdapter(new ArrayAdapter<>(activity, R.layout.support_simple_spinner_dropdown_item, farms.keySet().toArray()));
        binding.spinnerFarms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    activity.startActivity(new Intent(activity, GreenhouseSelectActivity.class).putExtra(Constants.GreenhouseSelectExtra.FARM, farms.get(parent.getAdapter().getItem(position).toString())));
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}

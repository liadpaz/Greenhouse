package com.liadpaz.greenhouse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.liadpaz.greenhouse.databinding.DialogFarmIdBinding;

class FarmIdSelectDialog extends Dialog {

    FarmIdSelectDialog(Activity activity) {
        super(activity);
        DialogFarmIdBinding binding = DialogFarmIdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText et_farm_id = binding.etFarmId;

        binding.btnFarmIdSelect.setOnClickListener(v -> FirebaseFirestore.getInstance().collection(Constants.FirebaseConstants.FARMS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot farm : task.getResult()) {
                    if (farm.getId().substring(0, 5).equals(et_farm_id.getText().toString())) {
                        activity.startActivity(new Intent(activity, GreenhouseSelectActivity.class).putExtra(Constants.GreenhouseSelectExtra.FARM, farm.getId()));
                        dismiss();
                        return;
                    }
                }
            }
            Toast.makeText(activity, R.string.farm_id_not_found, Toast.LENGTH_LONG).show();
        }));
    }
}

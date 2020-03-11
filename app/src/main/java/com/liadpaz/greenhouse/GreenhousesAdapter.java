package com.liadpaz.greenhouse;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.greenhouse.databinding.LayoutGreenhouseItemBinding;

import java.util.ArrayList;
import java.util.HashMap;

class GreenhousesAdapter extends BaseAdapter {

    private static final String TAG = "GREENHOUSE_ADAPTER";
    private Activity activity;
    private ArrayList<Greenhouse> greenhouses;
    private HashMap<String, Integer> bugs;

    GreenhousesAdapter(@NonNull Activity activity) {
        super();

        this.activity = activity;
        this.greenhouses = new ArrayList<>();
        this.bugs = new HashMap<>();
    }

    @Override
    public int getCount() {
        return greenhouses.size();
    }

    @Override
    public Object getItem(int position) {
        return greenhouses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutGreenhouseItemBinding binding;
        if (convertView == null) {
            binding = LayoutGreenhouseItemBinding.inflate(activity.getLayoutInflater());
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (LayoutGreenhouseItemBinding)convertView.getTag();
        }

        Greenhouse greenhouse = greenhouses.get(position);
        Log.d(TAG, String.format("getView: %d\n%d\n%s", greenhouse.Height, greenhouse.Width, greenhouse.Id));

        binding.tvGreenhouseId.setText(greenhouse.Id);
        binding.tvGreenhouseWidth.setText(String.valueOf(greenhouse.Width));
        binding.tvGreenhouseHeight.setText(String.valueOf(greenhouse.Height));
        Integer numBugs = bugs.get(greenhouses.get(position).Id);
        binding.tvGreenhouseBugs.setText(numBugs != null ? numBugs.toString() : "0");

        return convertView;
    }

    void addItem(Greenhouse greenhouse, int bugs) {
        greenhouses.add(greenhouse);
        this.bugs.put(greenhouse.Id, bugs);
        notifyDataSetChanged();
    }

    Greenhouse getAtPosition(int position) {
        return greenhouses.get(position);
    }
}

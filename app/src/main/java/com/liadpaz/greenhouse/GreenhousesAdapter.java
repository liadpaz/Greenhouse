package com.liadpaz.greenhouse;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.greenhouse.databinding.LayoutGreenhouseItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class GreenhousesAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
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
        return getItem(position).hashCode();
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

        binding.tvGreenhouseId.setText(greenhouse.getId());
        binding.tvGreenhouseWidth.setText(String.valueOf(greenhouse.getWidth()));
        binding.tvGreenhouseHeight.setText(String.valueOf(greenhouse.getHeight()));
        Integer numBugs = bugs.get(greenhouses.get(position).getId());
        binding.tvGreenhouseBugs.setText(numBugs != null ? numBugs.toString() : "0");

        return convertView;
    }

    void addItem(Greenhouse greenhouse, int bugs) {
        greenhouses.add(greenhouse);
        this.bugs.put(greenhouse.getId(), bugs);
        Collections.sort(greenhouses, (greenhouse1, greenhouse2) -> greenhouse1.getId().compareTo(greenhouse2.getId()));
        notifyDataSetChanged();
    }

    void clear() {
        try {
            bugs.clear();
            greenhouses.clear();
        } catch (Exception ignored) {
        } finally {
            notifyDataSetChanged();
        }
    }

    void updateGreenhouseBugs(String greenhouse, int count) {
        try {
            this.bugs.put(greenhouse, count);
            notifyDataSetChanged();
        } catch (Exception ignored) {
        }
    }
}

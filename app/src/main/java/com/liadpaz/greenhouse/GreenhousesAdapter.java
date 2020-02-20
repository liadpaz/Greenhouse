package com.liadpaz.greenhouse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

class GreenhousesAdapter extends ArrayAdapter<Greenhouse> {

    private Activity activity;
    private ArrayList<Greenhouse> greenhouses;

    GreenhousesAdapter(@NonNull Activity activity, int resource, @NonNull ArrayList<Greenhouse> greenhouses) {
        super(activity, resource, greenhouses);

        this.activity = activity;
        this.greenhouses = greenhouses;
    }

    @NonNull
    @Override
    @SuppressLint({"InflateParams", "ViewHolder"})
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_greenhouse_item, null);

        ((TextView) rowView.findViewById(R.id.tv_greenhouse_id)).setText(greenhouses.get(position).Id);
        ((TextView) rowView.findViewById(R.id.tv_greenhouse_width)).setText(greenhouses.get(position).Width);
        ((TextView) rowView.findViewById(R.id.tv_greenhouse_height)).setText(greenhouses.get(position).Height);

        return rowView;
    }
}

package com.liadpaz.greenhouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

class GreenhousesAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Greenhouse> greenhouses;
    private HashMap<String, Integer> bugs;

    GreenhousesAdapter(@NonNull Activity activity, @NonNull ArrayList<Greenhouse> greenhouses, @NonNull HashMap<String, Integer> bugs) {
        super();
        //(activity, R.layout.layout_greenhouse_item, greenhouses);

        this.activity = activity;
        this.greenhouses = greenhouses;
        this.bugs = bugs;
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
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.layout_greenhouse_item, parent, false);
            viewHolder = new ViewHolder(activity, convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Greenhouse greenhouse = greenhouses.get(position);

        viewHolder.tv_greenhouse_id.setText(greenhouse.Id);
        viewHolder.tv_greenhouse_width.setText(greenhouse.Width);
        viewHolder.tv_greenhouse_height.setText(greenhouse.Height);
        Integer numBugs = bugs.get(greenhouses.get(position).Id);
        viewHolder.tv_greenhouse_bugs.setText(numBugs != null ? numBugs.toString() : "0");

        convertView.setClickable(true);
        convertView.setOnClickListener(v -> activity.startActivity(new Intent(activity, GreenhouseActivity.class).putExtra("Greenhouse", greenhouse)));

        return convertView;
    }

    private class ViewHolder {

        TextView tv_greenhouse_id;
        TextView tv_greenhouse_width;
        TextView tv_greenhouse_height;
        TextView tv_greenhouse_bugs;

        ViewHolder(Context context, View view) {
            tv_greenhouse_id = view.findViewById(R.id.tv_greenhouse_id);
            tv_greenhouse_width = view.findViewById(R.id.tv_greenhouse_width);
            tv_greenhouse_height = view.findViewById(R.id.tv_greenhouse_height);
            tv_greenhouse_bugs = view.findViewById(R.id.tv_greenhouse_bugs);
        }
    }
}

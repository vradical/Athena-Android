package com.teamvh.orbital.athena;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EmergencyAdapter extends ArrayAdapter<EmergencyData> {

    ArrayList<EmergencyData> emergencyList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public EmergencyAdapter(Context context, int resource, ArrayList<EmergencyData> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        emergencyList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.tvEMID = (TextView) v.findViewById(R.id.tvEMID);
            holder.tvStartTime = (TextView) v.findViewById(R.id.tvStartTime);
            holder.tvEndTime = (TextView) v.findViewById(R.id.tvEndTime);
            holder.tvNumOfTrack = (TextView) v.findViewById(R.id.tvNumOfTrack);
            holder.tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            holder.tvCountry = (TextView) v.findViewById(R.id.tvCountry);
            holder.tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvEMID.setText(String.valueOf(position+1));
        //holder.tvEMID.setText(emergencyList.get(position).getEmID());
        holder.tvStartTime.setText(emergencyList.get(position).getStartTime());
        holder.tvEndTime.setText(emergencyList.get(position).getEndTime());
        holder.tvNumOfTrack.setText(emergencyList.get(position).getNumOfTrack());
        holder.tvAddress.setText(emergencyList.get(position).getAddress());
        holder.tvCountry.setText(emergencyList.get(position).getCountry());
        holder.tvStatus.setText(emergencyList.get(position).getStatus());
        return v;
    }

    static class ViewHolder {
        public TextView tvEMID;
        public TextView tvStartTime;
        public TextView tvEndTime;
        public TextView tvNumOfTrack;
        public TextView tvAddress;
        public TextView tvCountry;
        public TextView tvStatus;
    }

}

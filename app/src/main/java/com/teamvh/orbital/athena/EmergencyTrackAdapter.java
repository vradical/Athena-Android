package com.teamvh.orbital.athena;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EmergencyTrackAdapter extends ArrayAdapter<EmergencyTrackData> {

    ArrayList<EmergencyTrackData> emergencyList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    CountryList cl;

    public EmergencyTrackAdapter(Context context, int resource, ArrayList<EmergencyTrackData> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        emergencyList = objects;

        cl = new CountryList();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.tvTrackID = (TextView) v.findViewById(R.id.tvTrackID);
            holder.tvTrackAddress = (TextView) v.findViewById(R.id.tvTrackAddress);
            holder.tvTrackLatLng = (TextView) v.findViewById(R.id.tvLatLng);
            holder.tvTrackDate = (TextView) v.findViewById(R.id.tvDate);
            holder.tvTrackCountry = (TextView) v.findViewById(R.id.tvTrackCountry);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvTrackID.setText(String.valueOf(position + 1));
        if(emergencyList.get(position).getLocality().equals("Not Available")){
            holder.tvTrackCountry.setText(cl.findNameByCode(emergencyList.get(position).getCountry()));
        }else{
            holder.tvTrackCountry.setText(emergencyList.get(position).getLocality()+ ", " + cl.findNameByCode(emergencyList.get(position).getCountry()));
        }
        holder.tvTrackLatLng.setText(emergencyList.get(position).getLatitude() + ", " + emergencyList.get(position).getLongitude());
        holder.tvTrackDate.setText(emergencyList.get(position).getDateTime());
        holder.tvTrackAddress.setText(emergencyList.get(position).getAddress());
        return v;
    }

    static class ViewHolder {
        public TextView tvTrackAddress;
        public TextView tvTrackLatLng;
        public TextView tvTrackDate;
        public TextView tvTrackID;
        public TextView tvTrackCountry;
    }
}

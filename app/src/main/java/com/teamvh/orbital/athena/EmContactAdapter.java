package com.teamvh.orbital.athena;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ron on 09-Jul-15.
 */
public class EmContactAdapter extends ArrayAdapter<ContactData>{

    ArrayList<ContactData> contactList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public EmContactAdapter(Context context, int resource, ArrayList<ContactData> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        contactList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        final View vv = v;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.tvName = (TextView) v.findViewById(R.id.tvName);
            holder.tvEmail = (TextView) v.findViewById(R.id.tvEmail);
            holder.tvCountry = (TextView) v.findViewById(R.id.tvCountry);
            holder.tvPhone = (TextView) v.findViewById(R.id.tvPhone);
            holder.tvContactStatus = (TextView) v.findViewById(R.id.tvContactStatus);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvName.setText(contactList.get(position).getName());
        holder.tvEmail.setText(contactList.get(position).getEmail());
        holder.tvCountry.setText(contactList.get(position).getCountry());
        holder.tvPhone.setText(contactList.get(position).getPhone());
        holder.tvContactStatus.setText(contactList.get(position).getEmStatus());
        return v;
    }

    static class ViewHolder {
        public TextView tvName;
        public TextView tvEmail;
        public TextView tvCountry;
        public TextView tvPhone;
        public TextView tvContactStatus;
    }

}

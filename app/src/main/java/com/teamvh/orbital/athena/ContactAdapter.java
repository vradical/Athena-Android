package com.teamvh.orbital.athena;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ron on 09-Jul-15.
 */
public class ContactAdapter extends ArrayAdapter<ContactData>{

    ArrayList<ContactData> contactList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    public ContactAdapter(Context context, int resource, ArrayList<ContactData> objects) {
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
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvName.setText(contactList.get(position).getName());
        holder.tvEmail.setText(contactList.get(position).getEmail());
        holder.tvCountry.setText(contactList.get(position).getCountry());
        holder.tvPhone.setText(contactList.get(position).getPhone());
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event)
            {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE && x2 < x1)
                        {
                            Toast.makeText(vv.getContext(), "left2right swipe", Toast.LENGTH_SHORT).show ();
                        }
                        else
                        {
                            // consider as something else - a screen tap for example
                        }
                        break;
                }
                return true;
            }
        });
        return v;

    }


    static class ViewHolder {
        public TextView tvName;
        public TextView tvEmail;
        public TextView tvCountry;
        public TextView tvPhone;

    }

}

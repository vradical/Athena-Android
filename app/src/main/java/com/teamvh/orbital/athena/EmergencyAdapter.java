package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EmergencyAdapter extends ArrayAdapter<EmergencyData> {

    ArrayList<EmergencyData> emergencyList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    CountryList cl;
    SharedPreferences preferences;

    public EmergencyAdapter(Context context, int resource, ArrayList<EmergencyData> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        emergencyList = objects;
        cl = new CountryList();
        preferences = MainActivity.preferences;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.tvEMID = (TextView) v.findViewById(R.id.tvEMID);
            holder.tvTime = (TextView) v.findViewById(R.id.tvStartTime);
            holder.tvDate = (TextView) v.findViewById(R.id.tvStartDate);
            holder.tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            holder.tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            holder.IVCountry = (ImageView) v.findViewById(R.id.eml_country);
            holder.IBChangeStat = (ImageButton) v.findViewById(R.id.btnChangeStat);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvEMID.setText(String.valueOf(position + 1) + ". ");

        if(emergencyList.get(position).getEndTime().equals("Not Available")){
            holder.tvTime.setText(parseDateToTime(emergencyList.get(position).getStartTime()) + " to N/A");
        }else {
            holder.tvTime.setText(parseDateToTime(emergencyList.get(position).getStartTime()) + " to " + parseDateToTime(emergencyList.get(position).getEndTime()));
        }

        if(emergencyList.get(position).getEndTime().equals("Not Available")){
            holder.tvDate.setText(parseDateToOnlyDate(emergencyList.get(position).getStartTime()) + " to N/A");
        }else{
            String startDate = parseDateToOnlyDate(emergencyList.get(position).getStartTime());
            String endDate = parseDateToOnlyDate(emergencyList.get(position).getEndTime());
            if(!startDate.equals(endDate)) {
                holder.tvDate.setText((startDate) + " to " + parseDateToOnlyDate(endDate));
            }else{
                holder.tvDate.setText(startDate);
            }
        }

        if (emergencyList.get(position).getAddress().equals("Not Available")){
            LatLng lg = emergencyList.get(position).getLatlng();
            holder.tvAddress.setText(String.valueOf(lg.latitude) + ", " + String.valueOf(lg.longitude));
        }else {
            holder.tvAddress.setText(emergencyList.get(position).getAddress());
        }

        if(emergencyList.get(position).getCountry().equals("Not Available")){
            holder.IVCountry.setImageResource(R.drawable.unknown);
        }else {
            String pngName = emergencyList.get(position).getCountry().trim().toLowerCase();
            holder.tvStatus.setText(emergencyList.get(position).getStatus());
            holder.IVCountry.setImageResource(getContext().getResources().getIdentifier("drawable/" + pngName, null, getContext().getPackageName()));
        }

        holder.IBChangeStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Emergency Status");
                builder.setMessage("Please set the correct status for this activation. A correct status will help to improve the accuracy of danger zone.");

                builder.setPositiveButton("Emergency", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        changeStatus(position, "Emergency");
                    }
                });

                builder.setNegativeButton("False Alarm", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        changeStatus(position, "False Alarm");
                    }
                });
                builder.show();
            }
        });

        return v;
    }

    static class ViewHolder {
        public TextView tvEMID;
        public TextView tvTime;
        public TextView tvDate;
        public TextView tvAddress;
        public TextView tvStatus;
        public ImageView IVCountry;
        public ImageButton IBChangeStat;
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void changeStatus(final int position, final String status) {

        String uname = preferences.getString("fbsession", null);

        RequestParams params = new RequestParams();

        params.put("username", uname);
        params.put("track_em_id", emergencyList.get(position).getEmID());
        params.put("emStatus", status);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/changestatus", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        Toast.makeText(getContext(), "Status Change Successful", Toast.LENGTH_LONG).show();
                        emergencyList.get(position).setStatus(status);
                        MainActivity.country = "restart";
                    } else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getContext(), "Error Occured in Changing Status", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getContext(), "Change Status : Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getContext(), "Change Status : Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getContext(), "Change Status : Unexpected Error occcured!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {
                notifyDataSetChanged();
            }
        });
    }

    public String parseDateToOnlyDate(String time) {
        String inputPattern = "dd-MMM-yyyy h:mm a";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String parseDateToTime(String time) {
        String inputPattern = "dd-MMM-yyyy h:mm a";
        String outputPattern = "h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
}

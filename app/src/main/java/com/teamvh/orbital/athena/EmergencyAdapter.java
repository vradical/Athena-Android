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
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

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

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Please set the correct status for this activation. A correct status will help to improve the accuracy of danger zone.")
                        .positiveAction("EMERGENCY")
                        .negativeAction("FALSE ALARM")
                        .title("Emergency Status");
                final Dialog dialog = builder.build(getContext());
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!emergencyList.get(position).getStatus().equals("Emergency")) {
                            changeStatus(position, "Emergency");
                        }
                        dialog.cancel();
                    }
                });
                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!emergencyList.get(position).getStatus().equals("False Alarm")) {
                            changeStatus(position, "False Alarm");
                        }
                        dialog.cancel();
                    }
                });

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
                        emergencyList.get(position).setStatus(status);
                        MainActivity.country = "restart";

                        Dialog.Builder builder = null;
                        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                        ((SimpleDialog.Builder) builder).message("Emergency status change successfully.")
                                .positiveAction("OK")
                                .title("Status Change");
                        final Dialog dialog = builder.build(getContext());
                        dialog.show();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.positiveActionClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });


                    } else {
                        displayDialog("", 1);
                    }
                } catch (JSONException e) {
                    displayDialog("", 1);
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                displayDialog("", 2);
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

    public void displayDialog(String message, int i) {

        if(i == 1){
            message = "Unable to get information from server.";
        }else if(i == 2){
            message = "Unable to connect to server.";
        }

        Dialog.Builder builder = null;
        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
        ((SimpleDialog.Builder) builder).message(message)
                .positiveAction("OK")
                .title("Error");
        final Dialog dialog = builder.build(getContext());
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }
}

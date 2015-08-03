package com.teamvh.orbital.athena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EmergencyHistory extends AppCompatActivity {

    protected SharedPreferences preferences;
    protected ArrayList<EmergencyData> emergencyList;
    protected ListView listView;
    protected EmergencyAdapter adapter;
    protected TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_history);

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Emergency History");

        preferences = MainActivity.preferences;
        emergencyList = new ArrayList<EmergencyData>();

        getEmergency();
        listView = (ListView) findViewById(R.id.emergency_list);
        adapter = new EmergencyAdapter(this, R.layout.activity_emergency_row, emergencyList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(EmergencyHistory.this, EmergencyTrackHistory.class);
                i.putExtra("emID", emergencyList.get(position).getEmID());
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    //PREPARE QUERY TO GET CONTACT LIST
    public void getEmergency() {

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if (uname != null) {
            params.put("username", uname);

            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }
        // when any of the field is empty from token
        else {
            Toast.makeText(getApplicationContext(), "Failed to retrieve contacts", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/getemergency", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (!response.equals(null)) {

                        try {
                            JSONObject object = obj.getJSONObject("emergencyData");

                            EmergencyData emergency = new EmergencyData();

                            if(object.getString("endTime").equals("Not Available")){
                                emergency.setEndTime("Not Available");
                            }else {
                                emergency.setEndTime(parseDateToddMMyyyy(object.getString("endTime")));
                            }
                            emergency.setNumOfTrack(object.getString("numOfTrack"));
                            emergency.setStartTime(parseDateToddMMyyyy(object.getString("startTime")));
                            emergency.setEmID(String.valueOf(object.getInt("emID")));
                            emergency.setAddress(object.getString("address"));
                            emergency.setCountry(object.getString("country"));
                            emergency.setStatus(object.getString("status"));
                            emergency.setLatlng(new LatLng(Double.parseDouble(object.getString("latitude")), Double.parseDouble(object.getString("longitude"))));
                            emergency.setLocality(object.getString("locality"));

                            emergencyList.add(emergency);

                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("emergencyData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                EmergencyData emergency = new EmergencyData();

                                if(object.getString("endTime").equals("Not Available")){
                                    emergency.setEndTime("Not Available");
                                }else {
                                    emergency.setEndTime(parseDateToddMMyyyy(object.getString("endTime")));
                                }
                                emergency.setNumOfTrack(object.getString("numOfTrack"));
                                emergency.setStartTime(parseDateToddMMyyyy(object.getString("startTime")));
                                emergency.setEmID(String.valueOf(object.getInt("emID")));
                                emergency.setAddress(object.getString("address"));
                                emergency.setCountry(object.getString("country"));
                                emergency.setStatus(object.getString("status"));
                                emergency.setLatlng(new LatLng(Double.parseDouble(object.getString("latitude")), Double.parseDouble(object.getString("longitude"))));
                                emergency.setLocality(object.getString("locality"));

                                emergencyList.add(emergency);
                            }
                        }

                    } else {
                        //displayDialog("", 1);
                    }
                } catch (JSONException e) {
                    //displayDialog("", 1);
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
                adapter.notifyDataSetChanged();
            }
        });
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy h:mm a";
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
        final Dialog dialog = builder.build(EmergencyHistory.this);
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

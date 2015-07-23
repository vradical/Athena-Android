package com.teamvh.orbital.athena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_history);

        //set up action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

                            emergency.setEndTime(parseDateToddMMyyyy(object.getString("endTime")));
                            emergency.setNumOfTrack(object.getString("numOfTrack"));
                            emergency.setStartTime(parseDateToddMMyyyy(object.getString("startTime")));
                            emergency.setEmID(String.valueOf(object.getInt("emID")));
                            emergency.setAddress(object.getString("address"));
                            emergency.setCountry(object.getString("country"));
                            emergency.setStatus(object.getString("status"));

                            emergencyList.add(emergency);

                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("emergencyData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                EmergencyData emergency = new EmergencyData();

                                emergency.setEndTime(parseDateToddMMyyyy(object.getString("endTime")));
                                emergency.setNumOfTrack(object.getString("numOfTrack"));
                                emergency.setStartTime(parseDateToddMMyyyy(object.getString("startTime")));
                                emergency.setEmID(String.valueOf(object.getInt("emID")));
                                emergency.setAddress(object.getString("address"));
                                emergency.setCountry(object.getString("country"));
                                emergency.setStatus(object.getString("status"));

                                emergencyList.add(emergency);
                            }
                        }

                        Toast.makeText(getApplicationContext(), "Retrieve Successful", Toast.LENGTH_LONG).show();

                    } else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
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


}

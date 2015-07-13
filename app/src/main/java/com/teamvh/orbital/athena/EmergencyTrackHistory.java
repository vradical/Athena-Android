package com.teamvh.orbital.athena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EmergencyTrackHistory extends AppCompatActivity {

    protected SharedPreferences preferences;
    protected ArrayList<EmergencyTrackData> emergencyTrackList;
    protected ListView listView;
    protected EmergencyTrackAdapter adapter;
    protected String emID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_track_history);
        Bundle b = getIntent().getExtras();
        emID = b.getString("emID");

        preferences = MainActivity.preferences;
        emergencyTrackList = new ArrayList<EmergencyTrackData>();

        getTracks();
        listView = (ListView) findViewById(R.id.emergency_track_list);
        adapter = new EmergencyTrackAdapter(this, R.layout.activity_emergency_track_history_row, emergencyTrackList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emergency_track_history, menu);
        return true;
    }

    //Menu settings
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent i =new Intent(this, MainActivity.class);
                startActivity(i);
                break;
            case R.id.action_contacts:
                Intent i1 =new Intent(this, ContactInfo.class);
                startActivity(i1);
                break;
            case R.id.action_helpinfo:
                Intent i2 =new Intent(this, HelpInfo.class);
                startActivity(i2);
                break;
            default:
                break;
        }
        return true;
    }

    //PREPARE QUERY TO GET CONTACT LIST
    public void getTracks() {

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if (uname != null) {
            params.put("username", uname);
            params.put("track_em_id", emID);

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
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/gettrack", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (!response.equals(null)) {

                        try {
                            JSONObject object = obj.getJSONObject("trackData");

                            EmergencyTrackData emergency = new EmergencyTrackData();

                            emergency.setAddress(object.getString("address"));
                            emergency.setDateTime(object.getString("dateTime"));
                            emergency.setLatitude(String.valueOf(object.getDouble("latitude")));
                            emergency.setLongitude(String.valueOf(object.getDouble("longitude")));

                            emergencyTrackList.add(emergency);

                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("trackData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                EmergencyTrackData emergency = new EmergencyTrackData();

                                emergency.setAddress(object.getString("address"));
                                emergency.setDateTime(object.getString("dateTime"));
                                emergency.setLatitude(String.valueOf(object.getDouble("latitude")));
                                emergency.setLongitude(String.valueOf(object.getDouble("longitude")));

                                emergencyTrackList.add(emergency);
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
}

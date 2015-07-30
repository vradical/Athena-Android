package com.teamvh.orbital.athena;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class DangerZoneList extends AppCompatActivity {

    protected SharedPreferences preferences;
    protected ArrayList<DangerZoneData> dangerzoneList;
    protected ListView listView;
    protected DangerZoneAdapter adapter;
    protected TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger_zone_list);

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Emergency History");

        preferences = MainActivity.preferences;
        dangerzoneList = new ArrayList<DangerZoneData>();

        getDangerZone();
        listView = (ListView) findViewById(R.id.dangerzone_list);
        adapter = new DangerZoneAdapter(this, R.layout.activity_danger_zone_row, dangerzoneList);
        listView.setAdapter(adapter);
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
    public void getDangerZone() {

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
            Toast.makeText(getApplicationContext(), "Failed to retrieve danger zone", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/dangerzone/getdz", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (!response.equals(null)) {

                        try {
                            JSONObject object = obj.getJSONObject("dangerZoneData");

                            DangerZoneData dz = new DangerZoneData();

                            dz.setDateTime(parseDateToddMMyyyy(object.getString("dateTime")));
                            dz.setAddinfo(object.getString("addinfo"));
                            dz.setDz_id(object.getString("dz_id"));
                            dz.setLatitude(object.getString("latitude"));
                            dz.setLongitude(object.getString("longitude"));
                            dz.setTitle(object.getString("title"));

                            dangerzoneList.add(dz);

                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("dangerZoneData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                DangerZoneData dz = new DangerZoneData();

                                dz.setDateTime(parseDateToddMMyyyy(object.getString("dateTime")));
                                dz.setAddinfo(object.getString("addinfo"));
                                dz.setDz_id(object.getString("dz_id"));
                                dz.setLatitude(object.getString("latitude"));
                                dz.setLongitude(object.getString("longitude"));
                                dz.setTitle(object.getString("title"));

                                dangerzoneList.add(dz);
                            }
                        }

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
        final Dialog dialog = builder.build(DangerZoneList.this);
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

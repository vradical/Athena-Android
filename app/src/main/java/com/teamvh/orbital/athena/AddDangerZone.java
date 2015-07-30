package com.teamvh.orbital.athena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddDangerZone extends AppCompatActivity {

    private SharedPreferences preferences;
    protected EditText titleField;
    protected EditText addField;
    protected TextView mTitleText;
    protected TextView mTitleError;
    protected TextView mInfoError;
    protected GoogleMap mGoogleMap;

    protected double latitude;
    protected double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dangerzone);

        //Set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Add Danger Zone");

        preferences = MainActivity.preferences;
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");

        titleField = (EditText) findViewById(R.id.add_emTitle);
        addField = (EditText) findViewById(R.id.add_info);
        mTitleError = (TextView) findViewById(R.id.emTitle_helper);
        mInfoError = (TextView) findViewById(R.id.emInfo_helper);

        setupMap();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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

    public void setupMap() {
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.add_danger_map);
        mGoogleMap = fragment.getMap();
        LatLng ll = new LatLng(latitude, longitude);
        mGoogleMap.addMarker(new MarkerOptions().position(ll));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
    }

    public void completeDangerZone(View view) {
        String title = "";
        String info = "";

        if (isEmpty(titleField)) {
            mTitleError.setVisibility(View.VISIBLE);
        } else {
            mTitleError.setVisibility(View.INVISIBLE);
            title = titleField.getText().toString();
        }

        if(isEmpty(addField)){
            mInfoError.setVisibility(View.VISIBLE);
        }else{
            mInfoError.setVisibility(View.INVISIBLE);
            info = addField.getText().toString();
        }

        //CHECK FOR DEFAULT TYPE
        if (!isEmpty(titleField) && !isEmpty(addField)) {
            addDanger(title, info, latitude, longitude);
        }
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /*
    public void addItemsToSpinner() {
        // Custom ArrayAdapter with spinner item layout to set popup background

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Emergency_Type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeField.setAdapter(adapter);

        typeField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {

                type = adapter.getItemAtPosition(position).toString();
                // Showing selected spinner item
                Toast.makeText(getApplicationContext(), "Selected  : " + type,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }*/


    //PREPARE QUERY TO LOGIN USER
    public void addDanger(String title, String info, double latitude, double longitude) {

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        //
        if (uname != null) {
            params.put("username", uname);
            params.put("title", title);
            params.put("addinfo", info);
            params.put("latitude", String.valueOf(latitude));
            params.put("longitude", String.valueOf(longitude));
            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }
        // when any of the field is empty from token
        else {
            Toast.makeText(getApplicationContext(), "Failed to record contacts", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/dangerzone/adddz", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        MainActivity.country = "restart";
                        finish();
                    }
                    // Else display error message
                    else {
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

        });

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
        final Dialog dialog = builder.build(AddDangerZone.this);
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

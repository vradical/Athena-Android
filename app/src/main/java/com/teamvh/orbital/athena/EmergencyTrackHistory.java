package com.teamvh.orbital.athena;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class EmergencyTrackHistory extends AppCompatActivity {

    protected SharedPreferences preferences;
    protected ArrayList<EmergencyTrackData> emergencyTrackList;
    protected ListView listView;
    protected EmergencyTrackAdapter adapter;
    protected String emID;
    protected TextView mTitleText;
    protected GoogleMap mGoogleMap;
    protected ArrayList<Marker> markerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_track_history);

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Track History");

        Bundle b = getIntent().getExtras();
        emID = b.getString("emID");

        preferences = MainActivity.preferences;
        emergencyTrackList = new ArrayList<EmergencyTrackData>();

        getTracks();
        listView = (ListView) findViewById(R.id.emergency_track_list);
        adapter = new EmergencyTrackAdapter(this, R.layout.activity_emergency_track_history_row, emergencyTrackList);
        listView.setAdapter(adapter);

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.em_track_map);
        // Getting Google Map
        mGoogleMap = fragment.getMap();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LatLng ll = new LatLng(Double.parseDouble(emergencyTrackList.get(i).getLatitude()), Double.parseDouble(emergencyTrackList.get(i).getLongitude()));
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 18));
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

    public void populateMap() {
        mGoogleMap.clear();
        Bitmap bm;
        bm = drawableToBitmap(getResources().getDrawable(R.drawable.location_icon));
        markerList = new ArrayList<Marker>();

        if (emergencyTrackList.size() == 1) {
            LatLng ll = new LatLng(Double.parseDouble(emergencyTrackList.get(0).getLatitude()), Double.parseDouble(emergencyTrackList.get(0).getLongitude()));
            mGoogleMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth() / 2, bm.getHeight() / 2, false))));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 18));

        } else {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < emergencyTrackList.size(); i++) {
                LatLng ll = new LatLng(Double.parseDouble(emergencyTrackList.get(i).getLatitude()), Double.parseDouble(emergencyTrackList.get(i).getLongitude()));
                mGoogleMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth() / 2, bm.getHeight() / 2, false))));
                builder.include(ll);
            }

            LatLngBounds bounds = builder.build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
        }
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void getTracks() {

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        params.put("username", uname);
        params.put("track_em_id", emID);

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

                            EmergencyTrackData emergency = new EmergencyTrackData(object.getString("address"),
                                    parseDateToddMMyyyy(object.getString("dateTime")), String.valueOf(object.getDouble("latitude")),
                                    String.valueOf(object.getDouble("longitude")), object.getString("country"), object.getString("locality"));

                            emergencyTrackList.add(emergency);

                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("trackData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                EmergencyTrackData emergency = new EmergencyTrackData(object.getString("address"),
                                        parseDateToddMMyyyy(object.getString("dateTime")), String.valueOf(object.getDouble("latitude")),
                                        String.valueOf(object.getDouble("longitude")), object.getString("country"), object.getString("locality"));

                                emergencyTrackList.add(emergency);
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
                if (emergencyTrackList.size() > 0) {
                    populateMap();
                }
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
        final Dialog dialog = builder.build(EmergencyTrackHistory.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    //Supporting methods
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

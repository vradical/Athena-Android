package com.teamvh.orbital.athena;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ron on 27-Jun-15.
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationService";
    private Context mContext;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private AccessToken accessToken;
    private String trackType;
    private String emID;
    private double longitude;
    private double latitude;
    private String address;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Geocoder geocoder;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        geocoder = new Geocoder(this, Locale.getDefault());
        Bundle b = intent.getExtras();
        accessToken = (AccessToken) b.get("fb_token");
        trackType = b.getString("track_type");
        emID = String.valueOf(b.getInt("track_em_id"));
        getLocation();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        mContext = this;
        preferences = MainActivity.preferences;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        try {
            if (googleApiClient != null) {
                googleApiClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getLocation() {
        locationRequest = LocationRequest.create();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (trackType.equals("Standard")) {
            locationRequest.setInterval(Constants.CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.CHECK_FAST_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT);
        } else if (trackType.equals("High Alert")) {
            locationRequest.setInterval(Constants.HA_CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.HA_CHECK_FAST_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.HA_SMALLEST_DISPLACEMENT);
        } else if (trackType.equals("Emergency")) {
            locationRequest.setInterval(Constants.EM_CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.EM_CHECK_FAST_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.EM_SMALLEST_DISPLACEMENT);
        }
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
//  Location location = fusedLocationProviderApi.getLastLocation(googleApiClient);
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int arg0) {

    }


    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(mContext, "Driver location :" + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        recordLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    //PREPARE QUERY TO LOGIN USER
    public void recordLocation() {

        address = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(" ");
                }
                sb.append(address.getPostalCode()).append(" ");
                sb.append(address.getCountryName());
                this.address = sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        } finally {
            if (address == null) {
                address = "Unable to get address for this lat-long.";
            }
            String uname = accessToken.getUserId();
            java.util.Date date = new java.util.Date();

            editor = preferences.edit();
            editor.putString("Longitude", String.valueOf(longitude));
            editor.putString("Latitude", String.valueOf(latitude));
            editor.putString("Timestamp", String.valueOf(new Timestamp(date.getTime())));
            editor.putString("Address", address);
            editor.commit();
            editor.apply();

            // Instantiate Http Request Param Object
            RequestParams params = new RequestParams();

            if (uname != null) {
                params.put("username", uname);
                params.put("longitude", String.valueOf(longitude));
                params.put("latitude", String.valueOf(latitude));
                params.put("address", address);
                params.put("track_type", trackType);
                params.put("track_em_id", emID);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }
            // when any of the field is empty from token
            else {
                Toast.makeText(getApplicationContext(), "Failed to record current coordinates", Toast.LENGTH_LONG).show();
            }

        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress = null;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
            }
            address = locationAddress;
        }
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/location/dorecord", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        Toast.makeText(getApplicationContext(), "Record Successful", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        //navigatetoHomeActivity();
                    }
                    // Else display error message
                    else {
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
        });

    }
    
}
package com.teamvh.orbital.athena;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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

import java.util.List;

/**
 * Created by Ron on 27-Jun-15.
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationService";
    private Context mContext;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private AccessToken accessToken;
    private String address;
    private String trackType;

    Geocoder geocoder;
    List<Address> addresses;

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        Bundle b = intent.getExtras();
        accessToken = (AccessToken) b.get("fb_token");
        address = b.getString("address");
        trackType = b.getString("track_type");
        getLocation();
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        Log.e(TAG, "onCreate");
        mContext = this;
        //getLocation();
    }

    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        try{
            if(googleApiClient!=null){
                googleApiClient.disconnect();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private void getLocation(){
        locationRequest = LocationRequest.create();

        if(trackType.equals("Standard")) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(Constants.CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.CHECK_FAST_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT);
        }else if(trackType.equals("High Alert")){
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(Constants.HA_CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.HA_CHECK_FAST_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.HA_SMALLEST_DISPLACEMENT);
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
        Toast.makeText(mContext, "Driver location :"+location.getLatitude()+" , "+location.getLongitude(), Toast.LENGTH_SHORT).show();
        recordLocation(location.getLongitude(), location.getLatitude());

    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    //PREPARE QUERY TO LOGIN USER
    public void recordLocation(double longitude, double latitude){

        String uname = accessToken.getUserId();

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        //
        if(uname != null){
            // Put Http parameter name with value of Name Edit View control
            params.put("username", uname);
            // Put Http parameter username with value of Email Edit View control
            params.put("longitude", String.valueOf(longitude));
            //
            params.put("latitude", String.valueOf(latitude));
            // Put Http parameter username with value of Email Edit View control
            params.put("address", address);
            // Put Http parameter username with value of Email Edit View control
            params.put("track_type", trackType);
            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }
        // when any of the field is empty from token
        else{
            Toast.makeText(getApplicationContext(), "Failed to record current coordinates", Toast.LENGTH_LONG).show();
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
package com.teamvh.orbital.athena;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private String trackType;
    private String emID;
    private double longitude;
    private double latitude;
    private String address;
    private String addressWOcountry;
    private String country;
    private String countryCode;
    private String locality;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ArrayList<EmergencyTrackData> trackData;
    private Location curBest;

    //
    AlarmManager nextPointAlarmManager;
    private Intent alarmIntent;

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
        trackType = preferences.getString("Start Mode", "");
        emID = preferences.getString("emID", "");
        getLocation();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        mContext = this;
        preferences = MainActivity.preferences;
        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    private void CancelAlarm() {
        if (alarmIntent != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(sender);
        }
    }

    private void StopAlarm() {
        Intent i = new Intent(this, LocationService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        nextPointAlarmManager.cancel(pi);
    }

    private void SetAlarmForNextPoint() {
        Intent i = new Intent(this, LocationService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        nextPointAlarmManager.cancel(pi);

        if(preferences.getString("Start Mode", "").equals("Standard")) {
            nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + preferences.getInt("CHECK_INTERVAL", 0) * 1000, pi);
        }else if(preferences.getString("Start Mode", "").equals("High Alert")){
            nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + preferences.getInt("HA_CHECK_INTERVAL", 0) * 1000, pi);
        }else if(preferences.getString("Start Mode", "").equals("Emergency")){
            nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + preferences.getInt("EM_CHECK_INTERVAL", 0) * 1000, pi);
        }
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        stopLocation();
        super.onDestroy();
    }

    private void stopLocation(){
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
            locationRequest.setFastestInterval(Constants.CHECK_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT);
        } else if (trackType.equals("High Alert")) {
            locationRequest.setInterval(Constants.HA_CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.HA_CHECK_INTERVAL);
            locationRequest.setSmallestDisplacement(Constants.HA_SMALLEST_DISPLACEMENT);
        } else if (trackType.equals("Emergency")) {
            locationRequest.setInterval(Constants.EM_CHECK_INTERVAL);
            locationRequest.setFastestInterval(Constants.EM_CHECK_INTERVAL);
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
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        location.getTime();
        recordLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    //PREPARE QUERY TO LOGIN USER
    public void recordLocation() {
        Gson gson = new Gson();
        Type listOfTrack = new TypeToken<ArrayList<EmergencyTrackData>>() {}.getType();

        if (null == trackData) {
            trackData = new ArrayList<EmergencyTrackData>();
        }else{
            trackData = gson.fromJson(preferences.getString("TrackData",""), ArrayList.class);
        }

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
                if(address.getLocality() != null) {
                    sb.append(address.getLocality()).append(" ");
                    locality = address.getLocality();
                }else{
                    locality = "Not Available";
                }
                this.addressWOcountry = sb.toString();
                if(address.getPostalCode() != (null)){
                    sb.append(address.getPostalCode()).append(" ");
                }
                this.address = sb.toString();
                country = address.getCountryName().toString();

                if(address.getCountryCode() != null) {
                    countryCode = address.getCountryCode().toString();
                }else{
                    countryCode = "Not Available";
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        } finally {
            if (address == null) {
                address = "Not Available";
            }
            if(addressWOcountry == null){
                addressWOcountry = "Not Available";
            }

            String uname = preferences.getString("fbsession", "");
            java.util.Date date = new java.util.Date();

            editor = preferences.edit();
            editor.putString("Timestamp", parseDateToddMMyyyy(String.valueOf(new Timestamp(date.getTime()))));
            editor.putString("Address", addressWOcountry);
            editor.putString("Country", country);
            editor.putString("CountryCode", countryCode);
            editor.putString("Locality", locality);
            editor.putString("Longitude", String.valueOf(longitude));
            editor.putString("Latitude", String.valueOf(latitude));

            trackData.add(new EmergencyTrackData(addressWOcountry, parseDateToddMMyyyy(String.valueOf(new Timestamp(date.getTime()))), String.valueOf(latitude), String.valueOf(longitude), country, locality));

            editor.putString("TrackData", gson.toJson(trackData, listOfTrack));

            editor.commit();
            editor.apply();

            // Instantiate Http Request Param Object
            RequestParams params = new RequestParams();

            if (uname != null) {
                params.put("username", uname);
                params.put("longitude", String.valueOf(longitude));
                params.put("latitude", String.valueOf(latitude));
                params.put("address", address);
                params.put("country", countryCode);
                params.put("track_type", trackType);
                params.put("track_em_id", emID);
                params.put("locality", locality);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }
            // when any of the field is empty from token
            else {
                Toast.makeText(getApplicationContext(), "Failed to record current coordinates", Toast.LENGTH_LONG).show();
            }

        }
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
                        Toast.makeText(getApplicationContext(), "Location Tracked and Stored.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Location Service -  Error Occured!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Location Service - Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Location Service - Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Location Service - Unexpected Error occcured!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
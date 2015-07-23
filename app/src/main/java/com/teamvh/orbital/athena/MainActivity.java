package com.teamvh.orbital.athena;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AccessTokenTracker accessTokenTracker;
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor editor;
    public static Preference countryPref;

    //menu
    protected FloatingActionMenu actionMenu;
    protected FloatingActionButton helpAB;
    protected FloatingActionButton contactAB;
    protected FloatingActionButton historyAB;
    protected FloatingActionButton settingAB;

    protected TextView mLocationAddressTextView;
    protected TextView mCountryTextView;
    protected TextView mStatusTextView;

    protected ImageButton mStartUpdatesButton;
    protected TextView mLastUpdateTimeText;

    protected SlidingUpPanelLayout mPanelLayout;

    protected Profile profile;

    //--------------------------------------------Nearby----------------------------------------

    //high alert function
    protected Button mStartHighAlertButton;
    protected CountDownTimer highAlertCD;

    protected AlertDialog safeAlert;
    protected int safetyCount = 1;
    protected TextView alertMessage;

    protected Vibrator v;

    //emergency function
    //protected Button mStartEmergencyButton;
    protected int emID;

    //map
    protected GoogleMap mGoogleMap;
    protected double latitude;
    protected double longitude;
    protected Marker lastLocationMark;

    //-------------------------------------GENERAL METHOD------------------------------------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //INITIALIZE SHARED PREFERENCE
        preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);

        //INITIALIZE FACEBOOK
        FacebookSdk.sdkInitialize(getApplicationContext());

        //CHECK FOR LOGIN
        isLoggedIn();

        //CHECK FOR FACEBOOK ACCESS TOKEN
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                profile = Profile.getCurrentProfile();
                String name = profile.getName();
                editor = preferences.edit();
                editor.putString("fbsession", newAccessToken.getUserId());
                editor.putString("name", name);
                editor.commit();
                editor.apply();
            }
        };

        displayMain();
    }

    //Set up the activity page and initialize the content.
    public void displayMain() {
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //FOR THE CURRENT LOCATION
        mLocationAddressTextView = (TextView) findViewById(R.id.location_address_view);
        mCountryTextView = (TextView) findViewById(R.id.track_country);
        mLastUpdateTimeText = (TextView) findViewById(R.id.track_location_time);
        mStatusTextView = (TextView) findViewById(R.id.statusTV);
        mPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.main_sliding);

        //High alert button
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        createMap();
        prepareMenu();

        mStartUpdatesButton = (ImageButton) findViewById(R.id.start_updates_button);
        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMyServiceRunning(LocationService.class)) {
                    stopTracking();
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_button);
                } else {
                    startTracking("Standard", 0);
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_stop);
                }
            }
        });

        mStartUpdatesButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startHighAlert();
                return false;
            }
        });

        mPanelLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        mPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelExpanded(View panel) {
                startEmergency();
            }

            @Override
            public void onPanelCollapsed(View panel) {
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {
            }
        });

    }

    //Prepare menu
    public void prepareMenu() {
        actionMenu = (FloatingActionMenu) findViewById(R.id.fab);

        //Help info
        helpAB = (FloatingActionButton) findViewById(R.id.menu_helpinfo);
        helpAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, HelpInfo.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0, 0);
                actionMenu.close(false);
            }
        });

        //Contacts
        contactAB = (FloatingActionButton) findViewById(R.id.menu_contacts);
        contactAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ContactInfo.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0, 0);
                actionMenu.close(false);
            }
        });

        //History
        historyAB = (FloatingActionButton) findViewById(R.id.menu_history);
        historyAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, EmergencyHistory.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0, 0);
                actionMenu.close(false);
            }
        });

        //Settings
        settingAB = (FloatingActionButton) findViewById(R.id.menu_setting);
        settingAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0, 0);
                actionMenu.close(false);
            }
        });

    }

    //Check for facebook login
    public void isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null || accessToken.isExpired()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        if (isMyServiceRunning(LocationService.class) && preferences.getString("Main Status", "").equals("TRACKING")) {
            mStartUpdatesButton.setBackgroundResource(R.drawable.track_stop);
        } else if (isMyServiceRunning(LocationService.class) && preferences.getString("Main Status", "").equals("TRACKING (ALERT MODE)")) {
            mStartUpdatesButton.setBackgroundResource(R.drawable.alert_stop);
        }
        mPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        stopTracking();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("Latitude") || key.equals("Longitude")) {

            if (sharedPreferences.getString("Address", "").equals("Not Available")) {
                mLocationAddressTextView.setText("Lat: " + sharedPreferences.getString("Latitude", "") + ", Long: " + sharedPreferences.getString("Longitude", ""));
            } else {
                mLocationAddressTextView.setText(sharedPreferences.getString("Address", ""));
            }
            longitude = Double.parseDouble(sharedPreferences.getString("Longitude", ""));
            latitude = Double.parseDouble(sharedPreferences.getString("Latitude", ""));
            mLastUpdateTimeText.setText(sharedPreferences.getString("Timestamp", ""));

            if (sharedPreferences.getString("Locality", "").equals("Not Available")) {
                mCountryTextView.setText(sharedPreferences.getString("Country", ""));
            } else {
                mCountryTextView.setText(sharedPreferences.getString("Country", "") + ", " + sharedPreferences.getString("Locality", ""));
            }

            Gson gson = new Gson();
            Type listOfTrack = new TypeToken<ArrayList<EmergencyTrackData>>() {
            }.getType();

            ArrayList<EmergencyTrackData> trackData = gson.fromJson(preferences.getString("TrackData", ""), listOfTrack);
            mGoogleMap.clear();
            for (int i = 0; i < trackData.size() - 1; i++) {
                LatLng ll = new LatLng(Double.parseDouble(trackData.get(i).getLatitude()), Double.parseDouble(trackData.get(i).getLongitude()));
                mGoogleMap.addMarker(new MarkerOptions().position(ll));

            }

            // Enabling go to current location in Google Map
            LatLng ll = new LatLng(latitude, longitude);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 18);
            mGoogleMap.animateCamera(update);
            if (lastLocationMark != null) {
                lastLocationMark.remove();
            }
            lastLocationMark = mGoogleMap.addMarker(new MarkerOptions().position(ll).title("Last Location"));
        }

        if (key.equals("Main Status")) {
            mStatusTextView.setText(sharedPreferences.getString("Main Status", ""));
        }

        if (key.equals("Country")) {

        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //------------------------------------------------Location-------------------------------------------------//

    public void startTracking(String trackType, int emID) {
        editor = preferences.edit();
        if (trackType.equals("Standard")) {
            editor.putString("Main Status", "TRACKING");
        } else if (trackType.equals("High Alert")) {
            editor.putString("Main Status", "TRACKING (ALERT MODE)");
        } else {
            editor.putString("Main Status", "EMERGENCY MODE ON");
        }
        editor.commit();
        editor.apply();

        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra("fb_token", AccessToken.getCurrentAccessToken());
        intent.putExtra("track_type", trackType);
        intent.putExtra("track_em_id", emID);
        intent.putExtra("address", "address");
        startService(intent);
    }

    public void stopTracking() {
        editor = preferences.edit();
        editor.putString("Main Status", "IDLE");
        editor.commit();
        editor.apply();
        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
    }

    //-------------------------------------------HIGH ALERT-------------------------------------------------------------//

    //additionl method for high alert

    public void startHighAlertMode(View view) {
        startHighAlert();
    }

    public void stopHighAlertMode(View view) {
        stopHighAlert();
    }

    protected void startHighAlert() {
        stopTracking();
        startTracking("High Alert", 0);
        highAlertCD = new SafetyCountDown(5000, 1000, 1);
        highAlertCD.start();
    }

    protected void stopHighAlert() {
        stopTracking();
        startTracking("Standard", 0);
        highAlertCD.cancel();
    }

    public void stillSafe() {
        highAlertCD.cancel();
        highAlertCD.start();
    }

    //-----------------------------------------------Emergency Functions--------------------------------------

    public void startEmergency() {
        stopTracking();
        getEMID();
    }

    public void getEMID() {
        String uname = preferences.getString("fbsession", "");
        RequestParams params = new RequestParams();
        if (uname != null) {
            params.put("username", uname);
            invokeGetEMID(params);
        } else {
            Toast.makeText(getApplicationContext(), "Failed to retrieve contacts", Toast.LENGTH_LONG).show();
        }
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeGetEMID(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/getemcount", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    emID = obj.getInt("status");
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
                createEMID();
            }

        });
    }

    //PREPARE QUERY TO GET CONTACT LIST
    public void createEMID() {
        String uname = preferences.getString("fbsession", "");
        RequestParams params = new RequestParams();
        if (uname != null) {
            params.put("username", uname);
            params.put("em_times", String.valueOf(emID));
            invokeCreateEMID(params);
        } else {
            Toast.makeText(getApplicationContext(), "Failed to create contacts", Toast.LENGTH_LONG).show();
        }
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeCreateEMID(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/createemid", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);

                    if (obj.getBoolean("status")) {
                        Toast.makeText(getApplicationContext(), "Create Successful", Toast.LENGTH_LONG).show();
                    } else {
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
                startTracking("Emergency", emID);
                Intent i = new Intent(MainActivity.this, EmergencyActivity.class);
                i.putExtra("track_em_id", emID);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(0, 0);
            }
        });
    }

    //-------------------------------------MAP FUNCTION------------------------------------------//

    public void createMap() {
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map);
        mGoogleMap = fragment.getMap();
    }

    //--------------------------------SUPPORTING CLASS ------------------------------------------//
    public class SafetyCountDown extends CountDownTimer {

        protected int cdType;
        protected AlertDialog.Builder safetyCheck;
        protected CountDownTimer TriggerCountDown;


        public SafetyCountDown(long startTime, long interval, int cdType) {
            super(startTime, interval);
            this.cdType = cdType;
        }

        @Override
        public void onFinish() {
            if (cdType == 1) {
                safetyCheck = new AlertDialog.Builder(MainActivity.this, 4);
                safetyCheck.setCancelable(false);
                safetyCheck.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        safetyCount = 0;
                        TriggerCountDown.cancel();
                        v.cancel();
                        dialog.cancel();
                        stillSafe();
                    }
                });
                safetyCheck.setTitle("Are you safe?");
                safetyCheck.setMessage("Counting down...");
                safeAlert = safetyCheck.create();
                safeAlert.show();
                alertMessage = (TextView) safeAlert.findViewById(android.R.id.message);
                TriggerCountDown = new SafetyCountDown(20000, 1000, 2);
                TriggerCountDown.start();
                v.vibrate(20000);
            } else {
                if (safetyCount > 1) {
                    alertMessage.setText("No response from user - Triggering Emergency mode");
                    safeAlert.cancel();
                    highAlertCD.cancel();
                    stopTracking();
                    getEMID();
                } else {
                    safetyCount++;
                    stillSafe();
                    safeAlert.cancel();
                }
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (cdType == 2) {
                alertMessage.setText("Remaining time = " + millisUntilFinished / 1000 + "\n[Safety count is at " + safetyCount + "]");
            }
        }

    }

}


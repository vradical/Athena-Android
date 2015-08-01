package com.teamvh.orbital.athena;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.rey.material.widget.EditText;

import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AccessTokenTracker accessTokenTracker;
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor editor;
    protected LocationManager manager;
    protected String TAG = "MainActivity";
    protected long mLastClickTime;
    protected Dialog safeAlert;

    //menu
    protected FloatingActionMenu actionMenu;
    protected FloatingActionButton helpAB;
    protected FloatingActionButton historyAB;
    protected FloatingActionButton settingAB;
    protected FloatingActionButton dangerzoneAB;

    //Status
    protected TextView mLocationAddressTextView;
    protected TextView mCountryTextView;
    protected TextView mStatusTextView;
    protected TextView mDangerZoneTextView;

    protected ImageButton mStartUpdatesButton;
    protected TextView mLastUpdateTimeText;

    protected SlidingUpPanelLayout mPanelLayout;

    protected ProfileTracker mProfileTracker;
    protected Profile profile;

    protected TimeZone timezone;

    //--------------------------------------------Nearby----------------------------------------

    private boolean currentlyTracking;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;

    //high alert function
    protected CountDownTimer highAlertCD;
    protected int safetyCount = 1;
    protected Vibrator v;

    //emergency function
    protected int emID;

    //map
    protected GoogleMap mGoogleMap;
    protected double latitude;
    protected double longitude;
    protected Marker lastLocationMark;
    protected LatLngBounds.Builder bounds;
    protected static String country;
    protected ArrayList<Marker> markerList;
    protected ArrayList<Circle> circleList;
    protected ArrayList<Marker> dzMarkerList;
    protected ArrayList<SpecialZoneData> specialZoneList;
    protected Polyline route;

    //test
    HashMap<String, HashMap> extraMarkerInfo = new HashMap<String, HashMap>();

    //-------------------------------------GENERAL METHOD------------------------------------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //INITIALIZE SHARED PREFERENCE
        preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        editor = preferences.edit();

        //RESET COUNTRY
        country = "empty";
        markerList = new ArrayList<Marker>();
        circleList = new ArrayList<Circle>();
        dzMarkerList = new ArrayList<Marker>();


        //INITIALIZE FACEBOOK
        FacebookSdk.sdkInitialize(getApplicationContext());

        bounds = new LatLngBounds.Builder();
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        editor.putString("NoGPS", "No").commit();
        currentlyTracking = false;

        displayMain();

        //CHECK FOR LOGIN
        isLoggedIn();

        //CHECK FOR FACEBOOK PROFILE
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile != null) {
                    profile = Profile.getCurrentProfile();
                    String name = profile.getName();
                    editor.putString("Name", name).commit();
                }
            }
        };

        //CHECK FOR FACEBOOK ACCESS TOKEN
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                if (newAccessToken != null) {
                    editor.putString("fbsession", newAccessToken.getUserId());
                    editor.commit();
                }
            }
        };
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
        mDangerZoneTextView = (TextView) findViewById(R.id.danger_zone_view);
        mPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.main_sliding);

        //High alert button
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        timezone = TimeZone.getDefault();

        createMap();
        prepareMenu();

        mStartUpdatesButton = (ImageButton) findViewById(R.id.start_updates_button);
        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(currentlyTracking && preferences.getString("Start Mode", "").equals("High Alert")){
                    currentlyTracking = false;
                    editor.putBoolean("currentlyTracking", false);
                    stopHighAlert();
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_button);
                }else if(currentlyTracking){
                    currentlyTracking = false;
                    editor.putBoolean("currentlyTracking", false);
                    stopTracking();
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_button);
                }else{
                    currentlyTracking = true;
                    editor.putBoolean("currentlyTracking", true);
                    startTracking("Standard");
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_stop);
                }

                editor.apply();
                editor.commit();

                /*
                if (isMyServiceRunning(LocationService.class)) {
                    stopTracking();
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_button);
                } else {
                    startTracking("Standard");
                    mStartUpdatesButton.setBackgroundResource(R.drawable.track_stop);
                }*/
            }

        });

        mStartUpdatesButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return false;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                currentlyTracking = true;
                editor.putBoolean("currentlyTracking", true);
                startHighAlert();
                mStartUpdatesButton.setBackgroundResource(R.drawable.alert_stop);
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

        //Prepare setting
        if(!preferences.contains("CHECK_INTERVAL")){
            editor.putInt("CHECK_INTERVAL", Constants.CHECK_INTERVAL);
        }

        if(!preferences.contains("HA_CHECK_INTERVAL")){
            editor.putInt("HA_CHECK_INTERVAL", Constants.HA_CHECK_INTERVAL);
        }

        if(!preferences.contains("EM_CHECK_INTERVAL")){
            editor.putInt("EM_CHECK_INTERVAL", Constants.EM_CHECK_INTERVAL);
        }


        if(!preferences.contains("ALERT_TIMER")){
            editor.putInt("ALERT_TIMER", Constants.ALERT_TIMER);
        }

        if(!preferences.contains("ALERT_COUNTDOWN")){
            editor.putInt("ALERT_COUNTDOWN", Constants.ALERT_COUNTDOWN);
        }
        editor.apply();
        editor.commit();

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

        //Dangerzone
        dangerzoneAB = (FloatingActionButton) findViewById(R.id.menu_dangerzone);
        dangerzoneAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, DangerZoneList.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0, 0);
                actionMenu.close(false);
            }
        });

    }

    //Check for connection
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    //Check for passcode
    public void setPasscode() {

        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
        final Dialog dialog = new Dialog(this, 0);
        dialog.contentView(textEntryView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.positiveAction("OK");
        dialog.positiveActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.title("Please set your new passcode.");
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mUserText;
                String strPinCode;
                mUserText = (EditText) textEntryView.findViewById(R.id.editTextPasscode);
                strPinCode = mUserText.getText().toString();

                if (strPinCode.equals("")) {
                    mUserText.setError("Password is Required");
                } else {
                    editor.putString("Passcode", strPinCode).commit();
                    dialog.cancel();
                }
            }
        });
        dialog.show();
    }

    //Check for GPS
    private void buildAlertMessageNoGps() {

        Dialog.Builder builder = null;
        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
        ((SimpleDialog.Builder) builder).message("Your GPS seems to be disabled and it may affect the tracking quality, do you want to enable it?")
                .title("GPS Not Found")
                .positiveAction("Yes")
                .negativeAction("No");
        final Dialog dialog = builder.build(MainActivity.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.cancel();
            }
        });
        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("NoGPS", "Yes").commit();
                dialog.cancel();
            }
        });
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

        //CHECKS
        if (!isOnline()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if(!preferences.getString("NoGPS", "").equals("Yes")){
                buildAlertMessageNoGps();
            }
        }

        if (!preferences.contains("Passcode")) {
            setPasscode();
        }

        //CHECK AND SHOW CORRECT STATUS
        if (currentlyTracking && preferences.getString("Start Mode", "").equals("High Alert")) {
            mStartUpdatesButton.setBackgroundResource(R.drawable.alert_stop);
        }else if(currentlyTracking){
            mStartUpdatesButton.setBackgroundResource(R.drawable.track_stop);
        }else{
            mStartUpdatesButton.setBackgroundResource(R.drawable.track_button);
        }
        mPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        if (country.equals("restart")) {
            country = preferences.getString("Country", "");
            getSpecialZone();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //CLEAR TRACKDATA FOR NEW UPDATES
        accessTokenTracker.stopTracking();
        if(preferences.getBoolean("currentlyTracking", false)) {
            stopTracking();
        }
        preferences.edit().remove("TrackData").commit();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (actionMenu.isOpened()) {
            actionMenu.close(false);
        } else {
            super.onBackPressed();
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        //Only if location has changed.
        if (key.equals("Latitude") || key.equals("Longitude")) {

            DecimalFormat df = new DecimalFormat("0.0000");

            longitude = Double.parseDouble(sharedPreferences.getString("Longitude", ""));
            latitude = Double.parseDouble(sharedPreferences.getString("Latitude", ""));
            mLastUpdateTimeText.setText(sharedPreferences.getString("Timestamp", ""));

            //Check if address is available. Display Latlng if not.
            if (sharedPreferences.getString("Address", "").equals("Not Available")) {
                mLocationAddressTextView.setText("Lat: " + df.format(latitude) + ", Lng: " + df.format(longitude));
            } else {
                mLocationAddressTextView.setText(sharedPreferences.getString("Address", ""));
            }

            //Check if locality is available. display with or without locality in country.
            if (sharedPreferences.getString("Locality", "").equals("Not Available")) {
                mCountryTextView.setText(sharedPreferences.getString("Country", ""));
            } else {
                mCountryTextView.setText(sharedPreferences.getString("Locality", "") + ", " + sharedPreferences.getString("Country", ""));
            }

            //Go to current location in Google Map
            LatLng ll = new LatLng(latitude, longitude);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 16);
            mGoogleMap.animateCamera(update);

            //If country is different from existing country, repopulate emergency and dangerzone.
            if (!country.equals(sharedPreferences.getString("Country", ""))) {
                country = sharedPreferences.getString("Country", "");
                getSpecialZone();
            }

            /*
            //Clear all previous marker on map
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).remove();
            }

            //Reset marker list
            markerList.clear();

            //Add marker
            for (int i = 0; i < trackData.size() - 1; i++) {
                LatLng ll = new LatLng(Double.parseDouble(trackData.get(i).getLatitude()), Double.parseDouble(trackData.get(i).getLongitude()));
                markerList.add(mGoogleMap.addMarker(new MarkerOptions().position(ll)));
            }*/

            //Check if last location exist, if exist, delete and prepare for new one.
            if (lastLocationMark != null) {
                lastLocationMark.remove();
            }

            Bitmap bm = drawableToBitmap(getResources().getDrawable(R.drawable.location_icon));
            lastLocationMark = mGoogleMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth()/2, bm.getHeight()/2, false))));

            //Get this session track data.
            Gson gson = new Gson();
            Type listOfTrack = new TypeToken<ArrayList<EmergencyTrackData>>() {
            }.getType();
            ArrayList<EmergencyTrackData> trackData = gson.fromJson(preferences.getString("TrackData", ""), listOfTrack);

            //Clear path
            if (route != null) {
                route.remove();
            }

            //Add Path
            ArrayList<LatLng> latlngList = new ArrayList<LatLng>();
            for (int i = 0; i < trackData.size(); i++) {
                LatLng ll1 = new LatLng(Double.parseDouble(trackData.get(i).getLatitude()), Double.parseDouble(trackData.get(i).getLongitude()));
                latlngList.add(ll1);
            }
            route = mGoogleMap.addPolyline(new PolylineOptions().width(8).color(Color.parseColor("#5E65B5")).geodesic(true));
            route.setPoints(latlngList);
            route.setVisible(true);

            calculateDangerZone();
        }

        if (key.equals("Main Status")) {
            mStatusTextView.setText(sharedPreferences.getString("Main Status", ""));
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

    public void startTracking(String trackType) {
        editor = preferences.edit();
        if (trackType.equals("Standard")) {
            editor.putString("Main Status", "TRACKING...");
            editor.putString("Start Mode", "Standard");
            editor.putString("emID", "0");
        } else if (trackType.equals("High Alert")) {
            editor.putString("Main Status", "(ALERT MODE) TRACKING...");
            editor.putString("Start Mode", "High Alert");
            editor.putString("emID", "0");
        } else {
            editor.putString("Main Status", "EMERGENCY MODE ON");
            editor.putString("Start Mode", "Emergency");
            editor.putString("emID", String.valueOf(emID));
        }
        editor.commit();
        editor.apply();

        startAlarmManager();
        //Intent intent = new Intent(this, LocationService.class);
       // startService(intent);
    }

    public void stopTracking() {
        editor = preferences.edit();
        editor.putString("Main Status", "IDLE");
        editor.commit();
        editor.apply();

        cancelAlarmManager();
        //Intent intent = new Intent(this, LocationService.class);
        //stopService(intent);
    }

    private void startAlarmManager()
    {
        Log.d(TAG, "startAlarmManager");

        int alarmInterval = 30000;

        if (preferences.getString("Start Mode", "").equals("Standard")) {
            alarmInterval = preferences.getInt("CHECK_INTERVAL", 0);
        } else if (preferences.getString("Start Mode", "").equals("High Alert")) {
            alarmInterval = preferences.getInt("HA_CHECK_INTERVAL", 0);
        } else if (preferences.getString("Start Mode", "").equals("Emergency")) {
            alarmInterval = preferences.getInt("EM_CHECK_INTERVAL", 0);
        }

        Context context = getBaseContext();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, TrackServiceAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                alarmInterval, pendingIntent);
    }
    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager");
        Context context = getBaseContext();
        Intent gpsTrackerIntent = new Intent(context, TrackServiceAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    //-------------------------------------------HIGH ALERT-------------------------------------------------------------//

    protected void startHighAlert() {
        stopTracking();
        startTracking("High Alert");
        highAlertCD = new SafetyCountDown(preferences.getInt("ALERT_TIMER", Constants.ALERT_TIMER), 1000, 1);
        highAlertCD.start();
    }

    protected void stopHighAlert() {
        stopTracking();
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

    //SEND QUERY TO ATHENA WEB SERVICE
    public void getEMID() {
        String uname = preferences.getString("fbsession", "");
        RequestParams params = new RequestParams();
        params.put("username", uname);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/getemcount", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    emID = obj.getInt("status");
                    createEMID();
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

    //QUERY TO GET CONTACT LIST
    public void createEMID() {
        String uname = preferences.getString("fbsession", "");
        String rawOffset = String.valueOf(timezone.getRawOffset());
        RequestParams params = new RequestParams();
        params.put("username", uname);
        params.put("em_times", String.valueOf(emID));
        params.put("country", preferences.getString("CountryCode", ""));
        params.put("address", preferences.getString("Address", ""));
        params.put("latitude", preferences.getString("Latitude", ""));
        params.put("longitude", preferences.getString("Longitude", ""));
        params.put("locality", preferences.getString("Locality", ""));
        params.put("timezone", rawOffset);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/createemid", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);

                    if (obj.getBoolean("status")) {
                        startTracking("Emergency");
                        Intent i = new Intent(MainActivity.this, EmergencyActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                    } else {
                        displayDialog("", 1);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
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

            }
        });
    }

    //-------------------------------------GIS FUNCTION-------------------------------------------//

    public void getSpecialZone() {

        RequestParams params = new RequestParams();
        params.put("latitude", preferences.getString("Latitude", ""));
        params.put("longitude", preferences.getString("Longitude", ""));
        params.put("distance", Constants.DANGER_ZONE_DISTANCE);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/dangerzone/getspecialz", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {

                specialZoneList = new ArrayList<SpecialZoneData>();

                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);

                    // When the JSON response has status boolean value assigned with true
                    if (!response.equals(null)) {
                        try {
                            JSONObject object = obj.getJSONObject("specialZoneData");

                            SpecialZoneData sz = new SpecialZoneData();

                            sz.setDz_id(object.getString("dz_id"));
                            sz.setDateTime(parseDateToddMMyyyy(object.getString("dateTime")));
                            sz.setDz_info(object.getString("dz_info"));
                            sz.setDz_title(object.getString("dz_title"));
                            sz.setZone_type(object.getString("zone_type"));
                            sz.setCoordinate(new LatLng(Double.parseDouble(object.getString("latitude")), Double.parseDouble(object.getString("longitude"))));

                            specialZoneList.add(sz);

                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("specialZoneData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                SpecialZoneData sz = new SpecialZoneData();

                                sz.setDz_id(object.getString("dz_id"));
                                sz.setDateTime(parseDateToddMMyyyy(object.getString("dateTime")));
                                sz.setDz_info(object.getString("dz_info"));
                                sz.setDz_title(object.getString("dz_title"));
                                sz.setZone_type(object.getString("zone_type"));
                                sz.setCoordinate(new LatLng(Double.parseDouble(object.getString("latitude")), Double.parseDouble(object.getString("longitude"))));

                                specialZoneList.add(sz);
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
                populateMap();
            }
        });
    }

    private void calculateDangerZone() {

        //Check if there is any emergency records
        int dangerCount = 0;

        Location currentLoc = new Location("");
        currentLoc.setLatitude(latitude);
        currentLoc.setLongitude(longitude);

        Location circleLoc = new Location("");

        //calculate emergency count (overlaps)
        for (int i = 0; i < circleList.size(); i++) {
            circleLoc.setLatitude(circleList.get(i).getCenter().latitude);
            circleLoc.setLongitude(circleList.get(i).getCenter().longitude);

            if (currentLoc.distanceTo(circleLoc) <= circleList.get(i).getRadius()) {
                dangerCount++;
            }
        }

        if (dangerCount == 0) {
            mDangerZoneTextView.setText("Safe");
            mDangerZoneTextView.setTextColor(Color.WHITE);
        } else {
            mDangerZoneTextView.setText("Danger Lv " + dangerCount);
            mDangerZoneTextView.setTextColor(Color.RED);
        }
    }

    private void populateMap() {

        //Reset circle list and remove from map
        for (int i = 0; i < circleList.size(); i++) {
            circleList.get(i).remove();

            if (i < dzMarkerList.size()) {
                dzMarkerList.get(i).remove();
            }
        }
        circleList.clear();
        dzMarkerList.clear();

        CircleOptions co = new CircleOptions().radius(Constants.DANGER_ZONE_RADIUS).strokeWidth(0);
        MarkerOptions mo = new MarkerOptions();
        Bitmap bm;

        //Repopulate map
        for (int i = 0; i < specialZoneList.size(); i++) {
            SpecialZoneData sz = specialZoneList.get(i);
            if (specialZoneList.get(i).getZone_type().equals("Danger")) {
                bm = drawableToBitmap(getResources().getDrawable(R.drawable.emergency_icon));
                circleList.add(mGoogleMap.addCircle(co.center(sz.getCoordinate()).fillColor(0x20ffff00)));
                Marker mz = mGoogleMap.addMarker(mo.position(sz.getCoordinate()).icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth()/2, bm.getHeight()/2, false))));
                dzMarkerList.add(mz);

                HashMap<String, String> data = new HashMap<String, String>();
                data.put("dz_id", sz.getDz_id());
                data.put("title", sz.getDz_title());
                data.put("info", sz.getDz_info());
                extraMarkerInfo.put(mz.getId(), data);
            } else {
                circleList.add(mGoogleMap.addCircle(co.center(sz.getCoordinate()).fillColor(0x20ff0000)));
            }
        }

        //mGoogleMap.setInfoWindowAdapter(new WinInfoAdapter());
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                if (extraMarkerInfo.get(marker.getId()) != null) {
                    marker.hideInfoWindow();

                    final HashMap<String, String> marker_data = extraMarkerInfo.get(marker.getId());

                    Dialog.Builder builder = null;
                    builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                    ((SimpleDialog.Builder) builder).message(marker_data.get("info"))
                            .title(marker_data.get("title"))
                            .positiveAction("REPORT")
                            .negativeAction("CANCEL");
                    final Dialog dialog = builder.build(MainActivity.this);
                    dialog.show();
                    dialog.positiveActionClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setupReport(marker_data.get("dz_id"));
                            dialog.cancel();
                        }
                    });
                    dialog.negativeActionClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });

                }

                return false;
            }
        });

        //Calculate Dangerzone
        calculateDangerZone();
    }

    public void setupReport(final String dz_id) {

        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_feedback_box, null);
        final Dialog dialog = new Dialog(this, 0);
        dialog.contentView(textEntryView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveAction("OK");
        dialog.positiveActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.negativeAction("CANCEL");
        dialog.negativeActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.title("Please key in your reporting reason.");
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mTypeText;
                EditText mDetailText;
                mTypeText = (EditText) textEntryView.findViewById(R.id.editType);
                mDetailText = (EditText) textEntryView.findViewById(R.id.editDetail);

                String type = mTypeText.getText().toString();
                String detail = mDetailText.getText().toString();

                if (isEmpty(mDetailText) && isEmpty(mTypeText)) {
                    mDetailText.setError("Detail is required.");
                    mTypeText.setError("Type is required.");
                } else if (!isEmpty(mDetailText) && isEmpty(mTypeText)) {
                    mTypeText.setError("Type is required.");
                } else if (isEmpty(mDetailText) && !isEmpty(mTypeText)) {
                    mDetailText.setError("Detail is required.");
                } else {
                    reportZone(dz_id, type.trim(), detail.trim());
                    dialog.cancel();
                }
            }

        });

        dialog.negativeActionClickListener(new View.OnClickListener() {

                                               @Override
                                               public void onClick(View view) {
                                                   dialog.cancel();
                                               }
                                           }

        );
        dialog.show();
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public void reportZone(String dz_id, String type, String detail) {

        RequestParams params = new RequestParams();
        params.put("username", preferences.getString("fbsession", ""));
        params.put("dz_id", dz_id);
        params.put("type", type);
        params.put("detail", detail);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/dangerzone/reportdz", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {

                            Dialog.Builder builder = null;
                            builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                            ((SimpleDialog.Builder) builder).message("Report submit successfully.")
                                    .positiveAction("OK")
                                    .title("Report");
                            final Dialog dialog = builder.build(MainActivity.this);
                            dialog.show();
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.positiveActionClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                }
                            });

                    } else {

                        displayDialog("You have already reported before.", 0);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
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
            }
        });
    }

    //-------------------------------------MAP FUNCTION------------------------------------------//

    public void createMap() {
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map);
        mGoogleMap = fragment.getMap();
        /* //Prevent clustering of dangerzone.
        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLngBounds bounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                for (int i =0; i<circleList.size(); i++) {
                    if(bounds.contains(circleList.get(i).getCenter()) ){
                        circleList.get(i).setVisible(true);
                    }
                }
            }
        });*/
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Create a danger zone at this location?")
                        .positiveAction("YES")
                        .negativeAction("NO");
                final Dialog dialog = builder.build(MainActivity.this);
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(MainActivity.this, AddDangerZone.class);
                        myIntent.putExtra("latitude", latLng.latitude);
                        myIntent.putExtra("longitude", latLng.longitude);
                        MainActivity.this.startActivity(myIntent);
                        dialog.cancel();
                    }
                });
                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
        });
    }

    //--------------------------------SUPPORTING CLASS ------------------------------------------//
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

    public class SafetyCountDown extends CountDownTimer {

        protected int cdType;
        protected Dialog.Builder safetyCheck;
        protected CountDownTimer TriggerCountDown;


        public SafetyCountDown(long startTime, long interval, int cdType) {
            super(startTime, interval);
            this.cdType = cdType;
        }

        @Override
        public void onFinish() {
            if(cdType == 1) {

                safetyCheck = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) safetyCheck).message("Are you safe? \nTwo consecutive no-response will activate the emergency.")
                        .positiveAction("Yes")
                        .title("Safety Check");
                safeAlert = safetyCheck.build(MainActivity.this);
                safeAlert.show();
                safeAlert.setCancelable(false);
                safeAlert.setCanceledOnTouchOutside(false);
                safeAlert.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        safetyCount = 1;
                        TriggerCountDown.cancel();
                        v.cancel();
                        safeAlert.cancel();
                        stillSafe();
                    }
                });
                TriggerCountDown = new SafetyCountDown(preferences.getInt("ALERT_TIMER", Constants.ALERT_COUNTDOWN), 1000, 2);
                TriggerCountDown.start();
                v.vibrate(preferences.getInt("ALERT_COUNTDOWN", Constants.ALERT_COUNTDOWN));
            }else{
                if(safetyCount > 1){
                    //trigger emergency
                    safetyCount = 1;
                    safeAlert.setTitle("No response. Triggering Emergency mode.");
                    startEmergency();
                    highAlertCD.cancel();
                    safeAlert.cancel();
                }else{
                    safetyCount++;
                    stillSafe();
                    safeAlert.cancel();
                }
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(cdType == 2) {
                safeAlert.setTitle("Safety Check ." + safetyCount + " [" + millisUntilFinished / 1000+"]");
                //alertMessage.setText("Remaining time = " + millisUntilFinished / 1000 +"\n[Safety count is at " + safetyCount + "]");
            }
        }


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
        final Dialog dialog = builder.build(MainActivity.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

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


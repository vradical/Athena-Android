package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    protected static final String TAG = "main-activity";

    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;

    protected boolean mAddressRequested;


    public String[] nokPhoneArray = null;
    public String[] nokEmailArray = null;

    public int numberOfNok = 0;
    public String sendLocation = null;


    protected String mAddressOutput;

    private AddressResultReceiver mResultReceiver;

    protected TextView mLocationAddressTextView;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

    private SQLController dbcon;
    private SQLControlllerNOK dbcon2;

    protected ListView mTrackListView;
    protected ListView mNOKListView;
    protected ListView eNokPhoneListView;
    protected ListView eNokNameListView;

    private SimpleCursorAdapter adapter;
    private SimpleCursorAdapter adapter2;
    private SimpleCursorAdapter adapter3;
    private SimpleCursorAdapter adapter4;

    EditText editTextName;
    EditText editTextEmail;
    EditText editTextPhone;

    //SimpleAdapter variables
    final String[] from = new String[] { DBHelper._ID, DBHelper.TRACK_LAT, DBHelper.TRACK_LONG, DBHelper.TRACK_ADDR, DBHelper.TRACK_TIME};
    final int[] to = new int[] {R.id.id, R.id.longi, R.id.lat, R.id.address, R.id.time};
    final String[] from2 = new String[] {DBHelperNok.col_NAME, DBHelperNok.col_EMAIL, DBHelperNok.col_PHONE};
    final int[] to2 = new int[] { R.id.list_name,R.id.list_email, R.id.list_phone};
    final String[] from3 = new String[] {DBHelperNok.col_PHONE};
    final int[] to3 = new int[] { R.id.textViewListPhone};
    final String[] from4 = new String[] {DBHelperNok.col_NAME};
    final int[] to4 = new int[] { R.id.textViewListNokName};


    protected boolean mRequestingLocationUpdates;
    protected LocationRequest mLocationRequest;
    protected String mLastUpdateTime;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected Button test;
    protected TextView mLastUpdateTimeTextView;

    //--------------------------------------------Nearby----------------------------------------
    GoogleMap mGoogleMap;
    //high alert function
    protected Button mStartHighAlertButton;
    protected Button mStopHighAlertButton;
    protected CountDownTimer highAlertCD;

    protected AlertDialog safeAlert;
    protected int safetyCount = 1;
    protected TextView alertMessage;

    protected Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FOR THE DB
        dbcon = new SQLController(this);
        dbcon2 = new SQLControlllerNOK(this);

        //START SQLCONTROLLERS
        dbcon.open();
        dbcon2.open();

        displayMain();
        updateValuesFromBundle(savedInstanceState);
    }

    public void displayMain(){
        setContentView(R.layout.activity_main);
        mTrackListView = (ListView) findViewById(R.id.list_view);
        mTrackListView.setEmptyView(findViewById(R.id.empty_view));

        //FOR THE CURRENT LOCATION
        mResultReceiver = new AddressResultReceiver(new Handler());

        mLocationAddressTextView = (TextView) findViewById(R.id.location_address_view);
        mLatitudeText = (TextView) findViewById(R.id.latitude_text);
        mLongitudeText = (TextView) findViewById(R.id.longitude_text);
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.track_location_time);
        test = (Button) findViewById(R.id.testingButton);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        //High alert button
        mStartHighAlertButton = (Button) findViewById(R.id.start_high_alert_button);
        mStopHighAlertButton = (Button) findViewById(R.id.stop_high_alert_button);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);


        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = false;
        mAddressOutput = "";

        buildGoogleApiClient();
    }
    //MENU SETTINGS
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homePage:
                displayMain();
                break;
            case R.id.helpInfo:
                break;
            case R.id.history:

                break;
            case R.id.share:

                break;
            case R.id.sosSignal:

                break;
            case R.id.nokSettings:
                nokSettings();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //View the database, can be deleted at the end of the project, trigger by seeDB button on main page
    public void seeDB(View view){
        Intent dbmanager = new Intent(getApplicationContext(),AndroidDatabaseManager.class);
        startActivity(dbmanager);

    }

    //-------------------------------------------------------NOK Settings----------------------------------------------------
    //NOK Registration
    public void nokSettings(){
        setContentView(R.layout.activity_nok_registration);
        mNOKListView = (ListView) findViewById(R.id.list_nok);
        Cursor cscs = dbcon2.fetchAllNOK();
        adapter2 = new SimpleCursorAdapter(this,R.layout.activity_nok_entry,cscs,from2,to2,0);
        mNOKListView.setAdapter(adapter2);
    }

    //OnClick =  TRIGGER BY REGISTRATION "ADD" BUTTON
    public void addNOK(View view){
        setContentView(R.layout.activity_nok_details);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
    }

    //OnClick = TRIGGER BY REGISTRATION "SAVE" BUTTON
    public void saveNOK(View view){
        int phone = Integer.parseInt(editTextPhone.getText().toString());
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        NOKInfo newNok = new NOKInfo(name, email, phone);
        dbcon2.insert(newNok);
        Toast.makeText(this,"New NOK has added ", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_nok_details);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
    }


    //-----------------------------------------------Emergency Functions--------------------------------------


    public void emergencyButtonHandler(View view) {
            //original
        setContentView(R.layout.activity_emergency);
        numberOfNok = dbcon2.getNumOfNOK();
        nokPhoneArray = dbcon2.getNOKPhone();
        sendLocation = mAddressOutput;

        eNokPhoneListView = (ListView) findViewById(R.id.listViewEmergency);
        Cursor cscs = dbcon2.fetchAllNOK();
        adapter3 = new SimpleCursorAdapter(this,R.layout.activity_noknumbers,cscs,from3,to3,0);
        eNokPhoneListView.setAdapter(adapter3);
        eNokNameListView = (ListView) findViewById(R.id.listViewEmergencyCall);
        Cursor cscs2 = dbcon2.fetchAllNOK();
        adapter4 = new SimpleCursorAdapter(this,R.layout.activity_nok_emergency_contact,cscs,from4,to4,0);
        eNokNameListView.setAdapter(adapter4);
        //ends here
//
//        Intent intent = new Intent (this,GooglePlaces.class);
//        intent.putExtra("numberOfNok",numberOfNok);
//        intent.putExtra("numberOfNokPhoneArray", nokPhoneArray);
//        intent.putExtra("location",sendLocation);




//        Toast.makeText(this,"Number of NOK in DB " + nokPhoneArray.length +
//                " Are they the same? " + numberOfNok +
//                " 1st number " + nokPhoneArray[0][0]+
//                " 2nd number " + nokPhoneArray[1][0]+
////                " 3rd number " + checknum[2][0]+
////                " 4rd number " + checknum[3][0]+
////                " 5th number " + checknum[4][0]+
//                " his location " + sendLocation, Toast.LENGTH_LONG).show();
        //Toast.makeText(this,test123, Toast.LENGTH_LONG).show();


        //sendSMSMessage();
        //sendEmail();

    }

    protected void sendSMSMessage() {
        for(int i = 0 ; i < numberOfNok ; i++){
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(nokPhoneArray[i], null, "This is an emergency, your friend/relative/child has been compromised please " +
                        "contact him/her ASAP. His/her current location is at " + sendLocation, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "SMS faild, please try again.",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

    }


    protected void sendEmail() {
        nokEmailArray = dbcon2.getNOKEmail();
        Intent email = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        for(int i = 0 ; i < numberOfNok ; i++) {
        // prompts email clients only
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_EMAIL, nokEmailArray[i]);
        email.putExtra(Intent.EXTRA_SUBJECT, "Emergency");
        email.putExtra(Intent.EXTRA_TEXT, "testing");
            try {
                // the user can choose the email client
                startActivity(Intent.createChooser(email, "Choose an email client from..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "No email client installed.", Toast.LENGTH_LONG).show();
            }
        }

    }

    //OnClick = Trigger by activity_nok_emergency_contact
    public void callNOK(View view){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        final int positionNOK = eNokNameListView.getPositionForView((View) view.getParent());
        callIntent.setData(Uri.parse("tel:" + nokPhoneArray[positionNOK]));
        startActivity(callIntent);
    }
    /**
     * Retrieve the info of the NOK details from the sqlLite or server
     * Send a sms to the NOKs with their current location and time
     **/

    public void deactivateEmergency(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_password, null);
        builder.setTitle("Passcode");
        builder.setMessage("To deactivate please enter your passcode");
        builder.setView(textEntryView);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText mUserText;
                mUserText = (EditText) textEntryView.findViewById(R.id.editTextPasscode);
                String strPinCode = mUserText.getText().toString();
                if(strPinCode.equals("1234")) {
                    Log.d(TAG, "Yes it is right");
                    checkTrigger();
                }
                else
                    Log.d( TAG, "Try again");
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                return;
            }
        });
        builder.show();
    }

    public void checkTrigger(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_check_trigger, null);
        builder.setTitle("Status");
        builder.setMessage("Please choose one");
        builder.setView(textEntryView);

        builder.setPositiveButton("Safe", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                displayNearby();
            }
        });

        builder.setNegativeButton("False Alarm", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                return;
            }
        });
        builder.show();
    }


    public void displayNearby() {
        setContentView(R.layout.activity_safe);
        SupportMapFragment fragment = ( SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.safe_map);
        // Getting Google Map
        mGoogleMap = fragment.getMap();

        // Enabling go to current location in Google Map
        LatLng ll = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 12);
        mGoogleMap.animateCamera(update);

        //Add a marker to the current location
        mGoogleMap.addMarker(new MarkerOptions().
                position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).
                title("You are here").
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    public void displayHospital(View view){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
        sb.append("&radius=5000");
        sb.append("&types=" + "hospital");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCDeAvvUXWhlZZ1aov-zPS20C8enJCExH8");
        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();
        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());

    }

    public void displayPoliceStation(View view){
        //Retrieve the information from url
        //Ensure the key is a browser key
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location="+mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude());
        sb.append("&radius=5000");
        sb.append("&types=" + "police");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCDeAvvUXWhlZZ1aov-zPS20C8enJCExH8");

        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();
        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
    }

    //---------------------------------------------Nearby------------------------------------

   /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                HttpConnection http = new HttpConnection();
                data = http.downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();
            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJsonParser placeJsonParser = new PlaceJsonParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            // Clears all the existing markers
            mGoogleMap.clear();

            // Place the current back after clearing
            mGoogleMap.addMarker(new MarkerOptions().
                    position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).
                    title("You are here").
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            for(int i=0;i<list.size();i++){
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double dlat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double dlng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(dlat, dlng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                markerOptions.title(dlat + " " + dlng + " " + name + " : " + vicinity);

                // Placing a marker on the touched position
                mGoogleMap.addMarker(markerOptions);

                // Add OnClickListener to the markers if selected will trigger getRoute function by passing lat and lng
                mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        getRoute(marker.getPosition().latitude, marker.getPosition().longitude);
                        return false;
                    }
                });
            }

        }

    }

    //-----------------------------------------------Route-----------------------------------------------------
    public void getRoute(double destinationLat, double destinationLng){
        Toast.makeText(MainActivity.this, destinationLat + " " + destinationLng, Toast.LENGTH_SHORT).show();

        String startPoint = "origin=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
        String destPoint = "destination=" + destinationLat+ "," + destinationLng;

        String sensor = "sensor=false";
        String params = startPoint + "&" + destPoint + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        Log.v("Path ", url.toString());
        RouteTask routeTask = new RouteTask();
        routeTask.execute(url);
    }

    private class RouteTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTaskR().execute(result);
        }
    }

    private class ParserTaskR extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(6);
                polyLineOptions.color(Color.BLUE);
            }

            mGoogleMap.addPolyline(polyLineOptions);
        }
    }




    //------------------------------------------------Location-------------------------------------------------
    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }

            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }

    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //change to constants
    protected void createLocationRequest(int interval, int fastInterval) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       // mLocationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(mLastUpdateTime);
        updateAddress();
        dbcon.insert(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), mAddressOutput);
        fetchDB();
    }

    private void fetchDB(){
        Cursor cursor = dbcon.fetch();
        adapter = new SimpleCursorAdapter(this, R.layout.activity_view_record, cursor, from, to, 0);
        adapter.notifyDataSetChanged();
        mTrackListView.setAdapter(adapter);
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }


    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */

    public void startUpdatesButtonHandler(View view) {

        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }

    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
            mStartHighAlertButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
            mStartHighAlertButton.setEnabled(false);
        }
    }

    //additionl method for high alert

    public void startHighAlertMode(View view){
        if(mRequestingLocationUpdates) {
            startHighAlert();
            mStartHighAlertButton.setEnabled(false);
            mStopHighAlertButton.setEnabled(true);
        }
    }

    public void stopHighAlertMode(View view){
        if(mRequestingLocationUpdates) {
            stopHighAlert();
            mStartHighAlertButton.setEnabled(true);
            mStopHighAlertButton.setEnabled(false);
        }
    }

    protected void startHighAlert() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        createLocationRequest(5000, 2500);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(mLastUpdateTime);
        updateAddress();
        fetchDB();

        highAlertCD = new SafetyCountDown(5000, 1000, 1);
        highAlertCD.start();
        }

    protected void stopHighAlert(){
        highAlertCD.cancel();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        startLocationUpdates();
    }

    public void stillSafe() {
        highAlertCD.cancel();
        highAlertCD.start();
    }

    //end of high alert
    protected void startLocationUpdates() {
        createLocationRequest(Constants.CHECK_INTERVAL, Constants.CHECK_FAST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(mLastUpdateTime);
        updateAddress();
        fetchDB();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void updateAddress(){
        // We only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mCurrentLocation != null) {
            startIntentService();
        }
        // If GoogleApiClient isn't connected, we process the user's request by setting
        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        mLocationAddressTextView.setText(mAddressOutput);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mCurrentLocation != null) {

            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startIntentService();
            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

        AppEventsLogger.deactivateApp(this);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
        }
    }

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
            if(cdType == 1) {
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
            }else{
                if(safetyCount > 1){
                //trigger emergency
                alertMessage.setText("No response from user - Triggering Emergency mode");
                }else{
                    safetyCount++;
                    stillSafe();
                    safeAlert.cancel();
                    safeAlert.cancel();
                }
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(cdType == 2) {
                alertMessage.setText("Remaining time = " + millisUntilFinished / 1000 +"\n[Safety count is at " + safetyCount + "]");
            }
        }

    }

}


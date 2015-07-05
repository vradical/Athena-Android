package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.google.android.gms.location.LocationRequest;


public class MainActivity extends ActionBarActivity {

    private AccessTokenTracker accessTokenTracker;
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor editor;

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

    //high alert function
    protected Button mStartHighAlertButton;
    protected Button mStopHighAlertButton;
    protected CountDownTimer highAlertCD;

    protected AlertDialog safeAlert;
    protected int safetyCount = 1;
    protected TextView alertMessage;

    protected Vibrator v;

    //-------------------------------------GENERAL METHOD------------------------------------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //INITIALIZE SHARED PREFERENCE
        preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);

        //INITIALIZE FACEBOOK
        FacebookSdk.sdkInitialize(getApplicationContext());

        isLoggedIn();

        //CHECK FOR FACEBOOK ACCESS TOKEN
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                editor = preferences.edit();
                editor.putString("fbsession", newAccessToken.getUserId());
                editor.commit();
                editor.apply();
            }
        };

        //FOR THE DB
        dbcon2 = new SQLControlllerNOK(this);

        //START SQLCONTROLLERS
        dbcon2.open();

        displayMain();
    }

    //Set up the activity page and initialize the content.
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

    }

    //Check for facebook login
    public void isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null || accessToken.isExpired()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    //View the database, can be deleted at the end of the project, trigger by seeDB button on main page
    public void seeDB(View view){
        Intent dbmanager = new Intent(getApplicationContext(),AndroidDatabaseManager.class);
        startActivity(dbmanager);

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
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
            default:
                break;
        }
        return true;
    }

    //------------------------------------------------Location-------------------------------------------------//

    public void startUpdatesButtonHandler(View view) {
        startTracking("Standard");
        mStartHighAlertButton.setEnabled(true);
        mStartUpdatesButton.setEnabled(false);
        mStopUpdatesButton.setEnabled(true);
    }

    public void stopUpdatesButtonHandler(View view) {
        stopTracking();
        mStartHighAlertButton.setEnabled(false);
        mStartUpdatesButton.setEnabled(true);
        mStopUpdatesButton.setEnabled(false);
    }

    public void startTracking(String trackType){
        Intent intent = new Intent(this, LocationService.class) ;
        intent.putExtra("fb_token", AccessToken.getCurrentAccessToken());
        intent.putExtra("track_type", trackType);
        intent.putExtra("address", "address");
        startService(intent);
    }

    public void stopTracking(){
        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
    }


    protected void displayAddressOutput() {
        mLocationAddressTextView.setText(mAddressOutput);
    }


    //-------------------------------------------HIGH ALERT-------------------------------------------------------------//

    //additionl method for high alert

    public void startHighAlertMode(View view){
        startHighAlert();
        mStartHighAlertButton.setEnabled(false);
        mStopHighAlertButton.setEnabled(true);
    }

    public void stopHighAlertMode(View view){
        stopHighAlert();
        mStartHighAlertButton.setEnabled(true);
        mStopHighAlertButton.setEnabled(false);
    }

    protected void startHighAlert() {
        stopTracking();
        startTracking("High Alert");
        highAlertCD = new SafetyCountDown(5000, 1000, 1);
        highAlertCD.start();
    }

    protected void stopHighAlert(){
        stopTracking();
        startTracking("Standard");
        highAlertCD.cancel();
    }

    public void stillSafe() {
        highAlertCD.cancel();
        highAlertCD.start();
    }

    /*
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
*/

    //-----------------------------------------------Emergency Functions--------------------------------------
/*

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

    */

    /**
     * Retrieve the info of the NOK details from the sqlLite or server
     * Send a sms to the NOKs with their current location and time
     **/

    /*
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

    */


    //---------------------------------------------Nearby------------------------------------


    /** A class, to download Google Places */

   /*
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

    }*/

    /** A class to parse the Google Places in JSON format */

    /*
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJsonParser placeJsonParser = new PlaceJsonParser();

            try{
                jObject = new JSONObject(jsonData[0]);
*/
    /** Getting the parsed data as a List construct */
             /*   places = placeJsonParser.parse(jObject);

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

    }*/

    //-----------------------------------------------Route-----------------------------------------------------
                /*

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

    //end of high alert

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
                //showToast(getString(R.string.address_found));
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


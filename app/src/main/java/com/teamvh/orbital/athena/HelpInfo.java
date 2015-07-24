package com.teamvh.orbital.athena;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpInfo extends AppCompatActivity {

    protected GoogleMap mGoogleMap;
    protected SharedPreferences preferences;
    protected String latitude;
    protected String longitude;
    protected String displayChoice;
    protected TextView mPoliceText;
    protected TextView mHospitalText;
    protected Polyline polyLineStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpinfo);
        preferences = MainActivity.preferences;

        //set up action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        polyLineStore = null;

        longitude = preferences.getString("Longitude", "");
        latitude = preferences.getString("Latitude", "");

        displayEmergencyPhone();

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.safe_map);
        // Getting Google Map
        mGoogleMap = fragment.getMap();

        // Enabling go to current location in Google Map
        LatLng ll = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 14);
        mGoogleMap.animateCamera(update);

        //Add a marker to the current location
        mGoogleMap.addMarker(new MarkerOptions().
                position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))).
                title("You are here").
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
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

    public void displayEmergencyPhone(){
        mPoliceText = (TextView) findViewById(R.id.helpPoliceText);
        mHospitalText = (TextView) findViewById(R.id.helpHospitalText);

        CountryList country = new CountryList();
        CountryData curCountry =  country.findCountry(preferences.getString("CountryCode", ""));
        mPoliceText.append(" " + curCountry.getPoliceNum());
        mHospitalText.append(" " + curCountry.getHospitalNum());
    }

    public void callHospital(View view) {
        displayHospital();
    }

    public void callPoliceStation(View view) {
        displayPoliceStation();
    }

    public void displayHospital() {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + latitude + "," + longitude);
        sb.append("&radius=5000");
        sb.append("&types=" + "hospital");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCDeAvvUXWhlZZ1aov-zPS20C8enJCExH8");
        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();
        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
        displayChoice = "hospital";
    }

    public void displayPoliceStation() {
        //Retrieve the information from url
        //Ensure the key is a browser key
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + latitude + "," + longitude);
        sb.append("&radius=5000");
        sb.append("&types=" + "police");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCDeAvvUXWhlZZ1aov-zPS20C8enJCExH8");

        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();
        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
        displayChoice = "police";
    }

    //----------------------------------------------------NEARBY -------------------------------------------//

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                HttpConnection http = new HttpConnection();
                data = http.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();
            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /**
     * A class to parse the Google Places in JSON format
     */

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJsonParser placeJsonParser = new PlaceJsonParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            // Clears all the existing markers
            mGoogleMap.clear();

            // Place the current back after clearing
            mGoogleMap.addMarker(new MarkerOptions().
                    position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))).
                    title("You are here").
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            for (int i = 0; i < list.size(); i++) {
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
                markerOptions.title(name);
                markerOptions.snippet(vicinity);

                // Placing a marker on the touched position
                Marker marker = mGoogleMap.addMarker(markerOptions);
                marker.showInfoWindow();

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


    public void getRoute(double destinationLat, double destinationLng) {
        Toast.makeText(HelpInfo.this, destinationLat + " " + destinationLng, Toast.LENGTH_SHORT).show();

        String startPoint = "origin=" + latitude + "," + longitude;
        String destPoint = "destination=" + destinationLat + "," + destinationLng;
        String mode = "mode=" + Constants.PATH_TYPE;
        String sensor = "sensor=false";
        String params = startPoint + "&" + destPoint + "&" + sensor + "&" + mode;
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

        PolylineOptions polyLineOptions;

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
            polyLineOptions = null;

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

            if(polyLineStore != null){
                polyLineStore.remove();
            }

            polyLineStore = mGoogleMap.addPolyline(polyLineOptions);
        }

    }
}
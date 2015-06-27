package com.teamvh.orbital.athena;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

public class LocationUpdateService extends IntentService {

    public LocationUpdateService() {
        super(LocationUpdateService.class.getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        super.onStartCommand(intent, flags, startID);
        Log.d("LocationUpdateService","Location received");
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("LocationUpdateService","Intent received");

        LatLng latLng = intent.getParcelableExtra("location");
        Log.d("LocationUpdateService","Intent received: " + latLng.latitude + " " + latLng.longitude);
    }
}
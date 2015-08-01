package com.teamvh.orbital.athena;

/**
 * Created by Ron on 01-Aug-15.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class TrackServiceAlarmReceiver extends WakefulBroadcastReceiver {

        private static final String TAG = "GpsTrackerAlarmReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Alarm Received");
            context.startService(new Intent(context, LocationService.class));
        }

}

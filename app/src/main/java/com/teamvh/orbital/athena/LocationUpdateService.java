import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderApi;

public class LocationUpdateService extends IntentService {

    private final String TAG = "LocationUpdateService";
    Location location;

    public LocationUpdateService() {

        super("LocationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED)) {

            location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

            Log.d("locationtesting", "accuracy: " + location.getAccuracy() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude());
        }
    }
}
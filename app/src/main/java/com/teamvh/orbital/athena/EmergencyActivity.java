package com.teamvh.orbital.athena;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EmergencyActivity extends AppCompatActivity {

    protected ListView listView;
    protected EmContactAdapter adapter;
    protected ArrayList<ContactData> contactList;
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;
    protected String emStatus;
    protected TextView mContactStatus;
    protected Boolean smsDelivered;
    protected int[][] mSuccessCheck;
    protected Boolean isFinishByMethod;
    protected String emID;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;

    protected BroadcastReceiver sendBroadcastReceiver;
    protected BroadcastReceiver deliveryBroadcastReceiver;

    protected AudioManager am;
    protected SoundPool sp;
    protected int originalVolume;
    protected int maxVolume;
    protected int soundID;

    final String TAG = "De-activate Emergency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        getSupportActionBar().hide();

        preferences = MainActivity.preferences;
        editor = preferences.edit();

        emID = preferences.getString("emID", "");

        isFinishByMethod = false;

        contactList = new ArrayList<ContactData>();
        getContact();
        listView = (ListView) findViewById(R.id.listViewEmergencyCall);
        adapter = new EmContactAdapter(this, R.layout.activity_contact_row_e, contactList);
        listView.setAdapter(adapter);
        listView.setDivider(null);

        //initialize receiver
        sendBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };
        deliveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };

        startSiren();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {

        if (!isFinishByMethod) {
            emStatus = "Disrupted";
            endEmergency();
        }

        try {
            unregisterReceiver(sendBroadcastReceiver);
            unregisterReceiver(deliveryBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Receiver Not Registered");
        }
        super.onDestroy();
    }

    public void deactivateEmergency(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
        final Dialog dialog = new Dialog(this, 0);
        dialog.contentView(textEntryView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveAction("OK");
        dialog.positiveActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.negativeAction("CANCEL");
        dialog.negativeActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.title("Please key in your passcode to de-activate emergency mode.");
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.rey.material.widget.EditText mUserText;
                String strPinCode;
                mUserText = (com.rey.material.widget.EditText) textEntryView.findViewById(R.id.editTextPasscode);
                strPinCode = mUserText.getText().toString();

                if (!strPinCode.equals(preferences.getString("Passcode", ""))) {
                    mUserText.setError("Wrong Passcode");
                } else {
                    isFinishByMethod = true;
                    stopSiren();
                    checkTrigger();
                    stopTracking();
                    startTracking("Standard");
                    dialog.cancel();
                }
            }
        });

        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void checkTrigger() {

        Dialog.Builder builder = null;
        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
        ((SimpleDialog.Builder) builder).message("Ensure that you are safe before responding to this prompt! Was it an actual emergency?")
                .title("Status")
                .positiveAction("EMERGENCY")
                .negativeAction("FALSE ALARM");
        final Dialog dialog = builder.build(EmergencyActivity.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.cancelable(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emStatus = "Emergency";
                endEmergency();
                dialog.cancel();
            }
        });
        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emStatus = "False Alarm";
                endEmergency();
                dialog.cancel();
            }
        });

    }

    //-------------------------LOCATION TRACKING SERVICE------------------------------------//

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

    //-------------------------END EMERGENCY------------------------------------------------//

    public void endEmergency() {

        //Empty country for update of dangerzone
        MainActivity.country = "empty";

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        params.put("username", preferences.getString("fbsession", ""));
        params.put("track_em_id", emID);
        params.put("emStatus", emStatus);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/endem", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        if (emStatus.equals("Emergency")) {
                            Intent i = new Intent(EmergencyActivity.this, HelpInfo.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                        finish();
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
            }
        });
    }

    //-------------------------SEND EMAIL --------------------------------------------------------//

    //SEND QUERY TO ATHENA WEB SERVICE
    public void sendEmail() {
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        params.put("username", preferences.getString("fbsession", ""));
        params.put("name", preferences.getString("Name", ""));
        params.put("country", preferences.getString("Country", ""));
        params.put("address", preferences.getString("Address", ""));
        params.put("latitude", preferences.getString("Latitude", ""));
        params.put("longitude", preferences.getString("Longitude", ""));

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/sendmail", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getString("status").equals("Error")) {

                        for (int i = 0; i < contactList.size(); i++) {
                            mSuccessCheck[i][1] = 0;
                        }

                    } else {

                        String[] errorList = obj.getString("status").split(" ");

                        for (int i = 0; i < errorList.length; i++) {

                            for (int j = 0; j < contactList.size(); j++)

                                if (contactList.get(j).getEmail().equals(errorList[i])) {
                                    mSuccessCheck[j][1] = 0;
                                } else {
                                    mSuccessCheck[j][1] = 1;
                                }

                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
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

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < contactList.size(); i++) {

                            if (mSuccessCheck[i][0] == 1 && mSuccessCheck[i][1] == 1) {
                                adapter.getItem(i).setEmStatus("SMS and Email Sent");
                            } else if (mSuccessCheck[i][0] == 0 && mSuccessCheck[i][1] == 1) {
                                adapter.getItem(i).setEmStatus("SMS Error, Email Sent");
                            } else if (mSuccessCheck[i][0] == 1 && mSuccessCheck[i][1] == 0) {
                                adapter.getItem(i).setEmStatus("SMS Sent, Email Error");
                            } else {
                                adapter.getItem(i).setEmStatus("Error sending Email and SMS");
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, 3000);

            }
        });
    }

    //-----------------------------SEND SMS -----------------------------------------------------------//

    protected void sendSMSMessage() {
        for (int i = 0; i < contactList.size(); i++) {
            smsDelivered = false;
            mSuccessCheck[i][0] = 0;

            try {

                /*String sendMessage = "This is an emergency. " + preferences.getString("name","") + " may be in danger. Please reach out to him/her immediately at "
                        + preferences.getString("Country","") +" "+preferences.getString("Address", "")+" ("+preferences.getString("Latitude", "")
                        +", "+preferences.getString("Longitude", "")+"). Please do not reply to this SMS.";*/

                String sendMessage = "HELP! I might be in danger at ";

                if(!preferences.getString("Country","").equals("Not Available")){
                    sendMessage = sendMessage + preferences.getString("Country","") + ", ";
                }

                if(!preferences.getString("Address","").equals("Not Available")){
                    sendMessage = sendMessage + preferences.getString("Address", "") + ". ";
                }else{
                    sendMessage = sendMessage + ""+preferences.getString("Latitude", "") +", "+preferences.getString("Longitude", "")+". ";
                }

                sendMessage = sendMessage + " Contact me ASAP!";

                String phone = contactList.get(i).getPhone();
                sendSMS(phone, sendMessage);
                //sendSMSWS();

            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

    public void sendSMS(final String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        sendBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        break;
                }
            }
        };

        deliveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        for (int i = 0; i < contactList.size(); i++) {
                            if (phoneNumber.equals(contactList.get(i).getPhone())) {
                                mSuccessCheck[i][0] = 1;
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
            }
        };

        //---when the SMS has been sent---
        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeSMSWS() {

        RequestParams params = new RequestParams();

        params.put("username", preferences.getString("fbsession", ""));
        params.put("name", preferences.getString("name", ""));
        params.put("country", preferences.getString("Country", ""));
        params.put("address", preferences.getString("Address", ""));
        params.put("latitude", preferences.getString("Latitude", ""));
        params.put("longitude", preferences.getString("Longitude", ""));

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/sendsms", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        Toast.makeText(getApplicationContext(), "Record Successful", Toast.LENGTH_LONG).show();

                        // Else display error message
                    } else {
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

    //-------------------------GET CONTACTS CODE--------------------------------------------------//

    //SEND QUERY TO ATHENA WEB SERVICE
    public void getContact() {

        RequestParams params = new RequestParams();
        params.put("username", preferences.getString("fbsession", ""));

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/contacts/getcontact", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.equals(null)) {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                        try {
                            JSONObject object = obj.getJSONObject("contactData");

                            ContactData contact = new ContactData();

                            contact.setName(object.getString("name"));
                            contact.setEmail(object.getString("email"));
                            contact.setCountry(object.getString("country"));
                            contact.setPhone(object.getString("phone"));

                            contactList.add(contact);
                        } catch (JSONException e) {
                            JSONArray jarray = obj.getJSONArray("contactData");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject object = jarray.getJSONObject(i);

                                ContactData contact = new ContactData();

                                contact.setName(object.getString("name"));
                                contact.setEmail(object.getString("email"));
                                contact.setCountry(object.getString("country"));
                                contact.setPhone(object.getString("phone"));

                                contactList.add(contact);
                            }
                        }

                    } else {
                        displayDialog("No contact found. It is important to have at least 1 person to contact!" , 0);
                    }
                } catch (JSONException e) {
                    displayDialog("No contact found. It is important to have at least 1 person to contact!" , 0);
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
                adapter.notifyDataSetChanged();
                mSuccessCheck = new int[contactList.size()][2];
                sendSMSMessage();
                sendEmail();
            }
        });
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
        final Dialog dialog = builder.build(EmergencyActivity.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }


    //---------------------------START SIREN ----------------------------------------------------//

    public void startSiren(){
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        //get both volume
        originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        am.setSpeakerphoneOn(true);

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                sp.play(soundID, maxVolume, maxVolume, 1, -1, 1f);
            }
        });
        soundID = sp.load(this, R.raw.siren, 1);
    }

    public void stopSiren(){
        sp.stop(soundID);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        am.setSpeakerphoneOn(false);
    }
}

package com.teamvh.orbital.athena;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    protected String emID;
    protected String emStatus;
    protected String uname;
    protected TextView mContactStatus;
    protected Boolean smsDelivered;
    protected int[][] mSuccessCheck;

    protected BroadcastReceiver sendBroadcastReceiver;
    protected BroadcastReceiver deliveryBroadcastReceiver;

    final String TAG = "De-activate Emergency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        getSupportActionBar().hide();

        preferences = MainActivity.preferences;
        editor = preferences.edit();

        Bundle b = getIntent().getExtras();
        emID = String.valueOf(b.getInt("track_em_id"));
        uname = preferences.getString("fbsession", "");

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

    }

    @Override
    public void onBackPressed() {
    }

    @Override

    public void onStop() {
        try {
            unregisterReceiver(sendBroadcastReceiver);
            unregisterReceiver(deliveryBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            Log.d(TAG, "Receiver Not Registered");
        }
        super.onStop();
    }

    public void deactivateEmergency(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
        builder.setTitle("Passcode");
        builder.setMessage("Please key in your passcode to de-activate emergency mode.");
        builder.setView(textEntryView);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mUserText;
                TextView mErrorText;
                mUserText = (EditText) textEntryView.findViewById(R.id.editTextPasscode);
                mErrorText = (TextView) textEntryView.findViewById(R.id.PasscodeError);
                String strPinCode = mUserText.getText().toString();

                if (!strPinCode.equals(preferences.getString("Passcode", ""))) {
                    mErrorText.setVisibility(View.VISIBLE);
                    mErrorText.setText("Wrong Passcode");
                } else {
                    checkTrigger();
                    stopTracking();
                    startTracking("Standard", 0);
                    dialog.cancel();
                }
            }
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    public void checkTrigger() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Status");
        builder.setMessage("Ensure that you are safe before responding to this prompt!");
        builder.setCancelable(false);

        builder.setPositiveButton("Safe", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                emStatus = "Emergency";
                endEmergency();
            }
        });

        builder.setNegativeButton("False Alarm", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                emStatus = "False Alarm";
                endEmergency();
            }
        });
        builder.show();
    }

    //-------------------------LOCATION TRACKING SERVICE------------------------------------//

    public void startTracking(String trackType, int emID) {
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

    //-------------------------END EMERGENCY------------------------------------------------//

    //PREPARE QUERY TO GET CONTACT LIST
    public void endEmergency() {

        //Empty country for update
        MainActivity.country = "empty";

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if (uname != null) {
            params.put("username", uname);
            params.put("track_em_id", emID);
            params.put("emStatus", emStatus);

            // Invoke RESTful Web Service with Http parameters
            invokeEmergencyWS(params);
        }
        // when any of the field is empty from token
        else {
            Toast.makeText(getApplicationContext(), "Failed to end Emergency", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeEmergencyWS(RequestParams params) {
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
                        Toast.makeText(getApplicationContext(), "End Emergency Successful", Toast.LENGTH_LONG).show();

                        // Else display error message
                    } else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured in End Emergency", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "End EM : Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "End EM : Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "End EM : Unexpected Error occcured!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {
                if (emStatus.equals("Emergency")) {
                    Intent i = new Intent(EmergencyActivity.this, HelpInfo.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                finish();
            }
        });
    }

    //-------------------------SEND EMAIL --------------------------------------------------------//


    protected void sendEmail() {

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if (uname != null) {
            params.put("username", uname);
            params.put("name", preferences.getString("name", ""));
            params.put("country", preferences.getString("Country", ""));
            params.put("address", preferences.getString("Address", ""));
            params.put("latitude", preferences.getString("Latitude", ""));
            params.put("longitude", preferences.getString("Longitude", ""));

            // Invoke RESTful Web Service with Http parameters
            invokeEmailWS(params);
        }
        // when any of the field is empty from token
        else {
            Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeEmailWS(RequestParams params) {
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

                        for (int i = 0; i < errorList.length ;i++){

                            for(int j = 0; j < contactList.size(); j++)

                            if (contactList.get(j).getEmail().equals(errorList[i])) {
                                mSuccessCheck[j][1] = 0;
                            } else {
                                mSuccessCheck[j][1] = 1;
                            }

                        }

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

                String sendMessage = "HELP";
                String phone = contactList.get(i).getPhone();
                sendSMS(phone, sendMessage);
                //sendSMSWS();

            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

    public void sendSMS(final String phoneNumber, String message)
    {
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

        deliveryBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        for(int i = 0; i < contactList.size(); i++){
                            if(phoneNumber == contactList.get(i).getPhone()){
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

    //if above failed, use web service to send
    protected void sendSMSWS() {

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if (uname != null) {
            params.put("username", uname);
            params.put("name", preferences.getString("name", ""));
            params.put("country", preferences.getString("Country", ""));
            params.put("address", preferences.getString("Address", ""));
            params.put("latitude", preferences.getString("Latitude", ""));
            params.put("longitude", preferences.getString("Longitude", ""));

            // Invoke RESTful Web Service with Http parameters
            invokeSMSWS(params);
        }
        // when any of the field is empty from token
        else {
            Toast.makeText(getApplicationContext(), "Failed to send sms", Toast.LENGTH_LONG).show();
        }


    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeSMSWS(RequestParams params) {
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

    //PREPARE QUERY TO GET CONTACT LIST
    public void getContact() {

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if (uname != null) {
            params.put("username", uname);

            // Invoke RESTful Web Service with Http parameters
            invokeContactWS(params);
        }
        // when any of the field is empty from token
        else {
            Toast.makeText(getApplicationContext(), "Failed to retrieve contacts", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeContactWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/contacts/getcontact", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (!response.equals(null)) {

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

                        Toast.makeText(getApplicationContext(), "Contacts Retrieve Successful", Toast.LENGTH_LONG).show();

                    } else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured in get Contacts.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Get Contact : Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Get Contact : Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Get Contact : Unexpected Error occcured!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {
                adapter.notifyDataSetChanged();
                mSuccessCheck = new int[contactList.size()][2];
                //sendSMSMessage();
                //sendEmail();
            }
        });
    }
}

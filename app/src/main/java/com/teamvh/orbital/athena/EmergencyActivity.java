package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
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
    protected ContactAdapter adapter;
    protected ArrayList<ContactData> contactList;
    protected SharedPreferences preferences;
    protected String emID;
    protected String emStatus;
    final String TAG = "De-activate Emergency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        preferences = MainActivity.preferences;

        Bundle b = getIntent().getExtras();
        emID = String.valueOf(b.getInt("track_em_id"));

        contactList = new ArrayList<ContactData>();

        getContact();
        listView = (ListView) findViewById(R.id.listViewEmergencyCall);
        adapter = new ContactAdapter(this, R.layout.activity_contact_row, contactList);
        listView.setAdapter(adapter);
    }

    protected void sendSMSMessage() {
        for(int i = 0 ; i < contactList.size() ; i++){
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(contactList.get(i).getPhone(), null, "This is an emergency, your friend/relative/child has been compromised please " +
                        "contact him/her ASAP. His/her current location is at " + "Ron House!", null, null);
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
        Intent email = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        for(int i = 0 ; i < contactList.size() ; i++) {
            // prompts email clients only
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, contactList.get(i).getEmail());
            email.putExtra(Intent.EXTRA_SUBJECT, "Emergency");
            email.putExtra(Intent.EXTRA_TEXT, "testing");
            try {
                // the user can choose the email client
                startActivity(Intent.createChooser(email, "Choose an email client from..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email client installed.", Toast.LENGTH_LONG).show();
            }
        }

    }

    /*
    //OnClick = Trigger by activity_nok_emergency_contact
    public void callNOK(View view){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        final int positionNOK = eNokNameListView.getPositionForView((View) view.getParent());
        callIntent.setData(Uri.parse("tel:" + nokPhoneArray[positionNOK]));
        startActivity(callIntent);
    }*/

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
                if (strPinCode.equals("1234")) {
                    Log.d(TAG, "Correct Passcode");
                    checkTrigger();

                    stopTracking();
                    startTracking("Standard", 0);
                } else
                    Log.d(TAG, "Incorrect Passcode");
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
        builder.setTitle("Status");
        builder.setMessage(" ");
        builder.setCancelable(false);

        builder.setPositiveButton("Safe", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                emStatus = "Safe";
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


    //-------------------------LOCATION SERVICE METHOD-------------------------------------------//
    public void startTracking(String trackType, int emID){
        Intent intent = new Intent(this, LocationService.class) ;
        intent.putExtra("fb_token", AccessToken.getCurrentAccessToken());
        intent.putExtra("track_type", trackType);
        intent.putExtra("track_em_id", emID);
        intent.putExtra("address", "address");
        startService(intent);
    }

    public void stopTracking(){
        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
    }
    //-------------------------END EMERGENCY------------------------------------------------//

    //PREPARE QUERY TO GET CONTACT LIST
    public void endEmergency(){

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if(uname != null){
            params.put("username", uname);
            params.put("track_em_id", emID);
            params.put("emStatus", emStatus);

            // Invoke RESTful Web Service with Http parameters
            invokeEmergencyWS(params);
        }
        // when any of the field is empty from token
        else{
            Toast.makeText(getApplicationContext(), "Failed to retrieve contacts", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeEmergencyWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/emergency/getcontact", params, new AsyncHttpResponseHandler() {
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

                        Toast.makeText(getApplicationContext(), "Retrieve Successful", Toast.LENGTH_LONG).show();

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

            @Override
            public void onFinish() {
                if(emStatus.equals("Safe")) {
                    Intent i = new Intent(EmergencyActivity.this, HelpInfo.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                finish();
            }
        });
    }

    //-------------------------GET CONTACTS CODE--------------------------------------------------//

    //PREPARE QUERY TO GET CONTACT LIST
    public void getContact(){

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if(uname != null){
            params.put("username", uname);

            // Invoke RESTful Web Service with Http parameters
            invokeContactWS(params);
        }
        // when any of the field is empty from token
        else{
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

                        Toast.makeText(getApplicationContext(), "Retrieve Successful", Toast.LENGTH_LONG).show();

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

            @Override
            public void onFinish() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}

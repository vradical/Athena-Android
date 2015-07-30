package com.teamvh.orbital.athena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactInfo extends AppCompatActivity {

    protected ListView listView;
    protected ContactAdapter adapter;
    protected ArrayList<ContactData> contactList;
    private SharedPreferences preferences;
    protected TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Contacts");

        preferences = MainActivity.preferences;
        contactList = new ArrayList<ContactData>();

        getContact();
        listView = (ListView) findViewById(R.id.contact_list);
        adapter = new ContactAdapter(this, R.layout.activity_contact_row, contactList);
        listView.setAdapter(adapter);
        listView.setDivider(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_plus:
                Intent myIntent = new Intent(ContactInfo.this, AddContact.class);
                ContactInfo.this.startActivityForResult(myIntent, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            adapter.clear();
            getContact();
            adapter = new ContactAdapter(this, R.layout.activity_contact_row, contactList);
            listView.setAdapter(adapter);
        }
    }

    //PREPARE QUERY TO GET CONTACT LIST
    public void getContact(){

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        if(uname != null){
            params.put("username", uname);

            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }
        // when any of the field is empty from token
        else{
            Toast.makeText(getApplicationContext(), "Failed to retrieve contacts", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
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

                        try{
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
                adapter.notifyDataSetChanged();
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
        final Dialog dialog = builder.build(ContactInfo.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

}

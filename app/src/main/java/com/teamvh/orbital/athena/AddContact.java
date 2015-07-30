package com.teamvh.orbital.athena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddContact extends AppCompatActivity {

    private SharedPreferences preferences;
    protected EditText nameField;
    protected EditText emailField;
    protected EditText countryField;
    protected EditText phoneField;
    protected EditText areaField;
    protected TextView mTitleText;
    protected TextView mNameError;
    protected TextView mEmailError;
    protected TextView mEmailError2;
    protected TextView mPhoneError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        //Set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Add Contact");

        preferences = MainActivity.preferences;

        nameField = (EditText) findViewById(R.id.add_name);
        emailField = (EditText) findViewById(R.id.add_email);
        countryField = (EditText) findViewById(R.id.add_country_code);
        phoneField = (EditText) findViewById(R.id.add_phone_number);
        areaField = (EditText) findViewById(R.id.add_area_code);
        mNameError = (TextView) findViewById(R.id.name_helper);
        mEmailError = (TextView) findViewById(R.id.email_helper);
        mEmailError2 = (TextView) findViewById(R.id.email_valid);
        mPhoneError = (TextView) findViewById(R.id.phone_helper);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public void completeContact(View view){
        String name = "";
        String email = "";
        String country = "";
        String phone = "";

        if(isEmpty(nameField)){
            mNameError.setVisibility(View.VISIBLE);
        }else{
            mNameError.setVisibility(View.INVISIBLE);
            name = nameField.getText().toString();
        }

        if(isEmpty(emailField)){
            mEmailError2.setVisibility(View.GONE);
            mEmailError.setVisibility(View.VISIBLE);
        }else{
            mEmailError.setVisibility(View.GONE);
            email = emailField.getText().toString();
            if(!isEmailValid(email)){
                mEmailError2.setVisibility(View.VISIBLE);
            }
        }

        if(isEmpty(countryField) || isEmpty(phoneField)){
            mPhoneError.setVisibility(View.VISIBLE);
        }else{
            mPhoneError.setVisibility(View.GONE);
            country = countryField.getText().toString();

            if(isEmpty(areaField)) {
                phone = phoneField.getText().toString();
            }else{
                phone = areaField.getText().toString() + phoneField.getText().toString();
            }

        }

        if(!isEmpty(nameField) && !isEmpty(emailField) && !isEmpty(countryField) && !isEmpty(phoneField) && isEmailValid(email)) {
            addContact(name, email, country, phone);
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    //PREPARE QUERY TO LOGIN USER
    public void addContact(String name, String email, String country, String phone){

        String uname = preferences.getString("fbsession", "");

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        //
        if(uname != null){
            params.put("username", uname);
            params.put("name", name);
            params.put("email", email);
            params.put("country", country);
            params.put("phone", phone);
            // Invoke RESTful Web Service with Http parameters
            invokeWS(params);
        }
        // when any of the field is empty from token
        else{
            Toast.makeText(getApplicationContext(), "Failed to record contacts", Toast.LENGTH_LONG).show();
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/contacts/addcontact", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        Intent returnIntent = new Intent();
                        setResult(RESULT_CANCELED, returnIntent);
                        finish();
                    }
                    // Else display error message
                    else {
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
        final Dialog dialog = builder.build(AddContact.this);
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

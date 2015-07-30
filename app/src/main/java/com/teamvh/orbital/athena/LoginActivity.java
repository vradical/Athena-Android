package com.teamvh.orbital.athena;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Project Athena Login Page [Facebook Login]
 */
public class LoginActivity extends Activity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));

        preferences = MainActivity.preferences;
        editor = preferences.edit();

        //INITIALIZE FACEBOOK CALLBACK
        callbackManager = CallbackManager.Factory.create();

        //CREATE FACEBOOK LOGIN BUTTON
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    System.out.println(object);
                                    editor.putString("fbsession", object.getString("id"));
                                    editor.putString("Email", object.getString("email"));
                                    editor.putString("Name", object.getString("name"));
                                    editor.apply();
                                    editor.commit();
                                    loginUser();
                                } catch (JSONException e) {
                                    displayDialog("Failed to connect to Facebook", 0);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
            }
        });

    }

    @Override
    public void onBackPressed() {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //PREPARE QUERY TO LOGIN USER
    public void loginUser() {

        try {
            String userid = preferences.getString("fbsession", "");
            String name = preferences.getString("Name", "");

            // Instantiate Http Request Param Object
            RequestParams params = new RequestParams();

            // When Name Edit View, Email Edit View and Password Edit View have values other than Null
            if (!userid.equals("") || !name.equals("")) {
                // Put Http parameter name with value of Name Edit View control
                params.put("name", name);
                // Put Http parameter username with value of Email Edit View control
                params.put("username", userid);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }
            // when any of the field is empty from token
            else {
                displayDialog("Failed to connect to Facebook", 0);
            }
        } catch (Exception e) {
            displayDialog("Failed to connect to Facebook", 0);
        }

    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeWS(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/login/dologin", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);

                    if (obj.getBoolean("status")) {
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

            @Override
            public void onFinish() {
                finish();
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
                .positiveAction("OK");
        final Dialog dialog = builder.build(LoginActivity.this);
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


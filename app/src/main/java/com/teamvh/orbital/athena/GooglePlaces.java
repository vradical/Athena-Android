package com.teamvh.orbital.athena;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * Created by MSI-YJ on 6/8/2015.
 */
public class GooglePlaces extends ActionBarActivity {

    public int numberOfNok = 0;
    public String sendLocation = null;
    public String[] nokPhoneArray = null;
    public String[][] nokEmailArray = null;
    private SQLControlllerNOK dbcon2;
    protected String mAddressOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        Bundle intent = getIntent().getExtras();

        numberOfNok = Integer.parseInt(intent.getString("numberOfNok"));
        nokPhoneArray = intent.getStringArray("numberOfNokPhoneArray");
        sendLocation = intent.getString("location");



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }


}

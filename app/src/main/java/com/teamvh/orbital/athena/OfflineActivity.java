package com.teamvh.orbital.athena;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class OfflineActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        getSupportActionBar().hide();

        preferences = MainActivity.preferences;

        Spinner spn_label = (Spinner) findViewById(R.id.offline_country);
        final TextView policeNum = (TextView) findViewById(R.id.offline_countryPol);
        final TextView hosNum = (TextView) findViewById(R.id.offline_countryHos);
        final TextView countryName = (TextView) findViewById(R.id.offline_selectedCountry);
        final ArrayList<String> items = new ArrayList<String>();

        final CountryList country = new CountryList();

        for(int i = 0; i < country.getList().size(); i++){
            items.add(country.getList().get(i).getCountryName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spn_label.setAdapter(adapter);
        spn_label.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner spinner, View view, int i, long l) {
                countryName.setText(country.getList().get(i).getCountryName());
                policeNum.setText(country.getList().get(i).getPoliceNum());
                hosNum.setText(country.getList().get(i).getHospitalNum());
            }
        });

        if(!preferences.getString("CountryCode", "").equals("")){
            CountryData curCountry = country.findCountry(preferences.getString("CountryCode", ""));

            if(curCountry != null) {
                countryName.setText(curCountry.getCountryName());
                policeNum.setText(curCountry.getPoliceNum());
                hosNum.setText(curCountry.getHospitalNum());

                for (int i = 0; i < country.getList().size(); i++) {
                    if (curCountry == country.getList().get(i)) {
                        spn_label.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    public void checkConnection(View view){
        if(isOnline()){
            finish();
        }else{
            Dialog.Builder builder = null;
            builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
            ((SimpleDialog.Builder) builder).message("No internet connection found. Please try again later.")
                    .positiveAction("OK")
                    .title("Opps");
            final Dialog dialog = builder.build(OfflineActivity.this);
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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
    }

}

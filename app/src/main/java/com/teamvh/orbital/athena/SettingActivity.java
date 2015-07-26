package com.teamvh.orbital.athena;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    protected SeekBar mTrackingSB;
    protected SeekBar mAlertSB;
    protected SeekBar mAlertCDSB;
    protected SeekBar mEmergencySB;
    protected TextView mTrackingSBTV;
    protected Button mShowAdvanced;
    protected RelativeLayout mAdvancedLayout;
    protected Button mShowAlert;
    protected RelativeLayout mAlertLayout;
    protected Button mShowEmergency;
    protected RelativeLayout mEmergencyLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //set up action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupPage();



        mTrackingSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChanged = i;
                mTrackingSBTV.setText(String.valueOf(i+5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public void setupPage(){
        mTrackingSB = (SeekBar) findViewById(R.id.setting_trackSB);
        mAlertSB = (SeekBar) findViewById(R.id.setting_alertSB);
        mAlertCDSB = (SeekBar) findViewById(R.id.setting_alertCDSB);
        mEmergencySB = (SeekBar) findViewById(R.id.setting_emSB);
        mTrackingSBTV = (TextView) findViewById(R.id.setting_trackSBTV);
        mShowAdvanced = (Button) findViewById(R.id.setting_advanced);
        mAdvancedLayout = (RelativeLayout) findViewById(R.id.setting_advancedLayout);
        mShowAlert = (Button) findViewById(R.id.setting_alertButton);
        mAlertLayout = (RelativeLayout) findViewById(R.id.setting_alert);
        mShowEmergency = (Button) findViewById(R.id.setting_emergencyButton);
        mEmergencyLayout = (RelativeLayout) findViewById(R.id.setting_emergency);

        mShowAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdvancedLayout.isShown()){
                    mAdvancedLayout.setVisibility(View.GONE);
                    mShowAdvanced.setText("Show Advanced Settings");
                }else{
                    mAdvancedLayout.setVisibility(View.VISIBLE);
                    mShowAdvanced.setText("Hide Advanced Settings");
                }
            }
        });

        mShowAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlertLayout.isShown()) {
                    mAlertLayout.setVisibility(View.GONE);
                    mShowAlert.setText("Show Alert Settings");
                } else {
                    mAlertLayout.setVisibility(View.VISIBLE);
                    mShowAlert.setText("Hide Alert Settings");
                }
            }
        });

        mShowEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEmergencyLayout.isShown()){
                    mEmergencyLayout.setVisibility(View.GONE);
                    mShowEmergency.setText("Show Emergency Settings");
                }else{
                    mEmergencyLayout.setVisibility(View.VISIBLE);
                    mShowEmergency.setText("Hide Emergency Settings");
                }
            }
        });
    }
}

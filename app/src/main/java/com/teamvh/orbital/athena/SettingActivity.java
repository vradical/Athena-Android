package com.teamvh.orbital.athena;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    protected SeekBar mTrackingSB;
    protected SeekBar mAlertSB;
    protected SeekBar mAlertCDSB;
    protected SeekBar mEmergencySB;
    protected TextView mTrackingSBTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //set up action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTrackingSB = (SeekBar) findViewById(R.id.setting_trackSB);
        mAlertSB = (SeekBar) findViewById(R.id.setting_alertSB);
        mAlertCDSB = (SeekBar) findViewById(R.id.setting_alertCDSB);
        mEmergencySB = (SeekBar) findViewById(R.id.setting_emSB);
        mTrackingSBTV = (TextView) findViewById(R.id.setting_trackSBTV);

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
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}

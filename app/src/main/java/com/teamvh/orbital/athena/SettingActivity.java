package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

public class SettingActivity extends AppCompatActivity {

    protected TextView mTitleText;

    //Facebook
    protected ProfilePictureView mFBIV;
    protected TextView mUsernameTV;
    protected TextView mEmailTV;
    protected Button mLogout;

    //General
    protected LinearLayout mContactLayout;

    //Advanced
    protected SeekBar mTrackingSB;
    protected SeekBar mAlertSB;
    protected SeekBar mAlertCDSB;
    protected SeekBar mEmergencySB;
    protected TextView mTrackingSBTV;

    protected LinearLayout mShowAdvanced;
    protected TextView mAdvancedStatus;
    protected LinearLayout mAdvancedLayout;


    //Adv Standard
    protected LinearLayout mShowStandard;
    protected TextView mStandardStatus;
    protected LinearLayout mStandardLayout;

    //Adv Alert
    protected LinearLayout mShowAlert;
    protected LinearLayout mAlertLayout;
    protected TextView mAlertStatus;

    //Adv Emergency
    protected LinearLayout mShowEmergency;
    protected LinearLayout mEmergencyLayout;
    protected TextView mEmergencyStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e253f")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitleText = (TextView) findViewById(R.id.mytitle);
        mTitleText.setText("Contacts");

        setupFacebookSetting();

        setupGeneralSetting();

        setupAdvancedSetting();

        mTrackingSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChanged = i;
                mTrackingSBTV.setText(String.valueOf(i + 5));
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

    public void setupFacebookSetting() {
        mFBIV = (ProfilePictureView) findViewById(R.id.profilePicture);
        mUsernameTV = (TextView) findViewById(R.id.setting_fbName);
        mEmailTV = (TextView) findViewById(R.id.setting_fbMail);
        mLogout = (Button) findViewById(R.id.setting_logout);

        Profile profile = Profile.getCurrentProfile();

        mFBIV.setProfileId(profile.getId());
        mUsernameTV.setText(profile.getName());

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage("Logout from Athena?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                LoginManager.getInstance().logOut();
                                Intent i = new Intent(SettingActivity.this, LoginActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(i);
                                overridePendingTransition(0, 0);
                                finish();
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });


    }

    public void setupGeneralSetting() {

        //Contact List
        mContactLayout = (LinearLayout) findViewById(R.id.setting_contactsLL);
        mContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingActivity.this, ContactInfo.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0, 0);
            }
        });

    }

    public void setupAdvancedSetting() {
        mTrackingSB = (SeekBar) findViewById(R.id.setting_trackSB);
        mAlertSB = (SeekBar) findViewById(R.id.setting_alertSB);
        mAlertCDSB = (SeekBar) findViewById(R.id.setting_alertCDSB);
        mEmergencySB = (SeekBar) findViewById(R.id.setting_emSB);
        mTrackingSBTV = (TextView) findViewById(R.id.setting_trackSBTV);

        mShowAdvanced = (LinearLayout) findViewById(R.id.setting_RL2_advanced);
        mAdvancedStatus = (TextView) findViewById(R.id.setting_advancedStatus);
        mAdvancedLayout = (LinearLayout) findViewById(R.id.setting_advancedlayout);

        mShowAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdvancedLayout.isShown()) {
                    mAdvancedLayout.setVisibility(View.GONE);
                    mAdvancedStatus.setText("+");
                } else {
                    mAdvancedLayout.setVisibility(View.VISIBLE);
                    mAdvancedStatus.setText("-");
                }
            }
        });

        //Standard Function
        mShowStandard = (LinearLayout) findViewById(R.id.setting_RL2_header);
        mStandardStatus = (TextView) findViewById(R.id.setting_StandardStatus);
        mStandardLayout = (LinearLayout) findViewById(R.id.setting_standardLayout);

        mShowStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStandardLayout.isShown()) {
                    mStandardLayout.setVisibility(View.GONE);
                    mStandardStatus.setText("+");
                } else {
                    mStandardLayout.setVisibility(View.VISIBLE);
                    mStandardStatus.setText("-");
                }
            }
        });

        //Alert Functions
        mShowAlert = (LinearLayout) findViewById(R.id.setting_RL3_header);
        mAlertLayout = (LinearLayout) findViewById(R.id.setting_alert);
        mAlertStatus = (TextView) findViewById(R.id.setting_alertStatus);

        mShowAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlertLayout.isShown()) {
                    mAlertLayout.setVisibility(View.GONE);
                    mAlertStatus.setText("+");
                } else {
                    mAlertLayout.setVisibility(View.VISIBLE);
                    mAlertStatus.setText("-");
                }
            }
        });

        //Emergency Functions
        mShowEmergency = (LinearLayout) findViewById(R.id.setting_RL4_header);
        mEmergencyLayout = (LinearLayout) findViewById(R.id.setting_emergency);
        mEmergencyStatus = (TextView) findViewById(R.id.setting_emergencyStatus);

        mShowEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEmergencyLayout.isShown()) {
                    mEmergencyLayout.setVisibility(View.GONE);
                    mEmergencyStatus.setText("+");
                } else {
                    mEmergencyLayout.setVisibility(View.VISIBLE);
                    mEmergencyStatus.setText("-");
                }
            }
        });

    }
}

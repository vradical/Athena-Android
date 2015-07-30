package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.Slider;

import org.w3c.dom.Text;

import java.util.Set;

public class SettingActivity extends AppCompatActivity {

    protected TextView mTitleText;
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;

    //Facebook
    protected ProfilePictureView mFBIV;
    protected TextView mUsernameTV;
    protected TextView mEmailTV;
    protected Button mLogout;

    //General
    protected LinearLayout mContactLayout;
    protected LinearLayout mPasscodeLayout;
    protected LinearLayout mInviteFriendsLayout;
    protected LinearLayout mFeedbackLayout;

    //Advanced
    protected LinearLayout mShowAdvanced;
    protected TextView mAdvancedStatus;
    protected LinearLayout mAdvancedLayout;
    protected com.rey.material.widget.Button mSaveButton;
    protected com.rey.material.widget.Button mResetButton;


    //Adv Standard
    protected LinearLayout mShowStandard;
    protected TextView mStandardStatus;
    protected LinearLayout mStandardLayout;
    protected com.rey.material.widget.Slider mTrackingSB;
    protected TextView mTrackingSBTV;
    protected com.rey.material.widget.Slider mTrackingDisSB;
    protected TextView mTrackingDisSBTV;
    protected int curTrack;
    protected int curTrackDis;

    //Adv Alert
    protected LinearLayout mShowAlert;
    protected LinearLayout mAlertLayout;
    protected com.rey.material.widget.Slider mAlertSB;
    protected TextView mAlertSBTV;
    protected com.rey.material.widget.Slider mAlertDisSB;
    protected TextView mAlertDisSBTV;
    protected com.rey.material.widget.Slider mAlertTMSB;
    protected TextView mAlertTMSBTV;
    protected com.rey.material.widget.Slider mAlertCDSB;
    protected TextView mAlertCDSBTV;
    protected TextView mAlertStatus;
    protected int curAlert;
    protected int curAlertDis;
    protected int curAlertTM;
    protected int curAlertCD;

    //Adv Emergency
    protected LinearLayout mShowEmergency;
    protected LinearLayout mEmergencyLayout;
    protected TextView mEmergencyStatus;
    protected com.rey.material.widget.Slider mEmergencySB;
    protected TextView mEmergencySBTV;
    protected com.rey.material.widget.Slider mEmergencyDisSB;
    protected TextView mEmergencyDisSBTV;
    protected int curEM;
    protected int curEMDis;

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

        preferences = MainActivity.preferences;
        editor = preferences.edit();

        setupFacebookSetting();
        setupGeneralSetting();
        setupAdvancedSetting();
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
        mEmailTV.setText(preferences.getString("Email", ""));

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Logout from Athena?")
                        .title("Logout")
                        .positiveAction("YES")
                        .negativeAction("NO");
                final Dialog dialog = builder.build(SettingActivity.this);
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LoginManager.getInstance().logOut();
                        Intent i = new Intent(SettingActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                        finish();
                        dialog.cancel();
                    }
                });
                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });

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

        //Change Passcode
        mPasscodeLayout = (LinearLayout) findViewById(R.id.setting_passcodeLL);
        mPasscodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(SettingActivity.this);
                final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
                final Dialog dialog = new Dialog(SettingActivity.this, 0);
                dialog.contentView(textEntryView);
                dialog.setCanceledOnTouchOutside(false);
                dialog.positiveAction("OK");
                dialog.positiveActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
                dialog.negativeAction("CANCEL");
                dialog.negativeActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
                dialog.title("Please key in your existing passcode to continue.");
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        com.rey.material.widget.EditText mUserText;
                        String strPinCode;
                        mUserText = (com.rey.material.widget.EditText) textEntryView.findViewById(R.id.editTextPasscode);
                        strPinCode = mUserText.getText().toString();

                        if (!strPinCode.equals(preferences.getString("Passcode", ""))) {
                            mUserText.setError("Wrong Passcode");
                        } else {
                            setPasscode();
                            dialog.cancel();
                        }
                    }
                });

                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
                dialog.show();


                /*
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                LayoutInflater inflater = LayoutInflater.from(SettingActivity.this);
                final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
                builder.setTitle("Passcode");
                builder.setMessage("Please key in your existing passcode to continue.");
                builder.setView(textEntryView);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setNegativeButton(android.R.string.cancel, null);
                final AlertDialog dialog = builder.create();

                dialog.show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText mUserText;
                        mUserText = (EditText) textEntryView.findViewById(R.id.editTextPasscode);
                        String strPinCode = mUserText.getText().toString();

                        if (!strPinCode.equals(preferences.getString("Passcode", ""))) {
                            //mErrorText.setVisibility(View.VISIBLE);
                            //mErrorText.setText("Wrong Passcode");
                        } else {
                            setPasscode();
                            dialog.cancel();
                        }
                    }
                });

                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });*/
            }
        });

        //Invite Friends
        mInviteFriendsLayout = (LinearLayout) findViewById(R.id.setting_FBShareLL);
        mInviteFriendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Thank you for your interest in sharing Athena with your friends! However, this function is not available yet.")
                        .title("Opps!")
                        .positiveAction("OK");
                final Dialog dialog = builder.build(SettingActivity.this);
                dialog.show();
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });

            }
        });

        //Feedback
        mFeedbackLayout = (LinearLayout) findViewById(R.id.setting_feedbackLL);
        mFeedbackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Thank you for your interest in improving Athena! However, this function is not available yet.")
                        .title("Opps!")
                        .positiveAction("OK");
                final Dialog dialog = builder.build(SettingActivity.this);
                dialog.show();
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });

            }
        });

    }

    public void setPasscode() {

        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
        final Dialog dialog = new Dialog(this, 0);
        dialog.contentView(textEntryView);
        dialog.setCancelable(false);
        dialog.positiveAction("OK");
        dialog.positiveActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.negativeAction("CANCEL");
        dialog.negativeActionRipple(R.style.Material_Drawable_Ripple_Touch_Light);
        dialog.title("Please key in your new passcode.");
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.rey.material.widget.EditText mUserText;
                String strPinCode;
                mUserText = (com.rey.material.widget.EditText) textEntryView.findViewById(R.id.editTextPasscode);
                strPinCode = mUserText.getText().toString();

                if (strPinCode.equals("")) {
                    mUserText.setError("Password is Required");
                } else {
                    editor.putString("Passcode", strPinCode).commit();
                    dialog.cancel();
                }
            }
        });

        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
        /*
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_emergency_password, null);
        builder.setTitle("Passcode");
        builder.setMessage("Please key in your new passcode.");
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
                //mErrorText = (TextView) textEntryView.findViewById(R.id.PasscodeError);
                String strPinCode = mUserText.getText().toString();

                if (strPinCode.equals("")){
                    //mErrorText.setVisibility(View.VISIBLE);
                    //mErrorText.setText("Passcode cannot by empty.");
                } else {
                    editor.putString("Passcode", strPinCode).commit();
                    dialog.cancel();
                }
            }
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });*/
    }

    public void setupAdvancedSetting() {

        //General Advanced Function
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

        mTrackingSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_trackSB);
        mTrackingSBTV = (TextView) findViewById(R.id.setting_trackSBTV);

        mTrackingDisSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_trackDisSB);
        mTrackingDisSBTV = (TextView) findViewById(R.id.setting_trackDisSBTV);

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

        mAlertSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_alertSB);
        mAlertSBTV = (TextView) findViewById(R.id.setting_alertSBTV);

        mAlertDisSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_alertDisSB);
        mAlertDisSBTV = (TextView) findViewById(R.id.setting_alertDisSBTV);

        mAlertTMSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_alertTMSB);
        mAlertTMSBTV = (TextView) findViewById(R.id.setting_alertTMSBTV);

        mAlertCDSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_alertCDSB);
        mAlertCDSBTV = (TextView) findViewById(R.id.setting_alertCDSBTV);

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

        mEmergencySB = (com.rey.material.widget.Slider) findViewById(R.id.setting_emSB);
        mEmergencySBTV = (TextView) findViewById(R.id.setting_emSBTV);

        mEmergencyDisSB = (com.rey.material.widget.Slider) findViewById(R.id.setting_emDisSB);
        mEmergencyDisSBTV = (TextView) findViewById(R.id.setting_emDisSBTV);

        //update settings;
        updateSetting();

        //Button to save and reset
        mSaveButton = (com.rey.material.widget.Button) findViewById(R.id.setting_btnsave);
        mResetButton = (com.rey.material.widget.Button) findViewById(R.id.setting_btnreset);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putInt("CHECK_INTERVAL", curTrack*60000);
                editor.putFloat("SMALLEST_DISPLACEMENT", curTrackDis);
                editor.putInt("HA_CHECK_INTERVAL", curAlert*60000);
                editor.putFloat("HA_SMALLEST_DISPLACEMENT", curAlertDis);
                editor.putInt("EM_CHECK_INTERVAL", curEM*1000);
                editor.putFloat("EM_SMALLEST_DISPLACEMENT", curEMDis);
                editor.putInt("ALERT_TIMER", curAlertTM*60000);
                editor.putInt("ALERT_COUNTDOWN", curAlertCD* 1000);
                editor.apply();
                editor.commit();

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Setting Saved.")
                        .title("Status")
                        .positiveAction("OK");
                final Dialog dialog = builder.build(SettingActivity.this);
                dialog.show();

                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Do you want to reset the advanced setting to default?")
                        .title("Reset Settings")
                        .positiveAction("YES")
                        .negativeAction("NO");
                final Dialog dialog = builder.build(SettingActivity.this);
                dialog.show();

                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editor.putInt("CHECK_INTERVAL", Constants.CHECK_INTERVAL);
                        editor.putFloat("SMALLEST_DISPLACEMENT", Constants.SMALLEST_DISPLACEMENT);
                        editor.putInt("HA_CHECK_INTERVAL", Constants.HA_CHECK_INTERVAL);
                        editor.putFloat("HA_SMALLEST_DISPLACEMENT", Constants.HA_SMALLEST_DISPLACEMENT);
                        editor.putInt("EM_CHECK_INTERVAL", Constants.EM_CHECK_INTERVAL);
                        editor.putFloat("EM_SMALLEST_DISPLACEMENT", Constants.EM_SMALLEST_DISPLACEMENT);
                        editor.putInt("ALERT_TIMER", Constants.ALERT_TIMER);
                        editor.putInt("ALERT_COUNTDOWN", Constants.ALERT_COUNTDOWN);
                        editor.apply();
                        editor.commit();
                        dialog.cancel();

                        updateSetting();

                        Dialog.Builder builder2 = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                        ((SimpleDialog.Builder) builder2).message("Settings has been reset.")
                                .positiveAction("OK");
                        final Dialog dialog2 = builder2.build(SettingActivity.this);
                        dialog2.show();
                        dialog2.setCanceledOnTouchOutside(false);
                        dialog2.positiveActionClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog2.cancel();
                            }
                        });

                    }
                });
                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });


            }
        });

    }

    private void updateSetting(){
        int trackingSB = preferences.getInt("CHECK_INTERVAL", 0)/60000;
        curTrack = trackingSB;
        mTrackingSBTV.setText(String.valueOf(trackingSB));
        mTrackingSB.setValue(trackingSB, false);
        mTrackingSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mTrackingSBTV.setText(String.valueOf(i));
                curTrack = i;
            }
        });

        float trackingDisSB = preferences.getFloat("SMALLEST_DISPLACEMENT", 0);
        curTrackDis = (int) trackingDisSB;
        mTrackingDisSBTV.setText(String.valueOf(trackingDisSB));
        mTrackingDisSB.setValue(trackingDisSB, false);
        mTrackingDisSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mTrackingDisSBTV.setText(String.valueOf(i));
                curTrackDis = i;
            }
        });

        int alertSB = (preferences.getInt("HA_CHECK_INTERVAL", 0)/60000);
        curAlert = alertSB;
        mAlertSBTV.setText(String.valueOf(alertSB));
        mAlertSB.setValue(alertSB, false);
        mAlertSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mAlertSBTV.setText(String.valueOf(i));
                curAlert = i;
            }
        });

        float alertDisSB = preferences.getFloat("HA_SMALLEST_DISPLACEMENT", 0);
        curAlertDis = (int) alertDisSB;
        mAlertDisSBTV.setText(String.valueOf(alertDisSB));
        mAlertDisSB.setValue(alertDisSB, false);
        mAlertDisSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mAlertDisSBTV.setText(String.valueOf(i));
                curAlertDis = i;
            }
        });

        int alertTMSB = (preferences.getInt("ALERT_TIMER", 0) / 60000);
        curAlertTM = alertTMSB;
        mAlertTMSBTV.setText(String.valueOf(alertTMSB));
        mAlertTMSB.setValue(alertTMSB, false);
        mAlertTMSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mAlertTMSBTV.setText(String.valueOf(i));
                curAlertTM = i;
            }
        });

        int alertCDSB = preferences.getInt("ALERT_COUNTDOWN", 0) / 1000;
        curAlertCD = alertCDSB;
        mAlertCDSBTV.setText(String.valueOf(alertCDSB));
        mAlertCDSB.setValue(alertCDSB, false);
        mAlertCDSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mAlertCDSBTV.setText(String.valueOf(i));
                curAlertCD = i;
            }
        });

        int emergencySB = preferences.getInt("EM_CHECK_INTERVAL", 0) / 1000;
        curEM = emergencySB;
        mEmergencySBTV.setText(String.valueOf(emergencySB));
        mEmergencySB.setValue(emergencySB, false);
        mEmergencySB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mEmergencySBTV.setText(String.valueOf(i));
                curEM = i;
            }
        });

        float emergencyDisSB = preferences.getFloat("EM_SMALLEST_DISPLACEMENT", 0);
        curEMDis = (int) emergencyDisSB;
        mEmergencyDisSBTV.setText(String.valueOf(emergencyDisSB));
        mEmergencyDisSB.setValue(emergencyDisSB, false);
        mEmergencyDisSB.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                mEmergencyDisSBTV.setText(String.valueOf(i));
                curEMDis = i;
            }
        });
    }

    public void displayDialog(String message, int i) {

        if (i == 1) {
            message = "Unable to get information from server.";
        } else if (i == 2) {
            message = "Unable to connect to server.";
        }

        Dialog.Builder builder = null;
        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
        ((SimpleDialog.Builder) builder).message(message)
                .positiveAction("OK");
        final Dialog dialog = builder.build(SettingActivity.this);
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

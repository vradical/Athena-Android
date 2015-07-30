package com.teamvh.orbital.athena;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ron on 28-Jul-15.
 */
public class DangerZoneAdapter extends ArrayAdapter<DangerZoneData> {

    ArrayList<DangerZoneData> dangerZoneList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    SharedPreferences preferences;

    public DangerZoneAdapter(Context context, int resource, ArrayList<DangerZoneData> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = MainActivity.preferences;
        Resource = resource;
        dangerZoneList = objects;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.tvDZID = (TextView) v.findViewById(R.id.tvDZID);
            holder.tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            holder.tvInfo = (TextView) v.findViewById(R.id.tvInfo);
            holder.tvDate = (TextView) v.findViewById(R.id.tvDate);
            holder.ibDeleteDZ = (ImageButton) v.findViewById(R.id.btnDeleteZone);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvDZID.setText(String.valueOf(position + 1) + ". ");
        holder.tvTitle.setText(dangerZoneList.get(position).getTitle());
        holder.tvInfo.setText(dangerZoneList.get(position).getAddinfo());
        holder.tvDate.setText(dangerZoneList.get(position).getDateTime());

        holder.ibDeleteDZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectPos = position + 1;

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Delete danger zone " + selectPos + " from your list?")
                        .positiveAction("YES")
                        .negativeAction("NO")
                .title("Delete Zone");
                final Dialog dialog = builder.build(getContext());
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteDZ(position);
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

        return v;
    }

    static class ViewHolder {
        public TextView tvDZID;
        public TextView tvTitle;
        public TextView tvInfo;
        public TextView tvDate;
        public ImageButton ibDeleteDZ;
    }

    public void deleteDZ(int position) {
        String uname = preferences.getString("fbsession", "");
        RequestParams params = new RequestParams();
        if (!uname.equals("")) {
            params.put("username", uname);
            params.put("latitude", dangerZoneList.get(position).getLatitude());
            params.put("longitude", dangerZoneList.get(position).getLongitude());
            invokeDeleteContact(params, position);
        } else {
            Toast.makeText(getContext(), "Delete Zone : Username null", Toast.LENGTH_LONG).show();
        }
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeDeleteContact(RequestParams params, final int position) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/dangerzone/deletedz", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        dangerZoneList.remove(position);
                        MainActivity.country = "restart";

                        Dialog.Builder builder = null;
                        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                        ((SimpleDialog.Builder) builder).message("Danger zone delete successfully.")
                                .positiveAction("OK")
                                .title("Delete Danger Zone");
                        final Dialog dialog = builder.build(getContext());
                        dialog.show();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.positiveActionClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });


                    }
                    // Else display error message
                    else {
                        displayDialog("", 1);
                    }
                } catch (JSONException e) {
                    displayDialog("", 1);

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
                notifyDataSetChanged();
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
        final Dialog dialog = builder.build(getContext());
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
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ron on 09-Jul-15.
 */
public class ContactAdapter extends ArrayAdapter<ContactData> {

    ArrayList<ContactData> contactList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    SharedPreferences preferences;

    public ContactAdapter(Context context, int resource, ArrayList<ContactData> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = MainActivity.preferences;
        Resource = resource;
        contactList = objects;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.tvName = (TextView) v.findViewById(R.id.tvName);
            holder.tvEmail = (TextView) v.findViewById(R.id.tvEmail);
            holder.tvCountry = (TextView) v.findViewById(R.id.tvCountry);
            holder.tvPhone = (TextView) v.findViewById(R.id.tvPhone);
            holder.ibCallContact = (ImageButton) v.findViewById(R.id.btnCallC);
            holder.ibDeleteContact = (ImageButton) v.findViewById(R.id.btnDeleteC);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvName.setText(contactList.get(position).getName());
        holder.tvEmail.setText(contactList.get(position).getEmail());
        holder.tvCountry.setText(contactList.get(position).getCountry());
        holder.tvPhone.setText(contactList.get(position).getPhone());
        holder.ibCallContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + contactList.get(position).getCountry() +
                            contactList.get(position).getPhone()));
                    getContext().startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.e("Calling a Phone Number", "Call failed", activityException);
                }
            }
        });

        holder.ibDeleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                ((SimpleDialog.Builder) builder).message("Delete " + contactList.get(position).getName() + " from your contact list?")
                        .positiveAction("YES")
                        .negativeAction("NO")
                        .title("Delete Contact");
                final Dialog dialog = builder.build(getContext());
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteContact(position);
                        contactList.remove(position);
                        notifyDataSetChanged();
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
        public TextView tvName;
        public TextView tvEmail;
        public TextView tvCountry;
        public TextView tvPhone;
        public ImageButton ibCallContact;
        public ImageButton ibDeleteContact;
    }

    public void deleteContact(int position) {
        String uname = preferences.getString("fbsession", "");
        RequestParams params = new RequestParams();
        if (!uname.equals("")) {
            params.put("username", uname);
            params.put("email", contactList.get(position).getEmail());
            params.put("phone", contactList.get(position).getPhone());
            invokeDeleteContact(params);
        } else {
            Toast.makeText(getContext(), "Delete Contact : Username null", Toast.LENGTH_LONG).show();
        }
    }

    //SEND QUERY TO ATHENA WEB SERVICE
    public void invokeDeleteContact(RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://119.81.223.180:8080/ProjectAthenaWS/contacts/deletecontact", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {

                        Dialog.Builder builder = null;
                        builder = new SimpleDialog.Builder(R.style.SimpleDialogLight);
                        ((SimpleDialog.Builder) builder).message("Contact delete successfully.")
                                .positiveAction("OK")
                                .title("Delete Contact");
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

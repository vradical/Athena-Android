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

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Delete " + contactList.get(position).getName() + " from your contact list?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                deleteContact(position);
                                contactList.remove(position);
                                notifyDataSetChanged();
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
                        Toast.makeText(getContext(), "Contact Deleted!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    // Else display error message
                    else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getContext(), "Error Occured Deleting Contact!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getContext(), "GetEMID : Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getContext(), "Get EMID : Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getContext(), " Get EMID : Unexpected Error occcured!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {
            }

        });
    }

}

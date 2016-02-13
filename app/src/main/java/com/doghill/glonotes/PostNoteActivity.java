package com.doghill.glonotes;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_note);
    }

    public void postNote(View view) {
        EditText mEditSubject = (EditText) findViewById(R.id.editSubject);
        EditText mEditTextMessage = (EditText) findViewById(R.id.editTextMessage);
        final String subject = mEditSubject.getText().toString().trim();
        final String textmessage = mEditTextMessage.getText().toString().trim();
        final String API_TOKEN_HEADER = "Token " + MainActivity.API_KEY;

        // create note being posted progress dialog
        final ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Posting your note. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // create note posted successfully dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your note was posted successfully!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.setTitle("Note Posted!");

        // create note failed to post dialog
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage("Oops!  Note failed to post.  Please try again.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert2 = builder2.create();
        alert2.setTitle("Note Post Failed");


        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.POST_NOTE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject note_posted = new JSONObject(response);
                            String note_posted_check = note_posted.getString("detail");
                            if (note_posted_check.equals("NOTE POSTED")) {
                                dialog.cancel();
                                alert.show();
                            }

                            else {
                                dialog.cancel();
                                alert2.show();
                            }
                        }

                        catch(JSONException e) {
                            dialog.cancel();
                            alert2.show();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(PostNoteActivity.this, error.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                        dialog.cancel();
                        alert2.show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(MainActivity.KEY_SUBJECT, subject);
                params.put(MainActivity.KEY_TEXTMESSAGE, textmessage);
                params.put(MainActivity.KEY_LATITUDE, MainActivity.latitude);
                params.put(MainActivity.KEY_LONGITUDE, MainActivity.longitude);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", API_TOKEN_HEADER);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}

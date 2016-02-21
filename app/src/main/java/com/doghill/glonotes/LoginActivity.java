package com.doghill.glonotes;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void sendLoginCredentials(View view) {

        EditText mEditUsername = (EditText) findViewById(R.id.editUsername);
        EditText mEditPassword = (EditText) findViewById(R.id.editPassword);
        final String username = mEditUsername.getText().toString().trim();
        final String password = mEditPassword.getText().toString().trim();

        // create logging in progress dialog
        final ProgressDialog logging_in_dialog = new ProgressDialog(this); // this = YourActivity
        logging_in_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        logging_in_dialog.setMessage("We're trying to log you in.  Please wait...");
        logging_in_dialog.setIndeterminate(true);
        logging_in_dialog.setCanceledOnTouchOutside(false);


        // create error logging in alert
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Error logging in!!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        final AlertDialog error_logging_in_alert = builder1.create();
        error_logging_in_alert.setTitle("Oops!");

        logging_in_dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.GET_API_TOKEN_URL,
                new Response.Listener<String>() {
                    // Grab API auth response
                    @Override
                    public void onResponse(String response) {
                        logging_in_dialog.cancel();
                        storeAPIKey(response);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        logging_in_dialog.cancel();
                        String body = null;
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                body = new String(error.networkResponse.data, "UTF-8");
                                try {
                                    JSONObject response_json = new JSONObject(body);
                                    if (response_json.has("non_field_errors")) {
                                        JSONArray error_jsonarray = response_json.getJSONArray("non_field_errors");


                                        for (int i = 0; i < error_jsonarray.length(); i++) {
                                            String non_field_error = error_jsonarray.getString(0);
                                            Log.d("Type", error_jsonarray.getString(0));
                                            //  Toast toast3 = Toast.makeText(CreateAccountActivity.this, username_error, Toast.LENGTH_LONG);
                                            //  toast3.show();
                                            error_logging_in_alert.setMessage(non_field_error);
                                            error_logging_in_alert.show();
                                        }
                                    }
                                } catch (JSONException e) {
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        error_logging_in_alert.show();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, username);
                params.put(KEY_PASSWORD, password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // store api key in shared preferences
    public void storeAPIKey(String API_KEY) {
        String tokenString = null;

        try {
            JSONObject jsonToken = new JSONObject(API_KEY);
            tokenString = jsonToken.getString("token");
        }

        catch(JSONException e) {
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("glonotes-token", tokenString);
        editor.apply();

    }
    public void startCreateAccountActivity(View view) {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        finish();
        startActivity(intent);
    }
}

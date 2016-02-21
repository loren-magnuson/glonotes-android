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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
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

public class CreateAccountActivity extends AppCompatActivity {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }


    public void sendRegistrationCredentials(View view) {
        EditText mEditUsername = (EditText) findViewById(R.id.editUsername);
        EditText mEditPassword = (EditText) findViewById(R.id.editPassword);
        EditText mEditPasswordRepeat = (EditText) findViewById(R.id.editPasswordRepeat);
        EditText mEditEmailAddress = (EditText) findViewById(R.id.editEmailAddress);

        final String username = mEditUsername.getText().toString().trim();
        final String password = mEditPassword.getText().toString().trim();
        final String password_repeat = mEditPasswordRepeat.getText().toString().trim();
        final String email_address = mEditEmailAddress.getText().toString().trim();


        // create note being posted progress dialog
        final ProgressDialog creating_account_dialog = new ProgressDialog(this); // this = YourActivity
        creating_account_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        creating_account_dialog.setMessage("Creating your account.  Please wait...");
        creating_account_dialog.setIndeterminate(true);
        creating_account_dialog.setCanceledOnTouchOutside(false);


        // create note posted successfully dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Your account was created succesfully!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

        final AlertDialog account_created_alert = builder1.create();
        account_created_alert.setTitle("Account created!");

        // create account creation failed alert
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

        //gets reset to specific error from django
        builder2.setMessage("Account creation failed!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        final AlertDialog account_failed_alert = builder2.create();
        account_failed_alert.setTitle("Account Creation Failed!");


        // create passwords don't match alert
        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setMessage("Your passwords don't match!  Try again.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        final AlertDialog passwords_dont_match_alert = builder3.create();
        passwords_dont_match_alert.setTitle("Oops!");

        if (password.equals(password_repeat)) {
            creating_account_dialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.USER_REGISTRATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                   //     Toast toast = Toast.makeText(CreateAccountActivity.this, response, Toast.LENGTH_SHORT);
                   //     toast.show();
                        try {
                            JSONObject response_json = new JSONObject(response);
                            if (response_json.has("registration_status")) {
                                String registration_status = response_json.getString("registration_status");
                                if (registration_status.equals("Account created!")) {
                                    creating_account_dialog.cancel();
                                    account_created_alert.show();
                                }
                            }
                        }
                        catch(JSONException e) {}
                        }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String body = null;
                        if (error.networkResponse!= null && error.networkResponse.data!=null) {
                            try {
                                body = new String(error.networkResponse.data,"UTF-8");
                                try {
                                    JSONObject response_json = new JSONObject(body);
                                    if (response_json.has("username")) {
                                        JSONArray error_jsonarray = response_json.getJSONArray("username");

                                        for (int i = 0; i < error_jsonarray.length(); i++) {
                                            String username_error = error_jsonarray.getString(0);Log.d("Type", error_jsonarray.getString(0));
                                          //  Toast toast3 = Toast.makeText(CreateAccountActivity.this, username_error, Toast.LENGTH_LONG);
                                          //  toast3.show();
                                            creating_account_dialog.cancel();

                                            account_failed_alert.setMessage(username_error);
                                            account_failed_alert.show();
                                         }
                                    }
                                }
                                catch (JSONException e){
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        //Toast toast1 = Toast.makeText(CreateAccountActivity.this, error.toString(), Toast.LENGTH_SHORT);
                        //toast1.show();
                       // Toast toast2 = Toast.makeText(CreateAccountActivity.this, body, Toast.LENGTH_SHORT);
                      //  toast2.show();
                    }

                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, username);
                params.put(KEY_PASSWORD, password);
                params.put(KEY_EMAIL, email_address);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        }

        else {
            passwords_dont_match_alert.show();
        }
    }

}

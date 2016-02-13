package com.doghill.glonotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.GET_API_TOKEN_URL,
                new Response.Listener<String>() {
                    // Grab API auth response
                    @Override
                    public void onResponse(String response) {
                        storeAPIKey(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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

        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }
}

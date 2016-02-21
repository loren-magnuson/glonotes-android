package com.doghill.glonotes;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location old_location = null;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static String API_KEY = null;
    public static String API_TOKEN_HEADER = null;

    public static final String USER_REGISTRATION_URL = "http://192.168.0.7:8000/auth/users/register/";
    public static final String GET_API_TOKEN_URL = "http://192.168.0.7:8000/api-token-auth/";
    public static final String UPDATE_COORDINATES_URL = "http://192.168.0.7:8000/auth/update_coordinates/";
    public static final String POST_NOTE_URL = "http://192.168.0.7:8000/auth/post_note/";
    public static final String GET_NOTES_URL = "http://192.168.0.7:8000/auth/get_notes/";
    public static final String GET_SINGLE_NOTE_URL = "http://192.168.0.7:8000/auth/get_note/";
    public static final String MEDIA_ROOT = "http://192.168.0.7:8000/media/";
    public static final String DELETE_NOTE_URL = "http://192.168.0.7:8000/auth/delete_note/";


/*    public static final String USER_REGISTRATION_URL = "http://getbusychild.com:8000/auth/users/register/";
    public static final String GET_API_TOKEN_URL = "http://getbusychild.com:8000/api-token-auth/";
    public static final String UPDATE_COORDINATES_URL = "http://getbusychild.com:8000/auth/update_coordinates/";
    public static final String POST_NOTE_URL = "http://getbusychild.com:8000/auth/post_note/";
    public static final String GET_NOTES_URL = "http://getbusychild.com:8000/auth/get_notes/";
    public static final String GET_SINGLE_NOTE_URL = "http://getbusychild.com:8000/auth/get_note/";
    public static final String MEDIA_ROOT = "http://getbusychild.com:8000/media/";
    public static final String DELETE_NOTE_URL = "http://getbusychild.com:8000/auth/delete_note/";
*/

    public static final String KEY_SUBJECT = "subject";
    public static final String KEY_TEXTMESSAGE = "textMessage";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static String latitude = null;
    public static String longitude = null;
    public static Location current_location;
    public static Location initial_location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);


        // if API token exists go to main activity
        if (preferences.contains("glonotes-token")) {
            API_KEY = getStoredAPIKey();
            API_TOKEN_HEADER = "Token " + API_KEY;
            setContentView(R.layout.activity_main);
        }

        // else if no API  token exists go to login activity
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getStoredAPIKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("glonotes-token", "");
        return name;
    }

    public void clearAPIKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().remove("glonotes-token").apply();
    }

    public void glonotesLogout(View view) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        clearAPIKey();
        Context context = getApplicationContext();
        Intent intent = new Intent(context, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    public void sendNewCoordinates(String latitude_string, String longitude_string) {
        final String KEY_LATITUDE = "latitude";
        final String KEY_LONGITUDE = "longitude";
        final String API_TOKEN_HEADER = "Token " + API_KEY;
        final String latitude = latitude_string;
        final String longitude = longitude_string;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_COORDINATES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                      //  Toast toast = Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT);
                      //  toast.show();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_LATITUDE, latitude);
                params.put(KEY_LONGITUDE, longitude);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", API_TOKEN_HEADER);
                Log.d("param", "params" + params.get("Authorization"));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void enableLocationServices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        builder.setMessage("GloNotes Requires Location Services.  Would you like to enable Location services now?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Enable Location Services?");
        alert.show();
    }


    @Override
    public void onConnected(Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        // Check if location permission is granted
        if (permissionCheck == 0) {
            // If needed, guide user to enable location services

            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                enableLocationServices();
            }
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            // If location is null
            if (location == null) {
            }

            // If location is new
            else {
                handleNewLocation(location);
            }
        }
        else {
            //TODO handle permission not granted
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, "Current Location" + location.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        CharSequence testToast = "location services connection failed";
        Toast toast = Toast.makeText(MainActivity.this, testToast, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CharSequence testToast1 = "location services still connected";


        if (mGoogleApiClient.isConnected()) {
           // Toast toast = Toast.makeText(MainActivity.this, testToast1, Toast.LENGTH_SHORT);
          //  toast.show();
        }

        else {
            mGoogleApiClient.connect();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude_double = location.getLatitude();
        double longitude_double = location.getLongitude();

        String latitude_string = Double.toString(latitude_double);
        String longitude_string = Double.toString(longitude_double);

        // set initial value for old/previous location
        if (old_location == null) {
            old_location = new Location("old location");
            old_location.setLatitude(latitude_double);
            old_location.setLongitude(longitude_double);
        }

        else {
            double old_latitude_double = old_location.getLatitude();
            double old_longitude_double = old_location.getLongitude();
            String old_latitude_string = Double.toString(old_latitude_double);
            String old_longitude_string = Double.toString(old_longitude_double);
            float distance_from_old_location = old_location.distanceTo(location);
            String distance_from_old_location_string = Float.toString(distance_from_old_location);
        }

        old_location.setLatitude(latitude_double);
        old_location.setLongitude(longitude_double);
        latitude = latitude_string;
        longitude = longitude_string;
        current_location = location;
        sendNewCoordinates(latitude_string, longitude_string);
    }



    public void showAreaNotes(View view) {

        Context context = getApplicationContext();
        Intent intent = new Intent(context, NotesList.class);
        startActivity(intent);
    }

    public void showAreaMap(View view) {

        Context context = getApplicationContext();
        initial_location = current_location;
        Intent intent = new Intent(context, MapsActivity.class);

        startActivity(intent);
    }

    public void showPostNoteActivity(View view) {

        Context context = getApplicationContext();
        Intent intent = new Intent(context, PostNoteActivity.class);
        //finish();
        startActivity(intent);
    }

    /*    public void startUpdatingCoordinates() {
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                if (preferences.contains("glonotes-token")) {
                                }
                                else { return; }
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }*/

}



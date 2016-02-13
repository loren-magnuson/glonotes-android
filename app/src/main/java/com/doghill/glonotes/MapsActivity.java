package com.doghill.glonotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> noteMarkers = new ArrayList<Marker>();
    private List<JSONObject> newNotes = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.notes_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        double current_latitude = MainActivity.current_location.getLatitude();
        double current_longitude = MainActivity.current_location.getLongitude();
        LatLng current_location = new LatLng(current_latitude, current_longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (noteMarkers.contains(marker)) {
                    int marker_index = noteMarkers.indexOf(marker);
                    Log.d("new notes length", "new notes length" + Integer.toString(newNotes.size()));

                    try {

                        JSONObject note = newNotes.get(marker_index);
                        Context context = getApplicationContext();
                        Intent intent = new Intent(context, SingleNoteActivity.class);
                        intent.putExtra("subject", note.getString("subject"));
                        intent.putExtra("textMessage", note.getString("textMessage"));
                        intent.putExtra("author", note.getString("author"));
                        startActivity(intent);

                    } catch (JSONException e) {
                        Toast toast = Toast.makeText(MapsActivity.this, e.toString(), Toast.LENGTH_SHORT);
                        toast.show();

                    }


                } else {
                }
            }
        });
        startRefreshingMap();
    }

    public List<JSONObject> parseNotesJSON(String response) {
        List<JSONObject> newNotesObject = new ArrayList<JSONObject>();
        newNotes = new ArrayList<JSONObject>();
        try {
            JSONObject newNotesJSON = new JSONObject(response);
            Iterator<String> iter = newNotesJSON.keys();

            while (iter.hasNext()) {
                String key = iter.next();

                try {
                    JSONObject note = newNotesJSON.getJSONObject(key);
                    newNotes.add(note);
                }

                catch (JSONException e) {
                    Toast toast = Toast.makeText(MapsActivity.this, e.toString(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            newNotesObject = newNotes;
        }

        catch(JSONException e) {

        }
        return newNotesObject;
    }

    public void getNewNotes() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, MainActivity.GET_NOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<JSONObject> newNotes = parseNotesJSON(response);
                        updateMap(newNotes);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(MapsActivity.this, error.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", MainActivity.API_TOKEN_HEADER);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void updateMap(List<JSONObject> newNotes) {
        mMap.clear();
        for (JSONObject note : newNotes) {

            try {
                String latitude_string = note.getString("latitude");
                String longitude_string = note.getString("longitude");
                String note_subject = note.getString("subject");

                try {
                    double latitude  = Double.parseDouble(latitude_string);
                    double longitude  = Double.parseDouble(longitude_string);
                    LatLng note_location = new LatLng(latitude, longitude);
                    MarkerOptions noteMarkerOptions = new MarkerOptions().position(note_location).title(note_subject);

                    Marker marker = mMap.addMarker(noteMarkerOptions);
                    marker.showInfoWindow();
                    noteMarkers.add(marker);

                } catch (NumberFormatException e) {
                }
            }
            catch(JSONException e) {}

        }
    }
    public void startRefreshingMap() {
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {

                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                getNewNotes();
                            }
                        });
                        Thread.sleep(60000);


                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }
}

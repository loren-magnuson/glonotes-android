package com.doghill.glonotes;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NotesList extends ListActivity {

    public static final String KEY_NOTE_ID = "note_id";

    private List<String> noteIDs = new ArrayList<String>();
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ListView listView = getListView();
        setContentView(R.layout.activity_noteslist);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);
    }

    public void addItems(View v,  List<JSONObject> newNotes) {
        for (JSONObject note_json : newNotes) {
            try {
                listItems.add(note_json.getString("subject"));
                adapter.notifyDataSetChanged();
                noteIDs.add(note_json.getString("note_id"));
            } catch (JSONException e) {

            }

        }
    }

    public void clearItems(View v) {
        adapter.clear();
    }

    public List<JSONObject> parseNotesJSON(String response) {
        List<JSONObject> newNotesObject = new ArrayList<JSONObject>();
        try {
            JSONObject newNotesJSON = new JSONObject(response);
            List<JSONObject> newNotes = new ArrayList<JSONObject>();
            Iterator<String> iter = newNotesJSON.keys();

            while (iter.hasNext()) {
                String key = iter.next();

                try {
                    JSONObject note = newNotesJSON.getJSONObject(key);
                    newNotes.add(note);
                }

                catch (JSONException e) {
                    Toast toast = Toast.makeText(NotesList.this, e.toString(), Toast.LENGTH_SHORT);
                    toast.show();                }
            }
            newNotesObject = newNotes;
        }

        catch(JSONException e) {
        }
        return newNotesObject;
    }

    public void getNewNotes(View v) {
        final View view = v;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, MainActivity.GET_NOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<JSONObject> newNotes = parseNotesJSON(response);
                        clearItems(view);
                        addItems(view, newNotes);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(NotesList.this, error.toString(), Toast.LENGTH_SHORT);
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
    public void openSingleNote(String response) {
        Intent intent = new Intent(NotesList.this, SingleNoteActivity.class);
        try {
            JSONObject note = new JSONObject(response);
            intent.putExtra("subject", note.getString("subject"));
            intent.putExtra("textMessage", note.getString("textMessage"));
            intent.putExtra("author", note.getString("author"));
        }

        catch (JSONException e) {
            Toast toast = Toast.makeText(NotesList.this, e.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }
        startActivity(intent);
    }

    public void getSingleNote(int position) {
        final String note_id = noteIDs.get(position);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.GET_SINGLE_NOTE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                          openSingleNote(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(NotesList.this, error.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_NOTE_ID, note_id);

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

    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        getSingleNote(position);
        super.onListItemClick(listView, v, position, id);
    }

}
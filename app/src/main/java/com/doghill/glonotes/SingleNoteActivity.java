package com.doghill.glonotes;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SingleNoteActivity extends AppCompatActivity {
    public static String note_id;
    public static final String KEY_NOTE_ID = "note_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_note);
        Bundle extras = getIntent().getExtras();
        Button deleteButton = (Button) findViewById(R.id.deleteButton);

        if (extras.getString("is_owner").equals("TRUE")) {
            deleteButton.setVisibility(View.VISIBLE);
        }
        else {
            deleteButton.setVisibility(View.INVISIBLE);
        }

        note_id = extras.getString("note_id");
        TextView mViewSubject = (TextView) findViewById(R.id.textSubject);
        TextView mViewMessageBody = (TextView) findViewById(R.id.textMessageBody);
        TextView mViewMessageAuthor = (TextView) findViewById(R.id.textMessageAuthor);
        ImageView mViewMessageImage = (ImageView) findViewById(R.id.imageView);
        mViewSubject.setText(extras.getString("subject"));
        mViewMessageBody.setText(extras.getString("textMessage"));
        mViewMessageAuthor.setText(extras.getString("author"));
        String image_filename = extras.getString("image_filename");

        if (image_filename.equals("None")) {

        } else {
            // create note being posted progress dialog
            final ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Downloading image...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            String url = MainActivity.MEDIA_ROOT + extras.getString("author") + "/" + image_filename;
            Picasso.with(this)
                    .load(url)
                    .fit()
                    .centerCrop()
                    .rotate(90f)
                    .into(mViewMessageImage, new Callback() {

                        @Override
                        public void onSuccess() {
                            dialog.cancel();

                        }

                        @Override
                        public void onError() {
                            //TODO handle error loading image

                        }
                    });
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        System.gc();
    }
    public void deleteNote(View view) {
        // create deleting note progress dialog
        final ProgressDialog deleting_note_dialog = new ProgressDialog(this); // this = YourActivity
        deleting_note_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        deleting_note_dialog.setMessage("Deleting note.  Please wait...");
        deleting_note_dialog.setIndeterminate(true);
        deleting_note_dialog.setCanceledOnTouchOutside(false);


        // create note deleted successfully dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Your note was deleted!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

        final AlertDialog note_deleted_alert = builder1.create();
        note_deleted_alert.setTitle("Note Deleted!");

        // create note deleted successfully dialog
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage("This note was not deleted.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        final AlertDialog error_deleting_note_alert = builder2.create();
        error_deleting_note_alert.setTitle("Oops!");

        deleting_note_dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.DELETE_NOTE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        deleting_note_dialog.cancel();
                        note_deleted_alert.show();
                        //  Toast toast = Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT);
                        //  toast.show();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(SingleNoteActivity.this, error.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                        deleting_note_dialog.cancel();
                        error_deleting_note_alert.show();
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
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", MainActivity.API_TOKEN_HEADER);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

     /*   StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.DELETE_NOTE_URL,

            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    deleting_note_dialog.cancel();
                    note_deleted_alert.show();
                    }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    deleting_note_dialog.cancel();
                    error_deleting_note_alert.show();
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
*/


}

package com.doghill.glonotes;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
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
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostNoteActivity extends AppCompatActivity {
    public static List<String> imgPaths = null;


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

        if (imgPaths != null) {
            String charset = "UTF-8-";
            String requestURL = MainActivity.POST_NOTE_URL;

            try {
                FileUploader multipart = new FileUploader(PostNoteActivity.this, requestURL, charset, imgPaths, subject, textmessage);
                multipart.execute();

            } catch (IOException e) {
                System.err.println(e);
            }
        }

        else {
            dialog.show();
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
                                } else {
                                    dialog.cancel();
                                    alert2.show();
                                }
                            } catch (JSONException e) {
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", API_TOKEN_HEADER);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
      //  Toast toast = Toast.makeText(PostNoteActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT);
     //   toast.show();
        Log.d("tag", "Image filepath" + mCurrentPhotoPath);
        imgPaths = new ArrayList<String>();
        imgPaths.add(mCurrentPhotoPath);
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("tag", "photo dispatch" + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            else {
                Log.d("tag", "file creation failed");

            }
        }
    }
/*
    public void testPost(View view) {
        Toast.makeText(getApplicationContext(),"test post",Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),imgPaths.get(0),Toast.LENGTH_SHORT).show();
        String charset = "UTF-8";

        String requestURL = MainActivity.UPLOAD_IMAGE_URL;

        try {
            FileUploader multipart = new FileUploader(requestURL, charset, imgPaths);
            multipart.execute();

        } catch (IOException e) {
            System.err.println(e);
        }
    }*/

}




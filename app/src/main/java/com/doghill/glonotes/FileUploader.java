package com.doghill.glonotes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FileUploader extends AsyncTask<Void, Void, Void> {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private String uploadURL;
    private File[] sourceFile;
    private List<String> uploadPaths = new ArrayList<String>();
    private URL url;
    private String subject;
    private String textmessage;
    private Activity activity;
    private ProgressDialog dialog;
    private AlertDialog alert;
    private AlertDialog alert2;
    private String return_status;

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void v) {
        if (return_status.equals("NOTE POSTED")) {
            dialog.cancel();
            alert.show();
            //TODO imgPaths should not be nullified if user wants to retry (issue #2)
            PostNoteActivity.imgPaths = null;
        }

        else {
            dialog.cancel();
            alert2.show();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {

            startUpload(uploadURL);
            addHeaderField("Authorization", MainActivity.API_TOKEN_HEADER);
            addHeaderField("User-Agent", "GloNotes Android Client");
            addFormField(MainActivity.KEY_SUBJECT, subject);
            addFormField(MainActivity.KEY_TEXTMESSAGE, textmessage);
            addFormField(MainActivity.KEY_LATITUDE, MainActivity.latitude);
            addFormField(MainActivity.KEY_LONGITUDE, MainActivity.longitude);

            for (int i = 0; i < uploadPaths.size(); i++) {
                addFilePart("uploaded_image", sourceFile[i]);
            }
            List<String> response = finish();

            System.out.println("SERVER REPLIED:");

            for (String line : response) {
                System.out.println(line);
                if (line.equals("{\"detail\":\"NOTE POSTED\"}")) {
                    System.out.println(line);
                    return_status = "NOTE POSTED";
                }
            }
        }
        catch (IOException e) {
            System.err.println(e);
            return_status = "IOException uploading image";
        }
        catch (RuntimeException e) {
            System.err.println(e);
            return_status = "IOException uploading image";
        }


        return null;
    }

    public FileUploader(Activity activity, String requestURL, String charset, List imgPaths, String subject, String textmessage)
            throws IOException {
        this.charset = charset;
        this.uploadURL = requestURL;
        this.uploadPaths = imgPaths;
        this.subject = subject;
        this.textmessage = textmessage;
        this.activity = activity;

        // create note being posted progress dialog
        dialog = new ProgressDialog(activity);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Posting your note. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);


        // create note posted successfully dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your note was posted successfully!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alert = builder.create();
        alert.setTitle("Note Posted!");

        // create note failed to post dialog
        AlertDialog.Builder builder2 = new AlertDialog.Builder(activity);
        builder2.setMessage("Oops!  Note failed to post.  Please try again.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alert2 = builder2.create();
        alert2.setTitle("Note Post Failed");


        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
        sourceFile = new File[uploadPaths.size()];

        for (int i = 0; i < uploadPaths.size(); i++) {
            sourceFile[i] = new File(uploadPaths.get(i));
            //      Toast.makeText(getApplicationContext(), imgPaths.get(i), Toast.LENGTH_SHORT).show();
        }
    }


    public void startUpload(String requestURL) {

        try {
            url = new URL(requestURL);
            try {
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setUseCaches(false);
                httpConn.setDoOutput(true); // indicates POST method
                httpConn.setDoInput(true);
                httpConn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);
                httpConn.setRequestProperty("User-Agent", "GloNotes Android Client");
                httpConn.setRequestProperty("Authorization", MainActivity.API_TOKEN_HEADER);
                outputStream = httpConn.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                        true);
            }

            catch (IOException e) {
                return_status = e.toString();
            }
        } catch (MalformedURLException e) {
            return_status = e.toString();
        }

    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public List<String> finish() throws IOException {

        List<String> response = new ArrayList<String>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }

            reader.close();
            httpConn.disconnect();
        } else {
            return_status = "IOException";
            throw new IOException("Server returned non-OK status: " + status);

        }

        return response;
    }
}
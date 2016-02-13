package com.doghill.glonotes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SingleNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_note);
        Bundle extras = getIntent().getExtras();

        TextView mViewSubject = (TextView) findViewById(R.id.textSubject);
        TextView mViewMessageBody = (TextView) findViewById(R.id.textMessageBody);
        TextView mViewMessageAuthor = (TextView) findViewById(R.id.textMessageAuthor);

        mViewSubject.setText(extras.getString("subject"));
        mViewMessageBody.setText(extras.getString("textMessage"));
        mViewMessageAuthor.setText(extras.getString("author"));
    }

    protected void viewNoteOnMap(View v) {

    }
}

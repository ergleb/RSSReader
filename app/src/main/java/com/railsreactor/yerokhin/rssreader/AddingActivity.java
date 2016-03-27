package com.railsreactor.yerokhin.rssreader;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddingActivity extends AppCompatActivity {

    private final static String LOG_TAG = AddingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText urlEditText = (EditText) findViewById(R.id.url_edittext);
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//behavior of addButton
                RSSDatabaseHelper databaseHelper = new RSSDatabaseHelper(getBaseContext());
                Intent intent = new Intent(getBaseContext(),FeedsListActivity.class);
                try {
                    SQLiteDatabase database = databaseHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    Log.d(LOG_TAG, "adding " + urlEditText.getText().toString());
                    contentValues.put(RSSDatabaseHelper.getColumnName(),urlEditText.getText().toString());//putting to ContentValues
                    database.insert(RSSDatabaseHelper.getTableName(), null, contentValues);//insert to db
                    database.close();
                } catch (SQLiteException ex) {
                    Log.e(LOG_TAG,ex.getMessage());
                    Toast toast =Toast.makeText(getBaseContext(),"Couldn't add to db",Toast.LENGTH_SHORT);
                    toast.show();
                }
                startActivity(intent);
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),FeedsListActivity.class);
                startActivity(intent);
            }
        });

    }

}

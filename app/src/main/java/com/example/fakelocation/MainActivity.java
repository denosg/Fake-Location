package com.example.fakelocation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//TODO: when user presses the notification it should open MapsActivity.class

public class MainActivity extends AppCompatActivity {

    static TextView addressTextView; //shows the current address that the user has chosen after he set the fake location


    //button 'Set Location' on activity_main.xml, teleports the user to activity_maps.xml to choose the fake location
    public void setLocation(View view){
        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        startActivity(intent);

        Log.i("Button status: ", "Clicked");
    }

    //help page, shows the user how to use the app
    public void help(View view){
        Intent intent = new Intent(getApplicationContext(),HelpActivity.class);
        startActivity(intent);

        Log.i("Help Button status: ", "Clicked");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressTextView = findViewById(R.id.addressTextView);

    }
}
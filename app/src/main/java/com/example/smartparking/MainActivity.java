package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void handleClick(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // get location here
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                        new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    double lat = location.getLatitude();
                                    double longt = location.getLongitude();

                                    Intent intent = new Intent(MainActivity.this, NearestLocationsActivity.class);
                                    intent.putExtra("latitude", lat);
                                    intent.putExtra("longitude", longt);
                                    intent.putExtra("lid", 1);
                                    intent.putExtra("vehicle", 1);
                                    Log.d("msg", "Lol");
                                    startActivity(intent);
                                }
                            }
                        }
                );
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

    }

    public void handleHistory(View view) {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        // Log.d("msg", "Lol");
        startActivity(intent);
    }
    public void handleBookmarks(View view) {
        Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
        startActivity(intent);
    }

}
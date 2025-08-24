
package com.laxshana.project.geofence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng geofenceCenter;
    private Circle geofenceCircle;
    private float geofenceRadius = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference dbRef;
    private TextView radiusText;
    private Boolean previousIsOutside = null;
    private final Handler handler = new Handler();
    private final int interval = 5000; // 5 seconds

    Button btnViewImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 🔹 Step 1: Connect the button from XML to Java
        btnViewImages = findViewById(R.id.btnViewImages);

        // 🔹 Step 2: Set up what happens when the button is clicked
        btnViewImages.setOnClickListener(v -> {
            // This code runs when the button is pressed
         //   Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
            // Create an Intent to start the ImageListActivity
            Intent intent = new Intent(MainActivity.this, ImageListActivity.class);

            // Launch the ImageListActivity
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("geofenceAlert")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Subscribed to geofenceAlert");
                        Toast.makeText(this, "Subscribed to alerts", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.d("FCM", "Subscription failed");
                        Toast.makeText(this, "Failed to subscribe to alerts", Toast.LENGTH_SHORT).show();
                    }
                });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        radiusText = findViewById(R.id.radius_value);
        SeekBar radiusSeek = findViewById(R.id.radius_seek);
        radiusSeek.setMax(500);
        radiusSeek.setProgress((int) geofenceRadius);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dbRef = FirebaseDatabase.getInstance().getReference("geofence");

        radiusSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                geofenceRadius = progress;
                radiusText.setText(progress + " m");
                if (geofenceCenter != null) {
                    updateGeofenceCircle();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapLongClickListener(latLng -> {
            mMap.clear();
            geofenceCenter = latLng;
            mMap.addMarker(new MarkerOptions().position(latLng).title("Geofence Center"));
            updateGeofenceCircle();
            Toast.makeText(this, "Geofence set. Now monitoring your location...", Toast.LENGTH_LONG).show();
            startMonitoringLocation();
        });
    }

    private void updateGeofenceCircle() {
        if (geofenceCircle != null) geofenceCircle.remove();
        geofenceCircle = mMap.addCircle(new CircleOptions()
                .center(geofenceCenter)
                .radius(geofenceRadius)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(4));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(geofenceCenter, 15));
    }

    private void startMonitoringLocation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null && geofenceCenter != null) {
                        float[] distance = new float[1];
                        Location.distanceBetween(
                                location.getLatitude(), location.getLongitude(),
                                geofenceCenter.latitude, geofenceCenter.longitude,
                                distance);
                        boolean isOutside = distance[0] > geofenceRadius;
                        dbRef.child("isActive").setValue(isOutside)
                                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "isActive updated: " + isOutside))
                                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to update isActive", e));

                        if (previousIsOutside == null || previousIsOutside != isOutside) {
                            previousIsOutside = isOutside;
                            Toast.makeText(MainActivity.this, isOutside ? "You exited the geofence" : "You entered the geofence", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                handler.postDelayed(this, interval);
            }
        }, interval);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap);
        }
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

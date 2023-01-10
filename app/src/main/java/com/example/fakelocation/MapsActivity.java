package com.example.fakelocation;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.fakelocation.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    //adaugat 27.10.2022
    private NotificationManagerCompat notificationManager;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    SearchView searchView;

    SupportMapFragment mapFragment;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchView = findViewById(R.id.svLocation);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //adaugat 22.11.2022

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //adaugat 22.11.2022
        //search bar that points to the location searched (works only for english words)
        //TODO: implement way to show hints when searching for a city
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchedLocation = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (searchedLocation != null || !searchedLocation.equals("")){
                    Geocoder searchGeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {
                        addressList = searchGeocoder.getFromLocationName(searchedLocation, 1);
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    Address address = addressList.get(0); //address List to store the addreses of the places searched using the search feature
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude()); //gets the lat and long from the city searched
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15)); //moves the camera to the city searched

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
    }
    //the method sets the fake location by long clicking on the map
    public void onMapLongClick(LatLng point) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {

            String adresa = "";
            String judet = "";
            String oras = "";
            String bloc = "";

            List<Address> addressList;
            addressList = geocoder.getFromLocation(point.latitude, point.longitude, 1); //gets the information about the long click (adresa, city...)

            if (addressList.get(0).getThoroughfare() != null || addressList.get(0).getAdminArea() != null || addressList.get(0).getSubAdminArea() != null || addressList.get(0).getFeatureName() != null) {
                if(addressList.get(0).getThoroughfare() != null)
                adresa = addressList.get(0).getThoroughfare();

                if (addressList.get(0).getAdminArea() != null)
                judet = addressList.get(0).getAdminArea();

                if (addressList.get(0).getSubAdminArea() != null)
                oras = addressList.get(0).getSubAdminArea();

                if (addressList.get(0).getFeatureName() != null)
                bloc = addressList.get(0).getFeatureName();

                MainActivity.addressTextView.setText("Your fake location is: "+adresa + " "+ judet + " "+ oras+ " "+ bloc);
            }

            Log.i("addresa: ", String.valueOf(addressList.get(0)));

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Problem: ","There was a problem gathering location");
        }
        //adds a marker to show the user that the location has been changed
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Fake Location Here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        Log.i("Latitude: ", String.valueOf(point.latitude));
        Log.i("Longitude: ", String.valueOf(point.longitude));

        //sets the fake location
        setMock(point.latitude, point.longitude, 100);

        Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();

        //adaugat 27.10.2022
        //sends the user a notification regarding the location chosen
        notificationManager = NotificationManagerCompat.from(this);

        String message = (String) MainActivity.addressTextView.getText();

        //when user clicks on notification, it opens MapsActivity.class to change it
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotiForYou.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle("Fake Location")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
        //pana aici
    }
    //fake location method implementation
    private void setMock(double latitude, double longitude, float accuracy) {
        locationManager.addTestProvider (LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAccuracy(accuracy);
        newLocation.setAltitude(0);
        newLocation.setAccuracy(500);
        newLocation.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,System.currentTimeMillis());

        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults.length>0 && (grantResults[0]==PackageManager.PERMISSION_GRANTED)){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }
}
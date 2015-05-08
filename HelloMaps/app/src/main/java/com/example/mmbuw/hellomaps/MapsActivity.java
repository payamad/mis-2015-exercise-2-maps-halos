package com.example.mmbuw.hellomaps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private SharedPreferences sharedPref;

    private EditText editText;

    private int markerCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //setup location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        editText = (EditText) findViewById(R.id.edittext);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        /**
         * Set current location in map
         * Source: http://stackoverflow.com/questions/2227292/how-to-get-latitude-and-longitude-of-the-mobiledevice-in-android
         *
         */
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(Integer.toString(markerCounter)));

        // Save marker information
        setNewMarkerToPreferences("Marker", latLng);

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        String message = editText.getText().toString();

        // Add new marker in map
        mMap.addMarker(new MarkerOptions().position(latLng).title(Integer.toString(markerCounter)));

        // Save marker information
        setNewMarkerToPreferences(message, latLng);
    }

    private void setNewMarkerToPreferences(String value, LatLng latLng){
        /**
         * Add message, latitude and longitude to SharedPreferences
         * link -> https://developer.android.com/training/basics/data-storage/shared-preferences.html
         */
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Integer.toString(markerCounter), value);
        editor.putString(markerCounter + "-LatLng", latLng.toString());
        editor.commit();
        markerCounter++;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        /**
         * read marker information from SharedPreferences
         * and show a message box with message and latitude and longitude
         */
        int markerId = Integer.parseInt(marker.getTitle().toString());
        String text = sharedPref.getString(Integer.toString(markerId),"") + "\n"
                + sharedPref.getString(markerId + "-LatLng","");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text);
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
        return true;
    }
}

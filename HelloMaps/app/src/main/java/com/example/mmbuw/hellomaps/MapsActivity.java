package com.example.mmbuw.hellomaps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private SharedPreferences sharedPref;

    private EditText editText;

    private ArrayList<Circle>  circleList;

    private int markerCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //setup location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        editText = (EditText) findViewById(R.id.edittext);

        circleList = new ArrayList<Circle>();

        setUpMapIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save the number of markers
        savedInstanceState.putInt("MarkerCounter", markerCounter);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore number of markers
        markerCounter = savedInstanceState.getInt("MarkerCounter");
        if (mMap != null) {
            // Redraw markers and circles
            drawMarkers();
            drawCircles();
        }
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

        if(markerCounter == 0){
            // There is no previous markers, so add a marker with a current location
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(Integer.toString(markerCounter)));

            // Save marker information
            setNewMarkerToPreferences("Marker", latLng);

            mMap.setOnMapLongClickListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnCameraChangeListener(this);
        } else {
            // Redraw saved markers and circles
            drawMarkers();
            drawCircles();
        }

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
        editor.putString(markerCounter + "Lat", Double.toString(latLng.latitude));
        editor.putString(markerCounter + "Lng", Double.toString(latLng.longitude));
        editor.commit();

        // Add new circle
        addCircle(latLng);
        markerCounter++;
    }

    private void addCircle(LatLng latLng){
        /**
         * Add a circle for each marker
         * The radius is zero because the marker is visible
         */
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(0)
                .strokeColor(Color.RED));
        circleList.add(circle);
    }

    private void drawMarkers(){
        for(int i=1; i < markerCounter; i++){
            double latitude = Double.parseDouble(sharedPref.getString(i + "Lat",""));
            double longitude = Double.parseDouble(sharedPref.getString(i + "Lng",""));
            String message = sharedPref.getString(Integer.toString(i), "");
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(Integer.toString(i)));
            addCircle(new LatLng(latitude, longitude));

        }
    }

    private void drawCircles(){
        /**
         * Found visible map boundaries
         * Link -> http://stackoverflow.com/questions/14700498/android-google-maps-get-boundary-co-ordinates
         */
        LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
        Location boundCenter = new Location(String.valueOf(curScreen.getCenter()));
        boundCenter.setLatitude(curScreen.getCenter().latitude);
        boundCenter.setLongitude(curScreen.getCenter().longitude);

        // Check each marker to find out, is it in the boundary or not?
        // Also change circle radius if it is necessary
        for(Circle circle: circleList){
            Location circleCenter = new Location(String.valueOf(circle.getCenter()));
            circleCenter.setLatitude(circle.getCenter().latitude);
            circleCenter.setLongitude(circle.getCenter().longitude);

            if(curScreen.contains(circle.getCenter())){
                // If the marker is in the boundary make radius of its circle to zero
                circle.setRadius(0);
            } else {
                // Find the nearest point on the boundary to the circle's center point
                LatLng latlng = findClosestLocationInBoundary(curScreen, circle.getCenter());

                Location pointLocation = new Location(String.valueOf(circle.getCenter()));
                pointLocation.setLatitude(latlng.latitude);
                pointLocation.setLongitude(latlng.longitude);

                // Circle radius is equal to distance of circle center to the point that we just found plus to distance of the center of boundary to the point divided by 5
                // We use distance to center divided by 5 to make circle visible in our view
                circle.setRadius(circleCenter.distanceTo(pointLocation) + (boundCenter.distanceTo(pointLocation) / 5));
            }
        }

    }

    private LatLng findClosestLocationInBoundary(LatLngBounds bound, LatLng point)
    {
        double latitude = point.latitude;
        double longitude = point.longitude;
        LatLng boundCenter = bound.getCenter();

        // The point is upper than center of boundary
        if(boundCenter.latitude < point.latitude){
            // The point is in right half of the boundary
            if(boundCenter.longitude < point.longitude){
                if(bound.northeast.latitude < point.latitude)
                    latitude = bound.northeast.latitude;
                if(bound.northeast.longitude < point.longitude)
                    longitude = bound.northeast.longitude;

            } else { // The point is in left half of the boundary

                if(bound.northeast.latitude < point.latitude)
                    latitude = bound.northeast.latitude;
                if(bound.southwest.longitude > point.longitude)
                    longitude = bound.southwest.longitude;


            }
        } else { // The point is lower than center of boundary

            // The point is in left half of the boundary
            if(boundCenter.longitude > point.longitude){
                if(bound.southwest.latitude > point.latitude)
                    latitude = bound.southwest.latitude;
                if(bound.southwest.longitude > point.longitude)
                    longitude = bound.southwest.longitude;
            } else { // The point is in left half of the boundary

                if(bound.southwest.latitude > point.latitude)
                    latitude = bound.southwest.latitude;
                if(bound.northeast.longitude < point.longitude)
                    longitude = bound.northeast.longitude;
            }
        }
        return new LatLng(latitude,longitude);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        /**
         * read marker information from SharedPreferences
         * and show a message box with message and latitude and longitude
         */
        int markerId = Integer.parseInt(marker.getTitle().toString());
        String text = sharedPref.getString(Integer.toString(markerId),"") + "\n"
                + "Location:" + sharedPref.getString(markerId + "Lat","")
                + "," + sharedPref.getString(markerId + "Lng","");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text);
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
        return true;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        drawCircles();
    }
}

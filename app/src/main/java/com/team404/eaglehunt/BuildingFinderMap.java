package com.team404.eaglehunt;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class BuildingFinderMap extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{


    Location mCurrentLocation;
    LocationClient mLocationClient;

    // Open the shared preferences
    SharedPreferences mPrefs;
    // Get a SharedPreferences editor
    SharedPreferences.Editor mEditor;
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
    // Start with updates turned off
    boolean mUpdatesRequested = false;


    /*
 * Define a request code to send to Google Play services
 * This code is returned in Activity.onActivityResult
 */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        String title = i.getStringExtra("name");
        int position = i.getIntExtra("pos",0);
        mLocationClient = new LocationClient(this, this, this);

        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        setContentView(R.layout.activity_building_finder_map);

        TextView tv = (TextView) findViewById(R.id.textView13);
        tv.setText(title);

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.buildingFinderMap))
                .getMap();

        map.setMyLocationEnabled(true);


        moveMap(map, position);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (mLocationClient != null)
        {
            mLocationClient.connect();
        }
        else
        {
            Toast.makeText(this,"Location service error.", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
        super.onStop();

    }

    @Override
    protected void onResume() {
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

            // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }



    public void moveMap(GoogleMap map, int pos)
    {
        double la; double lo;

        switch (pos)
        {
            case 0:
                la = 43.039603; lo = -87.931179;
                Marker amu = createMapMarker(map, la, lo,"Alumni Memorial Union","AMU");
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 17),2000,null);
                break;
            case 1:
                la = 43.038353; lo = -87.933671;
                Marker ehall = createMapMarker(map, la, lo,"Engineering Hall","E-Hall");
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 17),2000,null);
                break;
            case 2:
                la = 43.038637; lo = -87.931552;
                Marker olin = createMapMarker(map, la, lo,"Olin Engineering/Haggerty Hall","Olin");
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 17),2000,null);
                break;
            case 3:
                la = 43.038075; lo = -87.929654;
                Marker raynor = createMapMarker(map, la, lo,"Raynor Memorial Libraries","Raynor/Memorial");
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 17),2000,null);
                break;
            case 4:
                la = 43.038568; lo = -87.927711;
                Marker marqHall = createMapMarker(map, la, lo,"Marquette Hall","Marquette Hall");
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 17),2000,null);
                break;

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_building_finder_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Marker createMapMarker(GoogleMap map, double lat, double lon, String name, String description)
    {
        Marker newMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title(name)
                .snippet(description)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)));
        return newMarker;
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Toast.makeText(this, "Location service connected.", Toast.LENGTH_SHORT).show();
        mCurrentLocation = mLocationClient.getLastLocation();
    }

    @Override
    public void onDisconnected()
    {
        Toast.makeText(this, "Location service disconnected.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Location service connection unsuccessful.", Toast.LENGTH_SHORT).show();

                /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        /**
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

                 // Thrown if Google Play services canceled the original
                 // PendingIntent

            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {

             // If no resolution is available, display a dialog to the
             // user with the error.

            showErrorDialog(connectionResult.getErrorCode());
        }

        */

    }

    @Override
    public void onLocationChanged(Location location) {


    }

/**
    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :

             // If the result code is Activity.RESULT_OK, try
             // to connect again

                switch (resultCode) {
                    case Activity.RESULT_OK :
                    //
                    // Try the request again
                    //
                        break;
                }
        }
    }
*/

/*
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),"Location Updates");
            }
        }
    }
    */



}

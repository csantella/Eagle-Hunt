package com.team404.eaglehunt;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class QuizMode extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    LocationClient mLocationClient;
    Location mCurrentLocation;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //android.support.v7.app.ActionBar bar = getSupportActionBar();
        //bar.setBackgroundDrawable(new ColorDrawable(R.color.gold_bg));
        mLocationClient = new LocationClient(this, this, this);


        setContentView(R.layout.activity_quiz_mode);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment))
                .getMap();

        map.setMyLocationEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void moveMap(GoogleMap map, Location loc)
    {
        double la; double lo;
/**
        la = 43.039603; lo = -87.931179;
        Marker amu = createMapMarker(map, la, lo,"Alumni Memorial Union","AMU");

        la = 43.038353; lo = -87.933671;
        Marker ehall = createMapMarker(map, la, lo,"Engineering Hall","Opus College of Engineering");
*/
        la = loc.getLatitude(); lo = loc.getLongitude();

        TextView txtLat = (TextView) findViewById(R.id.latValue);
        txtLat.setText(String.valueOf(la));

        TextView txtLon = (TextView) findViewById(R.id.lonValue);
        txtLon.setText(String.valueOf(lo));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la,lo),17),2000,null);
        //map.animateCamera(CameraUpdateFactory.zoomTo(5),2000,null);

    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        TextView txtLat = (TextView) findViewById(R.id.latValue);
        txtLat.setText(String.valueOf(lat));

        TextView txtLon = (TextView) findViewById(R.id.lonValue);
        txtLon.setText(String.valueOf(lon));

        moveMap(map, mCurrentLocation);
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
        moveMap(map, mCurrentLocation);
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
    }
}

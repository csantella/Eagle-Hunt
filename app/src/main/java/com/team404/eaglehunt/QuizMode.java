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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class QuizMode extends ActionBarActivity implements LocationListener {

    protected LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(R.color.gold_bg));

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                (float) 0, this);

        setContentView(R.layout.activity_quiz_mode);
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment))
                .getMap();
        moveMap(map);
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

    public void moveMap(GoogleMap map)
    {
        double la; double lo;

        la = 43.039603; lo = -87.931179;
        Marker amu = createMapMarker(map, la, lo,"Alumni Memorial Union","AMU");

        la = 43.038353; lo = -87.933671;
        Marker ehall = createMapMarker(map, la, lo,"Engineering Hall","Opus College of Engineering");

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
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
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
}

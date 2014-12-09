package com.team404.eaglehunt;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;


public class QuizMode extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        LocationClient.OnAddGeofencesResultListener {

    LocationClient mLocationClient;
    Location mCurrentLocation;
    GoogleMap map;
    //Chronometer riddleTimer = new Chronometer(this);
    boolean isRiddleActive = false;

   /*
    * Use to set an expiration time for a geofence. After this amount
    * of time Location Services will stop tracking the geofence.
    */
    private static final long SECONDS_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
                    SECONDS_PER_HOUR *
                    MILLISECONDS_PER_SECOND;


    /*
     * Handles to UI views containing geofence data
     */
    // Handle to geofence 1 latitude in the UI
    private EditText mLatitude1;
    // Handle to geofence 1 longitude in the UI
    private EditText mLongitude1;
    // Handle to geofence 1 radius in the UI
    private EditText mRadius1;
    // Handle to geofence 2 latitude in the UI
    private EditText mLatitude2;
    // Handle to geofence 2 longitude in the UI
    private EditText mLongitude2;
    // Handle to geofence 2 radius in the UI
    private EditText mRadius2;
    /*
     * Internal geofence objects for geofence 1 and 2
     */
    private SimpleGeofence mUIGeofence1;
    //private SimpleGeofence mUIGeofence2;
    // Internal List of Geofence objects
    List<Geofence> mGeofenceList;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;
    private ArrayList<Geofence> mCurrentGeofences;
    // Holds the location client
    //private LocationClient mLocationClient;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD,DO_NOTHING}
    private REQUEST_TYPE mRequestType = REQUEST_TYPE.DO_NOTHING;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    PendingIntent mTransitionPendingIntent;
    int difficulty;
    boolean isInGeofence = false;
    Riddle currentRiddle;
    int totalScore = 0;
    int riddleDeduction = 0;
    int riddleScore = 0;


    TextView tv;
    TextView tv2;
    TextView rs;
    TextView tv3;
    TextView txtLon;
    TextView txtLat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get difficulty level from previous screen

        Intent i = getIntent();
        difficulty = i.getIntExtra("difficulty",0);
        showDifficulty(difficulty);

        //android.support.v7.app.ActionBar bar = getSupportActionBar();
        //bar.setBackgroundDrawable(new ColorDrawable(R.color.gold_bg));
        mLocationClient = new LocationClient(this, this, this);


        setContentView(R.layout.activity_quiz_mode);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment))
                .getMap();
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(43.038663,-87.930123) , 14.0f) ); //set to default 'center of campus' location

        map.setMyLocationEnabled(true);
        initializeRiddleFactory();

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

    }

    public void onGoClick(View v)
    {
        isRiddleActive = !isRiddleActive;

        tv = (TextView) findViewById(R.id.riddle);
        tv2 = (TextView) findViewById(R.id.score);
        rs = (TextView) findViewById(R.id.roundScore);
        tv3 = (TextView) findViewById(R.id.textView);
        txtLat = (TextView) findViewById(R.id.latValue);
        txtLon = (TextView) findViewById(R.id.lonValue);

        int riddleIndex = getRiddleIndex(difficulty); // 0 easy, 1 med, 2 hard
        int radius = 0;

        if (isRiddleActive)
        {
            //riddleTimer.start();
            riddleScore = 600;
            currentRiddle = RiddleFactory.riddlesList.get(riddleIndex);
            tv.setText(currentRiddle.getContent());
            rs.setText(String.valueOf(riddleScore));
            Button bt = (Button) findViewById(R.id.button);
            bt.setText("Quit");
            //tv2.setBackgroundColor(getResources().getColor(R.color.gold_bg));
            //tv3.setBackgroundColor(getResources().getColor(R.color.gold_bg));
            //createGeofences();

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    int loop = 0;
                    //mCurrentLocation = map.getMyLocation();
                    while (!isInGeofence) {
                        try {
                            Thread.sleep(2000); // Waits for 1 second (1000 milliseconds)
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Loop interrupted.",Toast.LENGTH_SHORT).show();
                        }
                        //mCurrentLocation = mLocationClient.getLastLocation();
                        /**
                         Check to see if the user's latitude and longitude are within a square radius of the solution.
                         */

                        //final double lat = mCurrentLocation.getLatitude();
                        //final double lon = mCurrentLocation.getLongitude();



                        if (mCurrentLocation.getLatitude()>=(currentRiddle.latitude - (currentRiddle.getRadius()/10000)) &&
                                mCurrentLocation.getLatitude()<=(currentRiddle.latitude + (currentRiddle.getRadius()/10000)))
                        {
                            if (mCurrentLocation.getLongitude()>=(currentRiddle.longitude - (currentRiddle.getRadius()/10000)) &&
                                    mCurrentLocation.getLongitude()<=(currentRiddle.longitude + (currentRiddle.getRadius()/10000)) )
                            {
                                //User is within geofence
                                isInGeofence = true;

                            }
                        }

                        rs.post(new Runnable() {
                            @Override
                            public void run() {
                                //run loop
                                //riddleDeduction = riddleDeduction - 1;
                                riddleScore = riddleScore - 1;
                                if (riddleScore < 0)
                                    riddleScore = 0;
                                rs.setText(String.valueOf(riddleScore)); //put round score on text
                            }
                        });

                        txtLon.post(new Runnable() {
                            @Override
                            public void run() {
                                //run loop
                                txtLon.setText(String.valueOf(mCurrentLocation.getLatitude())); //put lon in text
                            }
                        });

                        txtLat.post(new Runnable() {
                            @Override
                            public void run() {
                                //run loop
                                txtLat.setText(String.valueOf(mCurrentLocation.getLongitude())); //put lat
                            }
                        });
                    }
                }

            };

            Thread myThread = new Thread(myRunnable);
            myThread.start();

            bt.setEnabled(false);

            if (isInGeofence)
            {
                updateTotalScore();
                Toast.makeText(getApplicationContext(),"Is in Geofence", Toast.LENGTH_SHORT).show();
            }


        }
        else
        {
            //riddleTimer.stop();
            //riddleScore = 100;
            riddleScore = 0;
            riddleDeduction = 0;
            tv.setText("[Click GO!]");
            //tv2.setText(String.valueOf(riddleScore));
            Button bt = (Button) findViewById(R.id.button);
            bt.setText("GO!");
            tv2.setBackgroundColor(getResources().getColor(R.color.transparent));
            tv3.setBackgroundColor(getResources().getColor(R.color.transparent));
            rs.setText(String.valueOf(riddleScore)); //put round score on text
            //tv2.setText(String.valueOf(totalScore)); //put total score on text

        }

}

    private void updateTotalScore() {


        /**
         * Code reaches this point when the geofence is reached
         */

        totalScore = totalScore + riddleScore;
        tv2.setText(String.valueOf(totalScore)); //put total score on text
    }

    public void checkGeofence()
    {

    }

    private int getRiddleIndex(int mode)
    {
        //int index = 0;
        final Random rand = new Random();
        int diceRoll = rand.nextInt(RiddleFactory.riddlesList.size()) + 1; // uniformly distributed int from 1 to 3
        diceRoll--;
        Riddle tryMe = RiddleFactory.riddlesList.get(diceRoll);
        while (tryMe.getDifficulty()!=mode)
        {
            diceRoll = rand.nextInt(RiddleFactory.riddlesList.size()) + 1; // uniformly distributed int from 1 to 3
            diceRoll--;
            tryMe = RiddleFactory.riddlesList.get(diceRoll);
        }
        return diceRoll;
    }

    /**
     * Get the geofence parameters for each geofence from the UI
     * and add them to a List.
     */
    public void createGeofences() {
        /*
         * Create an internal object to store the data. Set its
         * ID to "1". This is a "flattened" object that contains
         * a set of strings
         */
        mUIGeofence1 = new SimpleGeofence(
                "1",
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                100f,
                GEOFENCE_EXPIRATION_TIME,
                // This geofence records only entry transitions
                Geofence.GEOFENCE_TRANSITION_ENTER);
        // Store this flat version
        mGeofenceStorage.setGeofence("1", mUIGeofence1);
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Geofence Detection",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            /*
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
                errorFragment.show(
                        getSupportFragmentManager(),
                        "Geofence Detection");
                        */
            return false;
            }
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
            Toast.makeText(this,"Location service error.", Toast.LENGTH_SHORT).show();
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

        txtLat = (TextView) findViewById(R.id.latValue);
        txtLat.setText(String.valueOf(la));
        txtLon = (TextView) findViewById(R.id.lonValue);
        txtLon.setText(String.valueOf(lo));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la,lo),17),2000,null);
        //map.animateCamera(CameraUpdateFactory.zoomTo(5),2000,null);

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Updated.", Toast.LENGTH_SHORT).show();
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        txtLat = (TextView) findViewById(R.id.latValue);
        txtLat.setText(String.valueOf(lat));

        txtLon = (TextView) findViewById(R.id.lonValue);
        txtLon.setText(String.valueOf(lon));

        //moveMap(map, mCurrentLocation);
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
        //Toast.makeText(this, "Location service connected.", Toast.LENGTH_SHORT).show();
        mCurrentLocation = mLocationClient.getLastLocation();
        //txtLat.setText(String.valueOf(mCurrentLocation.getLatitude()));
        //txtLon.setText(String.valueOf(mCurrentLocation.getLongitude()));
        moveMap(map, mCurrentLocation);

        switch (mRequestType) {
            case ADD :
                // Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to add the current geofences
               // mLocationClient.addGeofences(
               //         mCurrentGeofences, mTransitionPendingIntent, this);
                break;
            case DO_NOTHING:
                break;
        }

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

    public void showDifficulty(int i)
    {
        String diff = "EASY";
        switch (i)
        {
            case 0:
                break;
            case 1:
                diff = "MEDIUM";
                break;
            case 2:
                diff = "HARD";
                break;
        }
        //Toast.makeText(this, diff, Toast.LENGTH_SHORT).show();
    }

    public void initializeRiddleFactory()
    {
        Riddle ehall = new Riddle("Which building is known as \"E-Hall\"?",0,43.038441,-87.933345,5);
        Riddle pit = new Riddle("What building is home to \"The Pit\"?",0,43.038300,-87.932046,5);
        RiddleFactory.riddlesList.add(ehall);
        RiddleFactory.riddlesList.add(pit);

        RiddleFactory.riddlesList.add(new Riddle("Navigate to the building that houses the Office of Residence Life",1,43.039051,-87.925525,2));
        RiddleFactory.riddlesList.add(new Riddle("Which University building was formerly a YMCA?",2,43.038297,-87.923559,3));
        RiddleFactory.riddlesList.add(new Riddle("This church is named after a famous saint.",1,43.037669,-87.931034,1));
        RiddleFactory.riddlesList.add(new Riddle("This building was formerly Marquette's Gymnasium.",2,43.036470,-87.932227,4));
        RiddleFactory.riddlesList.add(new Riddle("Navigate to the site that used to be referred to as South Hall?",2,43.036373,-87.934582,5));
        RiddleFactory.riddlesList.add(new Riddle("Which current academic building was formerly used as student apartments?",2,43.038410,-87.926651,4));
        RiddleFactory.riddlesList.add(new Riddle("Which building is named after a famous church in Rome?",1,43.038488,-87.927179,3));
        RiddleFactory.riddlesList.add(new Riddle("This building is home to Marquette Central and admissions staff.",0,43.039128,-87.927936,5));
    }

    public void addGeofences() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }


    @Override
    public void onAddGeofencesResult(int i, String[] strings)
    {

    }


    //GEOFENCE OBJECT

    /**
     * A single Geofence object, defined by its center and radius.
     */
    public class SimpleGeofence {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private long mExpirationDuration;
        private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude Latitude of the Geofence's center.
         * @param longitude Longitude of the Geofence's center.
         * @param radius Radius of the geofence circle.
         * @param expiration Geofence expiration duration
         * @param transition Type of Geofence transition.
         */
        public SimpleGeofence(
                String geofenceId,
                double latitude,
                double longitude,
                float radius,
                long expiration,
                int transition) {
            // Set the instance fields from the constructor
            this.mId = geofenceId;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            this.mExpirationDuration = expiration;
            this.mTransitionType = transition;
        }
        // Instance field getters
        public String getId() {
            return mId;
        }
        public double getLatitude() {
            return mLatitude;
        }
        public double getLongitude() {
            return mLongitude;
        }
        public float getRadius() {
            return mRadius;
        }
        public long getExpirationDuration() {
            return mExpirationDuration;
        }
        public int getTransitionType() {
            return mTransitionType;
        }
        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence() {
            // Build a new Geofence object
            return new Geofence.Builder()
                    .setRequestId(getId())
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(
                            getLatitude(), getLongitude(), getRadius())
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        }
    }
    /**
     * Storage for geofence values, implemented in SharedPreferences.
     */
    public class SimpleGeofenceStore {
        // Keys for flattened geofences stored in SharedPreferences
        public static final String KEY_LATITUDE =
                "com.example.android.geofence.KEY_LATITUDE";
        public static final String KEY_LONGITUDE =
                "com.example.android.geofence.KEY_LONGITUDE";
        public static final String KEY_RADIUS =
                "com.example.android.geofence.KEY_RADIUS";
        public static final String KEY_EXPIRATION_DURATION =
                "com.example.android.geofence.KEY_EXPIRATION_DURATION";
        public static final String KEY_TRANSITION_TYPE =
                "com.example.android.geofence.KEY_TRANSITION_TYPE";
        // The prefix for flattened geofence keys
        public static final String KEY_PREFIX =
                "com.example.android.geofence.KEY";
        /*
         * Invalid values, used to test geofence storage when
         * retrieving geofences
         */
        public static final long INVALID_LONG_VALUE = -999l;
        public static final float INVALID_FLOAT_VALUE = -999.0f;
        public static final int INVALID_INT_VALUE = -999;
        // The SharedPreferences object in which geofences are stored
        private final SharedPreferences mPrefs;
        // The name of the SharedPreferences
        private static final String SHARED_PREFERENCES =
                "SharedPreferences";
        // Create the SharedPreferences storage with private access only
        public SimpleGeofenceStore(Context context) {
            mPrefs =
                    context.getSharedPreferences(
                            SHARED_PREFERENCES,
                            Context.MODE_PRIVATE);
        }
        /**
         * Returns a stored geofence by its id, or returns null
         * if it's not found.
         *
         * @param id The ID of a stored geofence
         * @return A geofence defined by its center and radius. See
         */
        public SimpleGeofence getGeofence(String id) {
            /*
             * Get the latitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lat = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_LATITUDE),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the longitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lng = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_LONGITUDE),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the radius for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            float radius = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_RADIUS),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the expiration duration for the geofence identified
             * by id, or INVALID_LONG_VALUE if it doesn't exist
             */
            long expirationDuration = mPrefs.getLong(
                    getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    INVALID_LONG_VALUE);
            /*
             * Get the transition type for the geofence identified by
             * id, or INVALID_INT_VALUE if it doesn't exist
             */
            int transitionType = mPrefs.getInt(
                    getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    INVALID_INT_VALUE);
            // If none of the values is incorrect, return the object
            if (
                    lat != GeofenceUtils.INVALID_FLOAT_VALUE &&
                            lng != GeofenceUtils.INVALID_FLOAT_VALUE &&
                            radius != GeofenceUtils.INVALID_FLOAT_VALUE &&
                            expirationDuration !=
                                    GeofenceUtils.INVALID_LONG_VALUE &&
                            transitionType != GeofenceUtils.INVALID_INT_VALUE) {

                // Return a true Geofence object
                return new SimpleGeofence(
                        id, lat, lng, radius, expirationDuration,
                        transitionType);
                // Otherwise, return null.
            } else {
                return null;
            }
        }
        /**
         * Save a geofence.
         * @param geofence The SimpleGeofence containing the
         * values you want to save in SharedPreferences
         */
        public void setGeofence(String id, SimpleGeofence geofence) {
            /*
             * Get a SharedPreferences editor instance. Among other
             * things, SharedPreferences ensures that updates are atomic
             * and non-concurrent
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            // Write the Geofence values to SharedPreferences
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_LATITUDE),
                    (float) geofence.getLatitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_LONGITUDE),
                    (float) geofence.getLongitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_RADIUS),
                    geofence.getRadius());
            editor.putLong(
                    getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    geofence.getExpirationDuration());
            editor.putInt(
                    getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    geofence.getTransitionType());
            // Commit the changes
            editor.commit();
        }
        public void clearGeofence(String id) {
            /*
             * Remove a flattened geofence object from storage by
             * removing all of its keys
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
            editor.remove(getGeofenceFieldKey(id,
                    KEY_EXPIRATION_DURATION));
            editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
            editor.commit();
        }
        /**
         * Given a Geofence object's ID and the name of a field
         * (for example, KEY_LATITUDE), return the key name of the
         * object's values in SharedPreferences.
         *
         * @param id The ID of a Geofence object
         * @param fieldName The field represented by the key
         * @return The full key name of a value in SharedPreferences
         */
        private String getGeofenceFieldKey(String id,
                                           String fieldName) {
            return KEY_PREFIX + "_" + id + "_" + fieldName;
        }
    }

    public class ReceiveGeofenceTransitionIntentService extends IntentService {
        /**
         * Sets an identifier for the service
         */
        public ReceiveGeofenceTransitionIntentService() {
            super("ReceiveGeofenceTransitionsIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            // Create a local broadcast Intent
            Intent broadcastIntent = new Intent();

            // Give it the category for all intents sent by the Intent Service
            //broadcastIntent.addCategory(CATEGORY_LOCATION_SERVICES);


            // First check for errors
            if (LocationClient.hasError(intent)) {
                // Get the error code with a static method
                int errorCode = LocationClient.getErrorCode(intent);
            }
            else {
                // Get the type of transition (entry or exit)
                int transition =
                        LocationClient.getGeofenceTransition(intent);

                if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER)  ||
                        (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {

                    // Post a notification
                }
                else {
                    // handle the error
                }
            }
        }
    }

}

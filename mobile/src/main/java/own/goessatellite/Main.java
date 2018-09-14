package own.goessatellite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Locale;

import uk.co.senab.photoview.PhotoViewAttacher;

public class Main extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "Main";
    public static final int gpsinterval = 60000;
    public static final int interval = 1800000;
    public static final long GPSUPDATE_INTERVAL_IN_MILLISECONDS = gpsinterval;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = GPSUPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final static String REQUESTING_UPDATES_KEY = "requesting-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private LocationRequest mLocationRequest;
    private Location mylocation;
    protected GoogleApiClient GoogleApiClient;
    private Boolean mRequestingUpdates;

    private double lat = 0.0;
    private double lon = 0.0;
    private String mylat = "0.0";
    private String mylon = "0.0";

    String myimageurl = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/CONUS/GEOCOLOR/latest.jpg";
    String myimageurl2 = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/CONUS/01/5000x3000.jpg";
    ImageView myimage;

    ProgressDialog mProgressDialog;

    PhotoViewAttacher myimagezoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //needed for isOnline() to work
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initializeGoogleAPI();

        mRequestingUpdates = false;
        //start gps updates auto//
        updateValuesFromBundle(savedInstanceState);

        //image 1
        myimage = (ImageView) findViewById(R.id.myimage);
        new DownloadImage().execute(myimageurl);
        myimagezoom = new PhotoViewAttacher(myimage);
        myimagezoom.update();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.conusradar) {
            RequestConusRadar();
        }

        if (id == R.id.spchome) {
            SPCHomePage();
        }

        if (id == R.id.mesoanalysis) {
            SPCMesoAnalysis();
        }

        if (id == R.id.swomcd) {
            RequestMCD();
        }

        if (id == R.id.swowatch) {
            RequestWatches();
        }

        if (id == R.id.spcday1) {
            RequestSwody1();
        }

        if (id == R.id.spcday2) {
            RequestSwody2();
        }

        if (id == R.id.spcday3) {
            RequestSwody3();
        }

        if (id == R.id.spcday48) {
            RequestSwody48();
        }

        if (id == R.id.spcfire) {
            RequestSPCFireWeather();
        }


        if (id == R.id.visual) {
            RequestVisibleSatellite(0);
        }

        if (id == R.id.visualloop) {
            RequestVisibleSatellite(6);
        }

        if (id == R.id.infrared) {
            RequestIRSatellite(0);
        }

        if (id == R.id.infraredloop) {
            RequestIRSatellite(6);
        }

        if (id == R.id.watervapor) {
            RequestWaterVaporSatellite(0);
        }


        if (id == R.id.watervaporloop) {
            RequestIRSatellite(6);
        }

        if (id == R.id.visualconus) {
            requestVisConus(1);
        }

        if (id == R.id.infraredconus) {
            requestIRConus(1);
        }

        if (id == R.id.watervaporconus) {
            requestWVConus(1);
        }


        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    public void ShowUrl(String url) {
        if (isOnline(this)) {
            Intent urlActivity = new Intent(this, MyWebView.class);
            urlActivity.setClass(this, MyWebView.class);
            urlActivity.putExtra("own.goessatellite.webview.url", url);

            Log.d("WEBVIEW", "Webview startActivity>" + url);

            startActivity(urlActivity);
            return;
        }

        sendToast("No data connectivity");
    }


    public void RequestConusRadar() {
        ShowUrl("https://radar.weather.gov/ridge/Conus/RadarImg/latest.gif");
    }


    public void RequestSPCFireWeather() {
        ShowUrl("http://www.spc.noaa.gov/m/#day1fire");
    }

    public void requestIRConus(int numFrames) {
        ShowUrl("https://weather.msfc.nasa.gov/cgi-bin/get-abi?satellite=GOESEastconusband14&lat=35&lon=-97&zoom=4&width=900&height=900&info=ir&palette=ir9.pal&type=Animation&numframes=" + numFrames);
    }

    public void requestVisConus(int numFrames) {
        ShowUrl("https://weather.msfc.nasa.gov/cgi-bin/get-abi?satellite=GOESEastconusband02&lat=35&lon=-97&zoom=4&width=900&height=900&info=vis&palette=ir1.pal&type=Animation&numframes=" + numFrames);
    }

    public void requestWVConus(int numFrames) {
        ShowUrl("https://weather.msfc.nasa.gov/cgi-bin/get-abi?satellite=GOESEastconusband14&lat=35&lon=-97&zoom=4&width=900&height=900&info=wv&palette=wv3.pal&type=Animation&numframes=" + numFrames);
    }



    public void RequestIRSatellite(int numFrames) {
        String url;
        float lat = 0.0f;
        float lon = 0.0f;
        if (mylocation != null) {
            lat = (float) mylocation.getLatitude();
            lon = (float) mylocation.getLongitude();
        }
        if (lat == 0.0f) {
            lat = 36.0f;
        }
        if (lon == 0.0f) {
            lon = -96.0f;
        }
        if (lon < -104.0f) {
            url = "https://weather.msfc.nasa.gov/cgi-bin/get-goes?satellite=GOES-W%20PACUS&lat=" + ((int) lat) + "&lon=" + ((int) lon) + "&zoom=2&width=900&height=900&map=county&quality=80&palette=ir9.pal";
        } else {
            url = "https://weather.msfc.nasa.gov/cgi-bin/get-abi?satellite=GOESEastconusband14&lat=" + ((int) lat) + "&lon=" + ((int) lon) + "&zoom=2&width=900&height=900&map=county&quality=80&palette=ir9.pal";
        }
        if (numFrames > 0) {
            url = url + String.format(Locale.US, "&type=Animation&numframes=%d", new Object[]{Integer.valueOf(numFrames)});
        }
        ShowUrl(url);
    }

    public void RequestMCD() {
        ShowUrl("http://www.spc.noaa.gov/m/#mesodisc");
    }

    public void SPCMesoAnalysis() {
        ShowUrl("http://www.spc.noaa.gov/exper/mesoanalysis/new/mobile.php");
    }

    public void SPCHomePage() {
        ShowUrl("http://www.spc.noaa.gov/m/");
    }

    public void RequestVisibleSatellite(int numFrames) {
        String url;
        float lat = 0.0f;
        float lon = 0.0f;
        if (mylocation != null) {
            lat = (float) mylocation.getLatitude();
            lon = (float) mylocation.getLongitude();
        }
        if (lat == 0.0f) {
            lat = 36.0f;
        }
        if (lon == 0.0f) {
            lon = -96.0f;
        }
        if (lon < -104.0f) {
            url = "https://weather.msfc.nasa.gov/cgi-bin/get-goes?satellite=GOES-W%20PACUS&lat=" + ((int) lat) + "&lon=" + ((int) lon) + "&zoom=2&width=900&height=900&map=county&quality=80&info=vis&palette=ir1.pal";
        } else {
            url = "https://weather.msfc.nasa.gov/cgi-bin/get-abi?satellite=GOESEastconusband02&lat=" + ((int) lat) + "&lon=" + ((int) lon) + "&zoom=2&width=900&height=900&map=county&quality=80&info=vis&palette=ir1.pal";
        }
        if (numFrames > 0) {
            url = url + String.format(Locale.US, "&type=Animation&numframes=%d", new Object[]{Integer.valueOf(numFrames)});
        }
        ShowUrl(url);
    }

    public void RequestWaterVaporSatellite(int numFrames) {
        String url;
        float lat = 0.0f;
        float lon = 0.0f;
        if (mylocation != null) {
            lat = (float) mylocation.getLatitude();
            lon = (float) mylocation.getLongitude();
        }
        if (lat == 0.0f) {
            lat = 36.0f;
        }
        if (lon == 0.0f) {
            lon = -96.0f;
        }
        if (lon < -104.0f) {
            url = "https://weather.msfc.nasa.gov/cgi-bin/get-goes?satellite=GOES-W%20PACUS&lat=" + ((int) lat) + "&lon=" + ((int) lon) + "&zoom=2&width=900&height=900&palette=wv3.pal&map=county&quality=80&info=wv";
        } else {
            url = "https://weather.msfc.nasa.gov/cgi-bin/get-abi?satellite=GOESEastconusband08&lat=" + ((int) lat) + "&lon=" + ((int) lon) + "&zoom=2&width=900&height=900&palette=wv3.pal&map=county&quality=80&info=wv";
        }
        if (numFrames > 0) {
            url = url + String.format(Locale.US, "&type=Animation&numframes=%d", new Object[]{Integer.valueOf(numFrames)});
        }
        ShowUrl(url);
    }

    public void RequestWatches() {
        ShowUrl("http://www.spc.noaa.gov/m/#watches");
    }

    public void RequestSwody1() {
        ShowUrl("http://www.spc.noaa.gov/m/#day1ac");
    }

    public void RequestSwody2() {
        ShowUrl("http://www.spc.noaa.gov/m/#day2ac");
    }

    public void RequestSwody3() {
        ShowUrl("http://www.spc.noaa.gov/m/#day3ac");
    }

    public void RequestSwody48() {
        ShowUrl("http://www.spc.noaa.gov/m/#day48ac");
    }



    public void sendToast(String text) {
        Toast.makeText(Main.this, text, Toast.LENGTH_LONG).show();
    }


/*
    //gps stuff
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mylocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mylocation != null) {
            Toast.makeText(this, "lat: " +String.valueOf(mylocation.getLatitude()) + " lon: " + String.valueOf(mylocation.getLongitude()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No location detected. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        double lat = mylocation.getLatitude();
        double lon = mylocation.getLongitude();
        String mylat = String.valueOf(lat);
        String mylon = String.valueOf(lon);
        Log.i(TAG, "onLocationChanged lat: " + mylat + " lon: " + mylon);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
*/


    public static boolean isOnline(Context ctxt) {
        NetworkInfo ni = ((ConnectivityManager) ctxt.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (ni == null || ni.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    ////gps stufff///

    @Override
    protected void onStart() {
        super.onStart();
        GoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (GoogleApiClient != null && GoogleApiClient.isConnected()) {
            GoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_UPDATES_KEY)) {
                mRequestingUpdates = savedInstanceState.getBoolean(REQUESTING_UPDATES_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mylocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            lat = mylocation.getLatitude();
            lon = mylocation.getLongitude();
            mylat = String.valueOf(lat);
            mylon = String.valueOf(lon);
            Log.i(TAG, "updateValuesFromBundle lat: " + mylat + " lon: " + mylon);

        }
    }


    private void initializeGoogleAPI() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS) {
            GoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
        }
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPSUPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startGPSUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(GoogleApiClient, mLocationRequest, this);
        mRequestingUpdates = true;
        Log.d(TAG, "GPS updates started");

    }

    private void stopGPSUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(GoogleApiClient, this);
        mRequestingUpdates = false;
        Log.d(TAG, "GPS updates stopped");
    }



    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mylocation == null) {
            mylocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApiClient);
            lat = mylocation.getLatitude();
            lon = mylocation.getLongitude();
            mylat = String.valueOf(lat);
            mylon = String.valueOf(lon);
            Log.i(TAG, "onConnected lat: " + mylat + " lon: " + mylon);
        }
        if (mRequestingUpdates) {
            startGPSUpdates();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        lat = mylocation.getLatitude();
        lon = mylocation.getLongitude();
        mylat = String.valueOf(lat);
        mylon = String.valueOf(lon);
        Log.i(TAG, "onLocationChanged lat: " + mylat + " lon: " + mylon);

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        GoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_UPDATES_KEY, mRequestingUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mylocation);
        super.onSaveInstanceState(savedInstanceState);
    }
/////end of gps stuff//////


    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(Main.this);
            // Set progressdialog title
            mProgressDialog.setTitle(R.string.app_name);
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            myimage.setImageBitmap(result);
            // Close progressdialog
            mProgressDialog.dismiss();
        }
    }



}

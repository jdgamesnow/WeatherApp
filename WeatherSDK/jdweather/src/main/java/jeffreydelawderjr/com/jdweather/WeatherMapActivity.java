package jeffreydelawderjr.com.jdweather;

import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;


import com.android.volley.Response;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.jar.Manifest;

/**
 * Created by jdelawde on 3/20/2016.
 */
public class WeatherMapActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private android.location.Location mCurrentLocation;
    private static final int REQUEST_LOCATION = 1;

    public JDWeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);

        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void initializeMapFragmentWithID(int id){
        GoogleMapOptions mapOptions = new GoogleMapOptions();
        mapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(false)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true);
        mMapFragment = MapFragment.newInstance();


        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(id,mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
    }

    public void initializeWeatherManagerWithAppId(String appID){
        weatherManager = JDWeatherManager.getInstance(getApplicationContext(),appID);
    }

    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i("WeatherSDK", "onMapReady");
        mMap = map;
        if (mCurrentLocation != null){
            zoomToLocation(mCurrentLocation);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult r){

    }

    @Override
    public void onConnectionSuspended(int r){

    }

    @Override
    public void onConnected(Bundle bundle){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            zoomToLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        }


    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission has been granted, continue as usual
                try {
                    zoomToLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
                }catch (SecurityException e){

                }
            } else {
                // Permission was denied or request was cancelled
            }
        }
    }

    public void zoomToLocation(android.location.Location location){
        mCurrentLocation = location;// new Point((int)location.getLatitude(), (int)location.getLongitude());
        if (weatherManager != null) {
            weatherManager.updateCurrentWeatherForLatLong(new LatLng(location.getLatitude(), location.getLongitude()), new Response.Listener<Location>() {
                @Override
                public void onResponse(Location response) {
                    Log.i("WeatherSDK", "Current weather is " + response.currentWeather.title);
                }
            });
        }
        if (mMap != null){
            Log.i("WeatherSDK", "Zooming to Location");
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(14.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.moveCamera(cameraUpdate);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Marker"));
        }
    }
}

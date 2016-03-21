package jeffreydelawderjr.com.jdweather;

import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.jar.Manifest;

/**
 * Created by jdelawde on 3/20/2016.
 */
public class WeatherMapActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private LatLng mCurrentLocation;
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
        mMap = map;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
        if (mCurrentLocation != null){
            zoomToLocation(mCurrentLocation);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                zoomToLocation(latLng);
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult r){

    }

    @Override
    public void onConnectionSuspended(int r){

    }

    @Override
    public void onConnected(Bundle bundle){

    }

    public void detectLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            zoomToLocation(mCurrentLocation);

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
                    android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    zoomToLocation(mCurrentLocation);
                }catch (SecurityException e){

                }
            } else {
                // Permission was denied or request was cancelled
            }
        }
    }

    public void zoomToLocation(final LatLng latlong){
        if (weatherManager != null) {
            weatherManager.updateCurrentWeatherForLatLong(latlong, new Response.Listener<Location>() {
                @Override
                public void onResponse(Location response) {
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(assetNameForWeather(response.currentWeather));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(response.latLong.latitude, response.latLong.longitude))
                            .title(response.locationName)
                            .anchor(.5f,.5f)
                            .icon(bitmap))
                            .setSnippet(response.currentWeather.toString());

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(response.latLong).zoom(10.0f).build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                    mMap.moveCamera(cameraUpdate);
                }
            });
        }
    }

    public int assetNameForWeather(Weather weather){
        int id = getResources().getIdentifier(weather.icon, "drawable", getPackageName());
        return id;
    }
}

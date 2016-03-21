package jeffreydelawderjr.com.jdweather;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by jdelawde on 3/19/2016.
 */
public class JDWeatherManager {

    public long apiUpdateTime;


    private static OpenWeatherMap openWeatherMap;
    private static WeatherDB weatherDB;

    private static Context mContext;
    private static JDWeatherManager mInstance;

    public JDWeatherManager (Context context){
        super();
        mContext = context;
    }

    public static synchronized JDWeatherManager getInstance(Context context, String appID){
        if (mInstance == null){
            mInstance = new JDWeatherManager(context);
            openWeatherMap = OpenWeatherMap.getInstance(context, appID);
            weatherDB = WeatherDB.getInstance(context);
        }
        return mInstance;
    }

    private long minutesSinceUpdated(Date timeOfForecast){
        Date currentDate = new Date();
        return TimeUnit.MILLISECONDS.toMinutes(currentDate.getTime() - timeOfForecast.getTime());
    }

    //Method for receiving lat and long
    public void addLocationWithLatLong(LatLng latLong, final Response.Listener<Location> locationListener){
        openWeatherMap.singleCityCurrentWeatherWithLatLong(latLong,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Add the response to the database
                        Location location = weatherDB.insertLocation(response);

                        // Let caller know or update map
                        locationListener.onResponse(location);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Display or return some error code
                    }
                });
    }

    public void updateCurrentWeatherForLocation(Location location, final Response.Listener<Location> locationListener){
        openWeatherMap.singleCityCurrentWeatherWithCityId(Integer.toString(location.locationID),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Add the response to the database
                        Location location = weatherDB.insertLocation(response);

                        // Let caller know or update map
                        locationListener.onResponse(location);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Display or return some error code
                    }
                });
    }

    // Method for retrieving current weather data for single locations
    public void updateCurrentWeatherForLatLong(LatLng latLong, Response.Listener<Location> locationListener){
        //Check if location exists in database
        Location location = weatherDB.getLocationForLatLong(latLong);
        if (location == null) {
            // Then we need to make an api call to initialize this location
            addLocationWithLatLong(latLong, locationListener);
        }
        else {
            weatherDB.populateCurrentWeatherForLocation(location);
            if (location.currentWeather == null || minutesSinceUpdated(location.currentWeather.weatherDate) > 15){
                // Then will need to make an api call to update this current weather
                updateCurrentWeatherForLocation(location, locationListener);
            }
        }
    }


}

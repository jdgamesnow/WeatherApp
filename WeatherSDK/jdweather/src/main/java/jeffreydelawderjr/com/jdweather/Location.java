package jeffreydelawderjr.com.jdweather;

import android.content.ContentValues;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jdelawde on 3/19/2016.
 */
public class Location {

    public String locationName;
    public int locationID;
    public LatLng latLong;
    public Weather currentWeather;
    public Weather[] forecast;

    public static Location locationFromJSONObject(JSONObject jsonObject){
        Location location = new Location();

        if (jsonObject.has("list")){
            location.forecast = new Weather[JSONHelper.intValueForKeyWithDefault(jsonObject,"cnt",0)];
            JSONArray list = JSONHelper.jsonArrayForKeyWithDefault(jsonObject,"list",null);
            for (int i = 0; i < list.length(); i++){
                Weather weather = Weather.weatherFromJSONObject(JSONHelper.jsonObjectAtIndex(list,i));
                if (weather != null){
                    location.forecast[i] = weather;
                }
            }
        }
        else {
            Weather weather = Weather.weatherFromJSONObject(jsonObject);
            if (weather != null){
                location.currentWeather = weather;
            }
        }


        // Current and 5day forcast have different ways of returning city data
        jsonObject = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"city",jsonObject);

        location.locationName = JSONHelper.stringValueForKeyWithDefault(jsonObject,"name","");
        location.locationID = JSONHelper.intValueForKeyWithDefault(jsonObject,"id",-1);
        JSONObject coord = JSONHelper.jsonObjectForKeyWithDefault(jsonObject, "coord", null);
        if (coord != null){
            float lat = JSONHelper.floatValueForKeyWithDefault(coord, "lat", 0);
            float lon = JSONHelper.floatValueForKeyWithDefault(coord, "lon", 0);
            location.latLong = new LatLng(lat,lon);
        }
        return location;
    }

    public static Location[] locationsFromJSONArray(JSONObject jsonObject){
        ArrayList<Location> locations = new ArrayList<Location>();
        JSONArray jsonArray = JSONHelper.jsonArrayForKeyWithDefault(jsonObject,"list",null);
        if (jsonArray != null){
            for (int i = 0; i < jsonArray.length(); i++){
                Location loc = Location.locationFromJSONObject(JSONHelper.jsonObjectAtIndex(jsonArray,i));

                // Check for null
                if (loc != null){
                    locations.add(loc);
                }
            }
        }
        return locations.toArray(new Location[locations.size()]);
    }

    public ContentValues contentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WeatherDB.LOCATIONS_COLUMN_CITY_NAME, locationName);
        cv.put(WeatherDB.LOCATIONS_COLUMN_CITY_ID, locationID);
        cv.put(WeatherDB.LOCATIONS_COLUMN_LONGITUDE, latLong.latitude);
        cv.put(WeatherDB.LOCATIONS_COLUMN_LATITUDE, latLong.longitude);
        return cv;
    }
}

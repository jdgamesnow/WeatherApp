package jeffreydelawderjr.com.jdweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.android.gms.maps.model.LatLng;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jdelawde on 3/19/2016.
 */
public class WeatherDB extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "WeatherDatabase";

    // Location Table
    public static final String LOCATIONS_TABLE_NAME = "loc";
    public static final String LOCATIONS_COLUMN_LONGITUDE = "long";
    public static final String LOCATIONS_COLUMN_LATITUDE = "lat";
    public static final String LOCATIONS_COLUMN_CITY_ID = "c_id";
    public static final String LOCATIONS_COLUMN_CITY_NAME = "name";
   // public static final String LOCATIONS_COLUMN_CITY_ORDER = "c_order";

    // Weather Table
    public static final String WEATHER_TABLE_NAME = "weather";
    public static final String WEATHER_COLUMN_WEATHER_FORECAST_TIME = "time";
    public static final String WEATHER_COLUMN_CITY_ID = "c_id";
    public static final String WEATHER_COLUMN_ICON = "icon";
    public static final String WEATHER_COLUMN_TITLE = "title";
    public static final String WEATHER_COLUMN_DESCRIPTION = "desc";
    public static final String WEATHER_COLUMN_TEMPERATURE = "temp";
    public static final String WEATHER_COLUMN_PRESSURE = "pres";
    public static final String WEATHER_COLUMN_HUMIDITY = "humid";
    public static final String WEATHER_COLUMN_MINIMUM_TEMPERATURE = "min_t";
    public static final String WEATHER_COLUMN_MAXIMUM_TEMPERATURE = "max_t";
    public static final String WEATHER_COLUMN_SEA_LEVEL = "sea";
    public static final String WEATHER_COLUMN_GROUND_LEVEL = "grnd";
    public static final String WEATHER_COLUMN_WIND_SPEED = "w_speed";
    public static final String WEATHER_COLUMN_WIND_DIRECTION = "w_dir";
    public static final String WEATHER_COLUMN_CLOUDINESS = "cloud";
    public static final String WEATHER_COLUMN_RAIN_VOLUME = "rain";
    public static final String WEATHER_COLUMN_SNOW_VOLUME = "snow";
    public static final String WEATHER_COLUMN_SUNRISE = "sunrise";
    public static final String WEATHER_COLUMN_SUNSET = "sunset";
    public static final String WEATHER_COLUMN_IS_FORECAST = "is_forecast";

    private static WeatherDB mInstance;


    public WeatherDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static synchronized WeatherDB getInstance(Context context){
        if (mInstance == null){
            mInstance = new WeatherDB(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + LOCATIONS_TABLE_NAME + "("
            + LOCATIONS_COLUMN_CITY_ID + " integer primary key, "
            + LOCATIONS_COLUMN_CITY_NAME + " text, "
            + LOCATIONS_COLUMN_LATITUDE + " real, "
            + LOCATIONS_COLUMN_LONGITUDE + " real)");

        db.execSQL("create table " + WEATHER_TABLE_NAME + "("
                + WEATHER_COLUMN_WEATHER_FORECAST_TIME + " integer primary key, "
                + WEATHER_COLUMN_CITY_ID + " integer, "
                + WEATHER_COLUMN_TITLE + " text, "
                + WEATHER_COLUMN_DESCRIPTION + " text, "
                + WEATHER_COLUMN_ICON + " text, "
                + WEATHER_COLUMN_TEMPERATURE + " real, "
                + WEATHER_COLUMN_PRESSURE + " integer, "
                + WEATHER_COLUMN_HUMIDITY + " integer, "
                + WEATHER_COLUMN_MINIMUM_TEMPERATURE + " real, "
                + WEATHER_COLUMN_MAXIMUM_TEMPERATURE + " real, "
                + WEATHER_COLUMN_SEA_LEVEL + " real, "
                + WEATHER_COLUMN_GROUND_LEVEL + " real, "
                + WEATHER_COLUMN_WIND_SPEED + " real, "
                + WEATHER_COLUMN_WIND_DIRECTION + " real, "
                + WEATHER_COLUMN_CLOUDINESS + " integer, "
                + WEATHER_COLUMN_SUNRISE + " integer, "
                + WEATHER_COLUMN_SUNSET + " integer, "
                + WEATHER_COLUMN_IS_FORECAST + " integer, "
                + WEATHER_COLUMN_RAIN_VOLUME + " real, "
                + WEATHER_COLUMN_SNOW_VOLUME + " real,"
                + "FOREIGN KEY (" + WEATHER_COLUMN_CITY_ID + ") REFERENCES " + LOCATIONS_TABLE_NAME + "(" + LOCATIONS_COLUMN_CITY_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LOCATIONS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME);
        onCreate(db);
    }

    // Insert a locations into locations table. Expects unique city id
    public boolean insertLocation (Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        long rowID = db.insertWithOnConflict(LOCATIONS_TABLE_NAME, null, location.contentValues(), SQLiteDatabase.CONFLICT_IGNORE);
        insertWeatherForLocation(location);
        return  rowID >= 0;
    }

    public Location insertLocation(JSONObject jsonObject) {
        Location location = Location.locationFromJSONObject(jsonObject);
        insertLocation(location);
        return location;
    }

    public void insertLocations(JSONObject jsonObject) {
        Location[] locations = Location.locationsFromJSONArray(jsonObject);
        for (int i = 0; i < locations.length; i++){
            insertLocation(locations[i]);
        }
    }

    public boolean deleteLocation (Location location){
        SQLiteDatabase db = this.getWritableDatabase();
        int rowCount = db.delete(LOCATIONS_TABLE_NAME, LOCATIONS_COLUMN_CITY_ID + "=" + location.locationID, null);
        return rowCount > 0;
    }

    public int numberOfLocations(){
        return (int) DatabaseUtils.queryNumEntries(this.getReadableDatabase(), LOCATIONS_TABLE_NAME);
    }

    // Various methods for returning a location object
    public Location getLocationForCityID(int cityID){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + LOCATIONS_TABLE_NAME
                + " where " + LOCATIONS_COLUMN_CITY_ID + "=" + cityID, null);
        return locationFromCursor(cursor);
    }


    public Location getLocationForLatLong(LatLng latLong){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + LOCATIONS_TABLE_NAME
                + " where " + LOCATIONS_COLUMN_LONGITUDE + "=" + latLong.longitude
                + " AND " + LOCATIONS_COLUMN_LATITUDE + "=" + latLong.latitude, null);
        return locationFromCursor(cursor);
    }

    public Location[] getAllLocations(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + LOCATIONS_TABLE_NAME, null);
        Location[] locations = new Location[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++){
            locations[i] = locationFromCursor(cursor);
            cursor.moveToNext();
        }
        return locations;
    }

    public Location locationFromCursor(Cursor cursor){
        Location location = null;
        if (cursor.getCount() > 0){
            location = new Location();
            location.locationID = cursor.getInt(cursor.getColumnIndex(LOCATIONS_COLUMN_CITY_ID));
            location.locationName = cursor.getString(cursor.getColumnIndex(LOCATIONS_COLUMN_CITY_NAME));
            float lat = cursor.getFloat(cursor.getColumnIndex(LOCATIONS_COLUMN_LATITUDE));
            float lon = cursor.getFloat(cursor.getColumnIndex(LOCATIONS_COLUMN_LONGITUDE));
            location.latLong = new LatLng(lat, lon);
        }
        return location;
    }

    // Inserts weather row into weather table
    public void insertWeatherForLocation(Location location){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean successful = true;

        if (location.currentWeather != null){
            ContentValues cv = location.currentWeather.contentValues();

            // set the location id
            cv.put(WEATHER_COLUMN_CITY_ID, location.locationID);
            cv.put(WEATHER_COLUMN_IS_FORECAST, 0);
            db.insertWithOnConflict(WEATHER_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }

        if (location.forecast != null && location.forecast.length > 0){
            for (int i = 0; i < location.forecast.length; i++){
                ContentValues cv = location.forecast[i].contentValues();

                // set the location id
                cv.put(WEATHER_COLUMN_CITY_ID, location.locationID);
                cv.put(WEATHER_COLUMN_IS_FORECAST, 1);

                long rowID = db.insertWithOnConflict(WEATHER_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                if (rowID < 0) {
                    successful = false;
                }
            }
        }
    }

    public void populateCurrentWeatherForLocation(Location location){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + WEATHER_TABLE_NAME
                + " where " + WEATHER_COLUMN_CITY_ID + "=" + location.locationID
                + " AND " + WEATHER_COLUMN_IS_FORECAST + "=1", null);
        location.currentWeather = weatherFromCursor(cursor);
    }

    public void populateForecastWeatherForLocation(Location location){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + WEATHER_TABLE_NAME
                + " where " + WEATHER_COLUMN_CITY_ID + "=" + location.locationID
                + " AND " + WEATHER_COLUMN_IS_FORECAST + "=1", null);
        Weather[] weatherObjects = new Weather[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++){
            weatherObjects[i] = weatherFromCursor(cursor);
            cursor.moveToNext();
        }
        location.forecast = weatherObjects;
    }

    public Weather weatherFromCursor(Cursor cursor){
        Weather weather = new Weather();
        weather.setWeatherTime(cursor.getInt(cursor.getColumnIndex(WEATHER_COLUMN_WEATHER_FORECAST_TIME)));
        weather.setSunsetTime(cursor.getInt(cursor.getColumnIndex(WEATHER_COLUMN_SUNSET)));
        weather.setSunriseTime(cursor.getInt(cursor.getColumnIndex(WEATHER_COLUMN_SUNRISE)));
        weather.description = cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_DESCRIPTION));
        weather.title = cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_TITLE));
        weather.cloudiness = cursor.getInt(cursor.getColumnIndex(WEATHER_COLUMN_CLOUDINESS));
        weather.pressure = cursor.getInt(cursor.getColumnIndex(WEATHER_COLUMN_PRESSURE));
        weather.humidity = cursor.getInt(cursor.getColumnIndex(WEATHER_COLUMN_HUMIDITY));
        weather.icon = cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_ICON));

        weather.groundLevel = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_GROUND_LEVEL));
        weather.seaLevel = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_SEA_LEVEL));
        weather.maxTemperature = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_MAXIMUM_TEMPERATURE));
        weather.minTemperature = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_MINIMUM_TEMPERATURE));
        weather.rainVolume = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_RAIN_VOLUME));
        weather.snowVolume = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_SNOW_VOLUME));
        weather.windDirection = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_WIND_DIRECTION));
        weather.windSpeed = cursor.getFloat(cursor.getColumnIndex(WEATHER_COLUMN_WIND_SPEED));

        return weather;
    }

    public Cursor getWeatherDataForCityId(String cityID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + WEATHER_TABLE_NAME
                + " where " + WEATHER_COLUMN_CITY_ID + "=" + cityID, null);
        return cursor;
    }
}

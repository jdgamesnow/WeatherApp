package jeffreydelawderjr.com.jdweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

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
    public static final String LOCATIONS_COLUMN_CITY_ORDER = "c_order";

    // Weather Table
    public static final String WEATHER_TABLE_NAME = "weather";
    public static final String WEATHER_COLUMN_WEATHER_FORECAST_TIME = "time";
    public static final String WEATHER_COLUMN_CITY_ID = "c_id";
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


    public WeatherDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + LOCATIONS_TABLE_NAME + "("
            + LOCATIONS_COLUMN_CITY_ID + " integer primary key, "
            + LOCATIONS_COLUMN_CITY_NAME + " text, "
            + LOCATIONS_COLUMN_CITY_ORDER + " integer AUTOINCREMENT NOT NULL, "
            + LOCATIONS_COLUMN_LATITUDE + " integer, "
            + LOCATIONS_COLUMN_LONGITUDE + " integer)");

        db.execSQL("create table " + WEATHER_TABLE_NAME + "("
                + WEATHER_COLUMN_WEATHER_FORECAST_TIME + " integer primary key, "
                + "FOREIGN KEY (" + WEATHER_COLUMN_CITY_ID + ") REFERENCES " + LOCATIONS_TABLE_NAME + "(" + LOCATIONS_COLUMN_CITY_ID + "), "
                + WEATHER_COLUMN_TITLE + " text, "
                + WEATHER_COLUMN_DESCRIPTION + " text, "
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
                + WEATHER_COLUMN_RAIN_VOLUME + " real, "
                + WEATHER_COLUMN_SNOW_VOLUME + " real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LOCATIONS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME);
        onCreate(db);
    }

    // Insert a locations into locations table. Expects unique city id
    public boolean insertLocation (String name, int longitude, int latitude, int cityId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(LOCATIONS_COLUMN_CITY_ID, cityId);
        cv.put(LOCATIONS_COLUMN_CITY_NAME, name);
        cv.put(LOCATIONS_COLUMN_LONGITUDE, longitude);
        cv.put(LOCATIONS_COLUMN_LATITUDE, latitude);

        long rowID = db.insertWithOnConflict(LOCATIONS_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return  rowID > 0;
    }

    public boolean deleteLocation (int cityId){
        SQLiteDatabase db = this.getWritableDatabase();
        int rowCount = db.delete(LOCATIONS_TABLE_NAME, LOCATIONS_COLUMN_CITY_ID + "=" + cityId,null);
        return rowCount > 0;
    }

    public int numberOfLocations(){
        return (int) DatabaseUtils.queryNumEntries(this.getReadableDatabase(), LOCATIONS_TABLE_NAME);
    }

    // Returns the OpenWeatherMap city id based on Lat = x Long = y
    public String getCityIdForLatLong(Point coord){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + LOCATIONS_COLUMN_CITY_ID
                + " from " + LOCATIONS_TABLE_NAME
                + " where " + LOCATIONS_COLUMN_LONGITUDE + "=" + coord.x
                + " AND " + LOCATIONS_COLUMN_LATITUDE + "=" + coord.y, null);
        String cityID = null;
        if (cursor.getColumnCount() > 0){
            cityID = cursor.getString(0);
        }
        return cityID;
    }

    // Returns city name for city id
    public String getLocationNameForCityId(int cityID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + LOCATIONS_COLUMN_CITY_NAME
                + " from " + LOCATIONS_TABLE_NAME
                + " where " + LOCATIONS_COLUMN_CITY_ID + "=" + cityID, null);
        String cityName = null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0){
            cityName = cursor.getString(0);
        }
        return cityName;
    }

    // Returns Lat and Long for a city id where x = lat and y = long
    public Point getLatLongForCityId(int cityID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + LOCATIONS_COLUMN_LONGITUDE + ", " + LOCATIONS_COLUMN_LATITUDE
                + " from " + LOCATIONS_TABLE_NAME
                + " where " + LOCATIONS_COLUMN_CITY_ID + "=" + cityID, null);
        Point latLong = null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() == 2){
            latLong = new Point();
            latLong.x = cursor.getInt(0);
            latLong.y = cursor.getInt(1);
        }
        return latLong;
    }

    // Insert weather row into weather table, expects a city id
    public boolean insertWeatherWithData(ContentValues cv){
        if (cv.getAsString(WEATHER_COLUMN_CITY_ID) == null && cv.getAsInteger(WEATHER_COLUMN_WEATHER_FORECAST_TIME) == 0){
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        long rowID = db.insertWithOnConflict(WEATHER_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return  rowID > 0;
    }

    public Cursor getWeatherDataForCityId(String cityID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * "
                + " from " + WEATHER_TABLE_NAME
                + " where " + WEATHER_COLUMN_CITY_ID + "=" + cityID, null);
        return cursor;
    }
}

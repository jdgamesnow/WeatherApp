package jeffreydelawderjr.com.jdweather;

import android.content.ContentValues;
import android.graphics.Point;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jdelawde on 3/19/2016.
 */
public class Weather {
    public String title;
    public String description;
    public float temperature;
    public float minTemperature;
    public float maxTemperature;
    public float rainVolume;
    public float snowVolume;
    public float seaLevel;
    public float groundLevel;
    public float windSpeed;
    public float windDirection;
    public int pressure;
    public int humidity;
    public int cloudiness;
    public long weatherTime;
    public long sunriseTime;
    public long sunsetTime;
    public Date sunriseDate;
    public Date sunsetDate;
    public Date weatherDate;

    public static Weather weatherFromJSONObject(JSONObject jsonObject){
        if (jsonObject == null){
            return null;
        }
        Weather weather = new Weather();
        JSONObject weatherObject = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"weather",null);
        if (weather != null){
            weather.title = JSONHelper.stringValueForKeyWithDefault(weatherObject,"main","");
            weather.description = JSONHelper.stringValueForKeyWithDefault(weatherObject,"description","");
        }

        JSONObject main = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"main",null);
        if (main != null){
            weather.temperature = JSONHelper.floatValueForKeyWithDefault(main,"temp",0);
            weather.minTemperature = JSONHelper.floatValueForKeyWithDefault(main,"temp_min",0);
            weather.maxTemperature = JSONHelper.floatValueForKeyWithDefault(main,"temp_max",0);
            weather.pressure = JSONHelper.intValueForKeyWithDefault(main, "pressure", 0);
            weather.humidity = JSONHelper.intValueForKeyWithDefault(main,"humidity",0);
            weather.seaLevel = JSONHelper.floatValueForKeyWithDefault(main, "sea_level", 0);
            weather.groundLevel = JSONHelper.floatValueForKeyWithDefault(main, "grnd_level", 0);
        }

        JSONObject wind = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"wind",null);
        if (wind != null){
            weather.windSpeed = JSONHelper.floatValueForKeyWithDefault(wind,"temp",0);
            weather.windDirection = JSONHelper.floatValueForKeyWithDefault(wind,"temp_min",0);
        }

        JSONObject clouds = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"clouds",null);
        if (clouds != null){
            weather.cloudiness = JSONHelper.intValueForKeyWithDefault(clouds, "all", 0);
        }

        JSONObject rain = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"rain",null);
        if (rain != null){
            weather.rainVolume = JSONHelper.floatValueForKeyWithDefault(rain, "3h", 0);
        }

        JSONObject snow = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"snow",null);
        if (snow != null){
            weather.snowVolume = JSONHelper.floatValueForKeyWithDefault(snow, "3h", 0);
        }

        JSONObject sys = JSONHelper.jsonObjectForKeyWithDefault(jsonObject,"sys",null);
        if (sys != null){
            weather.setSunriseTime(JSONHelper.longValueForKeyWithDefault(sys, "sunrise", 0) * 1000L);
            weather.setSunsetTime(JSONHelper.longValueForKeyWithDefault(sys, "sunset", 0) * 1000L);
            if (weather.sunsetTime > 0){
                Date date = new Date(weather.sunsetTime);
                weather.sunsetDate = date;
            }
        }
        weather.weatherTime = JSONHelper.longValueForKeyWithDefault(jsonObject,"dt",0) * 1000L;
        if (weather.weatherTime > 0){
            Date date = new Date(weather.weatherTime);
            weather.weatherDate = date;
        }
        return weather;
    }

    public void setSunriseTime(long time){
        sunriseTime = time;
        sunriseDate = new Date(sunriseTime);
    }

    public void setSunsetTime(long time){
        sunsetTime = time;
        sunsetDate = new Date(sunsetTime);
    }

    public void setWeatherTime(long time){
        weatherTime = time;
        weatherDate = new Date(weatherTime);
    }




    public ContentValues contentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WeatherDB.WEATHER_COLUMN_WEATHER_FORECAST_TIME, weatherTime);
        cv.put(WeatherDB.WEATHER_COLUMN_CLOUDINESS, cloudiness);
        cv.put(WeatherDB.WEATHER_COLUMN_DESCRIPTION, description);
        cv.put(WeatherDB.WEATHER_COLUMN_TITLE, title);
        cv.put(WeatherDB.WEATHER_COLUMN_GROUND_LEVEL, groundLevel);
        cv.put(WeatherDB.WEATHER_COLUMN_SEA_LEVEL, seaLevel);
        cv.put(WeatherDB.WEATHER_COLUMN_HUMIDITY, humidity);
        cv.put(WeatherDB.WEATHER_COLUMN_MAXIMUM_TEMPERATURE, maxTemperature);
        cv.put(WeatherDB.WEATHER_COLUMN_MINIMUM_TEMPERATURE, minTemperature);
        cv.put(WeatherDB.WEATHER_COLUMN_PRESSURE, pressure);
        cv.put(WeatherDB.WEATHER_COLUMN_RAIN_VOLUME, rainVolume);
        cv.put(WeatherDB.WEATHER_COLUMN_SNOW_VOLUME, snowVolume);
        cv.put(WeatherDB.WEATHER_COLUMN_SUNRISE, sunriseTime);
        cv.put(WeatherDB.WEATHER_COLUMN_SUNSET, sunsetTime);
        cv.put(WeatherDB.WEATHER_COLUMN_WIND_DIRECTION, windDirection);
        cv.put(WeatherDB.WEATHER_COLUMN_WIND_SPEED, windSpeed);
        return cv;
    }
}

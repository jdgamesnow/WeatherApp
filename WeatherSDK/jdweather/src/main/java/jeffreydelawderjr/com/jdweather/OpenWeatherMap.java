package jeffreydelawderjr.com.jdweather;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.model.LatLng;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by jdelawde on 3/19/2016.
 */
public class OpenWeatherMap {

    private static OpenWeatherMap mInstance;
    private static String mAppID;
    private static String mUnits;
    private static String mLanguage;
    private static Context context;
    private RequestQueue volleyQueue;

    public OpenWeatherMap(Context c){
        super();
        context = c;
        mLanguage = "en";
        mUnits = "metric";
        volleyQueue = getRequestQueue();
    }

    public static synchronized OpenWeatherMap getInstance(Context context, String appID) {
        if (mInstance == null) {
            mInstance = new OpenWeatherMap(context);
            mAppID = appID;
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (volleyQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            volleyQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return volleyQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void setAppID(String appID){
        mAppID = appID;
    }

    public void setUnits(String units){
        mUnits = units;
    }

    public void setLanguage(String language){
        mLanguage = language;
    }

    private static Uri.Builder owmUriBuilder(){
        return new Uri.Builder().scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5");
    }

    private static Uri.Builder appendStandardRequirements(Uri.Builder builder){
        return builder.appendQueryParameter("appid", mAppID)
                .appendQueryParameter("lang", mLanguage)
                .appendQueryParameter("units", mUnits);
    }

    private static Uri.Builder singleCityCurrentWeatherURI(){
        return appendStandardRequirements(owmUriBuilder()
                .appendPath("weather"));
    }

    private static Uri.Builder multiCityCurrentWeatherBoxURI(){
        return appendStandardRequirements(owmUriBuilder()
                .appendPath("box").appendPath("city"));
    }

    private static Uri.Builder multiCityCurrentWeatherIdURI(){
        return appendStandardRequirements(owmUriBuilder()
                .appendPath("group"));
    }

    private static String cityIdString(String[] ids){
        StringBuilder result = new StringBuilder();
        for(String string : ids) {
            result.append(string);
            result.append(",");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1): "";
    }

    public void singleCityCurrentWeatherWithLatLong(LatLng latLong, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener){
        Uri.Builder builder = OpenWeatherMap.singleCityCurrentWeatherURI()
                .appendQueryParameter("lat", Double.toString(latLong.latitude))
                .appendQueryParameter("lon", Double.toString(latLong.longitude));

        String url = builder.build().toString();
        jsonRequest(url, responseListener, errorListener);
    }

    public void singleCityCurrentWeatherWithCityId(String cityID, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener){
        Uri.Builder builder = OpenWeatherMap.singleCityCurrentWeatherURI()
                .appendQueryParameter("id", cityID);

        String url = builder.build().toString();
        jsonRequest(url, responseListener, errorListener);
    }

    public void multiCityCurrentWeatherWithBox(Rect box, int zoom, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener){
        Uri.Builder builder = OpenWeatherMap.multiCityCurrentWeatherBoxURI()
                .appendQueryParameter("bbox", Integer.toString(box.left) + ","
                        + Integer.toString(box.bottom) + ","
                        + Integer.toString(box.right) + ","
                        + Integer.toString(box.top) + "," + zoom)
                .appendQueryParameter("cluster","yes");

        String url = builder.build().toString();
        jsonRequest(url, responseListener, errorListener);
    }

    public void multiCityCurrentWeatherWithIDs(String[] ids, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener){
        Uri.Builder builder = OpenWeatherMap.multiCityCurrentWeatherIdURI()
                .appendQueryParameter("id",cityIdString(ids));

        String url = builder.build().toString();
        jsonRequest(url, responseListener, errorListener);
    }

    public void jsonRequest(String url, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener){
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);
            addToRequestQueue(jsObjRequest);

        } else {
            // display error
            errorListener.onErrorResponse(null);
        }
    }
}

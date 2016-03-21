package jeffreydelawderjr.com.jdweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jdelawde on 3/19/2016.
 * Convenience class I created for retrieving values from jsonobject. Useful because sometimes the
 * api doesn't return all expected values; so by using this I can ensure that if one value is returned
 * incorrectly then it shouldn't stop the rest of the object from being parsed.
 */
public class JSONHelper {
    public static String stringValueForKeyWithDefault(JSONObject obj, String key, String def){
        try {
            return obj.getString(key);
        } catch (JSONException e){
            return def;
        }
    }

    public static int intValueForKeyWithDefault(JSONObject obj, String key, int def){
        try {
            return obj.getInt(key);
        } catch (JSONException e){
            return def;
        }
    }

    public static float floatValueForKeyWithDefault(JSONObject obj, String key, float def){
        try {
            return (float)obj.getDouble(key);
        } catch (JSONException e){
            return def;
        }
    }

    public static long longValueForKeyWithDefault(JSONObject obj, String key, long def){
        try {
            return obj.getLong(key);
        } catch (JSONException e){
            return def;
        }
    }

    public static  JSONObject jsonObjectForKeyWithDefault(JSONObject obj, String key, JSONObject def){
        try {
            return obj.getJSONObject(key);
        } catch (JSONException e){
            return def;
        }
    }

    public static JSONArray jsonArrayForKeyWithDefault(JSONObject obj, String key, JSONArray def){
        try {
            return obj.getJSONArray(key);
        } catch (JSONException e){
            return def;
        }
    }

    public static JSONObject jsonObjectAtIndex(JSONArray array, int index){
        try {
            return array.getJSONObject(index);
        } catch (JSONException e){
            return null;
        }
    }
}

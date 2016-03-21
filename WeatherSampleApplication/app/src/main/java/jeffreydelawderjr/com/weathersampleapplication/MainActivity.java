package jeffreydelawderjr.com.weathersampleapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jeffreydelawderjr.com.jdweather.WeatherMapActivity;

public class MainActivity extends WeatherMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeMapFragmentWithID(R.id.mapView);
    }

}

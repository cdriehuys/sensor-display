package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SensorPlotActivity extends AppCompatActivity implements SensorEventListener {
    private static final int POLLING_INTERVAL = 1000000;

    private static final String TAG = SensorPlotActivity.class.getSimpleName();

    private PlotView plotView;

    private Sensor sensor;

    private SensorManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_plot);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int sensorType = extras.getInt(getString(R.string.EXTRA_SENSOR_TYPE));

            manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = manager.getDefaultSensor(sensorType);

            manager.registerListener(this, sensor, POLLING_INTERVAL);
        }

        plotView = (PlotView) findViewById(R.id.plot_view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (manager != null && sensor != null) {
            manager.registerListener(this, sensor, POLLING_INTERVAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        float value = (float) Math.sqrt(x*x + y*y + z*z);

        Log.v(TAG, String.format("Received sensor value: %f", value));

        plotView.addPoint(value);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
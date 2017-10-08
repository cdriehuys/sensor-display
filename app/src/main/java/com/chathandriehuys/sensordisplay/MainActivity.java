package com.chathandriehuys.sensordisplay;

import android.content.Intent;
import android.hardware.Sensor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    /**
     * Handle click events from the activity.
     *
     * @param view The view that the click event occurred for.
     */
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_accelerometer:
                navigateSensorPlot(Sensor.TYPE_ACCELEROMETER);
                break;

            case R.id.btn_light_sensor:
                navigateSensorPlot(Sensor.TYPE_LIGHT);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorInfoView accelView = (SensorInfoView) findViewById(R.id.accelerometer_info);
        accelView.setSensorType(Sensor.TYPE_ACCELEROMETER);

        SensorInfoView lightSensorView = (SensorInfoView) findViewById(R.id.light_sensor_info);
        lightSensorView.setSensorType(Sensor.TYPE_LIGHT);
    }

    private void navigateSensorPlot(int sensorType) {
        Intent intent = new Intent(this, SensorPlotActivity.class);
        intent.putExtra(getString(R.string.EXTRA_SENSOR_TYPE), sensorType);

        startActivity(intent);
    }
}

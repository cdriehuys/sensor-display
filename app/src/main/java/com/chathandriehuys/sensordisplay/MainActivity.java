package com.chathandriehuys.sensordisplay;

import android.hardware.Sensor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorInfoView accelView = (SensorInfoView) findViewById(R.id.accelerometer_info);
        accelView.setSensorType(Sensor.TYPE_ACCELEROMETER);

        SensorInfoView proximityView = (SensorInfoView) findViewById(R.id.proximity_info);
        proximityView.setSensorType(Sensor.TYPE_PROXIMITY);
    }
}

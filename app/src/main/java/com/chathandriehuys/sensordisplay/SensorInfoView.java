package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.Locale;


public class SensorInfoView extends android.support.v7.widget.AppCompatTextView {
    private static final String INFO_FORMAT =
            "'%s' is present\n  - Range: %f\n  - Resolution: %f\n  - Delay: %d";

    private Resources resources;

    private Sensor sensor;

    public SensorInfoView(Context context) {
        super(context);

        init();
    }

    public SensorInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SensorInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @SuppressWarnings("unused")
    public SensorInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void setSensorType(int sensorType) {
        SensorManager manager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(sensorType);

        if (sensor != null) {
            setText(String.format(
                    Locale.US,
                    INFO_FORMAT,
                    sensor.getName(),
                    sensor.getMaximumRange(),
                    sensor.getResolution(),
                    sensor.getMinDelay()));
        } else {
            setText(resources.getString(R.string.sensor_info_not_found));
        }
    }

    /**
     * Initialize the view to display some loading text.
     */
    private void init() {
        resources = getResources();

        setText(resources.getString(R.string.sensor_info_loading));
    }
}

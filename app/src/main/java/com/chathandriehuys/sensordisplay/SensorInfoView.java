package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


public class SensorInfoView extends android.support.v7.widget.AppCompatTextView {
    private int sensorType;

    private Resources resources;

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

    /**
     * Get the type of sensor the view is displaying info for.
     *
     * @return An integer corresponding to the type of sensor the view is displaying information
     *         about.
     */
    public int getSensorType() {
        return sensorType;
    }

    /**
     * Set the type of sensor to display information for.
     *
     * The view will then obtain the default sensor for that type and display information about it.
     *
     * @param sensorType The type of sensor to read.
     */
    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;

        SensorManager manager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(sensorType);

        if (sensor != null) {
            setText(resources.getString(
                    R.string.sensor_info,
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

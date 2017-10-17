package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * Activity for plotting a sensor's data.
 */
public class SensorPlotActivity extends AppCompatActivity implements SensorEventListener {
    private static final int ACCELEROMETER_LOW_THRESHOLD = 10;
    private static final int ACCELEROMETER_HIGH_THRESHOLD = 15;
    private static final int LIGHT_THRESHOLD = 50;
    private static final int POLLING_INTERVAL = 1000000;

    private static final String TAG = SensorPlotActivity.class.getSimpleName();

    private ImageView animationView;

    private int animationViewHeight;
    private int currentAnimation;
    private int sensorType;

    private Sensor sensor;

    private SensorManager manager;

    private TimeSeries sensorData;

    /**
     * Handle action bar clicks.
     *
     * Solution adapted from the following URL:
     * https://stackoverflow.com/a/16755282/3762084
     *
     * @param item The item in the action bar that was selected.
     *
     * @return A boolean indicating if the menu item was handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate to the parent activity
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize the activity.
     *
     * @param savedInstanceState The activity's previous state if it's being resumed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_plot);

        // Create a new series to hold raw sensor data
        sensorData = new TimeSeries("Data");

        // Get the type of sensor the plot should display data for
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorType = extras.getInt(getString(R.string.EXTRA_SENSOR_TYPE));

            // Subscribe the activity to sensor events
            manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = manager.getDefaultSensor(sensorType);

            manager.registerListener(this, sensor, POLLING_INTERVAL);
        }

        // Add the raw data, mean, and variance to the plot
        PlotView plotView = (PlotView) findViewById(R.id.plot_view);

        plotView.addSeries(sensorData, Color.parseColor("#23af00"));
        plotView.addSeries(sensorData.getAverageSeries(), Color.parseColor("#2655ff"));
        plotView.addSeries(sensorData.getVarianceSeries(), Color.parseColor("#ffe732"));

        // Save a reference to the animation view
        animationView = (ImageView) findViewById(R.id.animation_view);
        animationView.requestLayout();
        animationViewHeight = animationView.getLayoutParams().height;

        // Enable the back button in the title bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Resume listening to sensor events.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // This may also be called before onCreate, so we need to make sure our manager and sensor
        // have been initialized.
        if (manager != null && sensor != null) {
            manager.registerListener(this, sensor, POLLING_INTERVAL);
        }
    }

    /**
     * Stop listening to sensor events when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();

        manager.unregisterListener(this);
    }

    /**
     * Receive data from the sensor the activity is listening to.
     *
     * @param sensorEvent The sensor event that was emitted.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // We only want the magnitude of the sensor event's value
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        float value = (float) Math.sqrt(x*x + y*y + z*z);

        Log.v(TAG, String.format("Received sensor value: %f", value));

        sensorData.addPoint(new DataPoint(value));

        updateAnimation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    private void updateAnimation() {
        float value = sensorData.getAverage();
        int newAnimation = 0;

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if (value < ACCELEROMETER_LOW_THRESHOLD) {
                newAnimation = R.drawable.stickman_slow;
            } else if (value < ACCELEROMETER_HIGH_THRESHOLD) {
                newAnimation = R.drawable.stickman_med;
            } else {
                newAnimation = R.drawable.stickman_fast;
            }
        } else if (sensorType == Sensor.TYPE_LIGHT) {
            if (value < LIGHT_THRESHOLD) {
                newAnimation = R.drawable.star;
            } else {
                newAnimation = R.drawable.sun;
            }
        } else {
            animationView.getLayoutParams().height = 0;
        }

        if (newAnimation != 0 && newAnimation != currentAnimation) {
            currentAnimation = newAnimation;
            animationView.setBackgroundResource(currentAnimation);

            animationView.getLayoutParams().height = animationViewHeight;

            AnimationDrawable anim = (AnimationDrawable) animationView.getBackground();
            anim.start();
        }
    }
}

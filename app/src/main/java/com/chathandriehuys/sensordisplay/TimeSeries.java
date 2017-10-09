package com.chathandriehuys.sensordisplay;

import java.util.ArrayList;
import java.util.Date;


/**
 * A series of data that is tracked over time.
 */
class TimeSeries {
    private static final int DOMAIN_MILLIS = 5000;

    private ArrayList<DataPoint> data;
    private ArrayList<TimeSeriesListener> listeners;

    private float average;

    private int domain;

    /**
     * Create a new series of data.
     */
    TimeSeries() {
        data = new ArrayList<>();
        listeners = new ArrayList<>();

        average = 0;

        domain = DOMAIN_MILLIS;
    }

    /**
     * Add a point to the series.
     *
     * Adding a point updates various statistics about the series, such as the average, and removes
     * any expired points from the series. It then notifies all the listeners about the new point.
     *
     * @param point The point to add to the series.
     */
    void addPoint(DataPoint point) {
        data.add(point);

        // Update the series' running average
        addToAverage(point.getData());

        // Remove any expired points
        Date now = new Date();
        long minTime = now.getTime() - domain;

        for (int i = data.size() - 1; i >= 0; i--) {
            if (data.get(i).getTimestamp().getTime() < minTime) {
                removePoint(i);
            }
        }

        // Notify listeners
        for (TimeSeriesListener listener : listeners) {
            listener.pointAdded(this, point);
        }
    }

    /**
     * Get a series that contains the average of the current series.
     *
     * The returned series is added as a listener to the current series so that it stays updated as
     * new points are added.
     *
     * @return A series that tracks the average of the current series.
     */
    TimeSeries getAverageSeries() {
        DerivedTimeSeries series = new DerivedTimeSeries() {
            @Override
            public void pointAdded(TimeSeries series, DataPoint point) {
                DataPoint average = new DataPoint(series.getAverage(), point.getTimestamp());
                addPoint(average);
            }
        };

        addListener(series);

        return series;
    }

    /**
     * Get the data associated with the series.
     *
     * @return A list of the data-points in the series.
     */
    ArrayList<DataPoint> getData() {
        return data;
    }

    /**
     * Get the series' domain.
     *
     * @return An interval containing the minimum and maximum x-values in the series.
     */
    Interval<Integer> getDomain() {
        return new Interval<>(0, domain);
    }

    /**
     * Get the series' range.
     *
     * @return An interval containing the minimum and maximum y-values in the series.
     */
    Interval<Float> getRange() {
        float min = Integer.MAX_VALUE;
        float max = Integer.MIN_VALUE;

        for (DataPoint point : data) {
            min = Math.min(point.getData(), min);
            max = Math.max(point.getData(), max);
        }

        return new Interval<>(min, max);
    }

    /**
     * Get a series that contains the variance of the current series.
     *
     * The returned series is added as a listener to the current series so that it stays updated as
     * new points are added.
     *
     * @return A series that tracks the variance of the current series.
     */
    TimeSeries getVarianceSeries() {
        DerivedTimeSeries series = new DerivedTimeSeries() {
            @Override
            public void pointAdded(TimeSeries series, DataPoint point) {
                DataPoint variance = new DataPoint(series.getVariance(), point.getTimestamp());
                addPoint(variance);
            }
        };

        addListener(series);

        return series;
    }

    /**
     * Add a listener to the series.
     *
     * The listener is notified each time a point is added to the current series.
     *
     * @param listener The listener to send notifications to.
     */
    private void addListener(TimeSeriesListener listener) {
        listeners.add(listener);
    }

    /**
     * Add a value to the series' running average.
     *
     * @param val The value to add to the series' average.
     */
    private void addToAverage(float val) {
        average = average * (data.size() - 1) / data.size() + val / data.size();
    }

    /**
     * Get the series' average value.
     *
     * @return The series' running average.
     */
    private float getAverage() {
        return average;
    }

    /**
     * Get the series' variance.
     *
     * @return The series' variance.
     */
    private float getVariance() {
        float sum = 0;

        for (DataPoint point : data) {
            sum += point.getData() * point.getData();
        }

        float average = getAverage();

        return sum / data.size() - average * average;
    }

    /**
     * Remove a value from the series' running average.
     *
     * @param val The value to remove from the series' average.
     */
    private void removeFromAverage(float val) {
        average = average * (data.size() + 1) / data.size() - val / data.size();
    }

    /**
     * Remove the point at the given index from the series.
     *
     * @param index The index of the point to remove.
     */
    private void removePoint(int index) {
        DataPoint point = data.remove(index);

        removeFromAverage(point.getData());
    }
}

package com.chathandriehuys.sensordisplay;

import java.util.ArrayList;
import java.util.Date;


class TimeSeries {
    /**
     * The default number of milliseconds the series should keep data for.
     */
    private static final int DOMAIN_MILLIS = 5000;

    private ArrayList<DataPoint> data;
    private ArrayList<TimeSeriesListener> listeners;

    private int domain;

    TimeSeries() {
        data = new ArrayList<>();
        listeners = new ArrayList<>();

        domain = DOMAIN_MILLIS;
    }

    void addListener(TimeSeriesListener listener) {
        listeners.add(listener);
    }

    void addPoint(DataPoint point) {
        data.add(point);

        Date now = new Date();
        long minTime = now.getTime() - domain;

        for (int i = data.size() - 1; i >= 0; i--) {
            if (data.get(i).getTimestamp().getTime() < minTime) {
                data.remove(i);
            }
        }

        for (TimeSeriesListener listener : listeners) {
            listener.pointAdded(this, point);
        }
    }

    float getAverage() {
        float sum = 0;

        for (DataPoint point : data) {
            sum += point.getData();
        }

        return sum / data.size();
    }

    ArrayList<DataPoint> getData() {
        return data;
    }

    Interval<Integer> getDomain() {
        return new Interval<>(0, domain);
    }

    Interval<Float> getRange() {
        float min = Integer.MAX_VALUE;
        float max = Integer.MIN_VALUE;

        for (DataPoint point : data) {
            min = Math.min(point.getData(), min);
            max = Math.max(point.getData(), max);
        }

        return new Interval<>(min, max);
    }

    float getVariance() {
        float sum = 0;

        for (DataPoint point : data) {
            sum += point.getData() * point.getData();
        }

        float average = getAverage();

        return sum / data.size() - average * average;
    }
}

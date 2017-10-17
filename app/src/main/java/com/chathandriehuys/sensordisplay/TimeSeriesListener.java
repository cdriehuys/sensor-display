package com.chathandriehuys.sensordisplay;


/**
 * An interface for listening to updates from a {@link TimeSeries}.
 */
interface TimeSeriesListener {

    /**
     * Handle the addition of a new point to the specified series.
     *
     * @param series The series the point was added to.
     * @param point The data-point added to the series.
     */
    void pointAdded(TimeSeries series, DataPoint point);
}

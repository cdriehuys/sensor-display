package com.chathandriehuys.sensordisplay;


import java.util.Date;


/**
 * A single point in a series.
 *
 * Each point has a value and an associated timestamp.
 */
class DataPoint {
    private float data;

    private Date timestamp;

    /**
     * Create a new data-point at the current time.
     *
     * @param data The value to attach to the data-point.
     */
    DataPoint(float data) {
        this(data, new Date());
    }

    /**
     * Create a new data-point at a specified time.
     *
     * @param data The value to attach to the data-point.
     * @param timestamp The timestamp to attach to the data-point.
     */
    DataPoint(float data, Date timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * Get the value associated with the data-point.
     *
     * @return The data-point's value.
     */
    float getData() { return data; }

    /**
     * Get the timestamp associated with the data-point.
     *
     * @return The data-point's timestamp.
     */
    Date getTimestamp() { return timestamp; }
}

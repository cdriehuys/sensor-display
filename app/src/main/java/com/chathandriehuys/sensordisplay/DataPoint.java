package com.chathandriehuys.sensordisplay;


import java.util.Date;

class DataPoint {
    private float data;

    private Date timestamp;

    DataPoint(float data) {
        this(data, new Date());
    }

    DataPoint(float data, Date timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    float getData() { return data; }

    Date getTimestamp() { return timestamp; }
}

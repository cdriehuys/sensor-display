package com.chathandriehuys.sensordisplay;


import java.util.Date;

class DataPoint<T> {
    private T data;

    private Date timestamp;

    DataPoint(T data) {
        this(data, new Date());
    }

    DataPoint(T data, Date timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public T getData() { return data; }

    public Date getTimestamp() { return timestamp; }
}

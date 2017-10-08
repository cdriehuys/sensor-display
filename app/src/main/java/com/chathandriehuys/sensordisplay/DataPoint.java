package com.chathandriehuys.sensordisplay;


class DataPoint<T> {
    private T data;

    DataPoint(T data) {
        this.data = data;
    }

    public T getData() { return data; }
}

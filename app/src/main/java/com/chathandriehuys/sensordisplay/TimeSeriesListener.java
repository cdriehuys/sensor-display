package com.chathandriehuys.sensordisplay;


public interface TimeSeriesListener {
    void pointAdded(TimeSeries series, DataPoint point);
}

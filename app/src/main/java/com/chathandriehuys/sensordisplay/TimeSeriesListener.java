package com.chathandriehuys.sensordisplay;


interface TimeSeriesListener {
    void pointAdded(TimeSeries series, DataPoint point);
}

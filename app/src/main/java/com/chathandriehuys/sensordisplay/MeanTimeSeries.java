package com.chathandriehuys.sensordisplay;


public class MeanTimeSeries extends TimeSeries implements TimeSeriesListener {
    @Override
    public void pointAdded(TimeSeries series, DataPoint point) {
        DataPoint avgPoint = new DataPoint(series.getAverage(), point.getTimestamp());
        addPoint(avgPoint);
    }
}

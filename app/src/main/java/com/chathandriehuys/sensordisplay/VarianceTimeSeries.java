package com.chathandriehuys.sensordisplay;


class VarianceTimeSeries extends TimeSeries implements TimeSeriesListener {
    @Override
    public void pointAdded(TimeSeries series, DataPoint point) {
        DataPoint variancePoint = new DataPoint(series.getVariance(), point.getTimestamp());
        addPoint(variancePoint);
    }
}
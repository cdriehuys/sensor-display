package com.chathandriehuys.sensordisplay;


class PlotSeriesEntry {
    private int color;

    private TimeSeries series;

    PlotSeriesEntry(TimeSeries series, int color) {
        this.series = series;
        this.color = color;
    }

    int getColor() { return color; }

    TimeSeries getSeries() { return series; }
}

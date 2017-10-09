package com.chathandriehuys.sensordisplay;


/**
 * A series that can be plotted.
 */
class PlotSeriesEntry {
    private int color;

    private TimeSeries series;

    /**
     * Create a new series that can be plotted.
     *
     * @param series The series to plot.
     * @param color The color that the series should be drawn in.
     */
    PlotSeriesEntry(TimeSeries series, int color) {
        this.series = series;
        this.color = color;
    }

    /**
     * Get the color to plot the series with.
     *
     * @return The color to plot the series with.
     */
    int getColor() { return color; }

    /**
     * Get the series to be plotted.
     *
     * @return The series to be plotted.
     */
    TimeSeries getSeries() { return series; }
}

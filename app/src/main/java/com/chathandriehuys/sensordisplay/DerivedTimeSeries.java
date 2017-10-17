package com.chathandriehuys.sensordisplay;


/**
 * A series of data that is derived from another series.
 *
 * Any update to the parent series will also trigger an update of this child series.
 */
abstract class DerivedTimeSeries extends TimeSeries implements TimeSeriesListener {
    /**
     * Create a new series of data derived from another series.
     *
     * @param title The title of the series.
     */
    DerivedTimeSeries(String title) {
        super(title);
    }
}

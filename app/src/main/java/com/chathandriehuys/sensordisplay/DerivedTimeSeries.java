package com.chathandriehuys.sensordisplay;


/**
 * A series of data that is derived from another series.
 *
 * Any update to the parent series will also trigger an update of this child series.
 */
abstract class DerivedTimeSeries extends TimeSeries implements TimeSeriesListener {
}

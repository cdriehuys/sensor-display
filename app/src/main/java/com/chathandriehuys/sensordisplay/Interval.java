package com.chathandriehuys.sensordisplay;


/**
 * An interval with a minimum and maximum value.
 *
 * This is intended to be used to represent the domain or range of a series.
 *
 * @param <T> The type of number to store in the interval.
 */
class Interval<T> {
    private T min, max;

    /**
     * Create a new interval.
     *
     * @param min The interval's minimum value.
     * @param max The interval's maximum value.
     */
    Interval(T min, T max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Get the interval's maximum value.
     *
     * @return The interval's maximum value.
     */
    T getMax() { return max; }

    /**
     * Get the interval's minimum value.
     *
     * @return The interval's minimum value.
     */
    T getMin() { return min; }
}

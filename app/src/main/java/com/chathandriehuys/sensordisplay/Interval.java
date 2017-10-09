package com.chathandriehuys.sensordisplay;


class Interval<T> {
    private T min, max;

    Interval(T min, T max) {
        this.min = min;
        this.max = max;
    }

    T getMax() { return max; }

    T getMin() { return min; }
}

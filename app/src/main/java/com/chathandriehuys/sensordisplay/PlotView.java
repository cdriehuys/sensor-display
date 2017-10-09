package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * A view for plotting different series of data.
 */
public class PlotView extends View {
    private static final int AXIS_SIZE = 200;
    private static final int AXIS_TICK_LENGTH = 24;
    private static final int DOMAIN_SECONDS = 5;
    private static final int LABEL_SIZE = 48;
    private static final int PLOT_GUTTER_SIZE = 50;
    private static final int PLOT_REFRESH_INTERVAL = 1000 / 60;
    private static final int POINT_RADIUS = 10;
    private static final int RANGE_BUFFER = 1;
    private static final int TEXT_PADDING = 10;

    private ArrayList<PlotSeriesEntry> series;

    private Canvas canvas;

    private Interval<Float> range;
    private Interval<Integer> domain;

    private Paint axisPaint;
    private Paint labelPaint;
    private Paint minorLabelPaint;
    private Paint pointPaint;

    private Rect axisAreaX;
    private Rect axisAreaY;
    private Rect plotArea;

    public PlotView(Context context) {
        super(context);

        init();
    }

    public PlotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @SuppressWarnings("unused")
    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init();
    }

    /**
     * Add a new series to the plot.
     *
     * @param series The series to plot.
     * @param color The color to plot the series with.
     */
    public void addSeries(TimeSeries series, int color) {
        this.series.add(new PlotSeriesEntry(series, color));
    }

    /**
     * Draw the plot and its associated series.
     *
     * @param canvas The canvas to draw the plot on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;

        // Set up geometry of plot components
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int xStart = PLOT_GUTTER_SIZE;
        int yStart = PLOT_GUTTER_SIZE;

        int xEnd = width - PLOT_GUTTER_SIZE;
        int yEnd = height - PLOT_GUTTER_SIZE;

        int plotXStart = xStart + AXIS_SIZE;
        int plotYEnd = yEnd - AXIS_SIZE;

        axisAreaX.set(plotXStart, plotYEnd, xEnd, yEnd);
        axisAreaY.set(xStart, yStart, plotXStart, plotYEnd);
        plotArea.set(plotXStart, yStart, xEnd, plotYEnd);

        // Calculate plot parameters
        domain = getDomain();
        range = getRange();

        // Draw plot
        drawAxisX();
        drawAxisY();
        drawData();
    }

    /**
     * Convert an x-coordinate from a series into a coordinate on the canvas.
     *
     * @param x The x-coordinate of a point in a series.
     *
     * @return The x-coordinate on the canvas where the provided value is located.
     */
    private float calculateCanvasX(float x) {
        float width = plotArea.width();

        return axisAreaX.right - width / (domain.getMax() - domain.getMin()) * x;
    }

    /**
     * Convert a y-coordinate from a series into a coordinate on the canvas.
     *
     * @param y The y-coordinate of a point in a series.
     *
     * @return The y-coordinate on the canvas where the provided value is located.
     */
    private float calculateCanvasY(float y) {
        float height = plotArea.height();

        return axisAreaY.bottom - height / (range.getMax() - range.getMin()) * (y - range.getMin());
    }

    /**
     * Draw the plot's x-axis.
     *
     * This includes the labels and tick marks for the axis.
     */
    private void drawAxisX() {
        // The actual axis
        canvas.drawLine(axisAreaX.left, axisAreaX.top, axisAreaX.right, axisAreaX.top, axisPaint);

        // Label the upper and lower bounds of the axis
        drawXAxisLabel(domain.getMin());
        drawXAxisLabel(domain.getMax());

        // Draw tick marks and labels at the appropriate intervals
        for (float tick : generateTickMarks(domain.getMin(), domain.getMax())) {
            drawXAxisLabel(tick);
        }

        // The axis title
        canvas.drawText(
                "Time (ms)",
                (axisAreaX.left + axisAreaX.right) / 2,
                axisAreaX.bottom,
                labelPaint);
    }

    /**
     * Draw the plot's y-axis.
     *
     * This includes the labels and tick marks for the axis.
     */
    private void drawAxisY() {
        // The actual axis
        canvas.drawLine(axisAreaY.right, axisAreaY.top, axisAreaY.right, axisAreaY.bottom, axisPaint);

        // Label the upper and lower bounds of the axis
        drawYAxisLabel(range.getMin());
        drawYAxisLabel(range.getMax());

        // Draw tick marks and labels at the appropriate intervals
        for (float tick : generateTickMarks((int) Math.floor(range.getMin()), (int) Math.ceil(range.getMax()))) {
            drawYAxisLabel(tick);
        }

        // Draw the axis title. This is more complex than the x-axis since we need to rotate the
        // text to be parallel with the axis.
        labelPaint.setTextAlign(Paint.Align.CENTER);

        float yTitleX = axisAreaY.left;
        float yTitleY = (axisAreaY.top + axisAreaY.bottom) / 2;

        canvas.save();
        canvas.rotate(270.0f, yTitleX, yTitleY);
        canvas.drawText("Data", yTitleX, yTitleY, labelPaint);
        canvas.restore();
    }

    /**
     * Draw the data from each series attached to the plot.
     */
    private void drawData() {
        // Create baseline for expired data.
        Date now = new Date();
        Date oldest = new Date(now.getTime() - 1000 * DOMAIN_SECONDS);

        // Plot each series attached to the plot
        for (PlotSeriesEntry entry : series) {
            pointPaint.setColor(entry.getColor());

            float prevX = 0;
            float prevY = 0;

            boolean shouldDrawConnector = false;

            // Loop through each data-point in the series
            for (DataPoint point : entry.getSeries().getData()) {
                Date pointDate = point.getTimestamp();

                if (pointDate.before(oldest)) {
                    // If the data-point is expired, we shouldn't draw it or a connecting line to it
                    shouldDrawConnector = false;

                    continue;
                }

                // Get the on-screen coordinates of the point and draw it
                float x = calculateCanvasX(now.getTime() - point.getTimestamp().getTime());
                float y = calculateCanvasY(point.getData());

                canvas.drawCircle(x, y, POINT_RADIUS, pointPaint);

                if (shouldDrawConnector) {
                    canvas.drawLine(prevX, prevY, x, y, pointPaint);
                }

                // Update information for drawing the next connecting line
                prevX = x;
                prevY = y;

                shouldDrawConnector = true;
            }
        }
    }

    /**
     * Draw a label on the x-axis.
     *
     * This draws a label and a line at the given x-coordinate.
     *
     * @param x The x-coordinate to place the label at.
     */
    private void drawXAxisLabel(float x) {
        float realX = calculateCanvasX(x);
        float realY = axisAreaX.top;

        canvas.drawLine(
                realX,
                plotArea.top,
                realX,
                plotArea.bottom + AXIS_TICK_LENGTH,
                minorLabelPaint);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(
                String.format(Locale.US, "-%.0f", x),
                realX,
                realY + AXIS_TICK_LENGTH + TEXT_PADDING + labelPaint.getTextSize(),
                labelPaint);
    }

    /**
     * Draw a label on the y-axis.
     *
     * This draws a label and a line at the given y-coordinate.
     *
     * @param y The y-coordinate to place the label.
     */
    private void drawYAxisLabel(float y) {
        float realX = axisAreaY.right;
        float realY = calculateCanvasY(y);

        canvas.drawLine(
                plotArea.left - AXIS_TICK_LENGTH,
                realY,
                plotArea.right,
                realY,
                minorLabelPaint);

        labelPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                String.format(Locale.US, "%.0f", y),
                realX - AXIS_TICK_LENGTH - TEXT_PADDING,
                realY + labelPaint.getTextSize() / 2,
                labelPaint);
    }

    /**
     * Generate the locations of the tick marks for a given axis.
     *
     * @param min The axis' minimum value.
     * @param max The axis' maximum value.
     *
     * @return A list of values to place tick marks at.
     */
    private ArrayList<Integer> generateTickMarks(int min, int max) {
        float range = max - min;

        ArrayList<Integer> ticks = new ArrayList<>();

        if (range <= 1) {
            // If the range is too small, we don't want any addition ticks.
            return ticks;
        }

        // The step is equivalent to 10 raised to the order of magnitude of the range of the
        // provided values. For example, with a range of 45, the step would be 10, and for a range
        // of 176, the step would be 100.
        int step = (int) Math.pow(10, Math.floor(Math.log10(range)));

        // Start the tick marks at the next whole multiple of the step value. For example, if the
        // min is 43 and the step is 10, we would start at 50.
        int start = min + step - (min % step);

        // Add tick marks until we reach the max value.
        for (int tick = start; tick < max; tick += step) {
            ticks.add(tick);
        }

        return ticks;
    }

    /**
     * Get the domain of all the series included in the plot.
     *
     * @return The smallest domain that encompasses the domains of all the series being plotted.
     */
    private Interval<Integer> getDomain() {
        int domainMin = Integer.MAX_VALUE, domainMax = Integer.MIN_VALUE;

        for (PlotSeriesEntry entry : series) {
            Interval<Integer> domain = entry.getSeries().getDomain();

            domainMin = Math.min(domain.getMin(), domainMin);
            domainMax = Math.max(domain.getMax(), domainMax);
        }

        return new Interval<>(domainMin, domainMax);
    }

    /**
     * Get the range of all the series included in the plot.
     *
     * @return The smallest range that encompasses the ranges of all the series being plotted.
     */
    private Interval<Float> getRange() {
        float rangeMin = Float.MAX_VALUE, rangeMax = Float.MIN_VALUE;

        for (PlotSeriesEntry entry : series) {
            Interval<Float> range = entry.getSeries().getRange();

            rangeMin = Math.min(range.getMin() - RANGE_BUFFER, rangeMin);
            rangeMax = Math.max(range.getMax() + RANGE_BUFFER, rangeMax);
        }

        return new Interval<>(rangeMin, rangeMax);
    }

    /**
     * Initialize the plot data structures.
     */
    private void init() {
        series = new ArrayList<>();

        // Set up different paint styles
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.GRAY);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.DKGRAY);
        labelPaint.setTextSize(LABEL_SIZE);

        minorLabelPaint = new Paint(labelPaint);
        minorLabelPaint.setColor(Color.LTGRAY);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.GREEN);

        // Initialize geometry
        axisAreaX = new Rect();
        axisAreaY = new Rect();
        plotArea = new Rect();

        // Set up handler to refresh the plot at the given interval. This allows us to keep moving
        // data along the time-axis when no new data is coming in.
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable refreshPlotRunnable = new Runnable() {
            public void run() {
                invalidate();
                handler.postDelayed(this, PLOT_REFRESH_INTERVAL);
            }
        };

        refreshPlotRunnable.run();
    }
}

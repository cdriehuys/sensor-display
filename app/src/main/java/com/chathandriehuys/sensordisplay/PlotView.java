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

    public void addSeries(TimeSeries series, int color) {
        this.series.add(new PlotSeriesEntry(series, color));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;

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

        domain = getDomain();
        range = getRange();

        drawAxisX();
        drawAxisY();

        drawData();
    }

    private float calculateCanvasX(float x) {
        float domain = 1000 * DOMAIN_SECONDS;
        float width = plotArea.width();

        return axisAreaX.right - width / domain * x;
    }

    private float calculateCanvasY(float y) {
        float height = plotArea.height();

        return axisAreaY.bottom - height / (range.getMax() - range.getMin()) * (y - range.getMin());
    }

    private void drawAxisX() {
        canvas.drawLine(axisAreaX.left, axisAreaX.top, axisAreaX.right, axisAreaX.top, axisPaint);

        drawXAxisLabel(domain.getMin());
        drawXAxisLabel(domain.getMax());

        for (float tick : generateTickMarks(domain.getMin(), domain.getMax())) {
            drawXAxisLabel(tick);
        }

        canvas.drawText(
                "Time (ms)",
                (axisAreaX.left + axisAreaX.right) / 2,
                axisAreaX.bottom,
                labelPaint);
    }

    private void drawAxisY() {
        canvas.drawLine(axisAreaY.right, axisAreaY.top, axisAreaY.right, axisAreaY.bottom, axisPaint);

        Interval<Float> range = getRange();

        drawYAxisLabel(range.getMin());
        drawYAxisLabel(range.getMax());

        for (float tick : generateTickMarks((int) Math.floor(range.getMin()), (int) Math.ceil(range.getMax()))) {
            drawYAxisLabel(tick);
        }

        labelPaint.setTextAlign(Paint.Align.CENTER);

        float yTitleX = axisAreaY.left;
        float yTitleY = (axisAreaY.top + axisAreaY.bottom) / 2;

        canvas.save();
        canvas.rotate(270.0f, yTitleX, yTitleY);
        canvas.drawText("Data", yTitleX, yTitleY, labelPaint);
        canvas.restore();
    }

    private void drawData() {
        Date now = new Date();
        Date oldest = new Date(now.getTime() - 1000 * DOMAIN_SECONDS);

        for (PlotSeriesEntry entry : series) {
            pointPaint.setColor(entry.getColor());

            float prevX = 0;
            float prevY = 0;

            boolean shouldDrawConnector = false;

            for (DataPoint point : entry.getSeries().getData()) {
                Date pointDate = point.getTimestamp();

                if (pointDate.before(oldest)) {
                    shouldDrawConnector = false;

                    continue;
                }

                float x = calculateCanvasX(now.getTime() - point.getTimestamp().getTime());
                float y = calculateCanvasY(point.getData());

                canvas.drawCircle(x, y, POINT_RADIUS, pointPaint);

                Log.v("TAG", String.format("Drawing point at (%f, %f)", x, y));

                if (shouldDrawConnector) {
                    canvas.drawLine(prevX, prevY, x, y, pointPaint);
                }

                prevX = x;
                prevY = y;

                shouldDrawConnector = true;
            }
        }
    }

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

    private ArrayList<Integer> generateTickMarks(int min, int max) {
        float range = max - min;

        ArrayList<Integer> ticks = new ArrayList<>();

        if (range <= 1) {
            return ticks;
        }

        int step = (int) Math.pow(10, Math.floor(Math.log10(range)));
        int start = min + step - (min % step);

        for (int tick = start; tick < max; tick += step) {
            ticks.add(tick);
        }

        return ticks;
    }

    private Interval<Integer> getDomain() {
        int domainMin = Integer.MAX_VALUE, domainMax = Integer.MIN_VALUE;

        for (PlotSeriesEntry entry : series) {
            Interval<Integer> domain = entry.getSeries().getDomain();

            domainMin = Math.min(domain.getMin(), domainMin);
            domainMax = Math.max(domain.getMax(), domainMax);
        }

        return new Interval<>(domainMin, domainMax);
    }

    private Interval<Float> getRange() {
        float rangeMin = Float.MAX_VALUE, rangeMax = Float.MIN_VALUE;

        for (PlotSeriesEntry entry : series) {
            Interval<Float> range = entry.getSeries().getRange();

            rangeMin = Math.min(range.getMin() - RANGE_BUFFER, rangeMin);
            rangeMax = Math.max(range.getMax() + RANGE_BUFFER, rangeMax);
        }

        return new Interval<>(rangeMin, rangeMax);
    }

    private void init() {
        series = new ArrayList<>();

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.GRAY);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.DKGRAY);
        labelPaint.setTextSize(LABEL_SIZE);

        minorLabelPaint = new Paint(labelPaint);
        minorLabelPaint.setColor(Color.LTGRAY);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.GREEN);

        axisAreaX = new Rect();
        axisAreaY = new Rect();
        plotArea = new Rect();

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

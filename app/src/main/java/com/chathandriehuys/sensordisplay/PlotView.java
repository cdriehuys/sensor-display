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

    private ArrayList<DataPoint<Float>> data;
    private ArrayList<Integer> xAxisTicks, yAxisTicks;

    private Canvas canvas;

    private int range, rangeMax, rangeMin;

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

    public void addPoint(DataPoint<Float> point) {
        data.add(point);

        Date now = new Date();
        long expirationTime = now.getTime() - 1000 * DOMAIN_SECONDS;

        for (int i = data.size() - 1; i >= 0; i--) {
            if (data.get(i).getTimestamp().getTime() < expirationTime) {
                data.remove(i);
            }
        }

        // TODO: Refresh data without requiring a point to be added
        refreshRange();

        xAxisTicks = generateTickMarks(0, 1000 * DOMAIN_SECONDS);
        yAxisTicks = generateTickMarks(rangeMin, rangeMax);
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

        return axisAreaY.bottom - height / range * (y - rangeMin);
    }

    private void drawAxisX() {
        canvas.drawLine(axisAreaX.left, axisAreaX.top, axisAreaX.right, axisAreaX.top, axisPaint);

        drawXAxisLabel(1000 * DOMAIN_SECONDS);
        drawXAxisLabel(0);

        for (float tick : xAxisTicks) {
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

        drawYAxisLabel(rangeMax);
        drawYAxisLabel(rangeMin);

        for (float tick : yAxisTicks) {
            drawYAxisLabel(tick);
        }

        labelPaint.setTextAlign(Paint.Align.CENTER);

        float yTitleX = axisAreaY.left;
        float yTitleY = (axisAreaY.top + axisAreaY.bottom) / 2;

        canvas.save();
        canvas.rotate(270.0f, yTitleX, yTitleY);
        canvas.drawText("Sensor Value", yTitleX, yTitleY, labelPaint);
        canvas.restore();
    }

    private void drawData() {
        Date now = new Date();
        Date oldest = new Date(now.getTime() - 1000 * DOMAIN_SECONDS);

        float prevX = 0;
        float prevY = 0;

        boolean shouldDrawConnector = false;

        for (DataPoint<Float> point : data) {
            Date pointDate = point.getTimestamp();

            if (pointDate.before(oldest)) {
                shouldDrawConnector = false;

                continue;
            }

            float x = calculateCanvasX(now.getTime() - point.getTimestamp().getTime());
            float y = calculateCanvasY(point.getData());

            canvas.drawCircle(x, y, POINT_RADIUS, pointPaint);

            if (shouldDrawConnector) {
                canvas.drawLine(prevX, prevY, x, y, pointPaint);
            }

            prevX = x;
            prevY = y;

            shouldDrawConnector = true;
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

    private void init() {
        data = new ArrayList<>();
        xAxisTicks = new ArrayList<>();
        yAxisTicks = new ArrayList<>();

        range = 2 * RANGE_BUFFER;
        rangeMax = RANGE_BUFFER;
        rangeMin = -RANGE_BUFFER;

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

    private void refreshRange() {
        rangeMax = Integer.MIN_VALUE;
        rangeMin = Integer.MAX_VALUE;

        for (DataPoint<Float> point : data) {
            float max = point.getData() + RANGE_BUFFER;
            float min = point.getData() - RANGE_BUFFER;

            if (max > rangeMax) {
                rangeMax = (int) Math.ceil(max);
            }

            if (min < rangeMin) {
                rangeMin = (int) Math.floor(min);
            }
        }

        range = rangeMax - rangeMin;
    }
}

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

    private Canvas canvas;

    private float range, rangeMax, rangeMin;

    private Paint axisPaint;
    private Paint labelPaint;
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

        refreshRange();
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

    private void drawAxisX() {
        canvas.drawLine(axisAreaX.left, axisAreaX.top, axisAreaX.right, axisAreaX.top, axisPaint);

        drawXAxisLabel(1000 * DOMAIN_SECONDS);
        drawXAxisLabel(0);

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

        labelPaint.setTextAlign(Paint.Align.CENTER);

        float yTitleX = axisAreaY.left;
        float yTitleY = (axisAreaY.top + axisAreaY.bottom) / 2;

        canvas.save();
        canvas.rotate(270.0f, yTitleX, yTitleY);
        canvas.drawText("Sensor Value", yTitleX, yTitleY, labelPaint);
        canvas.restore();
    }

    private void drawData() {
        int width = plotArea.width();
        int height = plotArea.height();

        Date now = new Date();
        Date oldest = new Date(now.getTime() - 1000 * DOMAIN_SECONDS);

        long domain = now.getTime() - oldest.getTime();

        float prevX = 0;
        float prevY = 0;

        boolean shouldDrawConnector = false;

        for (int i = 0; i < data.size(); i++) {
            Date pointDate = data.get(i).getTimestamp();

            if (pointDate.before(oldest)) {
                shouldDrawConnector = false;

                continue;
            }

            float x = ((float) width) / domain * (pointDate.getTime() - oldest.getTime()) + plotArea.left;
            float y = height - (height / range * (data.get(i).getData() - rangeMin)) + plotArea.top;

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
        float domain = 1000 * DOMAIN_SECONDS;
        float width = axisAreaX.right - axisAreaX.left;

        float realX = axisAreaX.right - width / domain * x;
        float realY = axisAreaX.top;

        labelPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawLine(realX, realY - AXIS_TICK_LENGTH, realX, realY + AXIS_TICK_LENGTH, labelPaint);

        canvas.drawText(
                String.format(Locale.US, "-%.0f", x),
                realX,
                realY + AXIS_TICK_LENGTH + TEXT_PADDING + labelPaint.getTextSize(),
                labelPaint);
    }

    private void drawYAxisLabel(float y) {
        float height = axisAreaY.bottom - axisAreaY.top;

        float realX = axisAreaY.right;
        float realY = axisAreaY.bottom - height / range * (y - rangeMin);

        labelPaint.setTextAlign(Paint.Align.RIGHT);

        canvas.drawLine(realX - AXIS_TICK_LENGTH, realY, realX + AXIS_TICK_LENGTH, realY, labelPaint);
        canvas.drawText(
                String.format(Locale.US, "%.0f", y),
                realX - AXIS_TICK_LENGTH - TEXT_PADDING,
                realY + labelPaint.getTextSize() / 2,
                labelPaint);
    }

    private void init() {
        data = new ArrayList<>();

        range = 2 * RANGE_BUFFER;
        rangeMax = RANGE_BUFFER;
        rangeMin = -RANGE_BUFFER;

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.LTGRAY);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.GRAY);
        labelPaint.setTextSize(LABEL_SIZE);

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
        rangeMax = Float.MIN_VALUE;
        rangeMin = Float.MAX_VALUE;

        for (DataPoint<Float> point : data) {
            float max = point.getData() + RANGE_BUFFER;
            float min = point.getData() - RANGE_BUFFER;

            if (max > rangeMax) {
                rangeMax = max;
            }

            if (min < rangeMin) {
                rangeMin = min;
            }
        }

        rangeMax = Math.round(rangeMax);
        rangeMin = Math.round(rangeMin);

        range = rangeMax - rangeMin;
    }
}

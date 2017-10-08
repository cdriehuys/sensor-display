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
    private static final int DOMAIN_SECONDS = 5;
    private static final int LABEL_SIZE = 48;
    private static final int PLOT_GUTTER_SIZE = 50;
    private static final int PLOT_REFRESH_INTERVAL = 100;
    private static final int POINT_RADIUS = 10;
    private static final int RANGE_BUFFER = 1;
    private static final int TEXT_PADDING = 10;

    private ArrayList<DataPoint<Float>> data;

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

        drawAxisX(canvas, axisAreaX);
        drawAxisY(canvas, axisAreaY);
        drawData(canvas, plotArea);
    }

    private void drawAxisX(Canvas canvas, Rect drawableArea) {
        canvas.drawLine(drawableArea.left, drawableArea.top, drawableArea.right, drawableArea.top, axisPaint);

        labelPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(
                "Now",
                drawableArea.right,
                drawableArea.top + TEXT_PADDING + labelPaint.getTextSize(),
                labelPaint);

        canvas.drawText(
                "Time",
                (drawableArea.left + drawableArea.right) / 2,
                drawableArea.bottom,
                labelPaint);
    }

    private void drawAxisY(Canvas canvas, Rect drawableArea) {
        canvas.drawLine(drawableArea.right, drawableArea.top, drawableArea.right, drawableArea.bottom, axisPaint);

        labelPaint.setTextAlign(Paint.Align.RIGHT);

        canvas.drawText(
                String.format(Locale.US, "%.2f", rangeMax),
                drawableArea.right - TEXT_PADDING,
                drawableArea.top + labelPaint.getTextSize() / 2,
                labelPaint);

        canvas.drawText(
                String.format(Locale.US, "%.2f", rangeMin),
                drawableArea.right - TEXT_PADDING,
                drawableArea.bottom,
                labelPaint);

        labelPaint.setTextAlign(Paint.Align.CENTER);

        float yTitleX = drawableArea.left;
        float yTitleY = (drawableArea.top + drawableArea.bottom) / 2;

        canvas.save();
        canvas.rotate(270.0f, yTitleX, yTitleY);
        canvas.drawText("Sensor Value", yTitleX, yTitleY, labelPaint);
        canvas.restore();
    }

    private void drawData(Canvas canvas, Rect drawableArea) {
        int width = drawableArea.width();
        int height = drawableArea.height();

        Date now = new Date();
        Date oldest = new Date(now.getTime() - 1000 * DOMAIN_SECONDS);

        long domain = now.getTime() - oldest.getTime();

        float prevX = 0;
        float prevY = 0;

        for (int i = 0; i < data.size(); i++) {
            Date pointDate = data.get(i).getTimestamp();

            if (pointDate.before(oldest)) {
                continue;
            }

            float x = ((float) width) / domain * (pointDate.getTime() - oldest.getTime()) + drawableArea.left;
            float y = height - (height / range * (data.get(i).getData() - rangeMin)) + drawableArea.top;

            canvas.drawCircle(x, y, POINT_RADIUS, pointPaint);

            if (i > 0) {
                canvas.drawLine(prevX, prevY, x, y, pointPaint);
            }

            prevX = x;
            prevY = y;
        }
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

package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class PlotView extends View {
    public static final int NUM_POINTS = 25;
    public static final int POINT_RADIUS = 10;
    public static final int RANGE_BUFFER = 1;

    private ArrayList<Float> data;

    private float range, rangeMax, rangeMin;

    private Paint pointPaint;

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

    public void addPoint(float point) {
        data.add(point);

        while (data.size() > NUM_POINTS) {
            data.remove(0);
        }

        refreshRange();

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        plotArea.set(
                Math.round(width * .2f),
                0,
                Math.round(width * .9f),
                height);

        drawData(canvas, plotArea);
    }

    private void drawData(Canvas canvas, Rect drawableArea) {
        int width = drawableArea.width();
        int height = drawableArea.height();

        float prevX = 0;
        float prevY = 0;

        for (int i = 0; i < data.size(); i++) {
            float x = width / (NUM_POINTS - 1.0f) * i + drawableArea.left;
            float y = height - (height / range * (data.get(i) - rangeMin)) + drawableArea.top;

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

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.GREEN);

        plotArea = new Rect();
    }

    private void refreshRange() {
        rangeMax = Float.MIN_VALUE;
        rangeMin = Float.MAX_VALUE;

        for (float point : data) {
            float max = point + RANGE_BUFFER;
            float min = point - RANGE_BUFFER;

            if (max > rangeMax) {
                rangeMax = max;
            }

            if (min < rangeMin) {
                rangeMin = min;
            }
        }

        range = rangeMax - rangeMin;
    }
}

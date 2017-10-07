package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class PlotView extends View {
    public static final int NUM_POINTS = 10;

    private ArrayList<Float> data;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void init() {
        data = new ArrayList<>();
    }
}

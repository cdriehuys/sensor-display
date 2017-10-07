package com.chathandriehuys.sensordisplay;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


public class SensorInfoView extends android.support.v7.widget.AppCompatTextView {
    public SensorInfoView(Context context) {
        super(context);

        init();
    }

    public SensorInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SensorInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @SuppressWarnings("unused")
    public SensorInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        Resources r = getResources();

        setText(r.getString(R.string.loading_sensor_info));
    }
}

package com.navigine.navigine.demo.ui.custom.lists;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

import com.navigine.navigine.demo.R;

public class ListViewLimit extends ListView {

    private final int HEIGHT_MAX;

    public ListViewLimit(Context context) {
        this(context, null);
    }

    public ListViewLimit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewLimit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListViewLimit);
            HEIGHT_MAX = typedArray.getDimensionPixelSize(R.styleable.ListViewLimit_maxHeight, 0);
            typedArray.recycle();
        } else {
            HEIGHT_MAX = 0;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (HEIGHT_MAX > 0 && HEIGHT_MAX < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(HEIGHT_MAX, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

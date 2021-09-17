package com.navigine.navigine.demo;

import android.content.*;
import android.util.*;
import java.lang.*;

public class ListView extends android.widget.ListView
{
    private final String TAG = "NAVIGINE.OverScrollListView";
    private OnOverScrollListener mListener = null;

    public interface OnOverScrollListener {
        public void onOverScroll(int scrollY);
    }

    public ListView(Context context)
    {
        super(context);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public ListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public void setOnOverScrollListener(OnOverScrollListener listener)
    {
        mListener = listener;
    }

    @Override protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                             int scrollY, int scrollRangeX, int scrollRangeY,
                                             int maxOverScrollX, int maxOverScrollY,
                                             boolean isTouchEvent)
    {
        //Log.d(TAG, String.format(Locale.ENGLISH, "deltaY=%d, scrollY=%d, scrollRangeY=%d, maxScrollRangeY=%d",
        //      deltaY, scrollY, scrollRangeY, maxOverScrollY));
        if (mListener != null)
            mListener.onOverScroll(deltaY);
        return super.overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0, 0, isTouchEvent);
    }

    @Override protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY)
    {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        //if (mListener != null)
        //  mListener.onOverScroll(scrollY);
    }
};

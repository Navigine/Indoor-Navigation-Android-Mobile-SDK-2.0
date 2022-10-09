package com.navigine.navigine.demo.adapters.debug;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class DebugAdapterBase<T extends RecyclerView.ViewHolder, V> extends RecyclerView.Adapter<T> {

    public static final int VIBRATION_DELAY = 75;

    protected Context          mContext          = null;
    protected RecyclerView     mRecyclerView     = null;
    protected ClipboardManager mClipboardManager = null;
    protected GestureDetector  mGestureDetector  = null;
    private   Vibrator         mVibrator         = null;

    private static final int TYPE_ROUNDED_TOP    = 0;
    private static final int TYPE_ROUNDED_BOTTOM = 1;
    private static final int TYPE_RECT           = 2;

    protected List<V> mCurrentList = new ArrayList<>();

    protected boolean isPressed  = false;
    protected static boolean isRootScrolling  = false;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView     = recyclerView;
        mContext          = mRecyclerView.getContext();
        mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        mVibrator         = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        mGestureDetector = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) { return false; }

            @Override
            public void onShowPress(MotionEvent e) { }

            @Override
            public boolean onSingleTapUp(MotionEvent e) { return false; }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }

            @Override
            public void onLongPress(MotionEvent e) {
                onCopyContent();
                onVibrate();
                isPressed = false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_ROUNDED_TOP;
        if (position == mCurrentList.size() - 1) return TYPE_ROUNDED_BOTTOM;
        return TYPE_RECT;
    }

    @Override
    public int getItemCount() {
        return mCurrentList.size();
    }

    public void submit(List<V> list) {
        mCurrentList.clear();
        mCurrentList.addAll(list);
        notifyDataSetChanged();
    }

    private void onVibrate() {
        mVibrator.vibrate(VIBRATION_DELAY);
    }

    public static void setRootView(View view) {
        if (view instanceof NestedScrollView) {
            ((NestedScrollView) view).setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                isRootScrolling = Math.abs(scrollY - oldScrollY) > 2;
            });
        }
    }

    abstract void onCopyContent();
}

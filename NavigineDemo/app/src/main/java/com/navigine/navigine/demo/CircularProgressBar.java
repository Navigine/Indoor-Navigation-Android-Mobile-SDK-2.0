package com.navigine.navigine.demo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class CircularProgressBar extends View
{
    private float mProgress = 0;
    private int mMin = 0;
    private int mMax = 100;

    private float mStartAngle = 270.0f;
    private float mStrokeWidth = 7;
    private int mColor = Color.parseColor("#14273D");
    private int mSecondaryColor = Color.parseColor("#A8A8A8");
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSecondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRectF = new RectF();

    public CircularProgressBar(Context context)
    {
        super(context);
    }

    public CircularProgressBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircularProgressBar,
                0, 0);
        try {
            mStrokeWidth = typedArray.getDimension(R.styleable.CircularProgressBar_thickness, mStrokeWidth);
            mProgress = typedArray.getFloat(R.styleable.CircularProgressBar_progress, mProgress);
            mColor = typedArray.getInt(R.styleable.CircularProgressBar_progressColor, mColor);
            mMin = typedArray.getInt(R.styleable.CircularProgressBar_min, mMin);
            mMax = typedArray.getInt(R.styleable.CircularProgressBar_max, mMax);
        } finally {
            typedArray.recycle();
        }

        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);

        mSecondaryPaint.setColor(mSecondaryColor);
        mSecondaryPaint.setStyle(Paint.Style.STROKE);
        mSecondaryPaint.setStrokeWidth(mStrokeWidth);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(height, width);

        setMeasuredDimension(min, min);
        mRectF.set(0 + mStrokeWidth / 2, 0 + mStrokeWidth / 2, min - mStrokeWidth / 2, min - mStrokeWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawArc(mRectF, 0.0f, 360.0f, false, mSecondaryPaint);
        float sweepAngle = 360.0f * mProgress / mMax;
        canvas.drawArc(mRectF, mStartAngle, sweepAngle, false, mPaint);
    }

    public void setProgress(float progress)
    {
        mProgress = progress;
        invalidate();
    }

    public void setProgressAnimated(float progress)
    {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(750);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();
    }

    public float getProgress()
    {
        return mProgress;
    }
}

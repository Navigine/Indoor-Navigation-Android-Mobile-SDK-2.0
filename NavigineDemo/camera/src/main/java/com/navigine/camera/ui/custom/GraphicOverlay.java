package com.navigine.camera.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.gms.common.images.Size;
import com.navigine.camera.camera.CameraSource;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {

    private final Object lock = new Object();

    private int   previewWidth      = 0;
    private float widthScaleFactor  = 1.0f;
    private int   previewHeight     = 0;
    private float heightScaleFactor = 1.0f;

    private List<Graphic> graphics = new ArrayList<>();

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
    }

    public void setCameraInfo(CameraSource cameraSource) {
        Size previewSize = cameraSource.getPreviewSize();
        if (previewSize != null) {
            previewWidth = previewSize.getHeight();
            previewHeight = previewSize.getWidth();
        }
    }

    public float translateX(float x) {
        return x * widthScaleFactor;
    }

    public float translateY(float y) {
        return y * heightScaleFactor;
    }


    public RectF translateRect(Rect rect) {
        return new RectF(translateX(
                (float) rect.left),
                translateY((float) rect.top),
                translateX((float) rect.right),
                translateY((float) rect.bottom)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (previewWidth > 0 && previewHeight > 0) {
            widthScaleFactor = (float) getWidth() / previewWidth;
            heightScaleFactor = (float) getHeight() / previewHeight;
        }

        synchronized (lock) {
            for (Graphic graphic : graphics) graphic.draw(canvas);
        }
    }
}

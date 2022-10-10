package com.navigine.camera.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.images.Size;
import com.navigine.camera.R;
import com.navigine.camera.camera.CameraSource;

import java.io.IOException;

public class CameraSourcePreview extends FrameLayout {

    private SurfaceView    surfaceView       = null;
    private GraphicOverlay graphicOverlay    = null;
    private CameraSource   cameraSource      = null;
    private Size           cameraPreviewSize = null;

    private boolean startRequested   = false;
    private boolean surfaceAvailable = false;

    public CameraSourcePreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay);
    }

    public void start(CameraSource cameraSource) throws IOException {
        this.cameraSource = cameraSource;
        startRequested = true;
        startIfReady();
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource = null;
            startRequested = false;
        }
    }

    private void startIfReady() throws IOException {
        if (startRequested && surfaceAvailable) {
            if (cameraSource != null) {
                cameraSource.start(surfaceView.getHolder());
                requestLayout();
                if (graphicOverlay != null) {
                    graphicOverlay.setCameraInfo(cameraSource);
                    graphicOverlay.clear();
                }
                startRequested = false;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int layoutWidth = right - left;
        int layoutHeight = bottom - top;

        if (cameraSource != null && cameraSource.getPreviewSize() != null) {
            cameraPreviewSize = cameraSource.getPreviewSize();
        }
        float previewSizeRatio = (float) layoutWidth / (float) layoutHeight;

        int childHeight = (int) (layoutWidth / previewSizeRatio);

        if (childHeight <= layoutHeight) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).layout(0, 0, layoutWidth, childHeight);
            }
        } else {
            int excessLenInHalf = (childHeight - layoutHeight) / 2;
            for (int i = 0; i < getChildCount(); i++) {
                View childView = getChildAt(i);
                if (childView.getId() == R.id.static_overlay_container) {
                    childView.layout(0, 0, layoutWidth, layoutHeight);
                } else {
                    childView.layout(0, -excessLenInHalf, layoutWidth, layoutHeight + excessLenInHalf);
                }
            }
        }

        try {
            startIfReady();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            surfaceAvailable = false;
        }
    }
}

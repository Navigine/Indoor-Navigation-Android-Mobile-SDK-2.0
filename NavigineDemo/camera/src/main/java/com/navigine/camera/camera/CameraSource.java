package com.navigine.camera.camera;

import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static com.navigine.camera.utils.Constants.ASPECT_RATIO_TOLERANCE;
import static com.navigine.camera.utils.Constants.DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT;
import static com.navigine.camera.utils.Constants.DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH;
import static com.navigine.camera.utils.Constants.MAX_CAMERA_PREVIEW_WIDTH;
import static com.navigine.camera.utils.Constants.MIN_CAMERA_PREVIEW_WIDTH;
import static com.navigine.camera.utils.Constants.REQUESTED_CAMERA_FPS;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;
import com.navigine.camera.model.CameraSizePair;
import com.navigine.camera.model.FrameMetadata;
import com.navigine.camera.ui.custom.GraphicOverlay;
import com.navigine.camera.utils.CameraInfoProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

public class CameraSource {

    private GraphicOverlay                      graphicOverlay     = null;
    private Context                             context            = null;
    private Camera                              camera             = null;
    private int                                 rotationDegrees    = 0;
    private Size                                previewSize        = null;
    private Thread                              processingThread   = null;
    private final Object                        processorLock      = new Object();
    private FrameProcessor                      frameProcessor     = null;
    private FrameProcessingRunnable             processingRunnable = new FrameProcessingRunnable();
    private IdentityHashMap<byte[], ByteBuffer> bytesToByteBuffer  = new IdentityHashMap<>();

    public CameraSource(GraphicOverlay graphicOverlay) {
        this.graphicOverlay = graphicOverlay;
        this.context        = graphicOverlay.getContext();
    }


    public synchronized void start(SurfaceHolder surfaceHolder) throws IOException {
        
        if (camera != null) return;
        camera = createCamera();
        camera.setPreviewDisplay(surfaceHolder);
        camera.startPreview();

        processingThread = new Thread(processingRunnable);
        processingRunnable.setActive(true);
        processingThread.start();
    }

    public synchronized void stop() {
        processingRunnable.setActive(false);
        if (processingThread != null) {
            try {
                processingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            processingThread = null;
        }

        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            try {
                camera.setPreviewDisplay(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.release();
            camera = null;
        }

        bytesToByteBuffer.clear();
    }

    public void release() {
        graphicOverlay.clear();
        synchronized (processorLock) {
            stop();
            if (frameProcessor != null) stop();
        }
    }

    public void setFrameProcessor(FrameProcessor processor) {
        graphicOverlay.clear();
        synchronized (processorLock) {
            if (frameProcessor != null) {
                frameProcessor.stop();
            }
            frameProcessor = processor;
        }
    }

    public void updateFlashMode(String flashMode) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(flashMode);
        camera.setParameters(parameters);
    }

    private Camera createCamera() throws IOException {
        Camera mCamera = Camera.open();
        if(mCamera == null) throw new IOException("There is no back-facing camera");
        Camera.Parameters parameters = mCamera.getParameters();
        setPreviewAndPictureSize(mCamera, parameters);
        setRotation(mCamera, parameters);
        int[] previewFpsRange = selectPreviewFpsRange(mCamera);
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
        );

        parameters.setPreviewFormat(ImageFormat.NV21);

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        mCamera.setParameters(parameters);

        mCamera.setPreviewCallbackWithBuffer(processingRunnable::setNextFrame);

        if (previewSize != null) {
            mCamera.addCallbackBuffer(createPreviewBuffer(previewSize));
            mCamera.addCallbackBuffer(createPreviewBuffer(previewSize));
            mCamera.addCallbackBuffer(createPreviewBuffer(previewSize));
            mCamera.addCallbackBuffer(createPreviewBuffer(previewSize));
        }

        return mCamera;
    }

    protected void setPreviewAndPictureSize(Camera camera, Camera.Parameters parameters) {
        float displayAspectRatio = (float) graphicOverlay.getHeight() / graphicOverlay.getWidth();
        CameraSizePair sizePair = selectSizePair(camera, displayAspectRatio);

        previewSize = sizePair.getPreview();
        parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());

        if (sizePair.getPicture() != null) {
            parameters.setPictureSize(sizePair.getPicture().getWidth(), sizePair.getPicture().getHeight());
        }
    }

    private static CameraSizePair selectSizePair(Camera camera, float displayAspectRatio) {
        List<CameraSizePair> validPreviewSizes = CameraInfoProvider.getValidPreviewSizeList(camera);
        CameraSizePair selectedPair = null;
        float minAspectRatioDiff = Float.MAX_VALUE;

        for (CameraSizePair sizePair : validPreviewSizes) {
            Size previewSize = sizePair.getPreview();
            if (previewSize.getWidth() < MIN_CAMERA_PREVIEW_WIDTH || previewSize.getWidth() > MAX_CAMERA_PREVIEW_WIDTH) {
                continue;
            }

            float previewAspectRatio = previewSize.getWidth() * 1f / previewSize.getHeight();
            float aspectRatioDiff = Math.abs(displayAspectRatio - previewAspectRatio);
            if (Math.abs(aspectRatioDiff - minAspectRatioDiff) < ASPECT_RATIO_TOLERANCE) {
                if (selectedPair == null || selectedPair.getPreview().getWidth() < sizePair.getPreview().getWidth()) {
                    selectedPair = sizePair;
                }
            } else if (aspectRatioDiff < minAspectRatioDiff) {
                minAspectRatioDiff = aspectRatioDiff;
                selectedPair = sizePair;
            }
        }

        if (selectedPair == null) {
            int minDiff = Integer.MAX_VALUE;
            for (CameraSizePair sizePair : validPreviewSizes) {
                Size size = sizePair.getPicture();
                int diff = Math.abs(size.getWidth() - DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH) +
                        Math.abs(size.getHeight() - DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT);
                if (diff < minDiff) {
                    selectedPair = sizePair;
                    minDiff = diff;
                }
            }
        }

        return selectedPair;
    }

    protected void setRotation(Camera camera, Camera.Parameters parameters) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int degrees;
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case ROTATION_90:
                degrees = 90;
                break;
            case ROTATION_180:
                degrees = 180;
                break;
            case ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        int angle = (cameraInfo.orientation - degrees + 360) % 360;
        rotationDegrees = angle;
        camera.setDisplayOrientation(angle);
        parameters.setRotation(angle);
    }

    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = (long) previewSize.getHeight() * (long) previewSize.getWidth() * (long) bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0) + 1;

        byte[] byteArray = new byte[bufferSize];
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        if (!byteBuffer.hasArray() || !Arrays.equals(byteBuffer.array(), byteArray)) {
            throw new IllegalStateException();
        }
        bytesToByteBuffer.put(byteArray, byteBuffer);
        return byteArray;
    }

    private static int[] selectPreviewFpsRange(Camera camera) {
        int desiredPreviewFpsScaled = (int) (REQUESTED_CAMERA_FPS * 1000f);
        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        for (int[] range : camera.getParameters().getSupportedPreviewFpsRange()) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }


    private class FrameProcessingRunnable implements Runnable {

        private final Object lock = new Object();
        private boolean active = false;
        private ByteBuffer pendingFrameData = null;

        private void setActive(Boolean active) {
            synchronized (lock) {
                this.active = active;
                lock.notifyAll();
            }
        }

        private void setNextFrame(byte[] data, Camera camera) {
            synchronized (lock) {
                if (pendingFrameData != null) {
                    camera.addCallbackBuffer(pendingFrameData.array());
                    pendingFrameData = null;
                }

                if (!bytesToByteBuffer.containsKey(data)) {
                    return;
                }
                pendingFrameData = bytesToByteBuffer.get(data);
                lock.notifyAll();
            }
        }

        @Override
        public void run() {
            ByteBuffer data = null;

            while (true) {
                synchronized (lock) {
                    while (active && pendingFrameData == null) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    if (!active) return;
                    data = pendingFrameData;
                    pendingFrameData = null;
                }

                try {
                    synchronized (processorLock) {
                        FrameMetadata frameMetadata = new FrameMetadata(previewSize.getWidth(), previewSize.getHeight(), rotationDegrees);
                        if (data != null && frameProcessor != null) {
                            frameProcessor.process(data, frameMetadata, graphicOverlay);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (data != null) camera.addCallbackBuffer(data.array());
                }
            }
        }
    }


    public Size getPreviewSize() {
        return previewSize;
    }
}

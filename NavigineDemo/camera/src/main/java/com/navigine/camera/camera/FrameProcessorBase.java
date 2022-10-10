package com.navigine.camera.camera;

import androidx.annotation.GuardedBy;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.mlkit.vision.common.InputImage;
import com.navigine.camera.model.CameraInputInfo;
import com.navigine.camera.model.FrameMetadata;
import com.navigine.camera.ui.custom.GraphicOverlay;
import com.navigine.camera.utils.ScopedExecutor;

import java.nio.ByteBuffer;

;

public abstract class FrameProcessorBase<T> implements FrameProcessor {

    @GuardedBy("this")
    private ByteBuffer latestFrame = null;

    @GuardedBy("this")
    private FrameMetadata latestFrameMetaData = null;

    @GuardedBy("this")
    private ByteBuffer processingFrame = null;

    @GuardedBy("this")
    private FrameMetadata processingFrameMetaData = null;

    private ScopedExecutor executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);

    @Override
    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {
        latestFrame = data;
        latestFrameMetaData = frameMetadata;
        if (processingFrame == null && processingFrameMetaData == null) {
            processLatestFrame(graphicOverlay);
        }
    }

    private synchronized void processLatestFrame(GraphicOverlay graphicOverlay) {
        processingFrame = latestFrame;
        processingFrameMetaData = latestFrameMetaData;
        latestFrame = null;
        latestFrameMetaData = null;

        if (processingFrame == null || processingFrameMetaData == null) return;
        ByteBuffer frame = processingFrame;
        FrameMetadata frameMetadata = processingFrameMetaData;

        InputImage image = InputImage.fromByteBuffer(frame, frameMetadata.getWidth(), frameMetadata.getHeight(), frameMetadata.getRotation(), InputImage.IMAGE_FORMAT_NV21);
        detectInImage(image).
                addOnSuccessListener(executor, results -> {
                    FrameProcessorBase.this.onSuccess(new CameraInputInfo(frame, frameMetadata), results, graphicOverlay);
                    processLatestFrame(graphicOverlay);
                })
                .addOnFailureListener(executor, FrameProcessorBase.this::onFailure);
    }

    protected abstract Task<T> detectInImage(InputImage image);

    @Override
    public void stop() {
        executor.shutdown();
    }

    protected abstract void onSuccess(InputInfo inputInfo, T results, GraphicOverlay graphicOverlay);

    protected abstract void onFailure(Exception e);
}

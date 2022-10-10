package com.navigine.camera.camera;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.navigine.camera.ui.custom.BarcodeLoadingGraphic;
import com.navigine.camera.ui.custom.BarcodeReticleGraphic;
import com.navigine.camera.ui.custom.CameraReticleAnimator;
import com.navigine.camera.ui.custom.GraphicOverlay;
import com.navigine.camera.viewmodel.CameraViewModel;

import java.util.List;

public class BarcodeProcessor extends FrameProcessorBase<List<Barcode>> {

    private GraphicOverlay        graphicOverlay        = null;
    private CameraViewModel       cameraViewModel       = null;
    private CameraReticleAnimator cameraReticleAnimator = null;

    private BarcodeScanner scanner = BarcodeScanning.getClient();

    public BarcodeProcessor(GraphicOverlay graphicOverlay, CameraViewModel cameraViewModel) {
        this.graphicOverlay   = graphicOverlay;
        this.cameraViewModel  = cameraViewModel;
        cameraReticleAnimator = new CameraReticleAnimator(graphicOverlay);
    }

    @Override
    protected Task<List<Barcode>> detectInImage(InputImage image) {
        return scanner.process(image);
    }

    @Override
    protected void onSuccess(InputInfo inputInfo, List<Barcode> results, GraphicOverlay graphicOverlay) {
        if (!cameraViewModel.isCameraLive()) return;

        Barcode barcodeInCenter = null;
        for (Barcode barcode : results) {
            Rect boundingBox = barcode.getBoundingBox();
            if (boundingBox != null) {
                RectF box = graphicOverlay.translateRect(boundingBox);
                if (box.contains(graphicOverlay.getWidth() / 2f, graphicOverlay.getHeight() / 2f))
                    barcodeInCenter = barcode;
            }
        }
        graphicOverlay.clear();
        if (barcodeInCenter == null) {
            cameraReticleAnimator.start();
            graphicOverlay.add(new BarcodeReticleGraphic(graphicOverlay, cameraReticleAnimator));
            cameraViewModel.setCameraState(CameraViewModel.CameraState.DETECTING);
        } else {
            cameraReticleAnimator.cancel();
            ValueAnimator loadingAnimator = createLoadingAnimator(graphicOverlay, barcodeInCenter);
            loadingAnimator.start();
            graphicOverlay.add(new BarcodeLoadingGraphic(graphicOverlay, loadingAnimator));
            cameraViewModel.setCameraState(CameraViewModel.CameraState.SEARCHING);
        }

        graphicOverlay.invalidate();
    }

    private ValueAnimator createLoadingAnimator(GraphicOverlay graphicOverlay, Barcode barcode) {
        float endProgress = 1.1f;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, endProgress);
        animator.setDuration(2000);
        animator.addUpdateListener(valueAnimator -> {
            if ((float) valueAnimator.getAnimatedValue() >= endProgress) {
                graphicOverlay.clear();
                cameraViewModel.setCameraState(CameraViewModel.CameraState.SEARCHED);
                cameraViewModel.detectedBarcode.setValue(barcode);
            } else {
                graphicOverlay.invalidate();
            }
        });
        return animator;
    }

    @Override
    protected void onFailure(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void stop() {
        super.stop();
        scanner.close();
    }
}

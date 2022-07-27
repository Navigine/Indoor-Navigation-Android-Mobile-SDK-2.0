package com.navigine.camera.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.mlkit.vision.barcode.common.Barcode;

public class CameraViewModel extends ViewModel {

    public enum CameraState {
        NOT_STARTED,
        DETECTING,
        DETECTED,
        CONFIRMING,
        CONFIRMED,
        SEARCHING,
        SEARCHED
    }

    public MutableLiveData<CameraState> cameraState = new MutableLiveData<>(CameraState.NOT_STARTED);
    public MutableLiveData<Barcode> detectedBarcode = new MutableLiveData<>();

    private boolean isCameraLive = false;

    public void markCameraLive() {
        isCameraLive = true;
    }

    public void markCameraFrozen() {
        isCameraLive = false;
    }

    public boolean isCameraLive() {
        return isCameraLive;
    }

    public void setCameraState(CameraState cameraState) {
        this.cameraState.setValue(cameraState);
    }
}

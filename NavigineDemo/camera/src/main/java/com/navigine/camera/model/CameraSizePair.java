package com.navigine.camera.model;

import android.hardware.Camera;

import com.google.android.gms.common.images.Size;

public class CameraSizePair {

    private Size preview = null;
    private Size picture = null;


    public CameraSizePair(Camera.Size previewSize, Camera.Size pictureSize) {
        preview = new Size(previewSize.width, previewSize.height);
        picture = pictureSize != null ? new Size(pictureSize.width, pictureSize.height) : null;
    }

    public Size getPreview() {
        return preview;
    }

    public Size getPicture() {
        return picture;
    }
}

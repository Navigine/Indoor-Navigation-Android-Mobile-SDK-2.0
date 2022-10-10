package com.navigine.camera.model;

import android.graphics.Bitmap;

import com.navigine.camera.camera.InputInfo;
import com.navigine.camera.utils.Utils;

import java.nio.ByteBuffer;

public class CameraInputInfo implements InputInfo {

    private ByteBuffer    frameByteBuffer  = null;
    private FrameMetadata frameMetadata    = null;
    private Bitmap        bitmap           = null;

    public CameraInputInfo(ByteBuffer frameByteBuffer, FrameMetadata frameMetadata) {
        this.frameByteBuffer = frameByteBuffer;
        this.frameMetadata   = frameMetadata;
    }

    @Override
    public Bitmap getBitmap() {
        if (bitmap == null) {
            bitmap = Utils.convertToBitmap(frameByteBuffer, frameMetadata.getWidth(), frameMetadata.getHeight(), frameMetadata.getRotation());
        }
        return bitmap;
    }
}

package com.navigine.camera.utils;


import static com.navigine.camera.utils.Constants.ASPECT_RATIO_TOLERANCE;

import android.hardware.Camera;

import com.navigine.camera.model.CameraSizePair;

import java.util.ArrayList;
import java.util.List;

public class CameraInfoProvider {

    public static List<CameraSizePair> getValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

        List<CameraSizePair> validPreviewSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = previewSize.width * 1f / previewSize.height;

            for (Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = pictureSize.width * 1f / pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new CameraSizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        if (validPreviewSizes.isEmpty()) {
            for (Camera.Size previewSize : supportedPreviewSizes) {
                validPreviewSizes.add(new CameraSizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }
}

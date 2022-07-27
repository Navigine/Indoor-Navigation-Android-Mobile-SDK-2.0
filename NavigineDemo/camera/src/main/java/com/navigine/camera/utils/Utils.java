package com.navigine.camera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;

import com.google.mlkit.vision.common.InputImage;
import com.navigine.camera.ui.custom.GraphicOverlay;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Utils {

    public static RectF getBarcodeReticleBox(GraphicOverlay graphicOverlay) {
        float overlayWidth  = (float) graphicOverlay.getWidth();
        float overlayHeight = (float) graphicOverlay.getHeight();
        float boxWidth  = overlayWidth * 80 / 100;
        float boxHeight = overlayHeight * 35 / 100;
        float cx = overlayWidth / 2;
        float cy = overlayHeight / 2;
        return new RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2);
    }

    public static Bitmap convertToBitmap(ByteBuffer data, int width, int height, int rotationDegrees) {
        data.rewind();
        byte[] imageInBuffer = new byte[data.limit()];
        data.get(imageInBuffer, 0, imageInBuffer.length);
        try {
            YuvImage image = new YuvImage(imageInBuffer, InputImage.IMAGE_FORMAT_NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();

            Matrix matrix = new Matrix();
            matrix.postRotate((float) rotationDegrees);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

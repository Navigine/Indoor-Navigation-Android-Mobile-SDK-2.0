package com.navigine.camera.camera;

import com.navigine.camera.model.FrameMetadata;
import com.navigine.camera.ui.custom.GraphicOverlay;

import java.nio.ByteBuffer;

public interface FrameProcessor {

    void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay);

    void stop();
}

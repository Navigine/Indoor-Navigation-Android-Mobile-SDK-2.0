package com.navigine.camera.model;

public class FrameMetadata {

    private int width;
    private int height;
    private int rotation;

    public FrameMetadata(int width, int height, int rotation) {
        this.width    = width;
        this.height   = height;
        this.rotation = rotation;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRotation() {
        return rotation;
    }
}

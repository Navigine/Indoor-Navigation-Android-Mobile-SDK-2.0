package com.navigine.camera.ui.custom;

import android.content.Context;
import android.graphics.Canvas;

public abstract class Graphic {

    protected GraphicOverlay graphicOverlay = null;
    protected Context        context        = null;

    public Graphic(GraphicOverlay graphicOverlay) {
        this.graphicOverlay = graphicOverlay;
        this.context        = graphicOverlay.getContext();
    }

    abstract void draw(Canvas canvas);
}

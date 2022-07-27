package com.navigine.camera.ui.custom;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.navigine.camera.R;

public class BarcodeReticleGraphic extends BarcodeGraphicBase {

    private GraphicOverlay graphicOverlay = null;
    private CameraReticleAnimator animator = null;

    private Paint ripplePaint = null;
    private int rippleSizeOffset;
    private int rippleStrokeWidth;
    private int rippleAlpha;

    public BarcodeReticleGraphic(GraphicOverlay graphicOverlay, CameraReticleAnimator animator) {
        super(graphicOverlay);
        this.graphicOverlay = graphicOverlay;
        this.animator = animator;

        Resources resources = graphicOverlay.getResources();
        ripplePaint = new Paint();
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        rippleSizeOffset = resources.getDimensionPixelOffset(R.dimen.barcode_reticle_ripple_size_offset);
        rippleStrokeWidth = resources.getDimensionPixelOffset(R.dimen.barcode_reticle_ripple_stroke_width);
        rippleAlpha = ripplePaint.getAlpha();
    }

    @Override
    void draw(Canvas canvas) {
        super.draw(canvas);
        ripplePaint.setAlpha((int) (rippleAlpha * animator.getRippleAlphaScale()));
        ripplePaint.setStrokeWidth(rippleStrokeWidth * animator.getRippleStrokeWidthScale());
        float offset = rippleSizeOffset * animator.getRippleSizeScale();
        RectF rippleRect = new RectF(
                boxRect.left - offset,
                boxRect.top - offset,
                boxRect.right + offset,
                boxRect.bottom + offset);
        canvas.drawRoundRect(rippleRect, boxCornerRadius, boxCornerRadius, ripplePaint);
    }
}

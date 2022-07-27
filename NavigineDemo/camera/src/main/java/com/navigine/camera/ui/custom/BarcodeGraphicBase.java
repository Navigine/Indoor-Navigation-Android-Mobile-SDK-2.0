package com.navigine.camera.ui.custom;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.navigine.camera.R;
import com.navigine.camera.utils.Utils;

public class BarcodeGraphicBase extends Graphic {

    protected Paint boxPaint    = new Paint();
    protected Paint scrimPaint  = new Paint();
    protected Paint eraserPaint = new Paint();
    protected Paint pathPaint   = new Paint();

    protected float boxCornerRadius = (float) context.getResources().getDimensionPixelOffset(R.dimen.barcode_reticle_corner_radius);
    protected RectF boxRect = Utils.getBarcodeReticleBox(graphicOverlay);

    public BarcodeGraphicBase(GraphicOverlay graphicOverlay) {
        super(graphicOverlay);

        boxPaint.setColor(ContextCompat.getColor(context, R.color.camera_reticle_stroke));
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth((float) context.getResources().getDimensionPixelOffset(R.dimen.barcode_reticle_stroke_width));

        scrimPaint.setColor(ContextCompat.getColor(context, R.color.camera_reticle_background));

        eraserPaint.setStrokeWidth(boxPaint.getStrokeWidth());
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        pathPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(boxPaint.getStrokeWidth());
        pathPaint.setPathEffect(new CornerPathEffect(boxCornerRadius));
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawRect(0f, 0f, (float) canvas.getWidth(), (float) canvas.getHeight(), scrimPaint);
        eraserPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint);
        eraserPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint);
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxPaint);
    }
}

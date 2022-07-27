package com.navigine.camera.ui.custom;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;

public class BarcodeLoadingGraphic extends BarcodeGraphicBase {

    private ValueAnimator loadingAnimator = null;

    public BarcodeLoadingGraphic(GraphicOverlay graphicOverlay, ValueAnimator loadingAnimator) {
        super(graphicOverlay);
        this.loadingAnimator = loadingAnimator;
    }

    private PointF[] boxClockwiseCoordinates = new PointF[]{
            new PointF(boxRect.left, boxRect.top),
            new PointF(boxRect.right, boxRect.top),
            new PointF(boxRect.right, boxRect.bottom),
            new PointF(boxRect.left, boxRect.bottom)
    };

    private Point[] coordinateOffsetBits = new Point[]{
            new Point(1, 0),
            new Point(0, 1),
            new Point(-1, 0),
            new Point(0, -1)
    };

    private PointF lastPathPoint = new PointF();

    @Override
    void draw(Canvas canvas) {
        super.draw(canvas);

        float boxPerimeter = (boxRect.width() + boxRect.height()) * 2;
        Path path = new Path();

        float offsetLen = (boxPerimeter * (float) loadingAnimator.getAnimatedValue()) % boxPerimeter;
        int i = 0;
        while (i < 4) {
            float edgeLen = i % 2 == 0 ? boxRect.width() : boxRect.height();
            if (offsetLen <= edgeLen) {
                lastPathPoint.x = boxClockwiseCoordinates[i].x + coordinateOffsetBits[i].x * offsetLen;
                lastPathPoint.y = boxClockwiseCoordinates[i].y + coordinateOffsetBits[i].y * offsetLen;
                path.moveTo(lastPathPoint.x, lastPathPoint.y);
                break;
            }
            offsetLen -= edgeLen;
            i++;
        }

        float pathLen = boxPerimeter * 0.3f;
        for (int j = 0; j < 3; j++) {
            int index = (i + j) % 4;
            int nextIndex = (i + j + 1) % 4;

            float lineLen = Math.abs(boxClockwiseCoordinates[nextIndex].x - lastPathPoint.x) + Math.abs(boxClockwiseCoordinates[nextIndex].y - lastPathPoint.y);

            if (lineLen >= pathLen) {
                path.lineTo(lastPathPoint.x + pathLen * coordinateOffsetBits[index].x,
                        lastPathPoint.y + pathLen * coordinateOffsetBits[index].y);
                break;
            }

            lastPathPoint.x = boxClockwiseCoordinates[nextIndex].x;
            lastPathPoint.y = boxClockwiseCoordinates[nextIndex].y;
            path.lineTo(lastPathPoint.x, lastPathPoint.y);
            pathLen -= lineLen;
        }
        canvas.drawPath(path, pathPaint);
    }
}

package com.effects.shadowblur;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class ShadowDrawable extends Drawable {
    private Paint shadowPaint;
    private ShadowConfig config;
    private RectF boundsRect;

    public ShadowDrawable(ShadowConfig config) {
        this.config = config;
        this.shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.boundsRect = new RectF();
        setupPaint();
    }

    private void setupPaint() {
        shadowPaint.setColor(config.getShadowColor());
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setShadowLayer(
            config.getShadowRadius(),
            config.getShadowDx(),
            config.getShadowDy(),
            config.getShadowColor()
        );
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        boundsRect.set(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        switch (config.getShape()) {
            case RECTANGLE:
                if (config.getCornerRadius() > 0) {
                    canvas.drawRoundRect(
                        boundsRect,
                        config.getCornerRadius(),
                        config.getCornerRadius(),
                        shadowPaint
                    );
                } else {
                    canvas.drawRect(boundsRect, shadowPaint);
                }
                break;
                
            case CIRCLE:
                float centerX = boundsRect.centerX();
                float centerY = boundsRect.centerY();
                float radius = Math.min(boundsRect.width(), boundsRect.height()) / 2f;
                canvas.drawCircle(centerX, centerY, radius, shadowPaint);
                break;
                
            case OVAL:
                canvas.drawOval(boundsRect, shadowPaint);
                break;
        }
    }

    @Override
    public void setAlpha(int alpha) {
        shadowPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        shadowPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
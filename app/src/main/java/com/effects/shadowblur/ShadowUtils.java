package com.effects.shadowblur;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;

public class ShadowUtils {
    
    private static ShadowUtils instance;
    
    private ShadowUtils() {
    }
    
    public static synchronized ShadowUtils getInstance() {
        if (instance == null) {
            instance = new ShadowUtils();
        }
        return instance;
    }
    
    public void applySimpleShadow(View view, int shadowColor, float radius, float dx, float dy) {
        if (view.getLayerType() != View.LAYER_TYPE_SOFTWARE) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(shadowColor);
        paint.setShadowLayer(radius, dx, dy, shadowColor);
        
        view.setWillNotDraw(false);
        view.invalidate();
    }
    
    public void applyShadowWithConfig(View view, ShadowConfig config) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        view.setBackground(new ShadowDrawable(config));
        view.invalidate();
    }
    
    public void applyElevationShadow(View view, float elevation, int shadowColor) {
        view.setElevation(elevation);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 8f);
            }
        });
        view.setClipToOutline(true);
    }
    
    public Bitmap createShadowBitmap(int width, int height, ShadowConfig config) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(config.getShadowColor());
        paint.setShadowLayer(config.getShadowRadius(), 
                            config.getShadowDx(), 
                            config.getShadowDy(), 
                            config.getShadowColor());
        paint.setStyle(Paint.Style.FILL);
        
        RectF rect = new RectF(0, 0, width, height);
        
        switch (config.getShape()) {
            case RECTANGLE:
                if (config.getCornerRadius() > 0) {
                    canvas.drawRoundRect(rect, 
                                        config.getCornerRadius(), 
                                        config.getCornerRadius(), 
                                        paint);
                } else {
                    canvas.drawRect(rect, paint);
                }
                break;
                
            case CIRCLE:
                float centerX = width / 2f;
                float centerY = height / 2f;
                float circleRadius = Math.min(width, height) / 2f;
                canvas.drawCircle(centerX, centerY, circleRadius, paint);
                break;
                
            case OVAL:
                canvas.drawOval(rect, paint);
                break;
        }
        
        return bitmap;
    }
    
    public Drawable createShadowDrawable(Context context, ShadowConfig config) {
        return new ShadowDrawable(config);
    }
}
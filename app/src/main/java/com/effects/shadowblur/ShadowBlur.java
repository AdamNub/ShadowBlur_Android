package com.effects.shadowblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ShadowBlur {
    
    private static ShadowBlur instance;
    private Context context;
    private ShadowUtils shadowUtils;
    private BlurUtils blurUtils;
    
    private ShadowBlur(Context context) {
        this.context = context.getApplicationContext();
        this.shadowUtils = ShadowUtils.getInstance();
        this.blurUtils = BlurUtils.getInstance();
    }
    
    public static synchronized ShadowBlur init(Context context) {
        if (instance == null) {
            instance = new ShadowBlur(context);
        }
        return instance;
    }
    
    public static ShadowBlur getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ShadowBlur must be initialized first. Call ShadowBlur.init(context)");
        }
        return instance;
    }
    
    public ShadowBlur applyShadow(View view, int shadowColor, float radius, float dx, float dy) {
        shadowUtils.applySimpleShadow(view, shadowColor, radius, dx, dy);
        return this;
    }
    
    public ShadowBlur applyShadow(View view, ShadowConfig config) {
        shadowUtils.applyShadowWithConfig(view, config);
        return this;
    }
    
    public ShadowBlur applyElevation(View view, float elevation, int shadowColor) {
        shadowUtils.applyElevationShadow(view, elevation, shadowColor);
        return this;
    }
    
    public Drawable createShadowDrawable(ShadowConfig config) {
        return shadowUtils.createShadowDrawable(context, config);
    }
    
    public Bitmap createShadowBitmap(int width, int height, ShadowConfig config) {
        return shadowUtils.createShadowBitmap(width, height, config);
    }
    
    public Bitmap fastBlur(Bitmap bitmap, int radius) {
        return blurUtils.fastBlur(bitmap, radius);
    }
    
    public ShadowBlur applyGlassEffect(View view, BlurConfig config) {
        if (view instanceof GlassView) {
            ((GlassView) view).setBlurConfig(config);
        }
        return this;
    }
    
    public ShadowBlur applyGlassEffect(View view) {
        return applyGlassEffect(view, new BlurConfig());
    }
    
    public ShadowBlur setGlassCornerRadius(View view, float radius) {
        if (view instanceof GlassView) {
            ((GlassView) view).setCornerRadius(radius);
        }
        return this;
    }
    
    public ShadowBlur setGlassBorder(View view, float width, int color) {
        if (view instanceof GlassView) {
            ((GlassView) view).setBorder(width, color);
        }
        return this;
    }
    
    public ShadowBlur refreshGlass(View view) {
        if (view instanceof GlassView) {
            ((GlassView) view).refresh();
        }
        return this;
    }
}
package com.effects.shadowblur;

import android.graphics.Color;

public class BlurConfig {
    private float blurRadius = 15f;
    private float scaleFactor = 0.25f;
    private int overlayColor = Color.TRANSPARENT;
    private float overlayAlpha = 0.3f;
    private BlurAlgorithm algorithm = BlurAlgorithm.FAST_JAVA;
    
    public enum BlurAlgorithm {
        RENDERSCRIPT,
        FAST_JAVA
    }
    
    public BlurConfig() {
    }
    
    public BlurConfig setBlurRadius(float radius) {
        this.blurRadius = radius;
        return this;
    }
    
    public BlurConfig setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        return this;
    }
    
    public BlurConfig setOverlayColor(int color) {
        this.overlayColor = color;
        return this;
    }
    
    public BlurConfig setOverlayAlpha(float alpha) {
        this.overlayAlpha = alpha;
        return this;
    }
    
    public BlurConfig setAlgorithm(BlurAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
    
    public float getBlurRadius() {
        return blurRadius;
    }
    
    public float getScaleFactor() {
        return scaleFactor;
    }
    
    public int getOverlayColor() {
        return overlayColor;
    }
    
    public float getOverlayAlpha() {
        return overlayAlpha;
    }
    
    public BlurAlgorithm getAlgorithm() {
        return algorithm;
    }
}
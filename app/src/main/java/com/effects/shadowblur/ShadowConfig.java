package com.effects.shadowblur;

import android.graphics.Color;

public class ShadowConfig {
    private int shadowColor = Color.BLACK;
    private float shadowRadius = 10f;
    private float shadowDx = 0f;
    private float shadowDy = 4f;
    private ShadowShape shape = ShadowShape.RECTANGLE;
    private float cornerRadius = 0f;
    private float elevation = 0f;

    public ShadowConfig() {
    }

    public ShadowConfig setShadowColor(int color) {
        this.shadowColor = color;
        return this;
    }

    public ShadowConfig setShadowRadius(float radius) {
        this.shadowRadius = radius;
        return this;
    }

    public ShadowConfig setShadowDx(float dx) {
        this.shadowDx = dx;
        return this;
    }

    public ShadowConfig setShadowDy(float dy) {
        this.shadowDy = dy;
        return this;
    }

    public ShadowConfig setShape(ShadowShape shape) {
        this.shape = shape;
        return this;
    }

    public ShadowConfig setCornerRadius(float radius) {
        this.cornerRadius = radius;
        return this;
    }

    public ShadowConfig setElevation(float elevation) {
        this.elevation = elevation;
        return this;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public float getShadowDx() {
        return shadowDx;
    }

    public float getShadowDy() {
        return shadowDy;
    }

    public ShadowShape getShape() {
        return shape;
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public float getElevation() {
        return elevation;
    }
}
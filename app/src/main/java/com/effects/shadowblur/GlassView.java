package com.effects.shadowblur;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class GlassView extends ViewGroup {
    
    private static final String TAG = "GlassView";
    
    // Paints
    private Paint glassPaint;
    private Paint borderPaint;
    private Paint edgeHighlightPaint;
    private Paint edgeShadowPaint;
    private Paint innerGlowPaint;
    private Paint cornerHighlightPaint;
    
    // Configuration
    private BlurConfig config;
    private Bitmap blurredBackground;
    private Bitmap originalCapturedBitmap; // Store original capture
    
    // 3D Properties
    private float cornerRadius = 30f;
    private float borderWidth = 2f;
    private int borderColor = Color.argb(180, 255, 255, 255);
    private int overlayColor = Color.WHITE;
    private float overlayAlpha = 0.15f;
    private float blurRadius = 20f;
    
    // 3D intensities
    private float edgeHighlightIntensity = 0.7f;
    private float edgeShadowIntensity = 0.5f;
    private float innerGlowIntensity = 0.3f;

    public GlassView(Context context) {
        super(context);
        init();
    }

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setWillNotDraw(false);
        
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setStyle(Paint.Style.FILL);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        
        edgeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgeHighlightPaint.setStyle(Paint.Style.STROKE);
        
        edgeShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgeShadowPaint.setStyle(Paint.Style.STROKE);
        
        innerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerGlowPaint.setStyle(Paint.Style.STROKE);
        
        cornerHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerHighlightPaint.setStyle(Paint.Style.FILL);
        
        config = new BlurConfig()
                .setBlurRadius(blurRadius)
                .setOverlayColor(overlayColor)
                .setOverlayAlpha(overlayAlpha);
        
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                captureBackground();
            }
        });
    }

    private void captureBackground() {
        try {
            View rootView = getRootView();
            if (rootView == null) return;
            
            int[] location = new int[2];
            getLocationOnScreen(location);
            
            rootView.setDrawingCacheEnabled(true);
            rootView.buildDrawingCache();
            
            Bitmap rootBitmap = rootView.getDrawingCache();
            if (rootBitmap == null) {
                rootView.setDrawingCacheEnabled(false);
                return;
            }
            
            int x = Math.max(0, location[0]);
            int y = Math.max(0, location[1]);
            int width = Math.min(getWidth(), rootBitmap.getWidth() - x);
            int height = Math.min(getHeight(), rootBitmap.getHeight() - y);
            
            if (width <= 0 || height <= 0) {
                rootView.setDrawingCacheEnabled(false);
                return;
            }
            
            // Store the original captured bitmap
            originalCapturedBitmap = Bitmap.createBitmap(rootBitmap, x, y, width, height);
            
            // Apply initial blur
            applyBlur();
            
            rootView.setDrawingCacheEnabled(false);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void applyBlur() {
        if (originalCapturedBitmap == null || originalCapturedBitmap.isRecycled()) {
            return;
        }
        
        try {
            // Scale down for performance
            float scale = 0.5f;
            int scaledWidth = (int)(originalCapturedBitmap.getWidth() * scale);
            int scaledHeight = (int)(originalCapturedBitmap.getHeight() * scale);
            
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalCapturedBitmap, 
                scaledWidth, scaledHeight, true);
            
            // Apply blur with current radius
            Bitmap blurred = BlurUtils.getInstance().fastBlur(scaledBitmap, (int) blurRadius);
            
            if (blurred != null) {
                // Scale back up
                blurredBackground = Bitmap.createScaledBitmap(blurred, 
                    originalCapturedBitmap.getWidth(), 
                    originalCapturedBitmap.getHeight(), 
                    true);
            }
            
            scaledBitmap.recycle();
            if (blurred != null && blurred != scaledBitmap) {
                blurred.recycle();
            }
            
            invalidate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxWidth = 0;
        int totalHeight = 0;
        
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                totalHeight += child.getMeasuredHeight();
            }
        }
        
        maxWidth += getPaddingLeft() + getPaddingRight();
        totalHeight += getPaddingTop() + getPaddingBottom();
        
        int width = resolveSize(Math.max(maxWidth, getSuggestedMinimumWidth()), widthMeasureSpec);
        int height = resolveSize(Math.max(totalHeight, getSuggestedMinimumHeight()), heightMeasureSpec);
        
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = getPaddingTop();
        int left = getPaddingLeft();
        
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                
                child.layout(left, top, left + childWidth, top + childHeight);
                top += childHeight;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw the blurred background
        if (blurredBackground != null && !blurredBackground.isRecycled()) {
            canvas.drawBitmap(blurredBackground, 0, 0, null);
        }
        
        // Inner glow
        if (innerGlowIntensity > 0) {
            innerGlowPaint.setColor(Color.argb((int)(30 * innerGlowIntensity), 255, 255, 255));
            innerGlowPaint.setStrokeWidth(cornerRadius / 3);
            
            RectF innerRect = new RectF(
                cornerRadius/4, cornerRadius/4,
                getWidth() - cornerRadius/4, getHeight() - cornerRadius/4
            );
            canvas.drawRoundRect(innerRect, cornerRadius/2, cornerRadius/2, innerGlowPaint);
        }
        
        // Edge highlights
        if (edgeHighlightIntensity > 0) {
            edgeHighlightPaint.setColor(Color.argb((int)(80 * edgeHighlightIntensity), 255, 255, 255));
            edgeHighlightPaint.setStrokeWidth(borderWidth * 2);
            
            Path topPath = new Path();
            topPath.moveTo(cornerRadius, borderWidth);
            topPath.lineTo(getWidth() - cornerRadius, borderWidth);
            canvas.drawPath(topPath, edgeHighlightPaint);
            
            Path leftPath = new Path();
            leftPath.moveTo(borderWidth, cornerRadius);
            leftPath.lineTo(borderWidth, getHeight() - cornerRadius);
            canvas.drawPath(leftPath, edgeHighlightPaint);
        }
        
        // Edge shadows
        if (edgeShadowIntensity > 0) {
            edgeShadowPaint.setColor(Color.argb((int)(60 * edgeShadowIntensity), 0, 0, 0));
            edgeShadowPaint.setStrokeWidth(borderWidth * 1.5f);
            
            Path bottomPath = new Path();
            bottomPath.moveTo(cornerRadius, getHeight() - borderWidth);
            bottomPath.lineTo(getWidth() - cornerRadius, getHeight() - borderWidth);
            canvas.drawPath(bottomPath, edgeShadowPaint);
            
            Path rightPath = new Path();
            rightPath.moveTo(getWidth() - borderWidth, cornerRadius);
            rightPath.lineTo(getWidth() - borderWidth, getHeight() - cornerRadius);
            canvas.drawPath(rightPath, edgeShadowPaint);
        }
        
        // Glass overlay
        int alpha = (int) (overlayAlpha * 255);
        glassPaint.setColor(Color.argb(alpha,
                Color.red(overlayColor),
                Color.green(overlayColor),
                Color.blue(overlayColor)));
        
        if (cornerRadius > 0) {
            RectF rect = new RectF(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glassPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), glassPaint);
        }
        
        // Border
        if (borderWidth > 0) {
            LinearGradient gradient = new LinearGradient(
                0, 0, getWidth(), getHeight(),
                new int[]{
                    Color.argb(255, 255, 255, 255),
                    Color.argb(200, 200, 220, 255),
                    Color.argb(150, 150, 180, 255)
                },
                null,
                Shader.TileMode.CLAMP
            );
            
            borderPaint.setShader(gradient);
            borderPaint.setStrokeWidth(borderWidth);
            borderPaint.setColor(borderColor);
            
            RectF borderRect = new RectF(
                borderWidth/2, borderWidth/2,
                getWidth() - borderWidth/2, getHeight() - borderWidth/2
            );
            
            if (cornerRadius > 0) {
                canvas.drawRoundRect(borderRect, 
                    cornerRadius - borderWidth/2, 
                    cornerRadius - borderWidth/2, 
                    borderPaint);
            } else {
                canvas.drawRect(borderRect, borderPaint);
            }
            
            borderPaint.setShader(null);
        }
        
        // Corner highlights
        if (cornerRadius > 0) {
            cornerHighlightPaint.setColor(Color.argb(80, 255, 255, 255));
            canvas.drawCircle(cornerRadius/2, cornerRadius/2, cornerRadius/5, cornerHighlightPaint);
            
            cornerHighlightPaint.setColor(Color.argb(40, 0, 0, 0));
            canvas.drawCircle(
                getWidth() - cornerRadius/2,
                getHeight() - cornerRadius/2,
                cornerRadius/5, 
                cornerHighlightPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            captureBackground();
        }
    }

    public void setBlurConfig(BlurConfig config) {
        this.config = config;
        this.blurRadius = config.getBlurRadius();
        this.overlayColor = config.getOverlayColor();
        this.overlayAlpha = config.getOverlayAlpha();
        
        // Reapply blur with new radius
        if (originalCapturedBitmap != null && !originalCapturedBitmap.isRecycled()) {
            applyBlur();
        } else {
            captureBackground();
        }
        
        invalidate();
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    public void setBorder(float width, int color) {
        this.borderWidth = width;
        this.borderColor = color;
        invalidate();
    }

    public void setOverlay(int color, float alpha) {
        this.overlayColor = color;
        this.overlayAlpha = alpha;
        invalidate();
    }

    public void setBlurRadius(float radius) {
        this.blurRadius = radius;
        
        // Reapply blur with new radius
        if (originalCapturedBitmap != null && !originalCapturedBitmap.isRecycled()) {
            applyBlur();
        } else {
            captureBackground();
        }
    }

    public void setEdgeHighlightIntensity(float intensity) {
        this.edgeHighlightIntensity = intensity;
        invalidate();
    }

    public void setEdgeShadowIntensity(float intensity) {
        this.edgeShadowIntensity = intensity;
        invalidate();
    }

    public void setInnerGlowIntensity(float intensity) {
        this.innerGlowIntensity = intensity;
        invalidate();
    }

    public void refresh() {
        captureBackground();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (blurredBackground != null && !blurredBackground.isRecycled()) {
            blurredBackground.recycle();
            blurredBackground = null;
        }
        if (originalCapturedBitmap != null && !originalCapturedBitmap.isRecycled()) {
            originalCapturedBitmap.recycle();
            originalCapturedBitmap = null;
        }
    }
}
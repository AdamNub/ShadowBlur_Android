package com.effects.shadowblur;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class GlassView extends ViewGroup {
    private Paint glassPaint;
    private Paint borderPaint;
    private Paint edgeHighlightPaint;
    private Paint innerGlowPaint;
    private BlurConfig config;
    private Bitmap blurredBackground;
    private float cornerRadius = 0f;
    private float borderWidth = 2f;
    private int borderColor = Color.WHITE;
    private boolean isInitialized = false;
    
    // 3D effect properties
    private float edgeHighlightIntensity = 0.3f;
    private float innerGlowIntensity = 0.2f;
    private int highlightColor = Color.WHITE;
    private int shadowColor = Color.BLACK;

    public GlassView(Context context) {
        super(context);
        init(context, null);
    }

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GlassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Main glass paint
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        
        // Edge highlight paint (for 3D effect)
        edgeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgeHighlightPaint.setStyle(Paint.Style.STROKE);
        
        // Inner glow paint
        innerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerGlowPaint.setStyle(Paint.Style.STROKE);

        // Default config
        config = new BlurConfig()
                .setBlurRadius(20f)
                .setOverlayColor(Color.WHITE)
                .setOverlayAlpha(0.15f) // More transparent for better 3D effect
                .setAlgorithm(BlurConfig.BlurAlgorithm.FAST_JAVA);

        // Enable drawing
        setWillNotDraw(false);
        
        // Use hardware layer for better performance with complex drawing
        setLayerType(LAYER_TYPE_HARDWARE, null);

        // Wait for layout
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isInitialized && getWidth() > 0 && getHeight() > 0) {
                    captureBackground();
                    isInitialized = true;
                }
            }
        });
    }

    private void captureBackground() {
        try {
            View rootView = getRootView();
            if (rootView == null) return;

            rootView.setDrawingCacheEnabled(true);
            rootView.buildDrawingCache();

            Bitmap rootBitmap = rootView.getDrawingCache();
            if (rootBitmap == null) {
                rootView.setDrawingCacheEnabled(false);
                return;
            }

            int[] location = new int[2];
            getLocationOnScreen(location);

            int x = Math.max(0, location[0]);
            int y = Math.max(0, location[1]);
            int width = Math.min(getWidth(), rootBitmap.getWidth() - x);
            int height = Math.min(getHeight(), rootBitmap.getHeight() - y);

            if (width <= 0 || height <= 0) {
                rootView.setDrawingCacheEnabled(false);
                return;
            }

            Bitmap background = Bitmap.createBitmap(rootBitmap, x, y, width, height);

            if (background != null) {
                // Scale down for performance
                float scale = 0.5f;
                int scaledWidth = (int) (width * scale);
                int scaledHeight = (int) (height * scale);

                Bitmap scaled = Bitmap.createScaledBitmap(background, scaledWidth, scaledHeight, true);

                // Apply blur
                blurredBackground = BlurUtils.getInstance().fastBlur(scaled, (int) config.getBlurRadius());

                // Scale back up
                if (blurredBackground != null) {
                    blurredBackground = Bitmap.createScaledBitmap(blurredBackground, width, height, true);
                }

                if (scaled != null && !scaled.isRecycled()) {
                    scaled.recycle();
                }
                background.recycle();
            }

            rootView.setDrawingCacheEnabled(false);
            invalidate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw blurred background
        if (blurredBackground != null && !blurredBackground.isRecycled()) {
            canvas.drawBitmap(blurredBackground, 0, 0, null);
        }

        // Draw base glass overlay
        int overlayColor = config.getOverlayColor();
        int alpha = (int) (config.getOverlayAlpha() * 255);
        glassPaint.setColor(Color.argb(alpha,
                Color.red(overlayColor),
                Color.green(overlayColor),
                Color.blue(overlayColor)));

        RectF rectF = new RectF(0, 0, getWidth(), getHeight());

        // Draw main glass with rounded corners
        if (cornerRadius > 0) {
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, glassPaint);
        } else {
            canvas.drawRect(rectF, glassPaint);
        }

        // ===== 3D GLASS EFFECTS =====

        // 1. Inner glow (foggy edge effect)
        if (innerGlowIntensity > 0 && cornerRadius > 0) {
            innerGlowPaint.setColor(Color.argb((int)(40 * innerGlowIntensity), 255, 255, 255));
            innerGlowPaint.setStrokeWidth(cornerRadius / 3);
            
            RectF innerGlowRect = new RectF(
                cornerRadius / 2,
                cornerRadius / 2,
                getWidth() - cornerRadius / 2,
                getHeight() - cornerRadius / 2
            );
            canvas.drawRoundRect(innerGlowRect, cornerRadius / 1.5f, cornerRadius / 1.5f, innerGlowPaint);
        }

        // 2. Edge highlights (top and left)
        if (edgeHighlightIntensity > 0) {
            edgeHighlightPaint.setColor(Color.argb((int)(80 * edgeHighlightIntensity), 255, 255, 255));
            edgeHighlightPaint.setStrokeWidth(borderWidth * 1.5f);
            
            Path highlightPath = new Path();
            if (cornerRadius > 0) {
                // Top edge highlight
                highlightPath.moveTo(cornerRadius / 2, borderWidth / 2);
                highlightPath.lineTo(getWidth() - cornerRadius / 2, borderWidth / 2);
                
                // Left edge highlight
                highlightPath.moveTo(borderWidth / 2, cornerRadius / 2);
                highlightPath.lineTo(borderWidth / 2, getHeight() - cornerRadius / 2);
            } else {
                highlightPath.moveTo(0, 0);
                highlightPath.lineTo(getWidth(), 0);
                highlightPath.moveTo(0, 0);
                highlightPath.lineTo(0, getHeight());
            }
            canvas.drawPath(highlightPath, edgeHighlightPaint);
        }

        // 3. Edge shadows (bottom and right)
        if (edgeHighlightIntensity > 0) {
            edgeHighlightPaint.setColor(Color.argb((int)(60 * edgeHighlightIntensity), 0, 0, 0));
            edgeHighlightPaint.setStrokeWidth(borderWidth);
            
            Path shadowPath = new Path();
            if (cornerRadius > 0) {
                // Bottom edge shadow
                shadowPath.moveTo(cornerRadius / 2, getHeight() - borderWidth / 2);
                shadowPath.lineTo(getWidth() - cornerRadius / 2, getHeight() - borderWidth / 2);
                
                // Right edge shadow
                shadowPath.moveTo(getWidth() - borderWidth / 2, cornerRadius / 2);
                shadowPath.lineTo(getWidth() - borderWidth / 2, getHeight() - cornerRadius / 2);
            } else {
                shadowPath.moveTo(0, getHeight());
                shadowPath.lineTo(getWidth(), getHeight());
                shadowPath.moveTo(getWidth(), 0);
                shadowPath.lineTo(getWidth(), getHeight());
            }
            canvas.drawPath(shadowPath, edgeHighlightPaint);
        }

        // 4. Main border (with gradient for 3D effect)
        if (borderWidth > 0) {
            // Create gradient shader for border
            LinearGradient borderGradient = new LinearGradient(
                0, 0, getWidth(), getHeight(),
                new int[]{
                    Color.argb(200, 255, 255, 255),
                    Color.argb(150, 200, 200, 255),
                    Color.argb(100, 100, 100, 255)
                },
                null,
                Shader.TileMode.CLAMP
            );
            
            borderPaint.setShader(borderGradient);
            borderPaint.setStrokeWidth(borderWidth);
            
            if (cornerRadius > 0) {
                RectF borderRect = new RectF(
                    borderWidth / 2,
                    borderWidth / 2,
                    getWidth() - borderWidth / 2,
                    getHeight() - borderWidth / 2
                );
                canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint);
            } else {
                RectF borderRect = new RectF(
                    borderWidth / 2,
                    borderWidth / 2,
                    getWidth() - borderWidth / 2,
                    getHeight() - borderWidth / 2
                );
                canvas.drawRect(borderRect, borderPaint);
            }
            
            borderPaint.setShader(null);
        }

        // 5. Corner highlights (for extra 3D effect)
        if (cornerRadius > 0 && edgeHighlightIntensity > 0) {
            Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cornerPaint.setColor(Color.argb(100, 255, 255, 255));
            
            // Top-left corner highlight
            canvas.drawCircle(cornerRadius / 2, cornerRadius / 2, cornerRadius / 4, cornerPaint);
            
            cornerPaint.setColor(Color.argb(50, 0, 0, 0));
            // Bottom-right corner shadow
            canvas.drawCircle(getWidth() - cornerRadius / 2, getHeight() - cornerRadius / 2, cornerRadius / 4, cornerPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            postDelayed(() -> captureBackground(), 100);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                captureBackground();
            }
        }, 200);
    }

    // Public methods
    public void setBlurConfig(BlurConfig config) {
        this.config = config;
        captureBackground();
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
    
    // New methods for 3D effect control
    public void setEdgeHighlightIntensity(float intensity) {
        this.edgeHighlightIntensity = Math.max(0, Math.min(1, intensity));
        invalidate();
    }
    
    public void setInnerGlowIntensity(float intensity) {
        this.innerGlowIntensity = Math.max(0, Math.min(1, intensity));
        invalidate();
    }
    
    public void setHighlightColor(int color) {
        this.highlightColor = color;
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
    }
}
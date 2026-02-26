package com.effects.shadowblur;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

public class GlassView extends View {
    
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
        // Enable hardware acceleration
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setWillNotDraw(false);
        
        // Initialize paints
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
        
        // Default config
        config = new BlurConfig()
                .setBlurRadius(blurRadius)
                .setOverlayColor(overlayColor)
                .setOverlayAlpha(overlayAlpha);
        
        // Listen for layout changes to recapture background
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                captureBackground();
            }
        });
    }

    /**
     * Captures whatever is BEHIND this view - no hierarchy dependency
     */
    private void captureBackground() {
        try {
            // Get the root view (top-most parent)
            View rootView = getRootView();
            if (rootView == null) return;
            
            // Get location of this view on screen
            int[] location = new int[2];
            getLocationOnScreen(location);
            
            // Enable drawing cache
            rootView.setDrawingCacheEnabled(true);
            rootView.buildDrawingCache();
            
            // Get the root view bitmap
            Bitmap rootBitmap = rootView.getDrawingCache();
            if (rootBitmap == null) {
                rootView.setDrawingCacheEnabled(false);
                return;
            }
            
            // Calculate the area BEHIND this view
            int x = Math.max(0, location[0]);
            int y = Math.max(0, location[1]);
            int width = Math.min(getWidth(), rootBitmap.getWidth() - x);
            int height = Math.min(getHeight(), rootBitmap.getHeight() - y);
            
            if (width <= 0 || height <= 0) {
                rootView.setDrawingCacheEnabled(false);
                return;
            }
            
            // Capture ONLY the area behind this view
            Bitmap behindBitmap = Bitmap.createBitmap(rootBitmap, x, y, width, height);
            
            // Scale down for performance
            float scale = 0.5f;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(behindBitmap, 
                (int)(width * scale), (int)(height * scale), true);
            
            // Apply blur using BlurUtils
            blurredBackground = BlurUtils.getInstance().fastBlur(scaledBitmap, (int) blurRadius);
            
            // Scale back up to original size
            if (blurredBackground != null) {
                blurredBackground = Bitmap.createScaledBitmap(blurredBackground, width, height, true);
            }
            
            // Clean up
            scaledBitmap.recycle();
            behindBitmap.recycle();
            rootView.setDrawingCacheEnabled(false);
            
            // Redraw with new blurred background
            invalidate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Respect XML measurements - NO stretching!
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw the blurred background (what's behind this view)
        if (blurredBackground != null && !blurredBackground.isRecycled()) {
            canvas.drawBitmap(blurredBackground, 0, 0, null);
        }
        
        // ===== 3D GLASS EFFECTS =====
        
        // 1. Inner glow
        if (innerGlowIntensity > 0) {
            innerGlowPaint.setColor(Color.argb((int)(30 * innerGlowIntensity), 255, 255, 255));
            innerGlowPaint.setStrokeWidth(cornerRadius / 3);
            
            RectF innerRect = new RectF(
                cornerRadius/4, cornerRadius/4,
                getWidth() - cornerRadius/4, getHeight() - cornerRadius/4
            );
            canvas.drawRoundRect(innerRect, cornerRadius/2, cornerRadius/2, innerGlowPaint);
        }
        
        // 2. Edge highlights (top & left)
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
        
        // 3. Edge shadows (bottom & right)
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
        
        // 4. Main glass overlay
        int alpha = (int) (overlayAlpha * 255);
        glassPaint.setColor(Color.argb(alpha,
                Color.red(overlayColor),
                Color.green(overlayColor),
                Color.blue(overlayColor)));
        
        // Draw with rounded corners
        if (cornerRadius > 0) {
            RectF rect = new RectF(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glassPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), glassPaint);
        }
        
        // 5. Gradient border
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
        
        // 6. Corner highlights
        if (cornerRadius > 0) {
            // Top-left highlight
            cornerHighlightPaint.setColor(Color.argb(80, 255, 255, 255));
            canvas.drawCircle(cornerRadius/2, cornerRadius/2, cornerRadius/5, cornerHighlightPaint);
            
            // Bottom-right shadow
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
        // Recapture background when size changes
        if (w > 0 && h > 0) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    captureBackground();
                }
            }, 100);
        }
    }

    // ========== PUBLIC METHODS ==========

    public void setBlurConfig(BlurConfig config) {
        this.config = config;
        this.blurRadius = config.getBlurRadius();
        this.overlayColor = config.getOverlayColor();
        this.overlayAlpha = config.getOverlayAlpha();
        captureBackground();
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
        captureBackground();
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
        // Clean up bitmap
        if (blurredBackground != null && !blurredBackground.isRecycled()) {
            blurredBackground.recycle();
            blurredBackground = null;
        }
    }
}
package com.effects.shadowblur;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class GlassView2 extends FrameLayout {
    private Paint glassPaint;
    private Paint borderPaint;
    private Paint edgeHighlightPaint;
    private Paint edgeShadowPaint;
    private Paint innerGlowPaint;
    private Paint cornerHighlightPaint;
    
    private float cornerRadius = 30f;
    private float borderWidth = 2f;
    private int borderColor = Color.argb(180, 255, 255, 255);
    private int overlayColor = Color.WHITE;
    private float overlayAlpha = 0.15f;
    private float blurRadius = 20f;
    
    // 3D effect properties
    private float edgeHighlightIntensity = 0.7f;
    private float edgeShadowIntensity = 0.5f;
    private float innerGlowIntensity = 0.4f;
    private float depthFactor = 1.0f;
    
    private Bitmap blurBitmap;
    private Canvas blurCanvas;

    public GlassView2(Context context) {
        super(context);
        init();
    }

    public GlassView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlassView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setBackgroundColor(Color.TRANSPARENT);
        
        // Main glass paint
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setStyle(Paint.Style.FILL);
        
        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        
        // Edge highlight paint (top and left)
        edgeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgeHighlightPaint.setStyle(Paint.Style.STROKE);
        
        // Edge shadow paint (bottom and right)
        edgeShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgeShadowPaint.setStyle(Paint.Style.STROKE);
        
        // Inner glow paint
        innerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerGlowPaint.setStyle(Paint.Style.STROKE);
        
        // Corner highlight paint
        cornerHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerHighlightPaint.setStyle(Paint.Style.FILL);
        
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            createBlurBitmap(w, h);
        }
    }

    private void createBlurBitmap(int width, int height) {
        try {
            blurBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            blurCanvas = new Canvas(blurBitmap);
            
            blurCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            
            // Create gradient for glass effect with depth
            LinearGradient gradient = new LinearGradient(
                0, 0, width * 0.7f, height * 0.7f,
                new int[]{
                    Color.argb((int)(80 * depthFactor), 255, 255, 255),
                    Color.argb((int)(40 * depthFactor), 200, 220, 255),
                    Color.argb((int)(20 * depthFactor), 150, 180, 255),
                    Color.argb((int)(10 * depthFactor), 100, 120, 200)
                },
                null,
                Shader.TileMode.CLAMP
            );
            
            Paint blurPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blurPaint.setShader(gradient);
            blurPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
            
            blurCanvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, blurPaint);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        
        // Create round rect clip
        Path path = new Path();
        path.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(path);
        
        // Draw base blur
        if (blurBitmap != null && !blurBitmap.isRecycled()) {
            canvas.drawBitmap(blurBitmap, 0, 0, null);
        }
        
        // Draw children
        super.dispatchDraw(canvas);
        
        // ===== 3D GLASS EFFECTS =====
        
        // 1. Inner glow (foggy edge effect)
        if (innerGlowIntensity > 0) {
            innerGlowPaint.setColor(Color.argb((int)(40 * innerGlowIntensity), 200, 220, 255));
            innerGlowPaint.setStrokeWidth(cornerRadius / 2);
            innerGlowPaint.setMaskFilter(new BlurMaskFilter(cornerRadius / 2, BlurMaskFilter.Blur.NORMAL));
            
            RectF innerGlowRect = new RectF(
                cornerRadius / 3,
                cornerRadius / 3,
                getWidth() - cornerRadius / 3,
                getHeight() - cornerRadius / 3
            );
            canvas.drawRoundRect(innerGlowRect, cornerRadius / 1.5f, cornerRadius / 1.5f, innerGlowPaint);
        }
        
        // 2. Edge highlights (top and left) - creates 3D raised effect
        if (edgeHighlightIntensity > 0) {
            edgeHighlightPaint.setColor(Color.argb((int)(100 * edgeHighlightIntensity), 255, 255, 255));
            edgeHighlightPaint.setStrokeWidth(borderWidth * 2);
            edgeHighlightPaint.setMaskFilter(new BlurMaskFilter(borderWidth, BlurMaskFilter.Blur.NORMAL));
            
            // Top edge highlight
            Path topHighlight = new Path();
            topHighlight.moveTo(cornerRadius, borderWidth);
            topHighlight.lineTo(getWidth() - cornerRadius, borderWidth);
            canvas.drawPath(topHighlight, edgeHighlightPaint);
            
            // Left edge highlight
            Path leftHighlight = new Path();
            leftHighlight.moveTo(borderWidth, cornerRadius);
            leftHighlight.lineTo(borderWidth, getHeight() - cornerRadius);
            canvas.drawPath(leftHighlight, edgeHighlightPaint);
        }
        
        // 3. Edge shadows (bottom and right) - creates depth
        if (edgeShadowIntensity > 0) {
            edgeShadowPaint.setColor(Color.argb((int)(80 * edgeShadowIntensity), 0, 0, 0));
            edgeShadowPaint.setStrokeWidth(borderWidth * 1.5f);
            edgeShadowPaint.setMaskFilter(new BlurMaskFilter(borderWidth, BlurMaskFilter.Blur.NORMAL));
            
            // Bottom edge shadow
            Path bottomShadow = new Path();
            bottomShadow.moveTo(cornerRadius, getHeight() - borderWidth);
            bottomShadow.lineTo(getWidth() - cornerRadius, getHeight() - borderWidth);
            canvas.drawPath(bottomShadow, edgeShadowPaint);
            
            // Right edge shadow
            Path rightShadow = new Path();
            rightShadow.moveTo(getWidth() - borderWidth, cornerRadius);
            rightShadow.lineTo(getWidth() - borderWidth, getHeight() - cornerRadius);
            canvas.drawPath(rightShadow, edgeShadowPaint);
        }
        
        // 4. Main glass overlay
        int alpha = (int) (overlayAlpha * 255);
        glassPaint.setColor(Color.argb(alpha,
                Color.red(overlayColor),
                Color.green(overlayColor),
                Color.blue(overlayColor)));
        
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius, glassPaint);
        
        // 5. 3D Gradient Border
        if (borderWidth > 0) {
            // Create 3D gradient for border
            LinearGradient borderGradient = new LinearGradient(
                0, 0, getWidth(), getHeight(),
                new int[]{
                    Color.argb(255, 255, 255, 255),
                    Color.argb(200, 200, 220, 255),
                    Color.argb(150, 150, 180, 255),
                    Color.argb(100, 100, 120, 200)
                },
                null,
                Shader.TileMode.CLAMP
            );
            
            borderPaint.setShader(borderGradient);
            borderPaint.setStrokeWidth(borderWidth);
            borderPaint.setStyle(Paint.Style.STROKE);
            
            canvas.drawRoundRect(
                    borderWidth/2,
                    borderWidth/2,
                    getWidth() - borderWidth/2,
                    getHeight() - borderWidth/2,
                    cornerRadius - borderWidth/2,
                    cornerRadius - borderWidth/2,
                    borderPaint);
            
            borderPaint.setShader(null);
        }
        
        // 6. Corner highlights (extra 3D effect)
        if (cornerRadius > 0) {
            // Top-left corner highlight
            cornerHighlightPaint.setColor(Color.argb(100, 255, 255, 255));
            cornerHighlightPaint.setMaskFilter(new BlurMaskFilter(cornerRadius / 3, BlurMaskFilter.Blur.NORMAL));
            canvas.drawCircle(cornerRadius / 2, cornerRadius / 2, cornerRadius / 4, cornerHighlightPaint);
            
            // Bottom-right corner shadow
            cornerHighlightPaint.setColor(Color.argb(60, 0, 0, 0));
            canvas.drawCircle(
                getWidth() - cornerRadius / 2,
                getHeight() - cornerRadius / 2,
                cornerRadius / 4,
                cornerHighlightPaint);
        }
        
        canvas.restore();
    }

    // Public methods for 3D customization
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        if (getWidth() > 0 && getHeight() > 0) {
            createBlurBitmap(getWidth(), getHeight());
        }
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
        if (getWidth() > 0 && getHeight() > 0) {
            createBlurBitmap(getWidth(), getHeight());
        }
        invalidate();
    }

    // 3D effect controls
    public void setEdgeHighlightIntensity(float intensity) {
        this.edgeHighlightIntensity = Math.max(0, Math.min(1, intensity));
        invalidate();
    }

    public void setEdgeShadowIntensity(float intensity) {
        this.edgeShadowIntensity = Math.max(0, Math.min(1, intensity));
        invalidate();
    }

    public void setInnerGlowIntensity(float intensity) {
        this.innerGlowIntensity = Math.max(0, Math.min(1, intensity));
        invalidate();
    }

    public void setDepthFactor(float factor) {
        this.depthFactor = Math.max(0.5f, Math.min(2.0f, factor));
        if (getWidth() > 0 && getHeight() > 0) {
            createBlurBitmap(getWidth(), getHeight());
        }
        invalidate();
    }

    public void refresh() {
        if (getWidth() > 0 && getHeight() > 0) {
            createBlurBitmap(getWidth(), getHeight());
            invalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (blurBitmap != null && !blurBitmap.isRecycled()) {
            blurBitmap.recycle();
            blurBitmap = null;
        }
    }
}
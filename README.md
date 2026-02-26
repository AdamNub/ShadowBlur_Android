
```
# ✨ ShadowBlur Library

<div align="center">
  <h3>A powerful Android library for 3D Glass, Blur and Shadow effects</h3>
  <p>Create stunning modern UIs with frosted glass, floating shadows, and smooth blur effects</p>
</div>

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🥂 **3D Glass Effect (GlassView2)** | Frosted glass with edge highlights, shadows, and depth |
| 🪟 **Live Glass Effect (GlassView)** | Real-time background capture with blur |
| 🌫️ **Blur Effects** | Fast blur algorithm for images and views |
| 🌑 **Shadow Effects** | Custom shadows with blur radius, offset, and color |
| 👆 **Button Press Effects** | Built-in touch animations with visual feedback |
| 🎭 **Customizable** | Full control over colors, radius, intensity, and depth |

## 📦 Installation

### Step 1: Add JitPack repository

Add this to your **project** `build.gradle` or `settings.gradle`:

```gradle
// In settings.gradle (root project)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }  // Add this line
    }
}
```

Step 2: Add dependency

Add this to your app build.gradle:

```gradle
dependencies {
    implementation 'com.github.AdamNub:ShadowBlur_Android:1.1.0'
}
```

Alternative: Use specific commit hash

```gradle
dependencies {
    implementation 'com.github.AdamNub:ShadowBlur_Android:main-SNAPSHOT'
}
```

🚀 Quick Start

Initialize the library

```java
// In your Activity or Application class
ShadowBlur.init(this);
ShadowBlur shadowBlur = ShadowBlur.getInstance();
```

🥂 3D Glass View (GlassView2) - Self-contained glass effect

GlassView2 creates its own glass effect using gradients and blurs - no background capture needed.

XML Layout

```xml
<com.effects.shadowblur.GlassView2
    android:id="@+id/glassView2"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="3D Glass"
        android:textColor="#FFFFFF"/>
</com.effects.shadowblur.GlassView2>
```

Java Code

```java
GlassView2 glassView2 = findViewById(R.id.glassView2);

// Basic configuration
glassView2.setCornerRadius(40f);
glassView2.setBorder(3f, Color.argb(200, 255, 255, 255));
glassView2.setOverlay(Color.argb(255, 70, 130, 200), 0.15f);
glassView2.setBlurRadius(25f);

// 3D effects
glassView2.setEdgeHighlightIntensity(0.8f);  // Bright edges on top/left
glassView2.setEdgeShadowIntensity(0.6f);     // Dark edges on bottom/right
glassView2.setInnerGlowIntensity(0.4f);      // Soft inner fog
glassView2.setDepthFactor(1.2f);              // Overall depth

glassView2.refresh();
```

🪟 Live Glass View (GlassView) - Real-time background blur

GlassView captures and blurs the content behind it in real-time.

XML Layout

```xml
<com.effects.shadowblur.GlassView
    android:id="@+id/glassView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Live Glass"
        android:textColor="#FFFFFF"/>
</com.effects.shadowblur.GlassView>
```

Java Code

```java
GlassView glassView = findViewById(R.id.glassView);

BlurConfig config = new BlurConfig()
        .setBlurRadius(20f)
        .setOverlayColor(Color.WHITE)
        .setOverlayAlpha(0.2f);

shadowBlur.applyGlassEffect(glassView, config);
shadowBlur.setGlassCornerRadius(glassView, 30f);
shadowBlur.setGlassBorder(glassView, 2f, Color.WHITE);
```

🌑 Shadow Effects

```java
// Floating shadow
ShadowConfig shadow = new ShadowConfig()
        .setShadowColor(Color.argb(100, 0, 0, 0))
        .setShadowRadius(30f)
        .setShadowDx(0f)
        .setShadowDy(15f)
        .setCornerRadius(20f);

shadowBlur.applyShadow(yourButton, shadow);

// Simple shadow
shadowBlur.applyShadow(yourButton, Color.BLUE, 15f, 5f, 5f);
```

🌫️ Blur Effects

```java
// Blur a bitmap
Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.image);
Bitmap blurred = shadowBlur.fastBlur(original, 15);
imageView.setImageBitmap(blurred);
```

📱 Complete Example: Glass Login Screen

```xml
<!-- activity_login.xml -->
<RelativeLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#0A0A0F">

    <!-- Background circles -->
    <ImageView
        android:layout_width="400dp"
        android:layout_height="400dp"
        android:background="@drawable/circle_gradient_blue"/>

    <!-- Glass Card -->
    <com.effects.shadowblur.GlassView2
        android:id="@+id/glassCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:layout_margin="24dp"
        android:padding="24dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Welcome Back!"
                android:textColor="#FFFFFF"
                android:textSize="24sp"/>

            <EditText
                android:id="@+id/emailInput"
                android:layout_width="match_parent"
                android:layout_height="50dp"
                android:hint="Email"
                android:textColorHint="#80FFFFFF"
                android:textColor="#FFFFFF"/>

            <EditText
                android:id="@+id/passwordInput"
                android:layout_width="match_parent"
                android:layout_height="50dp"
                android:hint="Password"
                android:textColorHint="#80FFFFFF"
                android:textColor="#FFFFFF"
                android:inputType="textPassword"/>

            <Button
                android:id="@+id/loginButton"
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:text="LOGIN"
                android:textColor="#FFFFFF"/>
        </LinearLayout>
    </com.effects.shadowblur.GlassView2>
</RelativeLayout>
```

```java
// LoginActivity.java
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ShadowBlur.init(this);
        
        GlassView2 glassCard = findViewById(R.id.glassCard);
        
        // Configure 3D glass
        glassCard.setCornerRadius(50f);
        glassCard.setBorder(3f, Color.argb(220, 255, 255, 255));
        glassCard.setOverlay(Color.argb(255, 70, 130, 200), 0.15f);
        glassCard.setBlurRadius(25f);
        glassCard.setEdgeHighlightIntensity(0.9f);
        glassCard.setEdgeShadowIntensity(0.7f);
        glassCard.setInnerGlowIntensity(0.5f);
        glassCard.setDepthFactor(1.3f);
        
        // Button press effect
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).start();
                    break;
                case MotionEvent.ACTION_UP:
                    v.animate().scaleX(1f).scaleY(1f).start();
                    v.performClick();
                    break;
            }
            return true;
        });
    }
}
```

🎨 Customization Options

GlassView2 Methods (3D Glass)

Method Description
setCornerRadius(float) Rounded corners
setBorder(float, int) Border width and color
setOverlay(int, float) Overlay color and alpha
setBlurRadius(float) Blur intensity
setEdgeHighlightIntensity(float) Top/left edge brightness (0-1)
setEdgeShadowIntensity(float) Bottom/right edge darkness (0-1)
setInnerGlowIntensity(float) Inner fog effect (0-1)
setDepthFactor(float) Overall 3D depth (0.5-2.0)
refresh() Force redraw

GlassView Methods (Live Background)

Method Description
setBlurConfig(BlurConfig) Configure blur and overlay
setCornerRadius(float) Rounded corners
setBorder(float, int) Border width and color
refresh() Recapture background

ShadowConfig Methods

Method Description
setShadowColor(int) Shadow color
setShadowRadius(float) Blur radius of shadow
setShadowDx(float) Horizontal offset
setShadowDy(float) Vertical offset
setCornerRadius(float) Rounded corners
setShape(ShadowShape) RECTANGLE, CIRCLE, or OVAL

📋 Requirements

· Minimum SDK: API 21 (Android 5.0)
· Target SDK: API 34 (Android 14)
· AndroidX

📦 Latest Version

https://img.shields.io/github/v/release/AdamNub/ShadowBlur_Android
https://jitpack.io/v/AdamNub/ShadowBlur_Android.svg

🤝 Contributing

Contributions are welcome! Feel free to:

· 🐛 Report bugs
· 💡 Suggest features
· 🔧 Submit pull requests

📄 License

```
MIT License

Copyright (c) 2026 AdamNub

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

⭐ Show Your Support

If you like this library, please:

· ⭐ Star this repository
· 🔄 Share with other developers
· 🐦 Follow on GitHub

📞 Contact

· GitHub: @AdamNub
· Project Link: https://github.com/AdamNub/ShadowBlur_Android

---

<div align="center">
  <b>Made with ❤️ for Android developers</b>
</div>
```

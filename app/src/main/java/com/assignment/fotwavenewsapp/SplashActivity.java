package com.assignment.fotwavenewsapp;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity{

    private static final int SPLASH_DELAY = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoImage);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        // Additional animations for other views
        animateAppName();
        animateSubtitle();
        animateLoadingIndicator();
        animateBackground();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DELAY);
    }

    private void animateAppName() {
        TextView appName = findViewById(R.id.appName);
        if (appName != null) {
            Animation slideInFromTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
            new Handler().postDelayed(() -> {
                appName.setVisibility(android.view.View.VISIBLE);
                appName.startAnimation(slideInFromTop);
            }, 800);
        }
    }

    private void animateSubtitle() {
        TextView subtitle = findViewById(R.id.subtitle);
        if (subtitle != null) {
            Animation slideInFromBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            new Handler().postDelayed(() -> {
                subtitle.setVisibility(android.view.View.VISIBLE);
                subtitle.startAnimation(slideInFromBottom);
            }, 1200);
        }
    }

    private void animateLoadingIndicator() {
        android.widget.LinearLayout loadingContainer = findViewById(R.id.loadingContainer);
        ImageView loadingDots = findViewById(R.id.loadingDots);
        if (loadingContainer != null && loadingDots != null) {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            new Handler().postDelayed(() -> {
                loadingContainer.setVisibility(android.view.View.VISIBLE);
                loadingDots.startAnimation(pulse);
            }, 1800);
        }
    }

    private void animateBackground() {
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        if (backgroundImage != null) {
            Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
            backgroundImage.startAnimation(zoomIn);
        }
    }
}
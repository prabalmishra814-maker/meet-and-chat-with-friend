package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View mainView = findViewById(R.id.main);
        View logo = findViewById(R.id.imageView3);
        View appName = findViewById(R.id.textViewAppName);
        View slogan = findViewById(R.id.textViewSlogan); // Need to add ID in XML

        // Initial state for animation
        if (logo != null) {
            logo.setAlpha(0f);
            logo.setScaleX(0.7f);
            logo.setScaleY(0.7f);
        }
        if (appName != null) {
            appName.setAlpha(0f);
            appName.setTranslationY(30f);
        }
        if (slogan != null) {
            slogan.setAlpha(0f);
            slogan.setTranslationY(20f);
        }

        // Animate Logo
        if (logo != null) {
            logo.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1200)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .start();
        }

        // Animate App Name
        if (appName != null) {
            appName.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(500)
                    .start();
        }

        // Animate Slogan
        if (slogan != null) {
            slogan.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(800)
                    .start();
        }

        // Delay for 2.5 seconds then navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("user_name", "new");
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}

package com.roomchatapps.Pmishra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class SeatAnimationManager {

    private static final String TAG_PULSE = "PULSE_ANIMATOR";

    /**
     * Seat entrance animation:
     * - Initially scale 0.75, Alpha 0
     * - Smoothly scale to 1.0, Alpha 1
     * - Slight overshoot/bounce effect (~350–450ms)
     * - Host receives a slightly different premium entrance
     */
    public static void animateSeatEntrance(View seatView, boolean isHost) {
        if (seatView == null) return;

        seatView.setScaleX(0.75f);
        seatView.setScaleY(0.75f);
        seatView.setAlpha(0f);
        seatView.setVisibility(View.VISIBLE);

        float overshootTension = isHost ? 1.6f : 1.25f;
        long duration = isHost ? 450L : 380L;

        seatView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(overshootTension))
                .withEndAction(() -> {
                    // Reset to normal static state after entrance completes
                    seatView.setScaleX(1.0f);
                    seatView.setScaleY(1.0f);
                    seatView.setAlpha(1.0f);
                })
                .start();

        if (isHost) {
            // Subtle premium bounce on host frame
            seatView.postDelayed(() -> {
                if (seatView.isAttachedToWindow()) {
                    seatView.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(150)
                            .withEndAction(() -> seatView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start())
                            .start();
                }
            }, duration);
        }
    }

    /**
     * User leave seat animation:
     * - Smoothly scale down (1.0 -> 0.75) + fade out (1 -> 0)
     */
    public static void animateSeatLeave(View seatView, @Nullable Runnable onComplete) {
        if (seatView == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        stopPulsingRing(seatView);

        seatView.animate()
                .scaleX(0.75f)
                .scaleY(0.75f)
                .alpha(0f)
                .setDuration(320)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        seatView.setVisibility(View.GONE);
                        seatView.setScaleX(1.0f);
                        seatView.setScaleY(1.0f);
                        seatView.setAlpha(1.0f);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                })
                .start();
    }

    /**
     * Mic ON/OFF animation:
     * - Mic icon smoothly scales 1.0 -> 1.15 -> 1.0
     */
    public static void animateMicStateChange(ImageView ivMic, boolean isOn) {
        if (ivMic == null) return;

        ivMic.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(160)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> ivMic.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(160)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    /**
     * Pulsing ring animation for speaking / active state.
     * Lightweight CPU/GPU footprint using ValueAnimator.
     */
    public static void startPulsingRing(View ringView) {
        if (ringView == null) return;

        stopPulsingRing(ringView); // Clear existing before starting new one

        ringView.setVisibility(View.VISIBLE);
        
        ObjectAnimator pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                ringView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.12f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.12f, 1.0f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.4f, 0.9f, 0.4f)
        );

        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        pulseAnimator.setInterpolator(new DecelerateInterpolator());

        ringView.setTag(pulseAnimator);
        pulseAnimator.start();
    }

    /**
     * Stops pulsing ring animation and resets view state.
     */
    public static void stopPulsingRing(View ringView) {
        if (ringView == null) return;

        Object tag = ringView.getTag();
        if (tag instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) tag;
            animator.cancel();
            ringView.setTag(null);
        }

        ringView.animate().cancel();
        ringView.setScaleX(1.0f);
        ringView.setScaleY(1.0f);
        ringView.setAlpha(1.0f);
        ringView.setVisibility(View.GONE);
    }
}

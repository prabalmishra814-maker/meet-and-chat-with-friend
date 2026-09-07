package com.roomchatapps.Pmishra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class AnimationHelper {

    public static void fadeIn(View view, long duration) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void slideUp(View view, long duration) {
        if (view == null) return;
        view.setTranslationY(100f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void scaleIn(View view, long duration) {
        if (view == null) return;
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    public static void applyStaggeredAnimation(ViewGroup parent) {
        if (parent == null) return;
        AnimationSet set = new AnimationSet(true);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(400);
        set.addAnimation(fadeIn);

        Animation slideUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideUp.setDuration(400);
        set.addAnimation(slideUp);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.15f);
        parent.setLayoutAnimation(controller);
    }

    public static void applyClickAnimation(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                    v.performClick();
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    public static void popIn(View view) {
        if (view == null) return;
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();
    }

    public static void bounceAnimation(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    public static void animateFollowButton(View btnFollow, Runnable onStateToggle) {
        if (btnFollow == null) return;
        btnFollow.setEnabled(false);
        btnFollow.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(120)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (onStateToggle != null) {
                        onStateToggle.run();
                    }
                    btnFollow.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(180)
                            .setInterpolator(new OvershootInterpolator(1.5f))
                            .withEndAction(() -> btnFollow.setEnabled(true))
                            .start();
                })
                .start();
    }

    public static void animateCoinUpdate(View coinIcon, TextView tvCoins, String newValue) {
        if (coinIcon != null) {
            bounceAnimation(coinIcon);
        }
        if (tvCoins != null) {
            tvCoins.animate()
                    .scaleX(1.25f)
                    .scaleY(1.25f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        tvCoins.setText(newValue);
                        tvCoins.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(150)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                    })
                    .start();
        }
    }

    public static void animateDialogEntry(View dialogView) {
        if (dialogView == null) return;
        dialogView.setAlpha(0f);
        dialogView.setScaleX(0.85f);
        dialogView.setScaleY(0.85f);
        dialogView.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    public static void pulseGlowAnimation(View view) {
        if (view == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.04f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.04f, 1.0f);
        scaleX.setDuration(1600);
        scaleY.setDuration(1600);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    public static void animateNumberCounter(TextView textView, long startVal, long endVal) {
        if (textView == null) return;
        ValueAnimator animator = ValueAnimator.ofInt((int) startVal, (int) endVal);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(val));
        });
        animator.start();
    }

    public static void animateDialogExit(View dialogView, Runnable onEnd) {
        if (dialogView == null) {
            if (onEnd != null) onEnd.run();
            return;
        }
        dialogView.animate()
                .alpha(0f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onEnd != null) onEnd.run();
                    }
                })
                .start();
    }
}

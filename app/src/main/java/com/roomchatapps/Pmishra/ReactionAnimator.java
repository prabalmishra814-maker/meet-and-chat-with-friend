package com.roomchatapps.Pmishra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.util.Random;

public class ReactionAnimator {

    private static final Random random = new Random();

    /**
     * Animate reaction / like button click:
     * - Scale down (0.82) then scale up with bounce (1.25 -> 1.0)
     */
    public static void animateLikeButtonClick(View button, View heartIcon) {
        if (button == null) return;

        View target = heartIcon != null ? heartIcon : button;

        target.animate()
                .scaleX(0.82f)
                .scaleY(0.82f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> target.animate()
                        .scaleX(1.25f)
                        .scaleY(1.25f)
                        .setDuration(160)
                        .setInterpolator(new OvershootInterpolator(2.0f))
                        .withEndAction(() -> target.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(120)
                                .start())
                        .start())
                .start();
    }

    /**
     * Spawns a floating reaction heart starting EXACTLY at the anchor view's (Heart Icon) location,
     * then spreading/scattering across the entire screen.
     */
    public static void spawnFloatingReaction(ViewGroup container, View anchorView, int iconRes) {
        if (container == null) return;
        float startX = 0f;
        float startY = 0f;

        if (anchorView != null) {
            int[] location = new int[2];
            anchorView.getLocationInWindow(location);
            int[] containerLoc = new int[2];
            container.getLocationInWindow(containerLoc);

            startX = location[0] - containerLoc[0] + (anchorView.getWidth() / 2f);
            startY = location[1] - containerLoc[1] + (anchorView.getHeight() / 2f);
        }

        spawnFloatingReactionAt(container, startX, startY, iconRes);
    }

    /**
     * Spawns a floating reaction heart starting EXACTLY at the specified Heart Icon coordinates,
     * then spreading/scattering across the ENTIRE width and height of the screen.
     */
    public static void spawnFloatingReactionAt(ViewGroup container, float startX, float startY, int iconRes) {
        if (container == null) return;

        Context context = container.getContext();
        ImageView floatingIcon = new ImageView(context);

        // Ensure only pure heart drawables are used (exclude gift drawables)
        int validIcon = (iconRes != 0 && iconRes != R.drawable._1000092341_removebg_preview)
                ? iconRes : R.drawable._1000092377_removebg_preview;
        floatingIcon.setImageResource(validIcon);

        // Varied heart size (38dp to 56dp) for rich screen depth
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38 + random.nextInt(18), context.getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        floatingIcon.setLayoutParams(params);

        // Fallback to bottom-right menu area if coordinates are uninitialized
        if (startX <= 0 || startY <= 0) {
            startX = container.getWidth() - dpToPx(context, 80);
            startY = container.getHeight() - dpToPx(context, 80);
        }

        floatingIcon.setX(startX - (size / 2f));
        floatingIcon.setY(startY - (size / 2f));
        floatingIcon.setAlpha(1.0f);
        floatingIcon.setScaleX(0.3f);
        floatingIcon.setScaleY(0.3f);

        container.addView(floatingIcon);

        // Trigger party confetti sparkles burst directly at the Heart Icon launch point
        spawnPartyConfettiBurst(container, startX, startY);

        // Full-Screen Scatter Trajectory:
        // Spawns AT the heart icon, then spreads across the FULL screen width (0% to 100%) as it rises
        int containerWidth = Math.max(container.getWidth(), (int) dpToPx(context, 360));
        float minX = dpToPx(context, 20);
        float maxX = containerWidth - dpToPx(context, 50);

        // Random target destination anywhere across full screen width
        float targetX = minX + random.nextInt((int) Math.max(1, maxX - minX));
        float midX = (startX + targetX) / 2f + (random.nextInt((int) dpToPx(context, 120)) - dpToPx(context, 60));
        float targetY = -dpToPx(context, 80); // Floats all the way off top of screen

        // Curved S-curve flight scattering across the entire screen
        ObjectAnimator animX = ObjectAnimator.ofFloat(floatingIcon, View.X, startX, midX, targetX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(floatingIcon, View.Y, startY, targetY);
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(floatingIcon, View.SCALE_X, 0.3f, 1.50f, 1.0f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(floatingIcon, View.SCALE_Y, 0.3f, 1.50f, 1.0f);
        ObjectAnimator animRot = ObjectAnimator.ofFloat(floatingIcon, View.ROTATION, 0f, (random.nextBoolean() ? 45f : -45f), (random.nextBoolean() ? -35f : 35f));
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(floatingIcon, View.ALPHA, 1.0f, 1.0f, 0.85f, 0.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY, animScaleX, animScaleY, animRot, animAlpha);
        set.setDuration(2400 + random.nextInt(800)); // 2.4s to 3.2s full screen scattering flight
        set.setInterpolator(new DecelerateInterpolator(1.2f));
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (container != null) {
                    container.removeView(floatingIcon);
                }
            }
        });
        set.start();
    }

    /**
     * Spawns a burst of sparkling party confetti particles.
     */
    public static void spawnPartyConfettiBurst(ViewGroup container, float startX, float startY) {
        if (container == null) return;
        Context context = container.getContext();

        for (int i = 0; i < 6; i++) {
            View star = new View(context);
            int size = (int) dpToPx(context, 8 + random.nextInt(12));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
            star.setLayoutParams(params);

            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            String[] colors = {"#FFD700", "#FF1493", "#00FFFF", "#40E0D0", "#FF4500", "#9D4EDD"};
            d.setColor(Color.parseColor(colors[random.nextInt(colors.length)]));
            star.setBackground(d);

            star.setX(startX);
            star.setY(startY);
            container.addView(star);

            float burstX = startX + (random.nextInt(160) - 80);
            float burstY = startY - (60 + random.nextInt(120));

            ObjectAnimator ax = ObjectAnimator.ofFloat(star, View.X, startX, burstX);
            ObjectAnimator ay = ObjectAnimator.ofFloat(star, View.Y, startY, burstY);
            ObjectAnimator aa = ObjectAnimator.ofFloat(star, View.ALPHA, 1f, 0f);
            ObjectAnimator as = ObjectAnimator.ofFloat(star, View.SCALE_X, 0.4f, 1.4f, 0f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(ax, ay, aa, as);
            set.setDuration(800 + random.nextInt(400));
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    container.removeView(star);
                }
            });
            set.start();
        }
    }

    private static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}

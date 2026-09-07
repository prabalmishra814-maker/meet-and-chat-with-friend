package com.roomchatapps.Pmishra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;

public class NotificationAnimator {

    public interface OnBannerClickListener {
        void onBannerClick(NotificationItem item);
    }

    private final ViewGroup containerView;
    private final Queue<NotificationItem> notificationQueue = new LinkedList<>();
    private boolean isShowing = false;
    private OnBannerClickListener bannerClickListener;

    public static class NotificationItem {
        public String title;
        public String message;
        public int iconRes;
        public String actionTarget;

        public NotificationItem(String title, String message, int iconRes) {
            this(title, message, iconRes, null);
        }

        public NotificationItem(String title, String message, int iconRes, String actionTarget) {
            this.title = title;
            this.message = message;
            this.iconRes = iconRes;
            this.actionTarget = actionTarget;
        }

        public NotificationItem(String message) {
            this("Notice", message, 0);
        }
    }

    public NotificationAnimator(ViewGroup containerView) {
        this.containerView = containerView;
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.bannerClickListener = listener;
    }

    public void showNotification(String message) {
        showNotification("Notice", message, 0);
    }

    public void showNotification(String title, String message, int iconRes) {
        showNotification(title, message, iconRes, null);
    }

    public void showNotification(String title, String message, int iconRes, String actionTarget) {
        if (containerView == null) return;
        notificationQueue.add(new NotificationItem(title, message, iconRes, actionTarget));
        if (!isShowing) {
            processNextNotification();
        }
    }

    private void processNextNotification() {
        if (notificationQueue.isEmpty()) {
            isShowing = false;
            return;
        }

        isShowing = true;
        NotificationItem item = notificationQueue.poll();
        if (item == null) {
            isShowing = false;
            return;
        }

        Context context = containerView.getContext();

        // Build glassmorphic banner layout
        LinearLayout banner = new LinearLayout(context);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackgroundResource(R.drawable.bg_card); // Uses modern glass card background
        banner.setPadding(dpToPx(context, 14), dpToPx(context, 10), dpToPx(context, 16), dpToPx(context, 10));

        if (item.iconRes != 0) {
            ImageView icon = new ImageView(context);
            icon.setImageResource(item.iconRes);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(context, 28), dpToPx(context, 28));
            iconParams.setMarginEnd(dpToPx(context, 10));
            banner.addView(icon, iconParams);

            // Icon 360 Spin + Scale bounce animation
            icon.setRotation(-360f);
            icon.setScaleX(0.4f);
            icon.setScaleY(0.4f);
            icon.animate()
                    .rotation(0f)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(500)
                    .setInterpolator(new OvershootInterpolator(2.0f))
                    .withEndAction(() -> icon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start())
                    .start();
        }

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        if (item.title != null && !item.title.isEmpty() && !item.title.equals("Notice")) {
            TextView tvTitle = new TextView(context);
            tvTitle.setText(item.title);

            // Deterministic vibrant user accent color for different users
            String[] accentColors = {"#FF1493", "#40E0D0", "#FFD700", "#9D4EDD", "#00FF7F", "#FF4500", "#00FFFF"};
            int colorIndex = Math.abs(item.title.hashCode()) % accentColors.length;
            tvTitle.setTextColor(Color.parseColor(accentColors[colorIndex]));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            textLayout.addView(tvTitle);
        }

        TextView tvMsg = new TextView(context);
        tvMsg.setText(item.message);
        tvMsg.setTextColor(Color.WHITE);
        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvMsg.setSingleLine(true);
        tvMsg.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textLayout.addView(tvMsg);

        banner.addView(textLayout);

        // Position on the LEFT side of the screen
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.topMargin = dpToPx(context, 110);
        params.leftMargin = dpToPx(context, 12);

        containerView.addView(banner, params);

        // Click handler
        banner.setOnClickListener(v -> {
            if (bannerClickListener != null) {
                bannerClickListener.onBannerClick(item);
            }
        });

        // Touch gesture
        banner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
            }
            return false;
        });

        // Initial off-screen state (Off to the LEFT with initial tilt)
        banner.setAlpha(0f);
        banner.setTranslationX(-dpToPx(context, 320));
        banner.setScaleX(0.7f);
        banner.setScaleY(0.7f);
        banner.setRotation(-12f);

        // Spin + Slide In from LEFT + Scale Spring Bounce
        banner.animate()
                .alpha(1f)
                .translationX(0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .rotation(0f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.3f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Display duration: 2.5 seconds
                        banner.postDelayed(() -> dismissBanner(banner), 2500);
                    }
                }).start();
    }

    /**
     * Dismisses the banner by sliding out towards the RIGHT side.
     */
    private void dismissBanner(View banner) {
        if (banner == null || banner.getParent() == null) return;
        banner.animate()
                .alpha(0f)
                .translationX(dpToPx(banner.getContext(), 350)) // Slide out towards the RIGHT!
                .scaleX(0.85f)
                .scaleY(0.85f)
                .rotation(8f)
                .setDuration(380)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (banner.getParent() != null) {
                            containerView.removeView(banner);
                        }
                        processNextNotification();
                    }
                }).start();
    }

    public void clear() {
        notificationQueue.clear();
        isShowing = false;
    }

    private int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}

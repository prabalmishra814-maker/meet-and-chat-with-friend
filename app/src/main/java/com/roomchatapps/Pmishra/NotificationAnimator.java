package com.roomchatapps.Pmishra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.LinkedList;
import java.util.Queue;

import de.hdodenhof.circleimageview.CircleImageView;

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
        public String avatarUrl;

        public NotificationItem(String title, String message, int iconRes) {
            this(title, message, iconRes, null);
        }

        public NotificationItem(String title, String message, int iconRes, String avatarUrl) {
            this.title = title;
            this.message = message;
            this.iconRes = iconRes;
            this.avatarUrl = avatarUrl;
        }

        public NotificationItem(String message) {
            this("Notice", message, 0, null);
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

    public void showNotification(String title, String message, int iconRes, String avatarUrl) {
        // Left-side notification disabled per user request
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

        // Build Red Pill Banner Layout matching user's design
        LinearLayout banner = new LinearLayout(context);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackgroundResource(R.drawable.bg_notification_red_pill);
        banner.setPadding(dpToPx(context, 8), dpToPx(context, 6), dpToPx(context, 18), dpToPx(context, 6));

        // Circular Avatar / Icon on Left with Gold Border
        CircleImageView ivAvatar = new CircleImageView(context);
        ivAvatar.setBorderWidth(dpToPx(context, 1.5f));
        ivAvatar.setBorderColor(Color.parseColor("#FFD700"));

        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dpToPx(context, 38), dpToPx(context, 38));
        avatarParams.setMarginEnd(dpToPx(context, 10));

        if (item.avatarUrl != null && !item.avatarUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(item.avatarUrl)
                    .placeholder(R.drawable.logo_placeholder)
                    .into(ivAvatar);
        } else if (item.iconRes != 0) {
            ivAvatar.setImageResource(item.iconRes);
        } else {
            ivAvatar.setImageResource(R.drawable.logo_placeholder);
        }
        banner.addView(ivAvatar, avatarParams);

        // Text Layout (Name on top line, Action/Notice on bottom line)
        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        // Top Line: User Name in Gold
        TextView tvTitle = new TextView(context);
        tvTitle.setText(item.title != null ? item.title : "User");
        tvTitle.setTextColor(Color.parseColor("#FFD700"));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setSingleLine(true);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        textLayout.addView(tvTitle);

        // Bottom Line: Message Notice in White
        TextView tvMsg = new TextView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvMsg.setText(Html.fromHtml(item.message, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMsg.setText(Html.fromHtml(item.message));
        }
        tvMsg.setTextColor(Color.WHITE);
        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        tvMsg.setSingleLine(true);
        tvMsg.setEllipsize(TextUtils.TruncateAt.END);
        textLayout.addView(tvMsg);

        banner.addView(textLayout);

        // Position on the LEFT side of the screen
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.topMargin = dpToPx(context, 95);
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

        // Initial off-screen state (Off to the LEFT)
        banner.setAlpha(0f);
        banner.setTranslationX(-dpToPx(context, 320));
        banner.setScaleX(0.7f);
        banner.setScaleY(0.7f);

        // Slide In from LEFT + Scale Spring Bounce
        banner.animate()
                .alpha(1f)
                .translationX(0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Display duration: 3.0 seconds
                        banner.postDelayed(() -> dismissBanner(banner), 3000);
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

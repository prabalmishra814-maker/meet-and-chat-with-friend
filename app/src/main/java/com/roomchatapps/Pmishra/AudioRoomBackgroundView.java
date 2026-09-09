package com.roomchatapps.Pmishra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.Random;

public class AudioRoomBackgroundView extends FrameLayout {

    private static final String TAG = "AudioRoomBackgroundView";

    private TextView roomName;
    private TextView roomID;
    private ImageView backgroundImageView;
    private TextureView backgroundVideoView;
    private MediaPlayer mediaPlayer;

    private View overlayView;
    private FrameLayout bubbleContainer;
    private OnGameIconClickListener gameIconClickListener;
    private LinearLayout messageArea;
    private TextView tvWelcome;
    private boolean isAnimatingWelcome = false;

    public interface OnGameIconClickListener {
        void onGameIconClick();
    }

    private final Handler bubbleHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean isBubbleLoopRunning = false;

    private final Runnable bubbleRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBubbleLoopRunning) return;
            spawnAmbientBubble();
            spawnAmbientBubble();
            if (random.nextBoolean()) {
                spawnAmbientParticle();
            }
            bubbleHandler.postDelayed(this, 200 + random.nextInt(250));
        }
    };

    public AudioRoomBackgroundView(@NonNull Context context) {
        super(context);
        initView();
    }

    public AudioRoomBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AudioRoomBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        initBackgroundVideo();

        backgroundImageView = new ImageView(getContext());
        backgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundImageView.setImageResource(R.drawable.bg_room_gradient);
        addView(backgroundImageView, new LayoutParams(-1, -1));

        overlayView = new View(getContext());
        GradientDrawable overlayGradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#70000000"), Color.parseColor("#30000000"), Color.parseColor("#800A0F1D")}
        );
        overlayView.setBackground(overlayGradient);
        addView(overlayView, new LayoutParams(-1, -1));

        bubbleContainer = new FrameLayout(getContext());
        addView(bubbleContainer, new LayoutParams(-1, -1));

        messageArea = new LinearLayout(getContext());
        messageArea.setBackgroundResource(R.drawable.bg_yellow_welcome_box);
        messageArea.setOrientation(LinearLayout.VERTICAL);
        messageArea.setPadding(dp2px(16), dp2px(10), dp2px(16), dp2px(10));
        messageArea.setVisibility(View.GONE);

        tvWelcome = new TextView(getContext());
        tvWelcome.setText("Welcome");
        tvWelcome.setTextColor(Color.parseColor("#FFE45C"));
        tvWelcome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvWelcome.getPaint().setFakeBoldText(true);
        tvWelcome.setGravity(Gravity.CENTER);
        messageArea.addView(tvWelcome);

        LayoutParams msgParams = new LayoutParams(dp2px(180), LayoutParams.WRAP_CONTENT);
        msgParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        msgParams.setMargins(0, 0, 0, dp2px(220));
        addView(messageArea, msgParams);

        View seatSpotlightAura = new View(getContext());
        GradientDrawable spotDrawable = new GradientDrawable();
        spotDrawable.setShape(GradientDrawable.OVAL);
        spotDrawable.setColors(new int[]{
                Color.parseColor("#3540E0D0"),
                Color.parseColor("#181E1B4B"),
                Color.parseColor("#00000000")
        });
        seatSpotlightAura.setBackground(spotDrawable);

        LayoutParams spotParams = new LayoutParams(dp2px(330), dp2px(280));
        spotParams.gravity = Gravity.CENTER;
        spotParams.setMargins(0, dp2px(80), 0, 0);
        addView(seatSpotlightAura, spotParams);

        AnimationHelper.pulseGlowAnimation(seatSpotlightAura);
    }

    private void initBackgroundVideo() {
        backgroundVideoView = new TextureView(getContext());
        addView(backgroundVideoView, new LayoutParams(-1, -1));

        backgroundVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                playThemeVideo(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                releaseMediaPlayer();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
        });
    }

    private void playThemeVideo(SurfaceTexture surface) {
        playThemeVideo(surface, "theme/theme1.mp4");
    }

    private void playThemeVideo(SurfaceTexture surface, String assetPath) {
        try {
            releaseMediaPlayer();
            Surface s = new Surface(surface);
            mediaPlayer = new MediaPlayer();

            String path = assetPath != null && !assetPath.isEmpty() ? assetPath : "theme/theme1.mp4";
            AssetFileDescriptor afd = getContext().getAssets().openFd(path);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.setSurface(s);
            mediaPlayer.setLooping(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            }

            mediaPlayer.setVolume(0f, 0f); // Muted for background video so voice audio remains crystal clear

            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    mp.start();
                    if (backgroundImageView != null) {
                        backgroundImageView.animate().alpha(0f).setDuration(500).start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error starting video playback", e);
                }
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error loading background theme video from assets", e);
        }
    }

    public void setThemeVideo(String assetPath) {
        final String videoPath = assetPath != null && !assetPath.isEmpty() ? assetPath : "theme/theme1.mp4";
        if (backgroundVideoView != null) {
            backgroundVideoView.setVisibility(View.VISIBLE);
            if (backgroundVideoView.isAvailable() && backgroundVideoView.getSurfaceTexture() != null) {
                playThemeVideo(backgroundVideoView.getSurfaceTexture(), videoPath);
            } else {
                backgroundVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                        playThemeVideo(surface, videoPath);
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

                    @Override
                    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                        releaseMediaPlayer();
                        return true;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
                });
            }
        }
    }

    public void setThemeImage(int resId) {
        releaseMediaPlayer();
        if (backgroundVideoView != null) {
            backgroundVideoView.setVisibility(View.GONE);
        }
        if (backgroundImageView != null) {
            backgroundImageView.setImageResource(resId);
            backgroundImageView.setAlpha(1f);
        }
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    public void showWelcomeAnimation() {
        if (messageArea == null || isAnimatingWelcome) return;
        isAnimatingWelcome = true;
        
        messageArea.setVisibility(View.VISIBLE);
        messageArea.setAlpha(0f);
        messageArea.setScaleX(0.5f);
        messageArea.setScaleY(0.5f);

        messageArea.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    messageArea.postDelayed(() -> {
                        messageArea.animate()
                                .alpha(0f)
                                .scaleX(0.5f)
                                .scaleY(0.5f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    messageArea.setVisibility(View.GONE);
                                    isAnimatingWelcome = false;
                                })
                                .start();
                    }, 3000);
                })
                .start();
    }

    private void setupPartyEqualizerBars() {
        // Equalizer bars removed as requested
    }

    private void startBubbleAnimation() {
        if (!isBubbleLoopRunning) {
            isBubbleLoopRunning = true;
            bubbleHandler.post(bubbleRunnable);
        }
    }

    private void stopBubbleAnimation() {
        isBubbleLoopRunning = false;
        bubbleHandler.removeCallbacks(bubbleRunnable);
        if (bubbleContainer != null) {
            bubbleContainer.removeAllViews();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startBubbleAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBubbleAnimation();
        releaseMediaPlayer();
    }

    private void spawnAmbientBubble() {
        if (bubbleContainer == null || getWidth() <= 0 || getHeight() <= 0) return;

        View bubble = new View(getContext());
        int size = dp2px(12 + random.nextInt(36));
        LayoutParams params = new LayoutParams(size, size);
        bubble.setLayoutParams(params);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);

        String[] bubbleColors = {
                "#5040E0D0", "#50FFD700", "#50FF1493", "#509D4EDD", "#5000FFFF", "#5000FF7F", "#50FF4500"
        };
        int color = Color.parseColor(bubbleColors[random.nextInt(bubbleColors.length)]);
        circle.setColor(color);
        circle.setStroke(dp2px(1.5f), Color.parseColor("#A0FFFFFF"));
        bubble.setBackground(circle);

        float startX = random.nextInt(Math.max(1, getWidth() - size));
        float startY = getHeight() - dp2px(110);

        bubble.setX(startX);
        bubble.setY(startY);
        bubble.setAlpha(0.0f);

        bubbleContainer.addView(bubble);

        float targetY = -dp2px(60);
        float targetX = startX + (random.nextInt(140) - 70);
        long duration = 2800 + random.nextInt(2400);

        ObjectAnimator animY = ObjectAnimator.ofFloat(bubble, View.Y, startY, targetY);
        ObjectAnimator animX = ObjectAnimator.ofFloat(bubble, View.X, startX, targetX);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(bubble, View.ALPHA, 0f, 0.9f, 0.9f, 0f);
        ObjectAnimator animScale = ObjectAnimator.ofFloat(bubble, View.SCALE_X, 0.5f, 1.35f, 0.6f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animY, animX, animAlpha, animScale);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (bubbleContainer != null) {
                    bubbleContainer.removeView(bubble);
                }
            }
        });
        animatorSet.start();
    }

    private void spawnAmbientParticle() {
        if (bubbleContainer == null || getWidth() <= 0 || getHeight() <= 0) return;

        TextView particle = new TextView(getContext());
        String[] symbols = {"✨", "🎵", "💫", "🌟", "🎶", "⚡"};
        particle.setText(symbols[random.nextInt(symbols.length)]);
        particle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 + random.nextInt(14));

        float startX = random.nextInt(Math.max(1, getWidth() - dp2px(30)));
        float startY = getHeight() - dp2px(110);

        particle.setX(startX);
        particle.setY(startY);
        particle.setAlpha(0f);

        bubbleContainer.addView(particle);

        float targetY = -dp2px(60);
        float targetX = startX + (random.nextInt(160) - 80);
        long duration = 3200 + random.nextInt(2200);

        ObjectAnimator animY = ObjectAnimator.ofFloat(particle, View.Y, startY, targetY);
        ObjectAnimator animX = ObjectAnimator.ofFloat(particle, View.X, startX, targetX);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(particle, View.ALPHA, 0f, 0.9f, 0.9f, 0f);
        ObjectAnimator animRot = ObjectAnimator.ofFloat(particle, View.ROTATION, 0f, (random.nextBoolean() ? 360f : -360f));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animY, animX, animAlpha, animRot);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (bubbleContainer != null) {
                    bubbleContainer.removeView(particle);
                }
            }
        });
        animatorSet.start();
    }

    public void setOnGameIconClickListener(OnGameIconClickListener listener) {
        this.gameIconClickListener = listener;
    }

    private ImageView createHeaderIcon(int resId) {
        ImageView iv = new ImageView(getContext());
        iv.setImageResource(resId);
        iv.setColorFilter(Color.WHITE);
        int size = dp2px(32);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMarginStart(dp2px(12));
        iv.setLayoutParams(params);
        iv.setPadding(dp2px(4), dp2px(4), dp2px(4), dp2px(4));
        iv.setBackgroundResource(R.drawable.bg_control_button);
        return iv;
    }

    private TextView createBadge(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tv.setBackgroundResource(R.drawable.bg_badge_transparent);
        tv.setPadding(dp2px(8), dp2px(2), dp2px(8), dp2px(2));
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMarginEnd(dp2px(8));
        tv.setLayoutParams(params);
        return tv;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setRoomName(String name) {
        if (roomName != null) {
            this.roomName.setText(name != null ? name : "Audio Room");
        }
        if (getRootView() != null) {
            TextView tv = getRootView().findViewById(R.id.tvRoomName);
            if (tv != null && name != null && !name.isEmpty()) {
                tv.setText(name);
            }
        }
    }

    public void setRoomID(String id) {
        if (roomID != null) {
            String displayId = id != null ? id : "N/A";
            this.roomID.setText("ID: " + displayId + "  👥 1/16");
        }
        if (getRootView() != null) {
            TextView tv = getRootView().findViewById(R.id.tvRoomId);
            if (tv != null && id != null && !id.isEmpty()) {
                tv.setText("ID:" + id);
            }
        }
    }

    public void setUserCount(int count) {
        if (roomID != null) {
            String currentText = roomID.getText().toString();
            if (currentText.contains("ID:")) {
                String idPart = currentText.split("👥")[0].trim();
                roomID.setText(idPart + "  👥 " + count + "/16");
            }
        }
    }

    public void setBackgroundImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_room_gradient)
                    .error(R.drawable.bg_room_gradient)
                    .into(backgroundImageView);
        } else {
            backgroundImageView.setImageResource(R.drawable.bg_room_gradient);
        }
    }

    public void setBackgroundImage(int resId) {
        backgroundImageView.setImageResource(resId);
    }
}

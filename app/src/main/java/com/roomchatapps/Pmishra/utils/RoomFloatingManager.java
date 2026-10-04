package com.roomchatapps.Pmishra.utils;

// ROOM MINIMIZE FIX
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.RoomChatActivity;

import java.lang.ref.WeakReference;

public class RoomFloatingManager {

    private static volatile RoomFloatingManager instance;
    private View floatingView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private boolean isBubbleShowing = false;
    private String currentRoomId;
    private String currentRoomName;
    private String currentRoomImg;

    private WeakReference<Activity> currentActivityRef;
    private Application.ActivityLifecycleCallbacks lifecycleCallbacks;
    private boolean isLifecycleRegistered = false;

    private RoomFloatingManager() {}

    public static RoomFloatingManager getInstance() {
        if (instance == null) {
            synchronized (RoomFloatingManager.class) {
                if (instance == null) {
                    instance = new RoomFloatingManager();
                }
            }
        }
        return instance;
    }

    public boolean isShowing() {
        return isBubbleShowing;
    }

    public void init(Application application) {
        if (isLifecycleRegistered || application == null) return;
        lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (!(activity instanceof RoomChatActivity)) {
                    currentActivityRef = new WeakReference<>(activity);
                    if (isBubbleShowing && windowManager == null) {
                        attachToActivityDecorView(activity);
                    }
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    currentActivityRef = null;
                }
            }
        };
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        isLifecycleRegistered = true;
    }

    public void showFloatingBubble(Context context, String roomId, String roomName) {
        showFloatingBubble(context, roomId, roomName, null);
    }

    public synchronized void showFloatingBubble(Context context, String roomId, String roomName, String roomImg) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        if (appContext instanceof Application) {
            init((Application) appContext);
        }

        this.currentRoomId = roomId;
        this.currentRoomName = roomName;
        this.currentRoomImg = roomImg;

        removeFloatingBubble();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(appContext)) {
            showSystemWindowBubble(appContext);
        } else {
            Activity activity = (context instanceof Activity) ? (Activity) context : (currentActivityRef != null ? currentActivityRef.get() : null);
            if (activity != null && !(activity instanceof RoomChatActivity)) {
                attachToActivityDecorView(activity);
            } else if (currentActivityRef != null && currentActivityRef.get() != null) {
                attachToActivityDecorView(currentActivityRef.get());
            } else {
                showSystemWindowBubble(appContext);
            }
        }
        isBubbleShowing = true;
    }

    private View createBubbleView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_floating_room_bubble, null, false);

        ImageView imgLogo = view.findViewById(R.id.imgFloatingLogo);
        if (imgLogo != null) {
            if (currentRoomImg != null && !currentRoomImg.trim().isEmpty()) {
                try {
                    Glide.with(context.getApplicationContext())
                            .load(currentRoomImg)
                            .placeholder(R.drawable.img_20260904_135725)
                            .error(R.drawable.img_20260904_135725)
                            .into(imgLogo);
                } catch (Exception e) {
                    imgLogo.setImageResource(R.drawable.img_20260904_135725);
                }
            } else {
                imgLogo.setImageResource(R.drawable.img_20260904_135725);
            }
        }

        setupDragAndClick(view, context);
        return view;
    }

    private void setupDragAndClick(View view, Context context) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private static final int CLICK_ACTION_THRESHOLD = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (windowParams != null) {
                            initialX = windowParams.x;
                            initialY = windowParams.y;
                        } else if (v.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                            initialX = params.leftMargin;
                            initialY = params.topMargin;
                        }
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - initialTouchX;
                        float deltaY = event.getRawY() - initialTouchY;

                        if (windowParams != null && windowManager != null && floatingView != null) {
                            windowParams.x = initialX + (int) deltaX;
                            windowParams.y = initialY + (int) deltaY;
                            try {
                                windowManager.updateViewLayout(floatingView, windowParams);
                            } catch (Exception ignored) {}
                        } else if (v.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                            params.leftMargin = Math.max(0, initialX + (int) deltaX);
                            params.topMargin = Math.max(0, initialY + (int) deltaY);
                            v.setLayoutParams(params);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        float diffX = Math.abs(event.getRawX() - initialTouchX);
                        float diffY = Math.abs(event.getRawY() - initialTouchY);
                        if (diffX < CLICK_ACTION_THRESHOLD && diffY < CLICK_ACTION_THRESHOLD) {
                            v.performClick();
                            restoreRoom(context);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void showSystemWindowBubble(Context context) {
        try {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            floatingView = createBubbleView(context);

            int layoutType;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutType = WindowManager.LayoutParams.TYPE_PHONE;
            }

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int bubbleSizePx = (int) (64 * metrics.density);

            windowParams = new WindowManager.LayoutParams(
                    bubbleSizePx,
                    bubbleSizePx,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );

            windowParams.gravity = Gravity.TOP | Gravity.START;
            windowParams.x = metrics.widthPixels - bubbleSizePx - (int) (16 * metrics.density);
            windowParams.y = metrics.heightPixels / 3;

            windowManager.addView(floatingView, windowParams);
        } catch (Exception e) {
            Log.e("RoomFloatingManager", "Error showing system window bubble, falling back to decor view", e);
            windowManager = null;
            if (currentActivityRef != null && currentActivityRef.get() != null) {
                attachToActivityDecorView(currentActivityRef.get());
            }
        }
    }

    private void attachToActivityDecorView(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        try {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            if (floatingView != null && floatingView.getParent() != null) {
                ((ViewGroup) floatingView.getParent()).removeView(floatingView);
            }

            floatingView = createBubbleView(activity);
            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            int bubbleSizePx = (int) (64 * metrics.density);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bubbleSizePx, bubbleSizePx);
            params.gravity = Gravity.TOP | Gravity.START;
            params.leftMargin = metrics.widthPixels - bubbleSizePx - (int) (16 * metrics.density);
            params.topMargin = metrics.heightPixels / 3;

            decorView.addView(floatingView, params);
        } catch (Exception e) {
            Log.e("RoomFloatingManager", "Error attaching bubble to decor view", e);
        }
    }

    public synchronized void restoreRoom(Context context) {
        if (!isBubbleShowing) return;
        removeFloatingBubble();
        if (context == null) return;
        try {
            Intent intent = new Intent(context, RoomChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (currentRoomId != null) intent.putExtra("roomID", currentRoomId);
            if (currentRoomName != null) intent.putExtra("room_name", currentRoomName);
            if (currentRoomImg != null) intent.putExtra("img", currentRoomImg);
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.zoom_in, R.anim.fade_out);
            } else if (currentActivityRef != null && currentActivityRef.get() != null) {
                currentActivityRef.get().overridePendingTransition(R.anim.zoom_in, R.anim.fade_out);
            }
        } catch (Exception e) {
            Log.e("RoomFloatingManager", "Error restoring RoomChatActivity", e);
        }
    }

    public synchronized void clearRoomState() {
        currentRoomId = null;
        currentRoomName = null;
        currentRoomImg = null;
        removeFloatingBubble();
    }

    public synchronized void removeFloatingBubble() {
        isBubbleShowing = false;
        try {
            if (floatingView != null) {
                if (windowManager != null) {
                    try {
                        windowManager.removeView(floatingView);
                    } catch (Exception ignored) {}
                    windowManager = null;
                }
                if (floatingView.getParent() != null) {
                    ((ViewGroup) floatingView.getParent()).removeView(floatingView);
                }
                floatingView = null;
            }
        } catch (Exception e) {
            Log.e("RoomFloatingManager", "Error removing floating bubble", e);
        }
    }
}

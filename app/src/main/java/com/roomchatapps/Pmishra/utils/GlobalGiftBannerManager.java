package com.roomchatapps.Pmishra.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

public class GlobalGiftBannerManager {

    private static GlobalGiftBannerManager instance;
    private static final String SVGA_ASSET_PATH = "Notification/rednotification.svga";

    public static class GiftBannerPayload {
        public String id;
        public String senderName;
        public String recipientName;
        public String giftName;
        public int quantity;
        public String roomName;
        public long timestamp;

        public GiftBannerPayload() {}

        public GiftBannerPayload(String senderName, String recipientName, String giftName, int quantity, String roomName) {
            this.senderName = senderName;
            this.recipientName = recipientName;
            this.giftName = giftName;
            this.quantity = Math.max(1, quantity);
            this.roomName = roomName;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Queue<GiftBannerPayload> bannerQueue = new LinkedList<>();
    private boolean isPlaying = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    private Activity activeActivity;
    private View activeContainer;
    private SVGAImageView activePlayer;
    private TextView activeTvNotice;

    private DatabaseReference broadcastRef;
    private ChildEventListener childEventListener;

    private GlobalGiftBannerManager() {}

    public static synchronized GlobalGiftBannerManager getInstance() {
        if (instance == null) {
            instance = new GlobalGiftBannerManager();
        }
        return instance;
    }

    public void startListening(Context context) {
        if (broadcastRef != null) return;

        broadcastRef = FirebaseDatabase.getInstance().getReference("global_gift_broadcasts");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    try {
                        GiftBannerPayload payload = snapshot.getValue(GiftBannerPayload.class);
                        if (payload != null) {
                            if (payload.id == null) payload.id = snapshot.getKey();
                            long age = System.currentTimeMillis() - payload.timestamp;
                            // Only queue broadcasts from the last 3 minutes
                            if (age < 180000L) {
                                enqueueBanner(payload);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        broadcastRef.limitToLast(30).addChildEventListener(childEventListener);
    }

    public void bindContainer(Activity activity, View container, SVGAImageView player, TextView tvNotice) {
        this.activeActivity = activity;
        this.activeContainer = container;
        this.activePlayer = player;
        this.activeTvNotice = tvNotice;

        startListening(activity.getApplicationContext());
    }

    public void unbindContainer(Activity activity) {
        if (this.activeActivity == activity) {
            this.activeActivity = null;
            this.activeContainer = null;
            this.activePlayer = null;
            this.activeTvNotice = null;
        }
    }

    public synchronized void enqueueBanner(GiftBannerPayload payload) {
        if (payload == null) return;
        bannerQueue.add(payload);
        if (!isPlaying) {
            processNextBanner();
        }
    }

    public static void broadcastGiftSent(String senderName, String recipientName, String giftName, int quantity, String roomName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("global_gift_broadcasts").push();
        String id = ref.getKey();
        GiftBannerPayload payload = new GiftBannerPayload(senderName, recipientName, giftName, quantity, roomName);
        payload.id = id;
        ref.setValue(payload);
    }

    private synchronized void processNextBanner() {
        if (bannerQueue.isEmpty()) {
            isPlaying = false;
            return;
        }

        if (activeActivity == null || activeActivity.isFinishing() || activeActivity.isDestroyed() || activeContainer == null || activePlayer == null || activeTvNotice == null) {
            isPlaying = false;
            return;
        }

        isPlaying = true;
        GiftBannerPayload payload = bannerQueue.poll();
        if (payload == null) {
            isPlaying = false;
            return;
        }

        String sender = (payload.senderName != null && !payload.senderName.trim().isEmpty()) ? payload.senderName.trim() : "A Friend";
        String recipient = (payload.recipientName != null && !payload.recipientName.trim().isEmpty()) ? payload.recipientName.trim() : "Someone";
        String gift = (payload.giftName != null && !payload.giftName.trim().isEmpty()) ? payload.giftName.trim() : "Gift";
        String qtyText = payload.quantity > 1 ? " x" + payload.quantity : "";

        String htmlNotice = "<font color='#FFD700'><b>" + sender + "</b></font> sent <font color='#00FFC6'><b>" + gift + qtyText + "</b></font> to <font color='#FF69B4'><b>" + recipient + "</b></font> 🎁";

        activeActivity.runOnUiThread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    activeTvNotice.setText(Html.fromHtml(htmlNotice, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    activeTvNotice.setText(Html.fromHtml(htmlNotice));
                }

                SVGAParser parser = new SVGAParser(activeActivity.getApplicationContext());
                parser.decodeFromAssets(SVGA_ASSET_PATH, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        if (activeActivity == null || activeActivity.isFinishing() || activeActivity.isDestroyed()) {
                            finishCurrentBanner();
                            return;
                        }

                        activeActivity.runOnUiThread(() -> {
                            try {
                                activePlayer.stopAnimation();
                                activePlayer.clear();
                                activePlayer.setVideoItem(videoItem);
                                activePlayer.setLoops(1);

                                activePlayer.setCallback(new SVGACallback() {
                                    @Override public void onFinished() { finishCurrentBanner(); }
                                    @Override public void onPause() {}
                                    @Override public void onRepeat() {}
                                    @Override public void onStep(int frame, double percentage) {}
                                });

                                activeContainer.setVisibility(View.VISIBLE);
                                activePlayer.setVisibility(View.VISIBLE);
                                activePlayer.startAnimation();

                                // Calculate animation duration + safety buffer
                                int frames = videoItem.getFrames();
                                int fps = videoItem.getFPS() > 0 ? videoItem.getFPS() : 20;
                                long durationMs = Math.max(3500L, (long) (((double) frames / fps) * 1000L));

                                if (timeoutRunnable != null) mainHandler.removeCallbacks(timeoutRunnable);
                                timeoutRunnable = GlobalGiftBannerManager.this::finishCurrentBanner;
                                mainHandler.postDelayed(timeoutRunnable, durationMs + 400L);

                            } catch (Exception e) {
                                finishCurrentBanner();
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        finishCurrentBanner();
                    }
                }, null);

            } catch (Exception e) {
                finishCurrentBanner();
            }
        });
    }

    private synchronized void finishCurrentBanner() {
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }

        if (activeActivity != null && !activeActivity.isFinishing() && !activeActivity.isDestroyed()) {
            activeActivity.runOnUiThread(() -> {
                if (activeContainer != null) activeContainer.setVisibility(View.GONE);
                if (activePlayer != null) {
                    try {
                        activePlayer.setCallback(null);
                        activePlayer.stopAnimation();
                        activePlayer.clear();
                    } catch (Exception ignored) {}
                }
                isPlaying = false;
                processNextBanner();
            });
        } else {
            isPlaying = false;
            processNextBanner();
        }
    }
}

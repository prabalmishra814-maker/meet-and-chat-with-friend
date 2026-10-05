package com.roomchatapps.Pmishra;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.NotificationModel;
import com.roomchatapps.Pmishra.models.UserCheckInState;
import com.roomchatapps.Pmishra.services.AppNotificationService;
import com.roomchatapps.Pmishra.utils.DailyCheckInManager;
import com.roomchatapps.Pmishra.utils.SystemNotificationManager;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navExplore, navMessage, navMe;
    private ImageView ivHome, ivExplore, ivMessage, ivMe;
    private TextView tvHome, tvExplore, tvMessage, tvMe;
    private View navIndicator;
    private int currentSelectedIndex = -1;

    private DatabaseReference userNotifRef;
    private ValueEventListener notifEventListener;
    private final long appStartTime = System.currentTimeMillis();
    private final java.util.Set<String> processedGlobalGiftIds = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_main);

        // Initialize System Notification Channel & Request Permission on Android 13+
        SystemNotificationManager.createNotificationChannel(this);
        requestNotificationPermission();

        // Start Persistent Background Notification Service so notifications arrive even when app is closed
        startBackgroundNotificationService();

        initViews();
        setupBottomNavigation();
        setupNotificationListener();
        setupGlobalGiftListener();
        
        // Handle window insets
        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                return insets;
            });
        }

        // Load default fragment if this is a fresh start
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateNavUI(0);
        }

        // DAILY CHECK-IN AUTO SHOW IF NOT CLAIMED TODAY
        checkAndShowDailyCheckIn();
    }

    // DAILY CHECK-IN AUTO SHOW
    private void checkAndShowDailyCheckIn() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null || currentUid.isEmpty()) return;

        DailyCheckInManager.loadUserCheckInState(currentUid, new DailyCheckInManager.StateCallback() {
            @Override
            public void onStateLoaded(UserCheckInState state) {
                if (isFinishing() || isDestroyed()) return;
                // Show Daily Check-In dialog ONLY if today's reward has NOT been claimed yet
                if (state != null && !state.isTodayClaimed()) {
                    findViewById(android.R.id.content).postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            DailyCheckInDialog dialog = new DailyCheckInDialog(MainActivity.this);
                            dialog.show();
                        }
                    }, 600);
                }
            }

            @Override
            public void onError(String error) {
                // Silently ignore errors on automatic background check
            }
        });
    }

    private void startBackgroundNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, AppNotificationService.class);
            startService(serviceIntent);
        } catch (Exception ignored) {}
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setupNotificationListener() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        userNotifRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUid);
        notifEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationModel notif = ds.getValue(NotificationModel.class);
                    if (notif != null && notif.getTimestamp() >= appStartTime - 3000) {
                        if (!notif.isRead()) {
                            SystemNotificationManager.showSystemNotification(
                                    MainActivity.this,
                                    notif.getTitle() != null ? notif.getTitle() : "Notification 🔔",
                                    notif.getMessage() != null ? notif.getMessage() : "New update received",
                                    notif.getType() != null ? notif.getType() : "SYSTEM",
                                    notif.getSenderId(),
                                    notif.getSenderName(),
                                    notif.getTargetId()
                            );
                            // Mark as read in Firebase so system notification fires only once
                            ds.getRef().child("read").setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userNotifRef.addValueEventListener(notifEventListener);
    }

    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navExplore = findViewById(R.id.nav_explore);
        navMessage = findViewById(R.id.nav_message);
        navMe = findViewById(R.id.nav_me);

        ivHome = findViewById(R.id.iv_nav_home);
        ivExplore = findViewById(R.id.iv_nav_explore);
        ivMessage = findViewById(R.id.iv_nav_message);
        ivMe = findViewById(R.id.iv_nav_me);

        tvHome = findViewById(R.id.tv_nav_home);
        tvExplore = findViewById(R.id.tv_nav_explore);
        tvMessage = findViewById(R.id.tv_nav_message);
        tvMe = findViewById(R.id.tv_nav_me);
        navIndicator = findViewById(R.id.nav_indicator);
    }

    private void setupBottomNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (currentSelectedIndex != 0) {
                    loadFragment(new HomeFragment());
                    updateNavUI(0);
                }
            });
        }

        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                if (currentSelectedIndex != 1) {
                    loadFragment(new ExploreFragment());
                    updateNavUI(1);
                }
            });
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> {
                if (currentSelectedIndex != 2) {
                    loadFragment(new MessageFragment());
                    updateNavUI(2);
                }
            });
        }

        if (navMe != null) {
            navMe.setOnClickListener(v -> {
                if (currentSelectedIndex != 3) {
                    loadFragment(new ProfileFragment());
                    updateNavUI(3);
                }
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void updateNavUI(int selectedIndex) {
        currentSelectedIndex = selectedIndex;

        // Reset all icons and text views smoothly
        resetNavItems();

        ImageView selectedIcon = null;
        TextView selectedText = null;

        switch (selectedIndex) {
            case 0:
                selectedIcon = ivHome;
                selectedText = tvHome;
                break;
            case 1:
                selectedIcon = ivExplore;
                selectedText = tvExplore;
                break;
            case 2:
                selectedIcon = ivMessage;
                selectedText = tvMessage;
                break;
            case 3:
                selectedIcon = ivMe;
                selectedText = tvMe;
                break;
        }

        if (selectedIcon != null) {
            selectedIcon.setColorFilter(Color.parseColor("#40E0D0"));
            selectedIcon.animate()
                    .scaleX(1.18f)
                    .scaleY(1.18f)
                    .setDuration(220)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
        }

        if (selectedText != null) {
            selectedText.setTextColor(Color.parseColor("#FFFFFF"));
        }

        animateIndicator(selectedIndex);
    }

    private void animateIndicator(int index) {
        if (navIndicator == null) return;
        
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav == null || bottomNav.getWidth() == 0) {
            navIndicator.post(() -> animateIndicator(index));
            return;
        }

        float tabWidth = bottomNav.getWidth() / 4f;
        float targetX = (tabWidth * index) + (tabWidth / 2f) - (navIndicator.getWidth() / 2f);
        
        navIndicator.animate()
                .translationX(targetX)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void resetNavItems() {
        ImageView[] icons = {ivHome, ivExplore, ivMessage, ivMe};
        TextView[] texts = {tvHome, tvExplore, tvMessage, tvMe};

        for (ImageView icon : icons) {
            if (icon != null) {
                icon.setColorFilter(Color.parseColor("#88FFFFFF"));
                icon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(180).start();
            }
        }

        for (TextView text : texts) {
            if (text != null) {
                text.setTextColor(Color.parseColor("#88FFFFFF"));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userNotifRef != null && notifEventListener != null) {
            userNotifRef.removeEventListener(notifEventListener);
        }
        if (globalGiftsRef != null && globalGiftsChildListener != null) {
            globalGiftsRef.removeEventListener(globalGiftsChildListener);
        }
        if (globalBannerTimeoutRunnable != null) {
            globalBannerHandler.removeCallbacks(globalBannerTimeoutRunnable);
        }
    }

    private DatabaseReference globalGiftsRef;
    private ChildEventListener globalGiftsChildListener;
    private NotificationAnimator globalNotificationAnimator;
    private final Queue<GlobalBannerItem> globalBannerQueue = new LinkedList<>();
    private boolean isGlobalBannerPlaying = false;
    private final Handler globalBannerHandler = new Handler(Looper.getMainLooper());
    private Runnable globalBannerTimeoutRunnable = null;

    private static class GlobalBannerItem {
        final String svgaAsset;
        final String htmlNotice;
        final String senderName;
        final String giftNotice;
        final int iconRes;
        final String avatarUrl;

        GlobalBannerItem(String svgaAsset, String htmlNotice, String senderName, String giftNotice, int iconRes, String avatarUrl) {
            this.svgaAsset = svgaAsset;
            this.htmlNotice = htmlNotice;
            this.senderName = senderName;
            this.giftNotice = giftNotice;
            this.iconRes = iconRes;
            this.avatarUrl = avatarUrl;
        }
    }

    private void setupGlobalGiftListener() {
        View overlayContainer = findViewById(R.id.globalGiftOverlayContainer);
        if (overlayContainer instanceof android.view.ViewGroup) {
            globalNotificationAnimator = new NotificationAnimator((android.view.ViewGroup) overlayContainer);
        }

        globalGiftsRef = FirebaseDatabase.getInstance().getReference("global_room_gifts");
        globalGiftsChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (!snapshot.exists()) return;

                Long ts = snapshot.child("timestamp").getValue(Long.class);
                if (ts != null && ts < appStartTime - 3000) return;

                String sName = snapshot.child("senderName").getValue(String.class);
                String gName = snapshot.child("giftName").getValue(String.class);
                if (sName == null || gName == null) return;

                String giftId = snapshot.child("giftId").getValue(String.class);
                if (giftId == null || giftId.isEmpty()) {
                    giftId = sName + "_" + gName + "_" + (ts != null ? ts : 0);
                }
                if (processedGlobalGiftIds.contains(giftId)) return;
                processedGlobalGiftIds.add(giftId);

                String rName = snapshot.child("recipientName").getValue(String.class);
                String rUid = snapshot.child("recipientId").getValue(String.class);
                String roomName = snapshot.child("roomName").getValue(String.class);
                String sAvatar = snapshot.child("senderAvatar").getValue(String.class);
                Long iconResLong = snapshot.child("iconRes").getValue(Long.class);
                int iconRes = iconResLong != null ? iconResLong.intValue() : R.drawable.gift_icon;

                String sender = sName.trim();
                String gift = gName.trim();
                String target = "";
                if (rName != null && !rName.trim().isEmpty()) {
                    target = " to <font color='#00FFC6'><b>" + rName.trim() + (rUid != null && !rUid.isEmpty() ? " (ID: " + rUid + ")" : "") + "</b></font>";
                }
                String room = (roomName != null && !roomName.trim().isEmpty()) ? (" in " + roomName.trim()) : "";

                Long qtyLong = snapshot.child("quantity").getValue(Long.class);
                int qty = qtyLong != null ? qtyLong.intValue() : 1;
                if (qty <= 1) {
                    int xIdx = gift.indexOf(" (x");
                    if (xIdx != -1) {
                        try {
                            String numStr = gift.substring(xIdx + 3, gift.indexOf(")", xIdx));
                            qty = Integer.parseInt(numStr);
                        } catch (Exception ignored) {}
                    }
                }

                String cleanGift = gift;
                int idx = cleanGift.indexOf(" (x");
                if (idx != -1) {
                    cleanGift = cleanGift.substring(0, idx);
                }

                String qtyText = (qty > 1) ? (qty + "x ") : "";
                String slideNotice = "sent " + qtyText + gift + (rName != null && !rName.trim().isEmpty() ? " to " + rName.trim() : "") + " 🎁";

                String htmlNotice = "<font color='#FFD700'><b>" + sender + "</b></font> sent <font color='#FF007A'><b>" + qtyText + cleanGift + "</b></font>" + target + room + " 🎁";
                globalBannerQueue.add(new GlobalBannerItem("Notification/rednotification (1).svga", htmlNotice, sender, slideNotice, iconRes, sAvatar));

                if (!isGlobalBannerPlaying) {
                    processNextGlobalBanner();
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        globalGiftsRef.addChildEventListener(globalGiftsChildListener);
    }

    private synchronized void processNextGlobalBanner() {
        if (globalBannerQueue.isEmpty()) {
            isGlobalBannerPlaying = false;
            return;
        }

        isGlobalBannerPlaying = true;
        GlobalBannerItem item = globalBannerQueue.poll();
        if (item == null) {
            isGlobalBannerPlaying = false;
            return;
        }

        View container = findViewById(R.id.globalBannerGiftContainer);
        com.opensource.svgaplayer.SVGAImageView player = findViewById(R.id.globalSvgaBannerPlayer);
        TextView tvNotice = findViewById(R.id.globalTvBannerNotice);

        if (container == null || player == null || tvNotice == null) {
            isGlobalBannerPlaying = false;
            processNextGlobalBanner();
            return;
        }

        runOnUiThread(() -> {
            try {
                container.setVisibility(View.GONE);
                player.stopAnimation();
                player.clear();
            } catch (Exception ignored) {}
        });

        com.opensource.svgaplayer.SVGAParser parser = new com.opensource.svgaplayer.SVGAParser(this);
        parser.decodeFromAssets(item.svgaAsset, new com.opensource.svgaplayer.SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NonNull com.opensource.svgaplayer.SVGAVideoEntity videoItem) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        finishCurrentGlobalBanner();
                        return;
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvNotice.setText(Html.fromHtml(item.htmlNotice, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            tvNotice.setText(Html.fromHtml(item.htmlNotice));
                        }

                        int frames = videoItem.getFrames();
                        int fps = videoItem.getFPS() > 0 ? videoItem.getFPS() : 20;
                        long durationMs = (long) (((double) frames / fps) * 1000L);
                        long bannerDisplayDurationMs = Math.max(3500L, durationMs);

                        if (globalBannerTimeoutRunnable != null) {
                            globalBannerHandler.removeCallbacks(globalBannerTimeoutRunnable);
                        }
                        globalBannerTimeoutRunnable = MainActivity.this::finishCurrentGlobalBanner;
                        globalBannerHandler.postDelayed(globalBannerTimeoutRunnable, bannerDisplayDurationMs + 300L);

                        player.stopAnimation();
                        player.clear();
                        player.setVideoItem(videoItem);
                        player.setLoops(1);
                        player.setCallback(new com.opensource.svgaplayer.SVGACallback() {
                            @Override public void onFinished() { finishCurrentGlobalBanner(); }
                            @Override public void onPause() {}
                            @Override public void onRepeat() {}
                            @Override public void onStep(int frame, double percentage) {}
                        });

                        player.setVisibility(View.VISIBLE);
                        container.setVisibility(View.VISIBLE);
                        player.startAnimation();
                    } catch (Exception e) {
                        finishCurrentGlobalBanner();
                    }
                });
            }

            @Override
            public void onError() {
                finishCurrentGlobalBanner();
            }
        }, null);
    }

    private void finishCurrentGlobalBanner() {
        runOnUiThread(() -> {
            if (globalBannerTimeoutRunnable != null) {
                globalBannerHandler.removeCallbacks(globalBannerTimeoutRunnable);
                globalBannerTimeoutRunnable = null;
            }
            View container = findViewById(R.id.globalBannerGiftContainer);
            com.opensource.svgaplayer.SVGAImageView player = findViewById(R.id.globalSvgaBannerPlayer);

            if (container != null) container.setVisibility(View.GONE);
            if (player != null) {
                try {
                    player.setCallback(null);
                    player.stopAnimation();
                    player.clear();
                } catch (Exception ignored) {}
            }
            isGlobalBannerPlaying = false;
            globalBannerHandler.postDelayed(this::processNextGlobalBanner, 150L);
        });
    }
}

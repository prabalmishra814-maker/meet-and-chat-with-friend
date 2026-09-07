package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.NotificationModel;
import com.roomchatapps.Pmishra.services.AppNotificationService;
import com.roomchatapps.Pmishra.utils.SystemNotificationManager;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navExplore, navMessage, navMe;
    private ImageView ivHome, ivExplore, ivMessage, ivMe;
    private TextView tvHome, tvExplore, tvMessage, tvMe;
    private View navIndicator;
    private int currentSelectedIndex = -1;

    private DatabaseReference userNotifRef;
    private ValueEventListener notifEventListener;
    private final long appStartTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize System Notification Channel & Request Permission on Android 13+
        SystemNotificationManager.createNotificationChannel(this);
        requestNotificationPermission();

        // Start Persistent Background Notification Service so notifications arrive even when app is closed
        startBackgroundNotificationService();

        initViews();
        setupBottomNavigation();
        setupNotificationListener();
        
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
    }

    private void startBackgroundNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, AppNotificationService.class);
            startService(serviceIntent);
        } catch (Exception ignored) {}
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
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
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
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
    }
}

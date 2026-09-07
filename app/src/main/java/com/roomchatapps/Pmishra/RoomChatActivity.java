package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.models.FriendRequestModel;
import com.roomchatapps.Pmishra.models.TransactionModel;
import com.roomchatapps.Pmishra.ui.ChatAdapter;
import com.roomchatapps.Pmishra.ui.SeatAdapter;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.utils.WalletManager;
import com.roomchatapps.Pmishra.zego.SeatManager;
import com.roomchatapps.Pmishra.zego.SeatModel;
import com.roomchatapps.Pmishra.zego.ZegoManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoUser;

public class RoomChatActivity extends AppCompatActivity {

    private final long appID = 1696254780L;
    private final String appSign = "bca17fa9d8559318d5e7d1cdf2c427018467d3a44f9d1212f2827e4d35b719d0";

    private String roomID;
    private String userID;
    private String userName;
    private String roomNameLabel;
    private String roomImg;
    private boolean isHost;

    private NotificationAnimator notificationAnimator;
    private SVGAImageView svgaPlayer;
    private SVGAParser svgaParser;

    private DatabaseReference roomGiftsRef;
    private ChildEventListener giftsChildEventListener;

    private DatabaseReference roomReactionsRef;
    private ChildEventListener reactionsChildEventListener;

    private final long roomJoinTime = System.currentTimeMillis();

    private AudioRoomBackgroundView backgroundView;
    private SeatAdapter seatAdapter;
    private ChatAdapter chatAdapter;
    private RecyclerView rvSeats, rvChat;

    private ImageView btnMic, btnSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_room_chat);

        initParams();
        initViews();
        initZego();
        setupFirebaseListeners();

        if (notificationAnimator != null) {
            notificationAnimator.showNotification("Welcome 🚪", userName + " joined the room!", R.drawable.img_20260904_135725);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leaveRoom();
            }
        });
    }

    private void initParams() {
        roomID = getIntent().getStringExtra("roomID");
        userName = getIntent().getStringExtra("username");
        userID = getIntent().getStringExtra("userID");
        isHost = getIntent().getBooleanExtra("host", false);
        roomNameLabel = getIntent().getStringExtra("room_name");
        roomImg = getIntent().getStringExtra("img");

        if (roomID == null || roomID.isEmpty()) roomID = "default_room";
        if (userID == null || userID.isEmpty()) userID = "user_" + System.currentTimeMillis();
        if (userName == null || userName.isEmpty()) userName = "User_" + new Random().nextInt(1000);

        UserProfileCache.getUserProfile(userID, profile -> {});
    }

    private void initViews() {
        svgaPlayer = findViewById(R.id.svgaPlayer);
        svgaParser = new SVGAParser(this);
        notificationAnimator = new NotificationAnimator(findViewById(R.id.giftOverlayContainer));

        backgroundView = findViewById(R.id.audioRoomBackground);
        backgroundView.setRoomName(roomNameLabel != null ? roomNameLabel : "Room");
        backgroundView.setRoomID(roomID);
        backgroundView.setBackgroundImage(roomImg);

        rvSeats = findViewById(R.id.rvSeats);
        rvSeats.setLayoutManager(new GridLayoutManager(this, 4));
        seatAdapter = new SeatAdapter(this::onSeatClicked);
        rvSeats.setAdapter(seatAdapter);

        rvChat = findViewById(R.id.rvChatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter();
        rvChat.setAdapter(chatAdapter);

        setupBottomButtons();
    }

    private void setupBottomButtons() {
        findViewById(R.id.btnChat).setOnClickListener(v -> showMessageCenterDialog());
        
        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnSpeaker.setOnClickListener(v -> {
            boolean newState = !ZegoManager.getInstance().isSpeakerOn();
            ZegoManager.getInstance().setSpeakerOn(newState);
            btnSpeaker.setImageResource(newState ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
        });

        btnMic = findViewById(R.id.btnMic);
        btnMic.setOnClickListener(v -> {
            int seatIndex = SeatManager.getInstance().findUserSeatIndex(userID);
            if (seatIndex != -1) {
                SeatModel model = SeatManager.getInstance().getSeats().get(seatIndex);
                boolean newState = !model.isMicOn;
                SeatManager.getInstance().updateMicStatus(seatIndex, newState);
                ZegoManager.getInstance().setMicEnabled(newState);
                btnMic.setImageResource(newState ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            } else {
                Toast.makeText(this, "Take a seat first to use mic", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnMore).setOnClickListener(v -> showRoomMenuDialog());
        findViewById(R.id.btnGame).setOnClickListener(v -> showRoomGameDialog());
        findViewById(R.id.btnGift).setOnClickListener(v -> showGiftDialog());

        ImageView btnSettings = findViewById(R.id.btnSettings);
        if (isHost) {
            btnSettings.setVisibility(View.VISIBLE);
            btnSettings.setOnClickListener(v -> openRoomSettings());
        }
    }

    private void initZego() {
        ZegoManager.getInstance().init(getApplication(), appID, appSign);
        ZegoManager.getInstance().addListener(zegoListener);
        ZegoManager.getInstance().loginRoom(roomID, userID, userName, isHost);

        SeatManager.getInstance().addListener(seatListener);
        if (isHost) {
            SeatManager.getInstance().takeSeat(0, userID, userName);
        }
    }

    private final ZegoManager.ZegoManagerListener zegoListener = new ZegoManager.ZegoManagerListener() {
        @Override
        public void onUserJoined(ZegoUser user) {
            backgroundView.showWelcomeAnimation();
            updateGlobalUserCount();
        }

        @Override
        public void onUserLeft(ZegoUser user) {
            updateGlobalUserCount();
        }

        @Override
        public void onRoomExtraInfoUpdate(String roomID, List<ZegoRoomExtraInfo> roomExtraInfoList) {
            SeatManager.getInstance().updateSeatsFromExtraInfo(roomExtraInfoList);
        }

        @Override
        public void onIMRecvBroadcastMessage(String roomID, List<ZegoBroadcastMessageInfo> messageList) {
            runOnUiThread(() -> {
                chatAdapter.addMessages(messageList);
                rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            });
        }
    };

    private final SeatManager.SeatListener seatListener = seats -> runOnUiThread(() -> {
        seatAdapter.setSeats(seats);
        // Sync local mic icon if self seat changed
        int myIndex = SeatManager.getInstance().findUserSeatIndex(userID);
        if (myIndex != -1) {
            boolean isMicOn = seats.get(myIndex).isMicOn;
            btnMic.setImageResource(isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            ZegoManager.getInstance().setMicEnabled(isMicOn);
        }
    });

    private void updateGlobalUserCount() {
        // Since we don't have UIKit's getAllUsers(), we might need a more complex way or just 
        // trust RoomUserUpdate. 
        // For simplicity, I'll add a counter if needed or just use 1/16 placeholder.
    }

    private void onSeatClicked(SeatModel model) {
        if (model.userID.isEmpty()) {
            // Join seat
            int currentIndex = SeatManager.getInstance().findUserSeatIndex(userID);
            if (currentIndex != -1) {
                SeatManager.getInstance().leaveSeat(currentIndex);
            }
            SeatManager.getInstance().takeSeat(model.index, userID, userName);
            ZegoManager.getInstance().startPublishing();
        } else if (model.userID.equals(userID)) {
            // Leave seat
            SeatManager.getInstance().leaveSeat(model.index);
            ZegoManager.getInstance().stopPublishing();
        } else {
            // Show user profile
            Intent intent = new Intent(this, UserDetailActivity.class);
            intent.putExtra("uid", model.userID);
            startActivity(intent);
        }
    }

    private void leaveRoom() {
        ZegoManager.getInstance().logoutRoom();
        ZegoManager.getInstance().removeListener(zegoListener);
        SeatManager.getInstance().removeListener(seatListener);
        finish();
    }

    private void setupFirebaseListeners() {
        setupRoomGiftListener();
        setupRoomReactionListener();
    }

    private void setupRoomGiftListener() {
        roomGiftsRef = FirebaseDatabase.getInstance().getReference("room_gifts").child(roomID);
        giftsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String giftKey = snapshot.getKey();
                    String senderName = snapshot.child("senderName").getValue(String.class);
                    String giftName = snapshot.child("giftName").getValue(String.class);
                    Long iconResLong = snapshot.child("iconRes").getValue(Long.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 3000) return;

                    if (senderName != null && giftName != null && iconResLong != null) {
                        int iconRes = iconResLong.intValue();
                        if (notificationAnimator != null) {
                            notificationAnimator.showNotification("🎁 Gift Received", senderName + " sent " + giftName, iconRes);
                        }
                        if (giftName.contains("Heart")) {
                            playSvgaAnimation("gift/aladdin.svga");
                        }
                        if (giftKey != null) {
                            snapshot.getRef().removeValue();
                        }
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomGiftsRef.addChildEventListener(giftsChildEventListener);
    }

    private void setupRoomReactionListener() {
        roomReactionsRef = FirebaseDatabase.getInstance().getReference("room_reactions").child(roomID);
        reactionsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String reactionKey = snapshot.getKey();
                    Long iconResLong = snapshot.child("iconRes").getValue(Long.class);
                    Double relXObj = snapshot.child("relX").getValue(Double.class);
                    Double relYObj = snapshot.child("relY").getValue(Double.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 3000) return;

                    if (iconResLong != null) {
                        int iconRes = iconResLong.intValue();
                        ViewGroup container = findViewById(R.id.giftOverlayContainer);
                        if (container != null && container.getWidth() > 0 && container.getHeight() > 0) {
                            float startX = (relXObj != null) ? (float) (relXObj * container.getWidth()) : (container.getWidth() - 120);
                            float startY = (relYObj != null) ? (float) (relYObj * container.getHeight()) : (container.getHeight() - 120);
                            ReactionAnimator.spawnFloatingReactionAt(container, startX, startY, iconRes);
                        }
                        if (reactionKey != null) {
                            snapshot.getRef().removeValue();
                        }
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomReactionsRef.addChildEventListener(reactionsChildEventListener);
    }

    private void openRoomSettings() {
        Intent intent = new Intent(this, RoomSettingsActivity.class);
        intent.putExtra("roomID", roomID);
        intent.putExtra("roomName", roomNameLabel);
        intent.putExtra("roomImg", roomImg);
        startActivity(intent);
    }

    private void showRoomGameDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_game, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);
            View parent = (View) dialogView.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            parent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            WebView webView = dialogView.findViewById(R.id.webViewPoki);
            ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
            if (webView != null) {
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl("https://poki.com/");
            }
            dialog.show();
        }
    }

    private void showRoomMenuDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_menu, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);
            View layoutRoomSettings = dialogView.findViewById(R.id.layoutRoomSettings);
            if (layoutRoomSettings != null) {
                layoutRoomSettings.setVisibility(isHost ? View.VISIBLE : View.GONE);
                layoutRoomSettings.setOnClickListener(v -> {
                    dialog.dismiss();
                    openRoomSettings();
                });
            }
            dialog.show();
        }
    }

    private void showMessageCenterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_message_center, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);
            dialog.show();
        }
    }

    private void showGiftDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_svga_play, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);
            View sendBtn = dialogView.findViewById(R.id.btnSendAnimation);
            if (sendBtn != null) {
                sendBtn.setOnClickListener(v -> {
                    playSvgaAnimation("gift/aladdin.svga");
                    dialog.dismiss();
                });
            }
            dialog.show();
        }
    }

    private void playSvgaAnimation(String fileName) {
        if (svgaPlayer == null || svgaParser == null) return;
        svgaParser.decodeFromAssets(fileName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                svgaPlayer.setVisibility(View.VISIBLE);
                svgaPlayer.setVideoItem(videoItem);
                svgaPlayer.startAnimation();
                svgaPlayer.setCallback(new SVGACallback() {
                    @Override public void onPause() {}
                    @Override public void onFinished() {
                        runOnUiThread(() -> svgaPlayer.setVisibility(View.GONE));
                    }
                    @Override public void onStep(int frame, double percentage) {}
                    @Override public void onRepeat() {}
                });
            }
            @Override public void onError() {}
        }, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }
}

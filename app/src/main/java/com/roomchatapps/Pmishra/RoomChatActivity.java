package com.roomchatapps.Pmishra;

import com.roomchatapps.Pmishra.spinwheel.SpinWheelController;
import com.roomchatapps.Pmishra.spinwheel.SpinWheelView;
import com.roomchatapps.Pmishra.spinwheel.SpinWinnerDialog;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
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
import com.roomchatapps.Pmishra.adapters.AudienceAdapter;
import com.roomchatapps.Pmishra.adapters.ChatAdapter;
import com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem;
import com.roomchatapps.Pmishra.adapters.SeatAdapter;
import com.roomchatapps.Pmishra.models.ChatMessage;
import com.roomchatapps.Pmishra.models.FriendRequestModel;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;
import com.roomchatapps.Pmishra.models.TransactionModel;
import com.roomchatapps.Pmishra.models.User;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.LevelUtils;
import com.roomchatapps.Pmishra.utils.NotificationHelper;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.utils.WalletManager;
import com.roomchatapps.Pmishra.zego.SeatManager;
import com.roomchatapps.Pmishra.zego.SeatModel;
import com.roomchatapps.Pmishra.zego.ZegoManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Random;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import kotlin.Unit;

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

    private View bannerGiftContainer;
    private SVGAImageView svgaBannerPlayer;
    private TextView tvBannerNotice;

    private View bannerEntryContainer;
    private SVGAImageView svgaEntryBannerPlayer;
    private TextView tvEntryBannerNotice;

    private DatabaseReference roomGiftsRef;
    private ChildEventListener giftsChildEventListener;

    private DatabaseReference roomInfoRef;
    private ValueEventListener roomInfoValueEventListener;

    private DatabaseReference roomSeatsRef;
    private ValueEventListener seatsValueEventListener;

    private DatabaseReference roomMessagesRef;
    private ChildEventListener roomMessagesChildEventListener;

    private DatabaseReference roomMusicRef;
    private ValueEventListener roomMusicValueEventListener;

    private DatabaseReference roomThemeRef;
    private ValueEventListener roomThemeValueEventListener;

    private DatabaseReference roomEntriesRef;
    private ChildEventListener roomEntriesChildEventListener;

    private DatabaseReference roomOnlineUsersRef;
    private DatabaseReference mySeatInviteRef;
    private ValueEventListener mySeatInviteListener;
    private Dialog currentInviteDialog;

    private final ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
                        handleSelectedAudioUri(audioUri);
                    }
                }
            }
    );

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
        
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 102);
        } else {
            initZego();
        }
        
        setupFirebaseListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitOptionsDialog();
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

        String hostUid = getIntent().getStringExtra("uid");
        if (hostUid == null || hostUid.trim().isEmpty()) {
            if (isHost) hostUid = userID;
        }
        SeatManager.getInstance().setHostUserID(hostUid);
        SeatManager.getInstance().setCurrentRoomID(roomID);

        UserProfileCache.getUserProfile(userID, profile -> {});
    }

    private void initViews() {
        svgaPlayer = findViewById(R.id.svgaPlayer);
        if (svgaPlayer != null) {
            svgaPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        bannerGiftContainer = findViewById(R.id.bannerGiftContainer);
        svgaBannerPlayer = findViewById(R.id.svgaBannerPlayer);
        tvBannerNotice = findViewById(R.id.tvBannerNotice);
        if (svgaBannerPlayer != null) {
            svgaBannerPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        bannerEntryContainer = findViewById(R.id.bannerEntryContainer);
        svgaEntryBannerPlayer = findViewById(R.id.svgaEntryBannerPlayer);
        tvEntryBannerNotice = findViewById(R.id.tvEntryBannerNotice);
        if (svgaEntryBannerPlayer != null) {
            svgaEntryBannerPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            SVGAParser.Companion.setThreadPoolExecutor(executor);
        } catch (Exception ignored) {}

        svgaParser = new SVGAParser(this);
        notificationAnimator = new NotificationAnimator(findViewById(R.id.giftOverlayContainer));

        backgroundView = findViewById(R.id.audioRoomBackground);
        backgroundView.setRoomName(roomNameLabel != null ? roomNameLabel : "Room");
        backgroundView.setRoomID(roomID);
        backgroundView.setBackgroundImage(roomImg);

        TextView tvRoomName = findViewById(R.id.tvRoomName);
        TextView tvRoomId = findViewById(R.id.tvRoomId);
        ImageView ivRoomAvatar = findViewById(R.id.ivRoomAvatar);

        if (tvRoomName != null && roomNameLabel != null && !roomNameLabel.isEmpty()) {
            tvRoomName.setText(roomNameLabel);
        }
        if (tvRoomId != null && roomID != null && !roomID.isEmpty()) {
            tvRoomId.setText("ID:" + roomID);
        }
        if (ivRoomAvatar != null && roomImg != null && !roomImg.isEmpty()) {
            Glide.with(this)
                    .load(roomImg)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .into(ivRoomAvatar);
        }

        View btnRoomClose = findViewById(R.id.btnRoomClose);
        if (btnRoomClose != null) {
            btnRoomClose.setOnClickListener(v -> showExitOptionsDialog());
        }

        View layoutUserInfo = findViewById(R.id.layoutUserInfo);
        if (layoutUserInfo != null) {
            layoutUserInfo.setOnClickListener(v -> {
                if (isHost) {
                    Intent intent = new Intent(RoomChatActivity.this, RoomSettingsActivity.class);
                    intent.putExtra("roomID", roomID);
                    intent.putExtra("hostUid", userID);
                    intent.putExtra("roomName", roomNameLabel);
                    intent.putExtra("roomImg", roomImg);
                    startActivity(intent);
                } else {
                    List<SeatModel> seats = SeatManager.getInstance().getSeats();
                    if (seats != null && !seats.isEmpty()) {
                        String hostUid = SeatManager.getInstance().getHostUserID();
                        int hostSeatIdx = SeatManager.getInstance().findUserSeatIndex(hostUid);
                        if (hostSeatIdx != -1) {
                            onSeatClicked(seats.get(hostSeatIdx));
                        } else {
                            SeatModel hostSeat = seats.get(0);
                            if (hostSeat != null) {
                                onSeatClicked(hostSeat);
                            }
                        }
                    }
                }
            });
        }


        View btnRoomShare = findViewById(R.id.btnRoomShare);
        if (btnRoomShare != null) {
            btnRoomShare.setOnClickListener(v -> {
                String displayRoomName = (roomNameLabel != null && !roomNameLabel.trim().isEmpty()) ? roomNameLabel : "Audio Party Room";
                String displayHostName = (userName != null && !userName.trim().isEmpty()) ? userName : "Room Host";

                String shareMessage = "🎉 You're invited to join our Live Voice Room! 🎙️\n\n"
                        + "🏠 Room Name: " + displayRoomName + "\n"
                        + "🔑 Room ID: " + roomID + "\n"
                        + "👤 Host: " + displayHostName + "\n\n"
                        + "🔥 Join us now for live voice chat, music, fun games & gifts! 💬🎁🎵\n\n"
                        + "👇 Open Room Chat App & enter Room ID (" + roomID + ") to join:\n"
                        + "https://play.google.com/store/apps/details?id=" + getPackageName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join " + displayRoomName + " (Room ID: " + roomID + ")");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Invite Friends via..."));
            });
        }

        rvSeats = findViewById(R.id.rvSeats);
        GridLayoutManager seatLayoutManager = new GridLayoutManager(this, 4);
        seatLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0) ? 4 : 1;
            }
        });
        rvSeats.setLayoutManager(seatLayoutManager);
        seatAdapter = new SeatAdapter(this::onSeatClicked);
        rvSeats.setAdapter(seatAdapter);
        seatAdapter.setSeats(SeatManager.getInstance().getSeats());

        rvChat = findViewById(R.id.rvChatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter();
        rvChat.setAdapter(chatAdapter);

        setupBottomButtons();
    }

    private void setupBottomButtons() {
        findViewById(R.id.btnChat).setOnClickListener(v -> showCommentInputDialog());
        
        btnSpeaker = findViewById(R.id.btnSpeaker);
        if (btnSpeaker != null) {
            boolean speakerState = ZegoManager.getInstance().isSpeakerOn();
            btnSpeaker.setImageResource(speakerState ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
            btnSpeaker.setOnClickListener(v -> {
                boolean newState = !ZegoManager.getInstance().isSpeakerOn();
                ZegoManager.getInstance().setSpeakerOn(newState);
                btnSpeaker.setImageResource(newState ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
                Toast.makeText(this, newState ? "Speaker Turned On 🔊" : "Speaker Muted 🔇", Toast.LENGTH_SHORT).show();
            });
        }

        btnMic = findViewById(R.id.btnMic);
        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                int seatIndex = SeatManager.getInstance().findUserSeatIndex(userID);
                if (seatIndex != -1) {
                    SeatModel model = SeatManager.getInstance().getSeats().get(seatIndex);
                    if (model.isMuted) {
                        Toast.makeText(this, "The host has muted your microphone!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean newState = !model.isMicOn;
                    SeatManager.getInstance().updateMicStatus(seatIndex, newState);
                    ZegoManager.getInstance().setMicEnabled(newState);
                    if (newState) {
                        ZegoManager.getInstance().startPublishing();
                    }
                    btnMic.setImageResource(newState ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
                    Toast.makeText(this, newState ? "Microphone Unmuted 🎙️" : "Microphone Muted 🔇", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Take a seat first to use mic", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View moreView = findViewById(R.id.more);
        if (moreView != null) {
            moreView.setVisibility(isHost ? View.VISIBLE : View.GONE);
            moreView.setOnClickListener(v -> showMorePanelDialog());
        }

        View btnMore = findViewById(R.id.btnMore);
        if (btnMore != null) {
            btnMore.setVisibility(isHost ? View.VISIBLE : View.GONE);
            btnMore.setOnClickListener(v -> showMorePanelDialog());
        }

        findViewById(R.id.btnGame).setOnClickListener(v -> showRoomGameDialog());
        findViewById(R.id.btnGift).setOnClickListener(v -> showGiftDialog());

        ImageView btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> showThemeSelectionDialog());
        }
    }

    private void initZego() {
        ZegoManager.getInstance().init(getApplication(), appID, appSign);
        ZegoManager.getInstance().addListener(zegoListener);
        ZegoManager.getInstance().loginRoom(roomID, userID, userName, isHost);

        SeatManager.getInstance().addListener(seatListener);
        updateGlobalUserCount();
    }

    private final ZegoManager.ZegoManagerListener zegoListener = new ZegoManager.ZegoManagerListener() {
        @Override
        public void onLoginResult(int errorCode) {
            if (errorCode == 0) {
                broadcastUserEntry();
                if (isHost) {
                    SeatManager.getInstance().takeSeat(0, userID, userName);
                    ZegoManager.getInstance().startPublishing();
                } else {
                    // Non-host users must NOT be on a seat when entering the room.
                    // If an old seat exists for this user, clear it so their profile doesn't show on a seat.
                    int existingIndex = SeatManager.getInstance().findUserSeatIndex(userID);
                    if (existingIndex != -1) {
                        SeatManager.getInstance().leaveSeat(existingIndex);
                    }
                }
            } else {
                Toast.makeText(RoomChatActivity.this, "Failed to join room: " + errorCode, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onUserJoined(ZegoUser user) {
            updateGlobalUserCount();
        }

        @Override
        public void onUserLeft(ZegoUser user) {
            updateGlobalUserCount();
            if (user != null && user.userID != null && !user.userID.isEmpty()) {
                int leftSeatIndex = SeatManager.getInstance().findUserSeatIndex(user.userID);
                if (leftSeatIndex != -1) {
                    SeatManager.getInstance().leaveSeat(leftSeatIndex);
                }
            }
        }

        @Override
        public void onRoomExtraInfoUpdate(String roomID, List<ZegoRoomExtraInfo> roomExtraInfoList) {
            SeatManager.getInstance().updateSeatsFromExtraInfo(roomExtraInfoList);
        }

        @Override
        public void onAudioLevelUpdate(String userID, float soundLevel) {
            runOnUiThread(() -> {
                // soundLevel > 5 is usually enough to consider someone speaking
                seatAdapter.setSpeaking(userID, soundLevel > 5);
            });
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
            SeatModel mySeat = seats.get(myIndex);
            boolean isMicOn = mySeat.isMicOn;
            
            // Host enforcement: if muted, force mic off locally
            if (mySeat.isMuted) {
                isMicOn = false;
                ZegoManager.getInstance().setMicEnabled(false);
            } else {
                ZegoManager.getInstance().setMicEnabled(isMicOn);
            }
            
            ZegoManager.getInstance().startPublishing();
            if (btnMic != null) {
                btnMic.setImageResource(isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            }
        } else {
            // Not on seat, ensure mic publishing is stopped
            ZegoManager.getInstance().stopPublishing();
            if (btnMic != null) {
                btnMic.setImageResource(R.drawable.ic_mic_off);
            }
        }
    });

    private void updateGlobalUserCount() {
        runOnUiThread(() -> {
            int count = ZegoManager.getInstance().getRoomUserCount();
            if (backgroundView != null) {
                backgroundView.setUserCount(count);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted. Click the seat again.", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initZego();
        }
    }

    private void onSeatClicked(SeatModel model) {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 101);
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_options, null, false);
        dialog.setContentView(dialogView);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        View vSeatHeaderBg = dialogView.findViewById(R.id.vSeatHeaderBg);
        ImageView ivSeatHeaderIcon = dialogView.findViewById(R.id.ivSeatHeaderIcon);
        ImageView ivSeatHeaderAvatar = dialogView.findViewById(R.id.ivSeatHeaderAvatar);
        TextView tvSeatTitle = dialogView.findViewById(R.id.tvSeatTitle);
        TextView tvSeatSubtitle = dialogView.findViewById(R.id.tvSeatSubtitle);

        View btnPrimaryAction = dialogView.findViewById(R.id.btnPrimaryAction);
        ImageView ivPrimaryActionIcon = dialogView.findViewById(R.id.ivPrimaryActionIcon);
        TextView tvPrimaryActionText = dialogView.findViewById(R.id.tvPrimaryActionText);

        View btnOptionProfile = dialogView.findViewById(R.id.btnOptionProfile);
        View btnOptionGift = dialogView.findViewById(R.id.btnOptionGift);
        View btnOptionLock = dialogView.findViewById(R.id.btnOptionLock);
        ImageView ivOptionLockIcon = dialogView.findViewById(R.id.ivOptionLockIcon);
        TextView tvOptionLockText = dialogView.findViewById(R.id.tvOptionLockText);
        View btnOptionMute = dialogView.findViewById(R.id.btnOptionMute);
        ImageView ivOptionMuteIcon = dialogView.findViewById(R.id.ivOptionMuteIcon);
        TextView tvOptionMuteText = dialogView.findViewById(R.id.tvOptionMuteText);
        View btnOptionKick = dialogView.findViewById(R.id.btnOptionKick);

        boolean isEmpty = model.userID == null || model.userID.trim().isEmpty();

        if (isEmpty) {
            if (model.isClosed && !isHost) {
                Toast.makeText(this, "This seat is locked", Toast.LENGTH_SHORT).show();
                return;
            }

            BottomSheetDialog emptyDialog = new BottomSheetDialog(this);
            applyGlassyStyle(emptyDialog);
            View emptyView = getLayoutInflater().inflate(R.layout.dialog_empty_seat_host, null, false);
            emptyDialog.setContentView(emptyView);

            TextView tvOptionOnMic = emptyView.findViewById(R.id.tvOptionOnMic);
            TextView tvOptionInvite = emptyView.findViewById(R.id.tvOptionInvite);
            TextView tvOptionLock = emptyView.findViewById(R.id.tvOptionLock);
            TextView tvOptionMute = emptyView.findViewById(R.id.tvOptionMute);
            View btnOptionCancel = emptyView.findViewById(R.id.btnOptionCancel);

            if (tvOptionLock != null) {
                tvOptionLock.setVisibility(isHost ? View.VISIBLE : View.GONE);
                tvOptionLock.setText(model.isClosed ? "Unlock" : "Lock");
            }

            if (tvOptionMute != null) {
                tvOptionMute.setVisibility(isHost ? View.VISIBLE : View.GONE);
                tvOptionMute.setText(model.isMuted ? "Unmute" : "Mute");
            }

            if (tvOptionInvite != null) {
                tvOptionInvite.setVisibility(isHost ? View.VISIBLE : View.GONE);
            }

            // 1. Take Seat option
            if (tvOptionOnMic != null) {
                tvOptionOnMic.setText("Take Seat");
                tvOptionOnMic.setOnClickListener(v -> {
                    if (model.isClosed && !isHost) {
                        Toast.makeText(this, "This seat is locked", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (model.index == 0 && !isHost) {
                        Toast.makeText(this, "Seat 1 is reserved for the Host!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int currentIndex = SeatManager.getInstance().findUserSeatIndex(userID);
                    if (currentIndex != -1 && currentIndex != model.index) {
                        SeatManager.getInstance().leaveSeat(currentIndex);
                    }
                    boolean success = SeatManager.getInstance().takeSeat(model.index, userID, userName);
                    if (!success) {
                        Toast.makeText(this, "This seat is already occupied!", Toast.LENGTH_SHORT).show();
                    } else {
                        ZegoManager.getInstance().startPublishing();
                        Toast.makeText(this, "You took Seat " + (model.index + 1) + "!", Toast.LENGTH_SHORT).show();
                    }
                    emptyDialog.dismiss();
                });
            }

            // 2. Invite
            if (tvOptionInvite != null) {
                tvOptionInvite.setOnClickListener(v -> {
                    emptyDialog.dismiss();
                    showRoomMembersDialog(model.index);
                });
            }

            // 3. Lock / Unlock
            if (tvOptionLock != null) {
                tvOptionLock.setOnClickListener(v -> {
                    boolean newClosedState = !model.isClosed;
                    SeatManager.getInstance().closeSeat(model.index, newClosedState);
                    Toast.makeText(this, newClosedState ? "Seat Locked" : "Seat Unlocked", Toast.LENGTH_SHORT).show();
                    emptyDialog.dismiss();
                });
            }

            // 4. Mute / Unmute
            if (tvOptionMute != null) {
                tvOptionMute.setOnClickListener(v -> {
                    boolean newMuteState = !model.isMuted;
                    SeatManager.getInstance().muteSeat(model.index, newMuteState);
                    Toast.makeText(this, newMuteState ? "Seat Muted" : "Seat Unmuted", Toast.LENGTH_SHORT).show();
                    emptyDialog.dismiss();
                });
            }

            // 5. Cancel
            if (btnOptionCancel != null) {
                btnOptionCancel.setOnClickListener(v -> emptyDialog.dismiss());
            }

            emptyDialog.show();
            return;
        }

        // Occupied Seat (Self or Other User)
        BottomSheetDialog profileDialog = new BottomSheetDialog(this);
        applyGlassyStyle(profileDialog);
        View profileView = getLayoutInflater().inflate(R.layout.dialog_user_profile_card, null, false);
        profileDialog.setContentView(profileView);

        ImageView ivProfileAvatar = profileView.findViewById(R.id.ivUserProfileAvatar);
        ImageView ivProfileFrame = profileView.findViewById(R.id.ivUserProfileFrame);
        SVGAImageView svgaProfileFrame = profileView.findViewById(R.id.svgaUserProfileFrame);
        TextView tvName = profileView.findViewById(R.id.tvUserName);
        TextView tvGenderBadge = profileView.findViewById(R.id.tvGenderBadge);
        TextView tvProfileId = profileView.findViewById(R.id.tvProfileId);
        TextView tvFollowersCount = profileView.findViewById(R.id.tvFollowersCount);
        TextView tvLikesBadge = profileView.findViewById(R.id.tvLikesBadge);

        View btnLikeUser = profileView.findViewById(R.id.btnLikeUser);
        ImageView ivLikeHeart = profileView.findViewById(R.id.ivLikeHeart);
        TextView tvLikesCount = profileView.findViewById(R.id.tvLikesCount);

        View btnMoreOptions = profileView.findViewById(R.id.btnMoreOptions);
        View btnActionFollow = profileView.findViewById(R.id.btnActionFollow);
        TextView tvActionFollowText = profileView.findViewById(R.id.tvActionFollowText);

        View btnActionAdd = profileView.findViewById(R.id.btnActionAdd);
        ImageView ivActionAddIcon = profileView.findViewById(R.id.ivActionAddIcon);
        TextView tvActionAddText = profileView.findViewById(R.id.tvActionAddText);


        View btnActionSend = profileView.findViewById(R.id.btnActionSend);

        String targetUid = model.userID != null ? model.userID : "";
        String initialName = (model.userName != null && !model.userName.isEmpty()) ? model.userName : "User";
        if (tvName != null) tvName.setText(initialName);

        if (ivProfileAvatar != null) {
            if (model.userAvatar != null && !model.userAvatar.isEmpty()) {
                Glide.with(this).load(model.userAvatar).placeholder(R.drawable.logo_placeholder).into(ivProfileAvatar);
            } else {
                Glide.with(this).load(R.drawable.logo_placeholder).into(ivProfileAvatar);
            }
        }

        DatabaseReference targetUserRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);
        targetUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String dbName = snapshot.child("name").getValue(String.class);
                    if (dbName != null && !dbName.isEmpty() && tvName != null) {
                        tvName.setText(dbName);
                    }

                    String dbAvatar = snapshot.child("avtar").getValue(String.class);
                    if (dbAvatar != null && !dbAvatar.isEmpty() && ivProfileAvatar != null) {
                        Glide.with(RoomChatActivity.this).load(dbAvatar).placeholder(R.drawable.logo_placeholder).into(ivProfileAvatar);
                    }

                    String pId = snapshot.child("profileId").getValue(String.class);
                    if (pId == null || pId.isEmpty()) pId = String.valueOf(100000 + Math.abs((long) targetUid.hashCode()) % 900000);
                    if (tvProfileId != null) tvProfileId.setText("ID: " + pId);

                    String followers = snapshot.child("Followers").getValue(String.class);
                    if (tvFollowersCount != null) tvFollowersCount.setText((followers != null ? followers : "0") + " followers");

                    String gender = snapshot.child("gender").getValue(String.class);
                    if (tvGenderBadge != null) {
                        if ("female".equalsIgnoreCase(gender)) {
                            tvGenderBadge.setText("♀ 23");
                            tvGenderBadge.setBackgroundResource(R.drawable.bg_send_button_glow);
                        } else {
                            tvGenderBadge.setText("♂ 24");
                            tvGenderBadge.setBackgroundResource(R.drawable.bg_glass_card);
                        }
                    }

                    // Frame Overlay
                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    FrameUtils.displayFrame(RoomChatActivity.this, equippedFrame, ivProfileFrame, svgaProfileFrame);

                    // Level & XP Progress
                    TextView tvLevelBadge = profileView.findViewById(R.id.tvLevelBadge);
                    ProgressBar pbLevelXp = profileView.findViewById(R.id.pbLevelXp);
                    TextView tvLevelXpText = profileView.findViewById(R.id.tvLevelXpText);

                    long coinsSpent = 0;
                    if (snapshot.child("coinsSpent").exists()) {
                        try {
                            coinsSpent = Long.parseLong(String.valueOf(snapshot.child("coinsSpent").getValue()));
                        } catch (Exception ignored) {}
                    } else if (snapshot.child("level").exists()) {
                        try {
                            long lvl = Long.parseLong(String.valueOf(snapshot.child("level").getValue()));
                            coinsSpent = Math.max(0, (lvl - 1) * LevelUtils.COINS_PER_LEVEL);
                        } catch (Exception ignored) {}
                    }

                    long level = LevelUtils.calculateLevel(coinsSpent);
                    int xpInLevel = LevelUtils.calculateCurrentXpInLevel(coinsSpent);

                    if (tvLevelBadge != null) tvLevelBadge.setText("Lv." + level);
                    if (pbLevelXp != null) pbLevelXp.setProgress(xpInLevel);
                    if (tvLevelXpText != null) tvLevelXpText.setText(xpInLevel + " / 100 XP (" + String.format("%,d", LevelUtils.calculateCoinsInCurrentLevel(coinsSpent)) + "/10,000 Coins)");
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Profile Like System
        DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("UserLikes").child(targetUid);
        DatabaseReference myLikeRef = userLikesRef.child(userID);

        userLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long likesCount = snapshot.getChildrenCount();
                if (tvLikesCount != null) tvLikesCount.setText(String.valueOf(likesCount));
                if (tvLikesBadge != null) tvLikesBadge.setText(likesCount + " Likes");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        myLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (ivLikeHeart != null) {
                    if (isLiked) {
                        ivLikeHeart.setColorFilter(Color.parseColor("#FF007A"));
                    } else {
                        ivLikeHeart.setColorFilter(Color.parseColor("#A0FFFFFF"));
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnLikeUser != null) {
            btnLikeUser.setOnClickListener(v -> {
                myLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            myLikeRef.removeValue();
                            Toast.makeText(RoomChatActivity.this, "Unliked " + initialName, Toast.LENGTH_SHORT).show();
                        } else {
                            myLikeRef.setValue(true);
                            if (ivLikeHeart != null) AnimationHelper.bounceAnimation(ivLikeHeart);
                            Toast.makeText(RoomChatActivity.this, "You liked " + initialName + "! ❤️", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        // 1. Follow Action Button
        if (btnActionFollow != null && targetUid.equals(userID)) {
            if (tvActionFollowText != null) tvActionFollowText.setText("Leave");
            btnActionFollow.setOnClickListener(v -> {
                SeatManager.getInstance().leaveSeat(model.index);
                ZegoManager.getInstance().stopPublishing();
                profileDialog.dismiss();
            });
        } else if (btnActionFollow != null) {
            DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                    .child(userID).child("following").child(targetUid);

            followRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (tvActionFollowText != null) tvActionFollowText.setText("Following");
                    } else {
                        if (tvActionFollowText != null) tvActionFollowText.setText("Follow");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            btnActionFollow.setOnClickListener(v -> {
                DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(userID).child("following").child(targetUid);
                DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(targetUid).child("followers").child(userID);

                followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            followingRef.removeValue();
                            followersRef.removeValue();
                            Toast.makeText(RoomChatActivity.this, "Unfollowed " + initialName, Toast.LENGTH_SHORT).show();
                        } else {
                            followingRef.setValue(true);
                            followersRef.setValue(true);
                            NotificationHelper.sendFollowNotification(targetUid);
                            Toast.makeText(RoomChatActivity.this, "Now following " + initialName + " ❤️", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        // 2. Add Friend Button (Handles Pending Request State)
        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(targetUid).child(userID);

        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (ivActionAddIcon != null) {
                        ivActionAddIcon.setImageResource(R.drawable.ic_check);
                        ivActionAddIcon.setColorFilter(Color.parseColor("#FFB703"));
                    }
                    if (tvActionAddText != null) {
                        tvActionAddText.setText("Requested");
                        tvActionAddText.setTextColor(Color.parseColor("#FFB703"));
                    }
                } else {
                    if (ivActionAddIcon != null) {
                        ivActionAddIcon.setImageResource(R.drawable.ic_person);
                        ivActionAddIcon.setColorFilter(Color.parseColor("#00FFC6"));
                    }
                    if (tvActionAddText != null) {
                        tvActionAddText.setText("Add");
                        tvActionAddText.setTextColor(Color.WHITE);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnActionAdd != null) {
            btnActionAdd.setOnClickListener(v -> {
                reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            reqRef.removeValue();
                            Toast.makeText(RoomChatActivity.this, "Friend request cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("senderId", userID);
                            map.put("senderName", userName != null ? userName : "User");
                            map.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                            map.put("timestamp", System.currentTimeMillis());
                            map.put("status", "pending");
                            reqRef.setValue(map);
                            Toast.makeText(RoomChatActivity.this, "Friend request sent to " + initialName + "! 🤝", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }


        // 3. Send Gift Button
        if (btnActionSend != null) {
            btnActionSend.setOnClickListener(v -> {
                profileDialog.dismiss();
                showGiftDialog(targetUid);
            });
        }

        // Host Seat Controls Row
        View vHostDivider = profileView.findViewById(R.id.vHostDivider);
        View llHostSeatControlsRow = profileView.findViewById(R.id.llHostSeatControlsRow);

        View btnHostMic = profileView.findViewById(R.id.btnHostMic);
        ImageView ivHostMicIcon = profileView.findViewById(R.id.ivHostMicIcon);

        View btnHostMute = profileView.findViewById(R.id.btnHostMute);
        ImageView ivHostMuteIcon = profileView.findViewById(R.id.ivHostMuteIcon);

        View btnHostLock = profileView.findViewById(R.id.btnHostLock);
        ImageView ivHostLockIcon = profileView.findViewById(R.id.ivHostLockIcon);

        View btnHostLeaveSeat = profileView.findViewById(R.id.btnHostLeaveSeat);

        View btnActionKick = profileView.findViewById(R.id.btnActionKick);

        boolean canControlSeat = isHost || targetUid.equals(userID);
        if (llHostSeatControlsRow != null) {
            llHostSeatControlsRow.setVisibility(canControlSeat ? View.VISIBLE : View.GONE);
        }
        if (vHostDivider != null) {
            vHostDivider.setVisibility(canControlSeat ? View.VISIBLE : View.GONE);
        }

        if (canControlSeat) {
            // Initial states
            if (ivHostMicIcon != null) {
                ivHostMicIcon.setImageResource(model.isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            }
            if (ivHostMuteIcon != null) {
                ivHostMuteIcon.setImageResource(model.isMuted ? R.drawable.ic_speaker_off : R.drawable.ic_speaker_on);
            }
            if (ivHostLockIcon != null) {
                ivHostLockIcon.setImageResource(model.isClosed ? R.drawable.ic_lock : R.drawable.ic_lock_open);
            }

            if (btnHostMic != null) {
                btnHostMic.setOnClickListener(v -> {
                    boolean newState = !model.isMicOn;
                    model.isMicOn = newState;
                    SeatManager.getInstance().updateMicStatus(model.index, newState);
                    if (ivHostMicIcon != null) {
                        ivHostMicIcon.setImageResource(newState ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
                    }
                    Toast.makeText(RoomChatActivity.this, initialName + " Mic " + (newState ? "Unmuted 🎙️" : "Muted 🔇"), Toast.LENGTH_SHORT).show();
                });
            }

            if (btnHostMute != null) {
                btnHostMute.setOnClickListener(v -> {
                    boolean newMuteState = !model.isMuted;
                    model.isMuted = newMuteState;
                    SeatManager.getInstance().muteSeat(model.index, newMuteState);
                    if (ivHostMuteIcon != null) {
                        ivHostMuteIcon.setImageResource(newMuteState ? R.drawable.ic_speaker_off : R.drawable.ic_speaker_on);
                    }
                    Toast.makeText(RoomChatActivity.this, (newMuteState ? "Muted " : "Unmuted ") + initialName, Toast.LENGTH_SHORT).show();
                });
            }

            if (btnHostLock != null) {
                btnHostLock.setOnClickListener(v -> {
                    boolean newClosedState = !model.isClosed;
                    model.isClosed = newClosedState;
                    SeatManager.getInstance().closeSeat(model.index, newClosedState);
                    if (ivHostLockIcon != null) {
                        ivHostLockIcon.setImageResource(newClosedState ? R.drawable.ic_lock : R.drawable.ic_lock_open);
                    }
                    Toast.makeText(RoomChatActivity.this, (newClosedState ? "Locked " : "Unlocked ") + "Seat " + (model.index + 1), Toast.LENGTH_SHORT).show();
                });
            }

            if (btnHostLeaveSeat != null) {
                btnHostLeaveSeat.setOnClickListener(v -> {
                    SeatManager.getInstance().leaveSeat(model.index);
                    String notice = "Host removed " + initialName + " from seat.";
                    ZegoManager.getInstance().sendInRoomTextMessage(notice);
                    Toast.makeText(RoomChatActivity.this, "Removed " + initialName + " from seat", Toast.LENGTH_SHORT).show();
                    profileDialog.dismiss();
                });
            }

            if (btnActionKick != null) {
                btnActionKick.setOnClickListener(v -> {
                    if (targetUid.equals(userID)) {
                        Toast.makeText(RoomChatActivity.this, "You cannot kick yourself!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (model != null && model.index >= 0) {
                        SeatManager.getInstance().leaveSeat(model.index);
                    }

                    DatabaseReference kickedRef = FirebaseDatabase.getInstance().getReference("rooms")
                            .child(roomID).child("kicked_users").child(targetUid);
                    Map<String, Object> kickData = new HashMap<>();
                    kickData.put("timestamp", System.currentTimeMillis());
                    kickData.put("kickedBy", userID);
                    kickData.put("userName", initialName);
                    kickedRef.setValue(kickData);

                    String notice = "Host kicked " + initialName + " out of the room!";
                    ZegoManager.getInstance().sendInRoomTextMessage(notice);

                    Toast.makeText(RoomChatActivity.this, "Kicked " + initialName + " out of the room!", Toast.LENGTH_SHORT).show();
                    profileDialog.dismiss();
                });
            }
        }

        // 5. More Options (...) -> Full Profile or Host Management
        if (btnMoreOptions != null) {
            btnMoreOptions.setOnClickListener(v -> {
                Intent intent = new Intent(this, UserDetailActivity.class);
                intent.putExtra("uid", targetUid);
                startActivity(intent);
                profileDialog.dismiss();
            });
        }

        profileDialog.show();
    }

    private void leaveRoom() {
        try {
            int mySeatIndex = SeatManager.getInstance().findUserSeatIndex(userID);
            if (mySeatIndex != -1) {
                SeatManager.getInstance().leaveSeat(mySeatIndex);
            }
            if (isHost) {
                SeatManager.getInstance().leaveSeat(0);
            }
            if (roomInfoRef != null && roomInfoValueEventListener != null) {
                roomInfoRef.removeEventListener(roomInfoValueEventListener);
            }
            if (roomSeatsRef != null && seatsValueEventListener != null) {
                roomSeatsRef.removeEventListener(seatsValueEventListener);
            }
            if (roomMessagesRef != null && roomMessagesChildEventListener != null) {
                roomMessagesRef.removeEventListener(roomMessagesChildEventListener);
            }
            if (roomMusicRef != null && roomMusicValueEventListener != null) {
                roomMusicRef.removeEventListener(roomMusicValueEventListener);
            }
            if (roomThemeRef != null && roomThemeValueEventListener != null) {
                roomThemeRef.removeEventListener(roomThemeValueEventListener);
            }
            if (roomEntriesRef != null && roomEntriesChildEventListener != null) {
                roomEntriesRef.removeEventListener(roomEntriesChildEventListener);
            }
            if (myKickRef != null && myKickListener != null) {
                myKickRef.removeEventListener(myKickListener);
            }
            unregisterOnlineUser();
            if (mySeatInviteRef != null && mySeatInviteListener != null) {
                mySeatInviteRef.removeEventListener(mySeatInviteListener);
            }
            ZegoManager.getInstance().stopMusic();
            ZegoManager.getInstance().logoutRoom();
            ZegoManager.getInstance().removeListener(zegoListener);
            SeatManager.getInstance().removeListener(seatListener);
            if (!isFinishing() && !isDestroyed()) {
                finish();
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error leaving room", e);
        }
    }

    private DatabaseReference myKickRef;
    private ValueEventListener myKickListener;

    private void listenForRoomKick() {
        if (userID == null || roomID == null) return;
        myKickRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users").child(userID);
        myKickListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isHost && !isFinishing() && !isDestroyed()) {
                    Toast.makeText(RoomChatActivity.this, "🚫 You were kicked out of the room by Host!", Toast.LENGTH_LONG).show();
                    if (myKickRef != null && myKickListener != null) {
                        myKickRef.removeEventListener(myKickListener);
                    }
                    leaveRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        myKickRef.addValueEventListener(myKickListener);
    }

    private void checkInitialRoomKick() {
        if (isHost || userID == null || roomID == null) return;
        DatabaseReference checkKickRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users").child(userID);
        checkKickRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isFinishing() && !isDestroyed()) {
                    Toast.makeText(RoomChatActivity.this, "🚫 You have been kicked from this room by Host!", Toast.LENGTH_LONG).show();
                    leaveRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupFirebaseListeners() {
        setupRoomInfoListener();
        setupRoomGiftListener();
        setupRoomSeatsListener();
        setupRoomMessagesListener();
        setupRoomMusicListener();
        setupRoomThemeListener();
        setupRoomEntriesListener();
        listenForRoomKick();
        checkInitialRoomKick();
        registerOnlineUser();
        setupSeatInviteListener();
    }

    private void setupRoomInfoListener() {
        roomInfoRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        roomInfoValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String newName = snapshot.child("room_name").getValue(String.class);
                    String newImg = snapshot.child("img").getValue(String.class);

                    if (newName != null && !newName.trim().isEmpty()) {
                        roomNameLabel = newName;
                        TextView tvRoomName = findViewById(R.id.tvRoomName);
                        if (tvRoomName != null) {
                            tvRoomName.setText(newName);
                        }
                        if (backgroundView != null) {
                            backgroundView.setRoomName(newName);
                        }
                    }

                    if (newImg != null && !newImg.trim().isEmpty()) {
                        roomImg = newImg;
                        ImageView ivRoomAvatar = findViewById(R.id.ivRoomAvatar);
                        if (ivRoomAvatar != null) {
                            Glide.with(RoomChatActivity.this)
                                    .load(newImg)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .error(R.drawable.logo_placeholder)
                                    .into(ivRoomAvatar);
                        }
                        if (backgroundView != null) {
                            backgroundView.setBackgroundImage(newImg);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomInfoRef.addValueEventListener(roomInfoValueEventListener);
    }

    private void setupRoomGiftListener() {
        roomGiftsRef = FirebaseDatabase.getInstance().getReference("room_gifts").child(roomID);
        giftsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String senderName = snapshot.child("senderName").getValue(String.class);
                    String giftName = snapshot.child("giftName").getValue(String.class);
                    Long iconResLong = snapshot.child("iconRes").getValue(Long.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 3000) return;

                    if (senderName != null && giftName != null) {
                        String senderAvatar = snapshot.child("senderAvatar").getValue(String.class);
                        int iconRes = (iconResLong != null) ? iconResLong.intValue() : R.drawable.gift_icon;
                        if (notificationAnimator != null) {
                            notificationAnimator.showNotification(senderName, "sent " + giftName, iconRes, senderAvatar);
                        }

                        // Broadcast Golden Banner SVGA at top between seats/profile for all users in the room!
                        showGoldenGiftBanner(senderName, giftName);

                        String giftSvga = snapshot.child("giftSvga").getValue(String.class);
                        if (giftSvga != null) {
                            playSvgaAnimation(giftSvga);
                        } else if (giftName.contains("Heart")) {
                            playSvgaAnimation("gift/aladdin.svga");
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

    private void setupRoomSeatsListener() {
        roomSeatsRef = FirebaseDatabase.getInstance().getReference("room_seats").child(roomID);
        seatsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    List<SeatModel> updatedSeats = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        SeatModel seat = child.getValue(SeatModel.class);
                        if (seat != null) {
                            String hostUid = SeatManager.getInstance().getHostUserID();
                            if (seat.index == 0 && hostUid != null && !hostUid.isEmpty() && !seat.userID.equals(hostUid)) {
                                seat.clear();
                            }
                            if (!seat.isEmpty() && !seat.userID.equals(hostUid) && !seat.userID.equals(userID)) {
                                if (ZegoManager.getInstance().getRoomUserCount() > 1 && !ZegoManager.getInstance().isUserInRoom(seat.userID)) {
                                    seat.clear();
                                }
                            }
                            updatedSeats.add(seat);
                        }
                    }
                    if (!updatedSeats.isEmpty()) {
                        SeatManager.getInstance().setSeatsFromExternal(updatedSeats);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomSeatsRef.addValueEventListener(seatsValueEventListener);
    }

    private void setupRoomMessagesListener() {
        roomMessagesRef = FirebaseDatabase.getInstance().getReference("room_messages").child(roomID);
        roomMessagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String senderId = snapshot.child("senderId").getValue(String.class);
                    String senderName = snapshot.child("senderName").getValue(String.class);
                    String senderAvatar = snapshot.child("senderAvatar").getValue(String.class);
                    if (senderAvatar == null || senderAvatar.trim().isEmpty()) {
                        senderAvatar = snapshot.child("avtar").getValue(String.class);
                    }
                    String text = snapshot.child("text").getValue(String.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 5000) return;

                    if (text != null && !text.trim().isEmpty()) {
                        ChatMessage msg = new ChatMessage();
                        msg.setSenderId(senderId != null ? senderId : "");
                        msg.setSenderAvatar(senderAvatar);
                        String name = senderName != null ? senderName : "User";
                        msg.setMessage(name + " : " + text);
                        msg.setTimestamp(ts != null ? ts : System.currentTimeMillis());

                        if (chatAdapter != null) {
                            chatAdapter.addAutoExpiringMessage(msg, 10000);
                        }
                        if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
                            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomMessagesRef.addChildEventListener(roomMessagesChildEventListener);
    }

    private String currentPlayingSongName = "";

    private void setupRoomMusicListener() {
        roomMusicRef = FirebaseDatabase.getInstance().getReference("room_music").child(roomID);
        roomMusicValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                View layoutMusicWidget = findViewById(R.id.layoutMusicWidget);
                TextView tvMusicTitle = findViewById(R.id.tvMusicTitle);
                View btnWidgetMusicPause = findViewById(R.id.btnWidgetMusicPause);

                if (snapshot.exists()) {
                    String songName = snapshot.child("songName").getValue(String.class);
                    Boolean isPlayingObj = snapshot.child("isPlaying").getValue(Boolean.class);
                    boolean isPlaying = Boolean.TRUE.equals(isPlayingObj);

                    if (isPlaying && songName != null && !songName.isEmpty()) {
                        currentPlayingSongName = songName;
                        if (layoutMusicWidget != null) {
                            layoutMusicWidget.setVisibility(View.VISIBLE);
                            layoutMusicWidget.setOnClickListener(v -> showMusicControlSheet());
                        }
                        if (tvMusicTitle != null) tvMusicTitle.setText(songName);

                        if (btnWidgetMusicPause != null) {
                            btnWidgetMusicPause.setVisibility(isHost ? View.VISIBLE : View.GONE);
                            btnWidgetMusicPause.setOnClickListener(v -> {
                                ZegoManager.getInstance().stopMusic();
                                syncMusicStateToFirebase("", false);
                            });
                        }
                    } else {
                        currentPlayingSongName = "";
                        if (layoutMusicWidget != null) layoutMusicWidget.setVisibility(View.GONE);
                    }
                } else {
                    currentPlayingSongName = "";
                    if (layoutMusicWidget != null) layoutMusicWidget.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomMusicRef.addValueEventListener(roomMusicValueEventListener);
    }

    private void syncThemeToFirebase(String type, String value) {
        if (roomID == null || roomID.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("room_themes").child(roomID);
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("themeValue", value);
        map.put("timestamp", System.currentTimeMillis());
        ref.setValue(map);
    }

    private void setupRoomThemeListener() {
        roomThemeRef = FirebaseDatabase.getInstance().getReference("room_themes").child(roomID);
        roomThemeValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue(String.class);
                    String value = snapshot.child("themeValue").getValue(String.class);

                    if (type != null && value != null && backgroundView != null) {
                        if ("VIDEO".equals(type)) {
                            backgroundView.setThemeVideo(value);
                        } else if ("IMAGE".equals(type)) {
                            backgroundView.setThemeImage(R.drawable.bg_main_gradient);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomThemeRef.addValueEventListener(roomThemeValueEventListener);
    }

    private void setupRoomEntriesListener() {
        roomEntriesRef = FirebaseDatabase.getInstance().getReference("room_entries").child(roomID);
        roomEntriesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String senderId = snapshot.child("userId").getValue(String.class);
                    String senderName = snapshot.child("userName").getValue(String.class);
                    String entranceSvga = snapshot.child("entranceSvga").getValue(String.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 3000) return;

                    String displayName = (senderName != null && !senderName.trim().isEmpty()) ? senderName : "User";

                    // Add room chat entrance message
                    ChatMessage entryMsg = new ChatMessage();
                    entryMsg.setSenderId(senderId != null ? senderId : "");
                    entryMsg.setSenderAvatar(SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                    entryMsg.setMessage(displayName + " Enter the room");
                    entryMsg.setTimestamp(ts != null ? ts : System.currentTimeMillis());

                    if (chatAdapter != null) {
                        chatAdapter.addAutoExpiringMessage(entryMsg, 5000);
                    }
                    if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
                        rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }

                    // Show Slide-in notification banner
                    if (notificationAnimator != null) {
                        String userAvatar = SessionManager.getInstance(RoomChatActivity.this).getAvatar();
                        notificationAnimator.showNotification(displayName, "entered the room", 0, userAvatar);
                    }

                    // Show User Entry Banner
                    showUserEntryBanner(displayName);

                    // If user equipped a custom entrance SVGA (e.g. Golden Super Car, Red Super Car, Anime Man, Toyota Car)
                    if (entranceSvga != null && !entranceSvga.trim().isEmpty()) {
                        playSvgaAnimation(entranceSvga);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomEntriesRef.addChildEventListener(roomEntriesChildEventListener);
    }

    private void broadcastUserEntry() {
        if (userID == null || userID.trim().isEmpty() || roomID == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String equippedEntranceId = snapshot.child("equipped_entrance").getValue(String.class);
                String entranceSvga = FrameUtils.getEntranceSvgaPath(equippedEntranceId);

                Map<String, Object> entryMap = new HashMap<>();
                entryMap.put("userId", userID);
                entryMap.put("userName", userName != null ? userName : "User");
                if (entranceSvga != null) {
                    entryMap.put("entranceSvga", entranceSvga);
                }
                if (equippedEntranceId != null) {
                    entryMap.put("entranceId", equippedEntranceId);
                }
                entryMap.put("timestamp", System.currentTimeMillis());

                FirebaseDatabase.getInstance().getReference("room_entries")
                        .child(roomID).push().setValue(entryMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void applyGlassyStyle(BottomSheetDialog dialog) {
        if (dialog == null) return;
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }
        });
    }

    private void showMusicControlSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_music_control, null, false);
        if (dialogView == null) return;

        TextView tvSongTitle = dialogView.findViewById(R.id.tvMusicDialogSongTitle);
        TextView tvVolumeText = dialogView.findViewById(R.id.tvMusicVolumeText);
        SeekBar seekBarVolume = dialogView.findViewById(R.id.seekBarMusicVolume);
        View btnSelectSong = dialogView.findViewById(R.id.btnDialogSelectSong);
        View btnStopMusic = dialogView.findViewById(R.id.btnDialogStopMusic);

        boolean isPlaying = ZegoManager.getInstance().isMusicPlaying();
        if (tvSongTitle != null) {
            tvSongTitle.setText(isPlaying && !currentPlayingSongName.isEmpty() ? "Playing: " + currentPlayingSongName : "Room Music Control");
        }

        int currentVol = ZegoManager.getInstance().getMusicVolume();
        if (seekBarVolume != null) {
            seekBarVolume.setProgress(currentVol);
            if (tvVolumeText != null) {
                tvVolumeText.setText(currentVol + "%");
            }
            seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ZegoManager.getInstance().setMusicVolume(progress);
                    if (tvVolumeText != null) {
                        tvVolumeText.setText(progress + "%");
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnSelectSong != null) {
            btnSelectSong.setVisibility(isHost ? View.VISIBLE : View.GONE);
            btnSelectSong.setOnClickListener(v -> {
                dialog.dismiss();
                openAudioFilePicker();
            });
        }

        if (btnStopMusic != null) {
            btnStopMusic.setVisibility(isHost ? View.VISIBLE : View.GONE);
            btnStopMusic.setOnClickListener(v -> {
                ZegoManager.getInstance().stopMusic();
                syncMusicStateToFirebase("", false);
                dialog.dismiss();
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void openAudioFilePicker() {
        if (!isHost) {
            Toast.makeText(this, "Only room owner/host can select and play live music!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        audioPickerLauncher.launch(intent);
    }

    private void handleSelectedAudioUri(Uri uri) {
        try {
            String fileName = getFileNameFromUri(uri);

            File cacheFile = new File(getCacheDir(), "room_music_" + System.currentTimeMillis() + ".mp3");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((in != null) && (bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            if (cacheFile.exists()) {
                String audioPath = cacheFile.getAbsolutePath();
                ZegoManager.getInstance().playMusicResource(audioPath, fileName);
                syncMusicStateToFirebase(fileName, true);
                Toast.makeText(this, "🎵 Playing: " + fileName + " (Live to room)", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error loading audio file", e);
            Toast.makeText(this, "Failed to load audio file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "Selected Song.mp3";
    }

    private void syncMusicStateToFirebase(String songName, boolean isPlaying) {
        if (roomID == null || roomID.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("room_music").child(roomID);
        Map<String, Object> map = new HashMap<>();
        map.put("songName", songName != null ? songName : "");
        map.put("isPlaying", isPlaying);
        map.put("timestamp", System.currentTimeMillis());
        ref.setValue(map);
    }

    private void showRoomGameDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
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

    private void showMorePanelDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_more_panel, null, false);
        if (dialogView == null) return;

        TextView tvMemberSubtitle = dialogView.findViewById(R.id.tvMemberSubtitle);
        TextView tvSeatsSubtitle = dialogView.findViewById(R.id.tvSeatsSubtitle);

        View btnOptionTheme = dialogView.findViewById(R.id.btnOptionTheme);
        View btnOptionMembers = dialogView.findViewById(R.id.btnOptionMembers);
        View btnOptionSeats = dialogView.findViewById(R.id.btnOptionSeats);
        View btnOptionMusic = dialogView.findViewById(R.id.btnOptionMusic);

        List<SeatModel> currentSeats = SeatManager.getInstance().getSeats();
        int occupiedCount = 0;
        for (SeatModel seat : currentSeats) {
            if (seat != null && !seat.isEmpty()) {
                occupiedCount++;
            }
        }

        int totalMembersCount = Math.max(occupiedCount, 1);

        if (tvMemberSubtitle != null) {
            tvMemberSubtitle.setText(totalMembersCount + " Member" + (totalMembersCount > 1 ? "s" : "") + " currently in room");
        }

        int totalSeatsCount = SeatManager.getInstance().getTotalSeats();
        if (tvSeatsSubtitle != null) {
            tvSeatsSubtitle.setText(totalSeatsCount + " Total Seats • " + occupiedCount + " Occupied, " + Math.max(0, totalSeatsCount - occupiedCount) + " Free");
        }

        // Option 0: Edit Room Title & Cover Image
        View btnOptionRoomSettings = dialogView.findViewById(R.id.btnOptionRoomSettings);
        if (btnOptionRoomSettings != null) {
            btnOptionRoomSettings.setVisibility(isHost ? View.VISIBLE : View.GONE);
            btnOptionRoomSettings.setOnClickListener(v -> {
                dialog.dismiss();
                if (!isHost) {
                    Toast.makeText(this, "Only room host can edit room title and image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(RoomChatActivity.this, RoomSettingsActivity.class);
                intent.putExtra("roomID", roomID);
                intent.putExtra("hostUid", userID);
                intent.putExtra("roomName", roomNameLabel);
                intent.putExtra("roomImg", roomImg);
                startActivity(intent);
            });
        }

        // Option 1: Theme Selection
        if (btnOptionTheme != null) {
            btnOptionTheme.setOnClickListener(v -> {
                dialog.dismiss();
                showThemeSelectionDialog();
            });
        }

        // Option 2: Members & Audience
        if (btnOptionMembers != null) {
            btnOptionMembers.setOnClickListener(v -> {
                dialog.dismiss();
                showRoomMembersDialog();
            });
        }

        // Option 3: Seats Management
        if (btnOptionSeats != null) {
            btnOptionSeats.setOnClickListener(v -> {
                dialog.dismiss();
                showSeatCapacityDialog();
            });
        }

        // Option 4: Room Music System
        if (btnOptionMusic != null) {
            btnOptionMusic.setOnClickListener(v -> {
                dialog.dismiss();
                showMusicControlSheet();
            });
        }

        // Option 5: Frame Store
        View btnOptionStore = dialogView.findViewById(R.id.btnOptionStore);
        if (btnOptionStore != null) {
            btnOptionStore.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(RoomChatActivity.this, StoreActivity.class));
            });
        }

        // Option 6: Edit Profile & Avatar
        View btnOptionProfile = dialogView.findViewById(R.id.btnOptionProfile);
        if (btnOptionProfile != null) {
            btnOptionProfile.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(RoomChatActivity.this, EditProfileActivity.class));
            });
        }

        // Option 7: Minimize Room
        View btnOptionMinimize = dialogView.findViewById(R.id.btnOptionMinimize);
        if (btnOptionMinimize != null) {
            btnOptionMinimize.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(RoomChatActivity.this, "Room running in background 🎙️", Toast.LENGTH_SHORT).show();
                moveTaskToBack(true);
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showExitOptionsDialog() {
        if (isFinishing() || isDestroyed()) return;
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_room, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setDimAmount(0.65f);
        }

        View btnMinimize = dialogView.findViewById(R.id.btnDialogMinimize);
        View btnExit = dialogView.findViewById(R.id.btnDialogExit);

        if (btnMinimize != null) {
            btnMinimize.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(this, "Room running in background 🎙️", Toast.LENGTH_SHORT).show();
                moveTaskToBack(true);
            });
        }

        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                dialog.dismiss();
                leaveRoom();
            });
        }

        dialog.show();
    }

    private void showSeatCapacityDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_capacity, null, false);
        if (dialogView == null) return;

        View cardOption8 = dialogView.findViewById(R.id.cardOption8Seats);
        View cardOption16 = dialogView.findViewById(R.id.cardOption16Seats);
        View cardOption24 = dialogView.findViewById(R.id.cardOption24Seats);

        View.OnClickListener select8 = v -> {
            if (!isHost) {
                Toast.makeText(this, "Only room owner/host can change seat capacity!", Toast.LENGTH_SHORT).show();
                return;
            }
            SeatManager.getInstance().setTotalSeats(8);
            Toast.makeText(this, "Room Seat Capacity updated to 8 Seats Grid!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        };

        View.OnClickListener select16 = v -> {
            if (!isHost) {
                Toast.makeText(this, "Only room owner/host can change seat capacity!", Toast.LENGTH_SHORT).show();
                return;
            }
            SeatManager.getInstance().setTotalSeats(16);
            Toast.makeText(this, "Room Seat Capacity updated to 16 Seats Grid!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        };

        View.OnClickListener select24 = v -> {
            if (!isHost) {
                Toast.makeText(this, "Only room owner/host can change seat capacity!", Toast.LENGTH_SHORT).show();
                return;
            }
            SeatManager.getInstance().setTotalSeats(24);
            Toast.makeText(this, "Room Seat Capacity updated to 24 Seats Grid!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        };

        if (cardOption8 != null) cardOption8.setOnClickListener(select8);
        if (cardOption16 != null) cardOption16.setOnClickListener(select16);
        if (cardOption24 != null) cardOption24.setOnClickListener(select24);

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void registerOnlineUser() {
        if (roomID == null || userID == null) return;
        roomOnlineUsersRef = FirebaseDatabase.getInstance()
                .getReference("room_users")
                .child(roomID)
                .child(userID);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userID);
        map.put("userName", userName != null ? userName : "User");
        String avatar = SessionManager.getInstance(this).getAvatar();
        map.put("userAvatar", avatar != null ? avatar : "");
        map.put("isHost", isHost);
        map.put("joinedAt", System.currentTimeMillis());

        roomOnlineUsersRef.setValue(map);
        roomOnlineUsersRef.onDisconnect().removeValue();
    }

    private void unregisterOnlineUser() {
        if (roomOnlineUsersRef != null) {
            roomOnlineUsersRef.removeValue();
        } else if (roomID != null && userID != null) {
            FirebaseDatabase.getInstance()
                    .getReference("room_users")
                    .child(roomID)
                    .child(userID)
                    .removeValue();
        }
    }

    public void sendSeatInvitation(String targetUserId, String targetUserName, int seatIndex) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) return;

        DatabaseReference inviteRef = FirebaseDatabase.getInstance()
                .getReference("room_seat_invitations")
                .child(roomID)
                .child(targetUserId);

        Map<String, Object> map = new HashMap<>();
        map.put("invitationId", inviteRef.push().getKey());
        map.put("roomId", roomID);
        map.put("roomName", roomNameLabel != null ? roomNameLabel : "Audio Room");
        map.put("hostId", userID);
        map.put("hostName", userName != null ? userName : "Host");
        map.put("targetUserId", targetUserId);
        map.put("targetUserName", targetUserName);
        map.put("seatIndex", seatIndex);
        map.put("status", "PENDING");
        map.put("timestamp", System.currentTimeMillis());

        inviteRef.setValue(map).addOnSuccessListener(aVoid -> {
            Toast.makeText(RoomChatActivity.this, "Invitation sent to " + targetUserName + " 🎙️", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(RoomChatActivity.this, "Failed to send invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSeatInviteListener() {
        if (roomID == null || userID == null) return;
        mySeatInviteRef = FirebaseDatabase.getInstance()
                .getReference("room_seat_invitations")
                .child(roomID)
                .child(userID);

        mySeatInviteListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (!snapshot.exists()) return;

                String status = snapshot.child("status").getValue(String.class);
                if (!"PENDING".equalsIgnoreCase(status)) return;

                Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                if (timestamp != null && (System.currentTimeMillis() - timestamp > 120000)) {
                    return;
                }

                String hostName = snapshot.child("hostName").getValue(String.class);
                Integer seatIndex = snapshot.child("seatIndex").getValue(Integer.class);
                int targetSeat = seatIndex != null ? seatIndex : -1;

                if (SeatManager.getInstance().findUserSeatIndex(userID) != -1) {
                    return;
                }

                showSeatInvitationDialog(hostName != null ? hostName : "Host", targetSeat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mySeatInviteRef.addValueEventListener(mySeatInviteListener);
    }

    private void showSeatInvitationDialog(String hostName, int targetSeat) {
        if (currentInviteDialog != null && currentInviteDialog.isShowing()) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_invitation, null, false);
        if (dialogView == null) return;

        TextView tvInviteTitle = dialogView.findViewById(R.id.tvInviteTitle);
        TextView tvInviteMessage = dialogView.findViewById(R.id.tvInviteMessage);
        View btnAccept = dialogView.findViewById(R.id.btnAcceptInvitation);
        View btnDecline = dialogView.findViewById(R.id.btnDeclineInvitation);

        String seatMsg = (targetSeat >= 0) ? ("Seat " + (targetSeat + 1)) : "a Seat";
        if (tvInviteTitle != null) {
            tvInviteTitle.setText("Seat Invitation");
        }
        if (tvInviteMessage != null) {
            tvInviteMessage.setText("Host " + hostName + " has invited you to take " + seatMsg + " in the room! Would you like to join the mic?");
        }

        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                dialog.dismiss();
                acceptSeatInvitation(targetSeat);
            });
        }

        if (btnDecline != null) {
            btnDecline.setOnClickListener(v -> {
                dialog.dismiss();
                declineSeatInvitation();
            });
        }

        dialog.setCancelable(false);
        currentInviteDialog = dialog;
        dialog.show();
    }

    private void acceptSeatInvitation(int targetSeat) {
        if (mySeatInviteRef != null) {
            mySeatInviteRef.child("status").setValue("ACCEPTED");
        }

        int seatToTake = targetSeat;
        List<SeatModel> seats = SeatManager.getInstance().getSeats();
        if (seatToTake < 0 || seatToTake >= seats.size() || !seats.get(seatToTake).isEmpty() || seats.get(seatToTake).isClosed) {
            seatToTake = -1;
            for (SeatModel seat : seats) {
                if (seat != null && seat.isEmpty() && !seat.isClosed) {
                    if (seat.index == 0 && !isHost) continue;
                    seatToTake = seat.index;
                    break;
                }
            }
        }

        if (seatToTake == -1) {
            Toast.makeText(this, "Sorry, all seats are currently full or locked!", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentSeat = SeatManager.getInstance().findUserSeatIndex(userID);
        if (currentSeat != -1 && currentSeat != seatToTake) {
            SeatManager.getInstance().leaveSeat(currentSeat);
        }

        String myAvatar = SessionManager.getInstance(this).getAvatar();
        boolean success = SeatManager.getInstance().takeSeat(seatToTake, userID, userName, myAvatar);
        if (success) {
            ZegoManager.getInstance().startPublishing();
            Toast.makeText(this, "You accepted the invitation and took Seat " + (seatToTake + 1) + "! 🎙️", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not take seat. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void declineSeatInvitation() {
        if (mySeatInviteRef != null) {
            mySeatInviteRef.child("status").setValue("DECLINED");
        }
        Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
    }

    private void showRoomMembersDialog() {
        showRoomMembersDialog(-1);
    }

    private void showRoomMembersDialog(int targetSeatIndex) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_members, null, false);
        if (dialogView == null) return;

        TextView tvMembersCountBadge = dialogView.findViewById(R.id.tvMembersCountBadge);
        TextView tvMembersDialogTitle = dialogView.findViewById(R.id.tvMembersDialogTitle);
        RecyclerView rvMembers = dialogView.findViewById(R.id.rvRoomMembers);

        List<User> activeUsers = new ArrayList<>();
        Map<String, User> userMap = new HashMap<>();

        List<SeatModel> seats = SeatManager.getInstance().getSeats();
        for (SeatModel seat : seats) {
            if (seat != null && !seat.isEmpty()) {
                User u = new User();
                u.setUserId(seat.userID);
                u.setUserName(seat.userName != null && !seat.userName.isEmpty() ? seat.userName : "Member");
                u.setUserIcon(seat.userAvatar);
                userMap.put(seat.userID, u);
                activeUsers.add(u);
            }
        }

        DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference("room_users").child(roomID);
        onlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.child("userId").getValue(String.class);
                    if (uid == null) uid = child.getKey();
                    if (uid != null && !userMap.containsKey(uid)) {
                        String name = child.child("userName").getValue(String.class);
                        String avatar = child.child("userAvatar").getValue(String.class);
                        Boolean hostFlag = child.child("isHost").getValue(Boolean.class);

                        User u = new User();
                        u.setUserId(uid);
                        u.setUserName(name != null ? name : "Member");
                        u.setUserIcon(avatar);
                        if (hostFlag != null) u.setHost(hostFlag);
                        userMap.put(uid, u);
                        activeUsers.add(u);
                    }
                }

                if (activeUsers.isEmpty()) {
                    User u = new User();
                    u.setUserId(userID);
                    u.setUserName(userName != null ? userName : "Host");
                    activeUsers.add(u);
                }

                int totalCount = activeUsers.size();
                if (tvMembersCountBadge != null) {
                    tvMembersCountBadge.setText(totalCount + " Online");
                }
                if (tvMembersDialogTitle != null) {
                    tvMembersDialogTitle.setText("Connected Room Members (" + totalCount + ")");
                }

                if (rvMembers != null) {
                    rvMembers.setLayoutManager(new LinearLayoutManager(RoomChatActivity.this));
                    rvMembers.setAdapter(new AudienceAdapter(activeUsers, isHost, targetUser -> {
                        sendSeatInvitation(targetUser.getUserId(), targetUser.getUserName(), targetSeatIndex);
                        dialog.dismiss();
                    }));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showThemeSelectionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_theme_selection, null, false);
        if (dialogView == null) return;

        // Theme 1: Cyber Neon Party (Free - 0 Coins)
        View cardCyber = dialogView.findViewById(R.id.cardThemeCyber);
        View tvUseCyber = dialogView.findViewById(R.id.tvUseCyber);
        View imgPosterTheme1 = dialogView.findViewById(R.id.imgPosterTheme1);
        View.OnClickListener applyCyber = v -> {
            if (backgroundView != null) {
                backgroundView.setThemeVideo("theme/theme1.mp4");
            }
            syncThemeToFirebase("VIDEO", "theme/theme1.mp4");
            dialog.dismiss();
        };
        if (cardCyber != null) cardCyber.setOnClickListener(applyCyber);
        if (tvUseCyber != null) tvUseCyber.setOnClickListener(applyCyber);
        if (imgPosterTheme1 != null) imgPosterTheme1.setOnClickListener(applyCyber);

        // Theme 2: Galaxy (50 Coins)
        View cardGalaxy = dialogView.findViewById(R.id.cardThemeGalaxy);
        View tvUseGalaxy = dialogView.findViewById(R.id.tvUseGalaxy);
        View imgPosterTheme2 = dialogView.findViewById(R.id.imgPosterTheme2);
        View.OnClickListener applyTheme2 = v -> {
            WalletManager.spendCoinsForGift(userID, null, 50, "Theme: Galaxy", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    if (backgroundView != null) {
                        backgroundView.setThemeVideo("theme/theme2.mp4");
                    }
                    syncThemeToFirebase("VIDEO", "theme/theme2.mp4");
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                }
            });
        };
        if (cardGalaxy != null) cardGalaxy.setOnClickListener(applyTheme2);
        if (tvUseGalaxy != null) tvUseGalaxy.setOnClickListener(applyTheme2);
        if (imgPosterTheme2 != null) imgPosterTheme2.setOnClickListener(applyTheme2);

        // Theme 3: Sunset (100 Coins)
        View cardSunset = dialogView.findViewById(R.id.cardThemeSunset);
        View tvUseSunset = dialogView.findViewById(R.id.tvUseSunset);
        View imgPosterTheme3 = dialogView.findViewById(R.id.imgPosterTheme3);
        View.OnClickListener applyTheme3 = v -> {
            WalletManager.spendCoinsForGift(userID, null, 100, "Theme: Sunset", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    if (backgroundView != null) {
                        backgroundView.setThemeVideo("theme/theme3.mp4");
                    }
                    syncThemeToFirebase("VIDEO", "theme/theme3.mp4");
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                }
            });
        };
        if (cardSunset != null) cardSunset.setOnClickListener(applyTheme3);
        if (tvUseSunset != null) tvUseSunset.setOnClickListener(applyTheme3);
        if (imgPosterTheme3 != null) imgPosterTheme3.setOnClickListener(applyTheme3);

        // Theme 4: Aurora Glass (150 Coins)
        View cardAurora = dialogView.findViewById(R.id.cardThemeAurora);
        View tvUseAurora = dialogView.findViewById(R.id.tvUseAurora);
        View.OnClickListener applyAurora = v -> {
            WalletManager.spendCoinsForGift(userID, null, 150, "Theme: Aurora", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    if (backgroundView != null) {
                        backgroundView.setThemeImage(R.drawable.bg_main_gradient);
                    }
                    syncThemeToFirebase("IMAGE", "bg_main_gradient");
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                }
            });
        };
        if (cardAurora != null) cardAurora.setOnClickListener(applyAurora);
        if (tvUseAurora != null) tvUseAurora.setOnClickListener(applyAurora);

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showCommentInputDialog() {
        showCommentInputDialogWithText(null);
    }

    private void showCommentInputDialogWithText(String initialText) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comment_input, null, false);
        if (dialogView == null) return;

        EditText etInput = dialogView.findViewById(R.id.etCommentInput);
        if (etInput != null && initialText != null && !initialText.trim().isEmpty()) {
            etInput.setText(initialText);
            etInput.setSelection(initialText.length());
        }

        View btnSend = dialogView.findViewById(R.id.btnSendComment);

        // Quick Phrase Chips
        int[] chipIds = {R.id.chipHi, R.id.chipWelcome, R.id.chipWave, R.id.chipRose, R.id.chipClap};
        for (int id : chipIds) {
            View chip = dialogView.findViewById(id);
            if (chip instanceof TextView) {
                chip.setOnClickListener(v -> {
                    String chipText = ((TextView) chip).getText().toString();
                    sendUserComment(chipText);
                    dialog.dismiss();
                });
            }
        }

        // Send Button Click
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (etInput != null) {
                    String text = etInput.getText().toString().trim();
                    if (!text.isEmpty()) {
                        sendUserComment(text);
                        dialog.dismiss();
                    }
                }
            });
        }

        // Keyboard Action Send
        if (etInput != null) {
            etInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String text = etInput.getText().toString().trim();
                    if (!text.isEmpty()) {
                        sendUserComment(text);
                        dialog.dismiss();
                    }
                    return true;
                }
                return false;
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void sendUserComment(String text) {
        if (text == null || text.trim().isEmpty()) return;

        if (roomMessagesRef != null) {
            String myAvatar = SessionManager.getInstance(this).getAvatar();
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("senderId", userID != null ? userID : "");
            msgMap.put("senderName", userName != null ? userName : "User");
            msgMap.put("senderAvatar", myAvatar != null ? myAvatar : "");
            msgMap.put("text", text.trim());
            msgMap.put("timestamp", System.currentTimeMillis());
            roomMessagesRef.push().setValue(msgMap);
        }
    }

    private void showMessageCenterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_message_center, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);
            dialog.show();
        }
    }

    private String selectedGiftSvga = "gift/aladdin.svga";

    private void showGiftDialog() {
        showGiftDialog(null);
    }

    private void showGiftDialog(String targetUid) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_gift_store, null, false);
        if (dialogView == null) return;

        RecyclerView rvGiftRecipients = dialogView.findViewById(R.id.rvGiftRecipients);
        RecyclerView rvGifts = dialogView.findViewById(R.id.rvGifts);
        View btnSendAction = dialogView.findViewById(R.id.btnSendGiftAction);
        TabLayout tabCategory = dialogView.findViewById(R.id.tabCategoryGifts);
        TextView tvGiftDialogCoins = dialogView.findViewById(R.id.tvGiftDialogCoins);

        if (tvGiftDialogCoins != null) {
            WalletManager.getUserCoins(userID, balance -> runOnUiThread(() ->
                    tvGiftDialogCoins.setText(String.valueOf(balance))
            ));
        }

        // Setup Recipients List (Room members / Mic seats)
        List<GiftRecipientModel> recipientList = new ArrayList<>();

        // "ALL" option
        GiftRecipientModel allItem = new GiftRecipientModel("", "All", "", "ALL", true, true);
        recipientList.add(allItem);

        List<SeatModel> seats = SeatManager.getInstance().getSeats();
        int occupiedCount = 0;
        if (seats != null) {
            for (SeatModel seat : seats) {
                if (seat != null && !seat.isEmpty()) {
                    occupiedCount++;
                    String seatBadge = seat.isHost() ? "Host" : String.valueOf(seat.index + 1);
                    String uName = (seat.userName != null && !seat.userName.isEmpty()) ? seat.userName : "Member";
                    boolean initialSelected = true;
                    if (targetUid != null && !targetUid.isEmpty()) {
                        initialSelected = seat.userID.equals(targetUid);
                    }
                    recipientList.add(new GiftRecipientModel(seat.userID, uName, seat.userAvatar, seatBadge, false, initialSelected));
                }
            }
        }

        // If a specific targetUid is requested, adjust selection states
        if (targetUid != null && !targetUid.isEmpty()) {
            boolean allMatches = true;
            boolean hasTarget = false;
            for (GiftRecipientModel item : recipientList) {
                if (!item.isAll()) {
                    if (item.getUid().equals(targetUid)) {
                        item.setSelected(true);
                        hasTarget = true;
                    } else {
                        item.setSelected(false);
                        allMatches = false;
                    }
                }
            }
            allItem.setSelected(hasTarget && allMatches);
        }

        if (recipientList.size() <= 1) { // Only ALL item present, add current user
            recipientList.add(new GiftRecipientModel(userID, userName != null ? userName : "User", SessionManager.getInstance(this).getAvatar(), "Host", false, true));
        }

        GiftRecipientAdapter recipientAdapter = new GiftRecipientAdapter(recipientList);
        if (rvGiftRecipients != null) {
            rvGiftRecipients.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvGiftRecipients.setAdapter(recipientAdapter);
        }

        List<GiftStoreItem> giftList = new ArrayList<>();

        // 1. Gift Category
        giftList.add(new GiftStoreItem("Doraemon Gift", "gift/doraemon_gift.svga", R.drawable.gift_doraemon, 150, "Gift"));
        giftList.add(new GiftStoreItem("Golden Tea", "gift/golden_tea.svga", R.drawable.gift_golden_tea, 100, "Gift"));
        giftList.add(new GiftStoreItem("Gold Ring", "gift/blue_ring_love.svga", R.drawable.gift_blue_ring, 200000, "Gift"));
        giftList.add(new GiftStoreItem("Royal Couple", "gift/royal_couple.svga", R.drawable.gift_royal_couple, 1000000, "Gift"));
        giftList.add(new GiftStoreItem("Wedding Hall", "gift/wedding_proposal.svga", R.drawable.gift_wedding_proposal, 500000, "Gift"));
        giftList.add(new GiftStoreItem("Blue Princess Gown", "gift/blue_princess_gown.svga", R.drawable.gift_blue_gown, 450, "Gift"));
        giftList.add(new GiftStoreItem("Love Fireworks", "gift/love_fireworks.svga", R.drawable.gift_love_fireworks, 600, "Gift"));
        giftList.add(new GiftStoreItem("Fantasy Castle", "gift/fantasy_castle.svga", R.drawable.gift_fantasy_castle, 800, "Gift"));
        giftList.add(new GiftStoreItem("Dream Birdcage", "gift/dream_birdcage.svga", R.drawable.gift_dream_birdcage, 600, "Gift"));
        giftList.add(new GiftStoreItem("Pearls Necklace", "gift/pearls_necklace.svga", R.drawable.gift_pearls_necklace, 650, "Gift"));
        giftList.add(new GiftStoreItem("Lucky Cat & Crow", "gift/lucky_cat_crow.svga", R.drawable.gift_lucky_cat, 180, "Gift"));
        giftList.add(new GiftStoreItem("Cosmic Float", "gift/cosmic_float.svga", R.drawable.gift_cosmic_float, 280, "Gift"));
        giftList.add(new GiftStoreItem("Fire Rocket", "gift/fire_rocket.svga", R.drawable.gift_fire_rocket, 750, "Gift"));
        giftList.add(new GiftStoreItem("Love Couple", "gift/love_couple.svga", R.drawable.gift_love_couple, 650, "Gift"));
        giftList.add(new GiftStoreItem("Love Proposal", "gift/love_proposal.svga", R.drawable.gift_love_proposal, 750, "Gift"));
        giftList.add(new GiftStoreItem("Love Pure", "gift/love_pure.svga", R.drawable.gift_love, 250, "Gift"));
        giftList.add(new GiftStoreItem("Smoke Effect", "gift/smoke.svga", R.drawable.gift_smoke, 300, "Gift"));
        giftList.add(new GiftStoreItem("Makeup Box", "gift/makeup_box.svga", R.drawable.gift_makeup_box, 450, "Gift"));
        giftList.add(new GiftStoreItem("Umbrella", "gift/umbrella.svga", R.drawable.gift_umbrella, 200, "Gift"));
        giftList.add(new GiftStoreItem("Love Gift Box", "gift/love_gift_box.svga", R.drawable.gift_love_gift_box, 350, "Gift"));
        giftList.add(new GiftStoreItem("Birthday Cake", "gift/birthday_cake.svga", R.drawable.gift_baklava, 120, "Gift"));
        giftList.add(new GiftStoreItem("Hassan II Mosque", "gift/hassan_mosque.svga", R.drawable.gift_hassan_mosque, 1500, "Gift"));
        giftList.add(new GiftStoreItem("Lipstick Gift", "gift/lipstick_gift.svga", R.drawable.gift_lipstick, 400, "Gift"));
        giftList.add(new GiftStoreItem("Flag Gift", "gift/rose.svga", R.drawable.room_gift_ic, 100, "Gift"));

        // 2. Lucky Category
        giftList.add(new GiftStoreItem("Magic Gift", "gift/magic_gift.svga", R.drawable.gift_magic_gift, 300, "Lucky"));
        giftList.add(new GiftStoreItem("Angel Queen Crown", "gift/angel_queen_crown.svga", R.drawable.gift_angel_queen, 600, "Lucky"));
        giftList.add(new GiftStoreItem("Forever Couple", "gift/forever_couple.svga", R.drawable.gift_forever_couple, 700, "Lucky"));
        giftList.add(new GiftStoreItem("Crystal Rose", "gift/crystal_rose.svga", R.drawable.gift_crystal_rose, 350, "Lucky"));
        giftList.add(new GiftStoreItem("Angel Bride", "gift/angel_bride.svga", R.drawable.gift_angel_bride, 500, "Lucky"));
        giftList.add(new GiftStoreItem("Popcorn", "gift/popcorn.svga", R.drawable.gift_popcorn, 40, "Lucky"));
        giftList.add(new GiftStoreItem("Baklava", "gift/baklava.svga", R.drawable.gift_baklava, 80, "Lucky"));
        giftList.add(new GiftStoreItem("Glass Glow Rose", "gift/glass_glow_rose.svga", R.drawable.gift_glass_glow_rose, 400, "Lucky"));
        giftList.add(new GiftStoreItem("Money Stack", "gift/money.svga", R.drawable.gift_money, 200, "Lucky"));
        giftList.add(new GiftStoreItem("Refrigerator", "gift/refrigerator.svga", R.drawable.gift_refrigerator, 500, "Lucky"));
        giftList.add(new GiftStoreItem("Party Popper", "gift/party_popper.svga", R.drawable.gift_party_popper, 160, "Lucky"));
        giftList.add(new GiftStoreItem("Gold Bar", "gift/gold_bar.svga", R.drawable.gift_gold_bar, 250, "Lucky"));
        giftList.add(new GiftStoreItem("Magic Sword", "gift/magic_sword.svga", R.drawable.gift_magic_sword, 700, "Lucky"));

        // 3. Relationship Category
        giftList.add(new GiftStoreItem("Blue Love Ring", "gift/blue_love_ring.svga", R.drawable.gift_blue_ring, 150, "Relationship"));
        giftList.add(new GiftStoreItem("Wedding Proposal", "gift/wedding_proposal.svga", R.drawable.gift_wedding_proposal, 850, "Relationship"));
        giftList.add(new GiftStoreItem("Love Confession", "gift/love_confession.svga", R.drawable.gift_love_confession, 450, "Relationship"));
        giftList.add(new GiftStoreItem("CP Celebration", "gift/cp_celebration.svga", R.drawable.gift_cp_celebration, 550, "Relationship"));
        giftList.add(new GiftStoreItem("Love City", "gift/love_city.svga", R.drawable.gift_love_city, 1100, "Relationship"));
        giftList.add(new GiftStoreItem("Royal Banquet", "gift/royal_banquet.svga", R.drawable.gift_royal_banquet, 1400, "Relationship"));
        giftList.add(new GiftStoreItem("Diamond Ring", "gift/diamond_ring_gift.svga", R.drawable.gift_golden_rings, 700, "Relationship"));
        giftList.add(new GiftStoreItem("Perfume", "gift/parfume.svga", R.drawable.gift_parfume, 350, "Relationship"));
        giftList.add(new GiftStoreItem("Forever Love", "gift/forever_love.svga", R.drawable.gift_forever_love, 800, "Relationship"));

        // 4. Nation Flag Category
        giftList.add(new GiftStoreItem("Coming Soon", "gift/rose.svga", R.drawable.room_gift_ic, 0, "Nation Flag"));

        // 5. Luxury Category
        giftList.add(new GiftStoreItem("Luxury Bag", "gift/luxury_bag.svga", R.drawable.gift_luxury_bag, 550, "Luxury"));
        giftList.add(new GiftStoreItem("Royal Suit", "gift/royal_suit.svga", R.drawable.gift_royal_suit, 950, "Luxury"));
        giftList.add(new GiftStoreItem("Floating Castle", "gift/floating_castle.svga", R.drawable.gift_floating_castle, 1200, "Luxury"));
        giftList.add(new GiftStoreItem("Church", "gift/church.svga", R.drawable.gift_church, 850, "Luxury"));

        // 6. Customization Category
        giftList.add(new GiftStoreItem("Coming Soon", "gift/aladdin.svga", R.drawable.king_icon, 0, "Customization"));

        String[] categories = new String[]{"Gift", "Lucky", "Relationship", "Nation Flag", "Luxury", "Customization"};

        List<GiftStoreItem> initialCategoryGifts = new ArrayList<>();
        for (GiftStoreItem item : giftList) {
            if ("Gift".equalsIgnoreCase(item.category)) {
                initialCategoryGifts.add(item);
            }
        }
        if (initialCategoryGifts.isEmpty()) {
            initialCategoryGifts.addAll(giftList);
        }

        if (rvGifts != null) {
            rvGifts.setLayoutManager(new GridLayoutManager(this, 4));
            GiftStoreAdapter adapter = new GiftStoreAdapter(initialCategoryGifts);
            rvGifts.setAdapter(adapter);

            adapter.setOnGiftSelectedListener((item, position) -> {
                if ("Lucky".equalsIgnoreCase(item.category)) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    showLuckySpinWheelDialog();
                }
            });

            if (tabCategory != null) {
                tabCategory.removeAllTabs();
                for (String cat : categories) {
                    tabCategory.addTab(tabCategory.newTab().setText(cat));
                }

                tabCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        String selectedCategory = categories[tab.getPosition()];
                        if ("Lucky".equalsIgnoreCase(selectedCategory)) {
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showLuckySpinWheelDialog();
                        }
                        List<GiftStoreItem> filtered = new ArrayList<>();
                        for (GiftStoreItem item : giftList) {
                            if (item.category.equalsIgnoreCase(selectedCategory)) {
                                filtered.add(item);
                            }
                        }
                        if (filtered.isEmpty()) {
                            filtered.addAll(giftList);
                        }
                        adapter.updateItems(filtered);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });
            }

            if (btnSendAction != null) {
                btnSendAction.setOnClickListener(v -> {
                    GiftStoreItem selectedItem = adapter.getSelectedGift();
                    if (selectedItem == null) {
                        Toast.makeText(RoomChatActivity.this, "Please select a gift first! 🎁", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<GiftRecipientModel> selectedRecipients = recipientAdapter.getSelectedRecipients();
                    if (selectedRecipients.isEmpty()) {
                        Toast.makeText(RoomChatActivity.this, "Please select at least one recipient! 👤", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long singleCost = selectedItem.cost;
                    int recipientCount = selectedRecipients.size();
                    long totalCost = singleCost * recipientCount;
                    String giftName = selectedItem.name;
                    selectedGiftSvga = selectedItem.svgaPath;

                    WalletManager.spendCoinsForGift(userID, null, totalCost, giftName, new WalletManager.WalletCallback() {
                        @Override
                        public void onSuccess(String message, long newCoinBalance) {
                            if (tvGiftDialogCoins != null) {
                                tvGiftDialogCoins.setText(String.valueOf(newCoinBalance));
                            }

                            StringBuilder recipientNames = new StringBuilder();
                            for (int i = 0; i < selectedRecipients.size(); i++) {
                                GiftRecipientModel recipient = selectedRecipients.get(i);

                                // Add coins directly to each recipient's account balance!
                                if (!userID.equals(recipient.getUid())) {
                                    WalletManager.addCoins(recipient.getUid(), singleCost, "Gift received: " + giftName, null);
                                }

                                if (i > 0) recipientNames.append(", ");
                                recipientNames.append(recipient.getName());

                                if (roomGiftsRef != null) {
                                    Map<String, Object> giftData = new HashMap<>();
                                    giftData.put("senderName", userName != null ? userName : "User");
                                    giftData.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                                    giftData.put("recipientName", recipient.getName());
                                    giftData.put("recipientUid", recipient.getUid());
                                    giftData.put("giftName", giftName);
                                    giftData.put("giftSvga", selectedGiftSvga);
                                    giftData.put("iconRes", (long) selectedItem.iconRes);
                                    giftData.put("timestamp", System.currentTimeMillis());
                                    roomGiftsRef.push().setValue(giftData);
                                }
                            }

                            String senderDisplayName = userName != null ? userName : "User";
                            String chatNotice = senderDisplayName + " sent " + giftName + " to " + recipientNames.toString() + "!";
                            ZegoManager.getInstance().sendInRoomTextMessage(chatNotice);

                            Toast.makeText(RoomChatActivity.this, "Gift sent to " + recipientCount + " member(s)!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void showLuckySpinWheelDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lucky_spin_wheel, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        TextView tvWheelCoins = dialogView.findViewById(R.id.tvWheelCoins);
        ImageView btnCloseWheel = dialogView.findViewById(R.id.btnCloseWheel);
        SpinWheelView spinWheelView = dialogView.findViewById(R.id.spinWheelView);
        View btnSpin = dialogView.findViewById(R.id.btnSpin);

        // Cache local balance so spin starts INSTANTLY with 0ms network latency
        final long[] cachedCoins = new long[]{0};

        Runnable refreshCoins = () -> WalletManager.getUserCoins(userID, balance -> runOnUiThread(() -> {
            cachedCoins[0] = balance;
            if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + balance);
        }));
        refreshCoins.run();

        if (btnCloseWheel != null) btnCloseWheel.setOnClickListener(v -> dialog.dismiss());

        Random random = new Random();

        View.OnClickListener spinAction = v -> {
            if (spinWheelView != null && spinWheelView.isSpinning()) return;

            final long spinCost = 100;
            if (cachedCoins[0] > 0 && cachedCoins[0] < spinCost) {
                Toast.makeText(this, "Insufficient coins! Please top-up.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Immediately update local UI balance & disable button for ZERO MS delay
            cachedCoins[0] = Math.max(0, cachedCoins[0] - spinCost);
            if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + cachedCoins[0]);
            if (btnSpin != null) btnSpin.setEnabled(false);

            // Select random winner immediately
            int randomWinnerIndex = random.nextInt(SpinWheelController.NUM_SEGMENTS);

            // START SPINNING INSTANTLY (0ms latency!)
            if (spinWheelView != null) {
                spinWheelView.spinToSegment(
                        randomWinnerIndex,
                        SpinWheelController.SPIN_DURATION_MS,
                        SpinWheelController.DEFAULT_FULL_ROTATIONS,
                        winner -> {
                            if (btnSpin != null) btnSpin.setEnabled(true);

                            // Show Custom Neon Winner Result Dialog
                            SpinWinnerDialog winnerDialog = new SpinWinnerDialog(RoomChatActivity.this);
                            winnerDialog.showWinner(winner, () -> {
                                // Close Spin Wheel Dialog so ALL dialogs are closed before SVGA animation plays!
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }

                                // Broadcast to Firebase Room Gifts feed (Firebase listener plays SVGA animation & banner ONCE)
                                if (roomGiftsRef != null) {
                                    Map<String, Object> giftData = new HashMap<>();
                                    giftData.put("senderName", userName != null ? userName : "User");
                                    giftData.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                                    giftData.put("giftName", winner.getName());
                                    giftData.put("giftSvga", winner.getSvgaPath());
                                    giftData.put("iconRes", (long) winner.getDrawableRes());
                                    giftData.put("timestamp", System.currentTimeMillis());
                                    roomGiftsRef.push().setValue(giftData);
                                } else {
                                    // Offline fallback
                                    playSvgaAnimation(winner.getSvgaPath());
                                    showGoldenGiftBanner(userName, "🎰 Lucky Spin Won: " + winner.getName());
                                }

                                Toast.makeText(RoomChatActivity.this, "🎉 You won and sent " + winner.getName() + "!", Toast.LENGTH_SHORT).show();
                                refreshCoins.run();
                                return Unit.INSTANCE;
                            });
                        }
                );
            } else {
                if (btnSpin != null) btnSpin.setEnabled(true);
            }

            // Deduct coins on Firebase asynchronously in background while wheel is spinning
            WalletManager.spendCoinsForGift(userID, null, spinCost, "Lucky Wheel Spin", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    runOnUiThread(() -> {
                        cachedCoins[0] = newCoinBalance;
                        if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + newCoinBalance);
                    });
                }

                @Override
                public void onError(String error) {
                    refreshCoins.run();
                }
            });
        };

        if (spinWheelView != null) {
            spinWheelView.setOnSpinButtonClickListener(() -> spinAction.onClick(spinWheelView));
        }
        if (btnSpin != null) btnSpin.setOnClickListener(spinAction);

        dialog.show();
    }

    // --- ANIMATION & BANNER QUEUE MANAGER ---
    private static class GiftAnimationItem {
        String svgaPath;
        GiftAnimationItem(String svgaPath) {
            this.svgaPath = svgaPath;
        }
    }

    private static class BannerQueueItem {
        String svgaAsset;
        String htmlNotice;
        boolean isEntryBanner;
        BannerQueueItem(String svgaAsset, String htmlNotice, boolean isEntryBanner) {
            this.svgaAsset = svgaAsset;
            this.htmlNotice = htmlNotice;
            this.isEntryBanner = isEntryBanner;
        }
    }

    private final Queue<GiftAnimationItem> giftAnimationQueue = new ConcurrentLinkedQueue<>();
    private boolean isGiftAnimationPlaying = false;
    private final Handler giftHandler = new Handler(Looper.getMainLooper());
    private Runnable giftTimeoutRunnable = null;

    private final Queue<BannerQueueItem> bannerQueue = new ConcurrentLinkedQueue<>();
    private boolean isBannerPlaying = false;

    public void playSvgaAnimation(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return;
        giftAnimationQueue.offer(new GiftAnimationItem(fileName));
        processNextGiftAnimation();
    }

    private synchronized void processNextGiftAnimation() {
        if (isGiftAnimationPlaying) return;
        GiftAnimationItem nextItem = giftAnimationQueue.poll();
        if (nextItem == null) return;

        isGiftAnimationPlaying = true;
        playSvgaAnimationInternal(nextItem.svgaPath);
    }

    private void finishCurrentGiftAnimation() {
        runOnUiThread(() -> {
            if (giftTimeoutRunnable != null) {
                giftHandler.removeCallbacks(giftTimeoutRunnable);
                giftTimeoutRunnable = null;
            }
            if (svgaPlayer != null) {
                try {
                    svgaPlayer.stopAnimation();
                    svgaPlayer.setVisibility(View.GONE);
                    svgaPlayer.clear();
                } catch (Exception ignored) {}
            }
            isGiftAnimationPlaying = false;
            processNextGiftAnimation();
        });
    }

    private void playSvgaAnimationInternal(String fileName) {
        if (svgaPlayer == null || svgaParser == null || fileName == null) {
            finishCurrentGiftAnimation();
            return;
        }

        // Safety timeout (8s max) in case SVGACallback.onFinished() is missed by the player
        if (giftTimeoutRunnable != null) {
            giftHandler.removeCallbacks(giftTimeoutRunnable);
        }
        giftTimeoutRunnable = this::finishCurrentGiftAnimation;
        giftHandler.postDelayed(giftTimeoutRunnable, 8000);

        runOnUiThread(() -> {
            try {
                svgaPlayer.stopAnimation();
                svgaPlayer.clear();
            } catch (Exception ignored) {}
        });

        svgaParser.decodeFromAssets(fileName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                runOnUiThread(() -> {
                    try {
                        svgaPlayer.stopAnimation();
                        svgaPlayer.clear();
                        svgaPlayer.setVisibility(View.VISIBLE);
                        svgaPlayer.setVideoItem(videoItem);
                        svgaPlayer.setLoops(1);
                        svgaPlayer.startAnimation();
                        svgaPlayer.setCallback(new SVGACallback() {
                            @Override public void onPause() {}
                            @Override public void onFinished() {
                                finishCurrentGiftAnimation();
                            }
                            @Override public void onStep(int frame, double percentage) {}
                            @Override public void onRepeat() {}
                        });
                    } catch (Exception e) {
                        Log.e("RoomChatActivity", "Error playing SVGA animation", e);
                        finishCurrentGiftAnimation();
                    }
                });
            }

            @Override
            public void onError() {
                finishCurrentGiftAnimation();
            }
        }, null);
    }

    public void showUserEntryBanner(String displayUserName) {
        if (isFinishing() || isDestroyed()) return;
        String formattedName = (displayUserName != null && !displayUserName.trim().isEmpty()) ? displayUserName : "User";
        String htmlNotice = "<font color='#FFFFFF'><b>" + formattedName + "</b></font> <font color='#FFE082'>entered the room</font> 👋";

        bannerQueue.offer(new BannerQueueItem("Notification/full_banner_red_packet.svga", htmlNotice, true));
        processNextBanner();
    }

    public void showGoldenGiftBanner(String senderName, String giftName) {
        if (isFinishing() || isDestroyed()) return;
        String formattedSender = (senderName != null && !senderName.trim().isEmpty()) ? senderName : "User";
        String formattedGift = (giftName != null && !giftName.trim().isEmpty()) ? giftName : "Gift";

        String htmlNotice = "<font color='#FFFFFF'><b>" + formattedSender + "</b></font>" +
                            " <font color='#FFE082'>sent</font> " +
                            "<font color='#FFF59D'><b>" + formattedGift + "</b></font>";

        bannerQueue.offer(new BannerQueueItem("Notification/full_banner_game.svga", htmlNotice, false));
        processNextBanner();
    }

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerTimeoutRunnable = null;

    private synchronized void processNextBanner() {
        if (isBannerPlaying) return;
        BannerQueueItem nextBanner = bannerQueue.poll();
        if (nextBanner == null) return;

        isBannerPlaying = true;
        playBannerInternal(nextBanner);
    }

    private void finishCurrentBanner() {
        runOnUiThread(() -> {
            if (bannerTimeoutRunnable != null) {
                bannerHandler.removeCallbacks(bannerTimeoutRunnable);
                bannerTimeoutRunnable = null;
            }
            if (bannerEntryContainer != null) bannerEntryContainer.setVisibility(View.GONE);
            if (bannerGiftContainer != null) bannerGiftContainer.setVisibility(View.GONE);
            if (svgaEntryBannerPlayer != null) {
                try {
                    svgaEntryBannerPlayer.stopAnimation();
                    svgaEntryBannerPlayer.clear();
                } catch (Exception ignored) {}
            }
            if (svgaBannerPlayer != null) {
                try {
                    svgaBannerPlayer.stopAnimation();
                    svgaBannerPlayer.clear();
                } catch (Exception ignored) {}
            }
            isBannerPlaying = false;
            processNextBanner();
        });
    }

    private void playBannerInternal(BannerQueueItem item) {
        if (isFinishing() || isDestroyed() || item == null) {
            finishCurrentBanner();
            return;
        }

        View container = item.isEntryBanner ? bannerEntryContainer : bannerGiftContainer;
        SVGAImageView player = item.isEntryBanner ? svgaEntryBannerPlayer : svgaBannerPlayer;
        TextView tvNotice = item.isEntryBanner ? tvEntryBannerNotice : tvBannerNotice;

        if (container == null || player == null || tvNotice == null) {
            finishCurrentBanner();
            return;
        }

        if (bannerTimeoutRunnable != null) {
            bannerHandler.removeCallbacks(bannerTimeoutRunnable);
        }
        bannerTimeoutRunnable = this::finishCurrentBanner;
        bannerHandler.postDelayed(bannerTimeoutRunnable, 6000);

        runOnUiThread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvNotice.setText(Html.fromHtml(item.htmlNotice, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvNotice.setText(Html.fromHtml(item.htmlNotice));
                }
                container.setVisibility(View.VISIBLE);

                player.stopAnimation();
                player.clear();

                SVGAParser parser = (svgaParser != null) ? svgaParser : new SVGAParser(this);
                parser.decodeFromAssets(item.svgaAsset, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        runOnUiThread(() -> {
                            try {
                                player.setVisibility(View.VISIBLE);
                                player.setVideoItem(videoItem);
                                player.setLoops(1);
                                player.startAnimation();
                                player.setCallback(new SVGACallback() {
                                    @Override
                                    public void onFinished() {
                                        finishCurrentBanner();
                                    }

                                    @Override public void onPause() {}
                                    @Override public void onRepeat() {}
                                    @Override public void onStep(int frame, double percentage) {}
                                });
                            } catch (Exception e) {
                                Log.e("RoomChatActivity", "Error playing banner SVGA", e);
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
                Log.e("RoomChatActivity", "Error showing banner", e);
                finishCurrentBanner();
            }
        });
    }

    private void playEntrySceneVideo() {
        // Entry video dialog removed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (giftTimeoutRunnable != null) giftHandler.removeCallbacks(giftTimeoutRunnable);
        if (bannerTimeoutRunnable != null) bannerHandler.removeCallbacks(bannerTimeoutRunnable);
        giftAnimationQueue.clear();
        bannerQueue.clear();
        isGiftAnimationPlaying = false;
        isBannerPlaying = false;
        leaveRoom();
    }
}

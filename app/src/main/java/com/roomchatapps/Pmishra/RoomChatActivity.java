package com.roomchatapps.Pmishra;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
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
import com.roomchatapps.Pmishra.models.ChatMessage;
import com.roomchatapps.Pmishra.models.FriendRequestModel;
import com.roomchatapps.Pmishra.models.TransactionModel;
import com.roomchatapps.Pmishra.adapters.ChatAdapter;
import com.roomchatapps.Pmishra.adapters.SeatAdapter;
import com.roomchatapps.Pmishra.utils.NotificationHelper;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.utils.WalletManager;
import com.roomchatapps.Pmishra.zego.SeatManager;
import com.roomchatapps.Pmishra.zego.SeatModel;
import com.roomchatapps.Pmishra.zego.ZegoManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
        
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 102);
        } else {
            initZego();
        }
        
        setupFirebaseListeners();

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
            btnRoomClose.setOnClickListener(v -> leaveRoom());
        }

        View layoutUserInfo = findViewById(R.id.layoutUserInfo);
        if (layoutUserInfo != null) {
            layoutUserInfo.setOnClickListener(v -> {
                List<SeatModel> seats = SeatManager.getInstance().getSeats();
                if (seats != null && !seats.isEmpty()) {
                    SeatModel hostSeat = seats.get(0);
                    if (hostSeat != null && !hostSeat.isEmpty()) {
                        onSeatClicked(hostSeat);
                    }
                }
            });
        }


        View btnRoomShare = findViewById(R.id.btnRoomShare);
        if (btnRoomShare != null) {
            btnRoomShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my room ID: " + roomID);
                startActivity(Intent.createChooser(shareIntent, "Share Room"));
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
        btnSpeaker.setOnClickListener(v -> {
            boolean newState = !ZegoManager.getInstance().isSpeakerOn();
            btnSpeaker.setImageResource(newState ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
            
            // Execute setSpeakerOn on a background thread so audio route changes do not freeze/lag the UI video playback
            new Thread(() -> {
                ZegoManager.getInstance().setSpeakerOn(newState);
            }).start();
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

        View moreView = findViewById(R.id.more);
        if (moreView != null) {
            moreView.setOnClickListener(v -> showThemeSelectionDialog());
        }

        View btnMore = findViewById(R.id.btnMore);
        if (btnMore != null) {
            btnMore.setVisibility(View.VISIBLE);
            btnMore.setOnClickListener(v -> showThemeSelectionDialog());
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
                showUserEnteredMessage(userName);
                if (isHost) {
                    SeatManager.getInstance().takeSeat(0, userID, userName);
                    ZegoManager.getInstance().startPublishing();
                }
            } else {
                Toast.makeText(RoomChatActivity.this, "Failed to join room: " + errorCode, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onUserJoined(ZegoUser user) {
            updateGlobalUserCount();
            if (user != null) {
                String displayName = user.userName != null && !user.userName.isEmpty() ? user.userName : user.userID;
                showUserEnteredMessage(displayName);
            }
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
            
            btnMic.setImageResource(isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
        } else {
            // Not on seat, ensure mic is off
            ZegoManager.getInstance().stopPublishing();
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
            ivSeatHeaderAvatar.setVisibility(View.GONE);
            ivSeatHeaderIcon.setVisibility(View.VISIBLE);

            if (model.isClosed) {
                // Locked / Closed Seat
                tvSeatTitle.setText(model.index == 0 ? "Host Seat" : "Seat #" + (model.index + 1));
                tvSeatSubtitle.setText("Seat Locked by Host");
                ivSeatHeaderIcon.setImageResource(R.drawable.ic_lock);
                ivSeatHeaderIcon.setColorFilter(Color.parseColor("#EF4444"));

                if (isHost) {
                    btnPrimaryAction.setVisibility(View.VISIBLE);
                    btnPrimaryAction.setBackgroundResource(R.drawable.bg_seat_primary_btn);
                    ivPrimaryActionIcon.setImageResource(R.drawable.ic_lock_open);
                    tvPrimaryActionText.setText("Open Seat");
                    btnPrimaryAction.setOnClickListener(v -> {
                        SeatManager.getInstance().closeSeat(model.index, false);
                        dialog.dismiss();
                    });
                } else {
                    btnPrimaryAction.setVisibility(View.GONE);
                    Toast.makeText(this, "This seat is locked", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Open Empty Seat
                tvSeatTitle.setText(model.index == 0 ? "Host Seat" : "Seat #" + (model.index + 1));
                tvSeatSubtitle.setText("Empty Seat • Tap below to join voice");
                ivSeatHeaderIcon.setImageResource(R.drawable.ic_armchair);
                ivPrimaryActionIcon.setColorFilter(Color.WHITE);

                btnPrimaryAction.setVisibility(View.VISIBLE);
                btnPrimaryAction.setBackgroundResource(R.drawable.bg_seat_primary_btn);
                ivPrimaryActionIcon.setImageResource(R.drawable.ic_mic_on);
                tvPrimaryActionText.setText("Take Seat & Speak");

                btnPrimaryAction.setOnClickListener(v -> {
                    int currentIndex = SeatManager.getInstance().findUserSeatIndex(userID);
                    if (currentIndex != -1) {
                        SeatManager.getInstance().leaveSeat(currentIndex);
                    }
                    SeatManager.getInstance().takeSeat(model.index, userID, userName);
                    ZegoManager.getInstance().startPublishing();
                    dialog.dismiss();
                });

                if (isHost) {
                    btnOptionLock.setVisibility(View.VISIBLE);
                    tvOptionLockText.setText("Close Seat");
                    ivOptionLockIcon.setImageResource(R.drawable.ic_lock);
                    btnOptionLock.setOnClickListener(v -> {
                        SeatManager.getInstance().closeSeat(model.index, true);
                        dialog.dismiss();
                    });
                }
            }
        } else if (model.userID.equals(userID)) {
            // Self Seat
            tvSeatTitle.setText("Your Seat (Seat #" + (model.index + 1) + ")");
            tvSeatSubtitle.setText("You are currently on mic");
            ivSeatHeaderIcon.setVisibility(View.GONE);
            ivSeatHeaderAvatar.setVisibility(View.VISIBLE);

            UserProfileCache.getUserProfile(userID, profile -> {
                if (profile != null && profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                    Glide.with(this).load(profile.avatarUrl).placeholder(R.drawable.logo_placeholder).into(ivSeatHeaderAvatar);
                } else {
                    Glide.with(this).load(R.drawable.logo_placeholder).into(ivSeatHeaderAvatar);
                }
            });

            btnPrimaryAction.setVisibility(View.VISIBLE);
            btnPrimaryAction.setBackgroundResource(R.drawable.bg_seat_danger_btn);
            ivPrimaryActionIcon.setImageResource(R.drawable.ic_leave_seat);
            tvPrimaryActionText.setText("Leave Seat");

            btnPrimaryAction.setOnClickListener(v -> {
                SeatManager.getInstance().leaveSeat(model.index);
                ZegoManager.getInstance().stopPublishing();
                dialog.dismiss();
            });
        } else {
            // Other User's Seat (Host or Joined Audience)
            String name = (model.userName != null && !model.userName.isEmpty()) ? model.userName : "User";
            tvSeatTitle.setText(name);
            tvSeatSubtitle.setText(model.index == 0 ? "Room Host • Seat #1" : "Joined Seat #" + (model.index + 1));
            ivSeatHeaderIcon.setVisibility(View.GONE);
            ivSeatHeaderAvatar.setVisibility(View.VISIBLE);

            if (model.userAvatar != null && !model.userAvatar.isEmpty()) {
                Glide.with(this).load(model.userAvatar).placeholder(R.drawable.logo_placeholder).into(ivSeatHeaderAvatar);
            } else {
                UserProfileCache.getUserProfile(model.userID, profile -> {
                    if (profile != null && profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                        Glide.with(this).load(profile.avatarUrl).placeholder(R.drawable.logo_placeholder).into(ivSeatHeaderAvatar);
                    } else {
                        Glide.with(this).load(R.drawable.logo_placeholder).into(ivSeatHeaderAvatar);
                    }
                });
            }

            // Real-time Follow / Following Button in Seat Dialog
            btnPrimaryAction.setVisibility(View.VISIBLE);
            DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                    .child(userID).child("following").child(model.userID);

            followRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        btnPrimaryAction.setBackgroundResource(R.drawable.bg_seat_option_item);
                        ivPrimaryActionIcon.setImageResource(R.drawable.ic_person);
                        ivPrimaryActionIcon.setColorFilter(Color.parseColor("#A0AEC0"));
                        tvPrimaryActionText.setText("Following");
                        tvPrimaryActionText.setTextColor(Color.parseColor("#A0AEC0"));
                    } else {
                        btnPrimaryAction.setBackgroundResource(R.drawable.bg_seat_primary_btn);
                        ivPrimaryActionIcon.setImageResource(R.drawable.ic_person);
                        ivPrimaryActionIcon.setColorFilter(Color.WHITE);
                        tvPrimaryActionText.setText("+ Follow User");
                        tvPrimaryActionText.setTextColor(Color.WHITE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            btnPrimaryAction.setOnClickListener(v -> {
                AnimationHelper.animateFollowButton(btnPrimaryAction, () -> {
                    DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                            .child(userID).child("following").child(model.userID);
                    DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                            .child(model.userID).child("followers").child(userID);

                    followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                followingRef.removeValue();
                                followersRef.removeValue();
                            } else {
                                followingRef.setValue(true);
                                followersRef.setValue(true);
                                NotificationHelper.sendFollowNotification(model.userID);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                });
            });

            btnOptionProfile.setVisibility(View.VISIBLE);
            btnOptionProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, UserDetailActivity.class);
                intent.putExtra("uid", model.userID);
                startActivity(intent);
                dialog.dismiss();
            });

            btnOptionGift.setVisibility(View.VISIBLE);
            btnOptionGift.setOnClickListener(v -> {
                dialog.dismiss();
                showGiftDialog();
            });

            if (isHost) {
                btnOptionMute.setVisibility(View.VISIBLE);
                tvOptionMuteText.setText(model.isMuted ? "Unmute User" : "Mute User");
                ivOptionMuteIcon.setImageResource(model.isMuted ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
                btnOptionMute.setOnClickListener(v -> {
                    SeatManager.getInstance().muteSeat(model.index, !model.isMuted);
                    dialog.dismiss();
                });

                btnOptionKick.setVisibility(View.VISIBLE);
                btnOptionKick.setOnClickListener(v -> {
                    SeatManager.getInstance().kickUser(model.index);
                    dialog.dismiss();
                });
            }
        }

        dialog.show();
    }

    private void leaveRoom() {
        try {
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
                        String giftSvga = snapshot.child("giftSvga").getValue(String.class);
                        if (giftSvga != null) {
                            playSvgaAnimation(giftSvga);
                        } else if (giftName.contains("Heart")) {
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

    private void showThemeSelectionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
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

    private void showUserEnteredMessage(String displayUserName) {
        if (displayUserName == null || displayUserName.trim().isEmpty()) return;
        ChatMessage entryMsg = new ChatMessage();
        entryMsg.setSenderId("");
        entryMsg.setMessage(displayUserName + " Enter the room");
        entryMsg.setTimestamp(System.currentTimeMillis());

        if (chatAdapter != null) {
            chatAdapter.addAutoExpiringMessage(entryMsg, 5000);
        }
        if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private void showCommentInputDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comment_input, null, false);
        if (dialogView == null) return;

        EditText etInput = dialogView.findViewById(R.id.etCommentInput);
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
        ZegoManager.getInstance().sendInRoomTextMessage(text);

        ChatMessage myMsg = new ChatMessage();
        myMsg.setSenderId(userID);
        myMsg.setMessage(userName + " : " + text);
        myMsg.setTimestamp(System.currentTimeMillis());

        if (chatAdapter != null) {
            chatAdapter.addAutoExpiringMessage(myMsg, 5000);
        }
        if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
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

    private String selectedGiftSvga = "gift/aladdin.svga";

    private void showGiftDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_gift_store, null, false);
        if (dialogView == null) return;

        selectedGiftSvga = "gift/aladdin.svga";

        MaterialCardView[] allCards = {
                dialogView.findViewById(R.id.cardGiftAladdin),
                dialogView.findViewById(R.id.cardGiftHeart),
                dialogView.findViewById(R.id.cardGiftBox),
                dialogView.findViewById(R.id.cardGiftRose),
                dialogView.findViewById(R.id.cardGiftCrown),
                dialogView.findViewById(R.id.cardGiftRocket),
                dialogView.findViewById(R.id.cardGiftSvip),
                dialogView.findViewById(R.id.cardGift8)
        };

        ImageView[] allStaticImgs = {
                dialogView.findViewById(R.id.imgGiftAladdin),
                dialogView.findViewById(R.id.imgGiftHeart),
                dialogView.findViewById(R.id.imgGiftBox),
                dialogView.findViewById(R.id.imgGiftRose),
                dialogView.findViewById(R.id.imgGiftCrown),
                dialogView.findViewById(R.id.imgGiftRocket),
                dialogView.findViewById(R.id.imgGiftSvip),
                dialogView.findViewById(R.id.imgGift8)
        };

        SVGAImageView[] allPreviewSvgas = {
                dialogView.findViewById(R.id.svgaPreviewAladdin),
                dialogView.findViewById(R.id.svgaPreviewHeart),
                dialogView.findViewById(R.id.svgaPreviewBox),
                dialogView.findViewById(R.id.svgaPreviewRose),
                dialogView.findViewById(R.id.svgaPreviewCrown),
                dialogView.findViewById(R.id.svgaPreviewRocket),
                dialogView.findViewById(R.id.svgaPreviewSvip),
                dialogView.findViewById(R.id.svgaPreview8)
        };

        String[] allSvgaFiles = {
                "gift/aladdin.svga",
                "gift/Walkthrough.svga",
                "gift/angel.svga",
                "gift/rose.svga",
                "gift/Rocket.svga",
                "gift/posche.svga",
                "gift/halloween.svga",
                "gift/gift8sv.svga"
        };

        String[] giftNames = {
                "Aladdin Lamp 🧞",
                "Walkthrough 🚶",
                "Angel 👼",
                "Magic Rose 🌹",
                "Rocket 🚀",
                "Porsche Car 🏎️",
                "Halloween 🎃",
                "Super Castle 🏰"
        };

        long[] giftCosts = {50, 100, 150, 200, 300, 500, 1000, 0};
        int[] giftIconRes = {
                R.drawable.aladin,
                R.drawable.gift2,
                R.drawable.gift3,
                R.drawable.gift4,
                R.drawable.gift5,
                R.drawable.gift6,
                R.drawable.gift7,
                R.drawable.gift8
        };
        final int[] selectedIndex = {0};

        View btnSendAction = dialogView.findViewById(R.id.btnSendGiftAction);

        // Selection Handler
        for (int i = 0; i < allCards.length; i++) {
            final int index = i;
            if (allCards[index] != null) {
                allCards[index].setOnClickListener(v -> {
                    selectedIndex[0] = index;
                    selectedGiftSvga = allSvgaFiles[index];

                    // Reset all cards to unselected state
                    for (int j = 0; j < allCards.length; j++) {
                        if (allCards[j] != null) {
                            allCards[j].setStrokeColor(Color.parseColor("#20FFFFFF"));
                            allCards[j].setStrokeWidth(dp2px(1));
                        }
                        if (allStaticImgs[j] != null) {
                            allStaticImgs[j].setVisibility(View.VISIBLE);
                        }
                        if (allPreviewSvgas[j] != null) {
                            allPreviewSvgas[j].setVisibility(View.GONE);
                            allPreviewSvgas[j].stopAnimation();
                        }
                    }

                    // Highlight selected card
                    allCards[index].setStrokeColor(Color.parseColor("#FF007A"));
                    allCards[index].setStrokeWidth(dp2px(2));

                    // Show SVGA preview inside selected card
                    if (allStaticImgs[index] != null) {
                        allStaticImgs[index].setVisibility(View.GONE);
                    }
                    if (allPreviewSvgas[index] != null && svgaParser != null) {
                        SVGAImageView previewPlayer = allPreviewSvgas[index];
                        previewPlayer.setVisibility(View.VISIBLE);
                        svgaParser.decodeFromAssets(allSvgaFiles[index], new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                                previewPlayer.setVideoItem(videoItem);
                                previewPlayer.startAnimation();
                            }
                            @Override
                            public void onError() {}
                        }, null);
                    }
                });
            }
        }

        // Auto-select first gift (Aladdin) on open
        if (allCards[0] != null) {
            allCards[0].performClick();
        }

        // Send Button Click with Coin Balance Check & Deduction
        if (btnSendAction != null) {
            btnSendAction.setOnClickListener(v -> {
                int idx = selectedIndex[0];
                long cost = giftCosts[idx];
                String giftName = giftNames[idx];

                WalletManager.spendCoinsForGift(userID, null, cost, giftName, new WalletManager.WalletCallback() {
                    @Override
                    public void onSuccess(String message, long newCoinBalance) {
                        playSvgaAnimation(selectedGiftSvga);
                        if (roomGiftsRef != null) {
                            Map<String, Object> giftData = new HashMap<>();
                            giftData.put("senderName", userName != null ? userName : "User");
                            giftData.put("giftName", giftName);
                            giftData.put("giftSvga", selectedGiftSvga);
                            giftData.put("iconRes", (long) giftIconRes[idx]);
                            giftData.put("timestamp", System.currentTimeMillis());
                            roomGiftsRef.push().setValue(giftData);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void playSvgaAnimation(String fileName) {
        if (svgaPlayer == null || svgaParser == null || fileName == null) return;

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
                        svgaPlayer.startAnimation();
                        svgaPlayer.setCallback(new SVGACallback() {
                            @Override public void onPause() {}
                            @Override public void onFinished() {
                                runOnUiThread(() -> {
                                    if (svgaPlayer != null) {
                                        svgaPlayer.setVisibility(View.GONE);
                                        svgaPlayer.clear();
                                    }
                                });
                            }
                            @Override public void onStep(int frame, double percentage) {}
                            @Override public void onRepeat() {}
                        });
                    } catch (Exception e) {
                        Log.e("RoomChatActivity", "Error playing SVGA animation", e);
                    }
                });
            }

            @Override
            public void onError() {
                runOnUiThread(() -> {
                    if (svgaPlayer != null) {
                        svgaPlayer.setVisibility(View.GONE);
                    }
                });
            }
        }, null);
    }

    private void playEntrySceneVideo() {
        // Entry video dialog removed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }
}

package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opensource.svgaplayer.SVGAImageView;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.NotificationHelper;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FamilyDetailActivity extends AppCompatActivity {

    private String familyId, familyName, familyLogo, familyOwnerUid;

    private TextView tvFamilyTitle, tvFamilyTagBadge, tvMemberCount, tvFamilyNoticeText;
    private TextView tvLevelTitle, tvLevelNeed, tvExpText, tvLeaderName;
    private ShapeableImageView ivFamilyCrest, ivLeaderAvatar;
    private ShapeableImageView ivManager1, ivManager2, ivManager3, ivManager4, ivBottomUserAvatar;
    private ImageView ivHeaderBackground, ivFamilyFrame, ivLeaderFrame, ivLevelShield;
    private SVGAImageView svgaFamilyFrame, svgaLeaderFrame;
    private ProgressBar pbExpProgress;

    private TextView tabContribution, tabRoom, tvFamilyRoomTitle;
    private TextView btnDetailFilterDaily, btnDetailFilterWeekly, btnDetailFilterSeason;
    private TextView tvDetailSelectedDate, tvOnlineCount;
    private TextView tvTaskTodayExp, tvTaskMonthExp, tvBottomUserMessage;
    private ImageView btnDetailPrevDate, btnDetailNextDate;
    private View layoutBottomJoinBar, layoutBottomChatBar, btnBottomQuickChat, cardFamilyTask;
    private View indicatorContribution, indicatorRoom, layoutContributionContent, layoutRoomContent, cardFamilyRoomContainer;

    private TextView tvPodium1Name, tvPodium1Exp, tvPodium2Name, tvPodium2Exp, tvPodium3Name, tvPodium3Exp;
    private ShapeableImageView ivPodium1Avatar, ivPodium2Avatar, ivPodium3Avatar;
    private View podium1, podium2, podium3;

    private RecyclerView rvOnlineMembers, rvMemberContributionList;
    private TextView btnJoinFamily;

    private Calendar selectedCalendar = Calendar.getInstance();
    private String activePeriod = "Daily";

    private final List<GroupMessageTickerItem> groupTickerMessages = new ArrayList<>();
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private Runnable tickerRunnable;
    private int currentTickerIndex = 0;
    private ValueEventListener groupMessageTickerListener;
    private DatabaseReference groupMessagesRef;

    private OnlineMemberAdapter onlineAdapter;
    private final List<OnlineMemberModel> onlineMemberList = new ArrayList<>();

    private MemberContributionAdapter memberAdapter;
    private final List<MemberContributionModel> memberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_family_detail);

        familyId = getIntent().getStringExtra("familyId");
        familyName = getIntent().getStringExtra("familyName");
        familyLogo = getIntent().getStringExtra("familyLogo");

        setupWindowInsets();
        initViews();
        bindIntentData();
        setupListeners();
        updateDateDisplay();
        loadFamilyDetailData();
    }

    private void setupWindowInsets() {
        View topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView tabFamilySquareHeader = findViewById(R.id.tabFamilySquareHeader);
        if (tabFamilySquareHeader != null) tabFamilySquareHeader.setOnClickListener(v -> finish());

        View btnHeaderOptions = findViewById(R.id.btnHeaderOptions);
        if (btnHeaderOptions != null) btnHeaderOptions.setOnClickListener(v -> showFamilyOptionsMenuDialog());

        tvFamilyTitle = findViewById(R.id.tvFamilyTitle);
        tvFamilyTagBadge = findViewById(R.id.tvFamilyTagBadge);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        tvFamilyNoticeText = findViewById(R.id.tvFamilyNoticeText);

        ivHeaderBackground = findViewById(R.id.ivHeaderBackground);
        ivFamilyCrest = findViewById(R.id.ivFamilyCrest);
        ivFamilyFrame = findViewById(R.id.ivFamilyFrame);
        svgaFamilyFrame = findViewById(R.id.svgaFamilyFrame);
        tvLevelTitle = findViewById(R.id.tvLevelTitle);
        tvLevelNeed = findViewById(R.id.tvLevelNeed);
        pbExpProgress = findViewById(R.id.pbExpProgress);
        tvExpText = findViewById(R.id.tvExpText);
        ivLevelShield = findViewById(R.id.ivLevelShield);

        ivLeaderAvatar = findViewById(R.id.ivLeaderAvatar);
        ivLeaderFrame = findViewById(R.id.ivLeaderFrame);
        svgaLeaderFrame = findViewById(R.id.svgaLeaderFrame);
        tvLeaderName = findViewById(R.id.tvLeaderName);

        ivManager1 = findViewById(R.id.ivManager1);
        ivManager2 = findViewById(R.id.ivManager2);
        ivManager3 = findViewById(R.id.ivManager3);
        ivManager4 = findViewById(R.id.ivManager4);

        tvTaskTodayExp = findViewById(R.id.tvTaskTodayExp);
        tvTaskMonthExp = findViewById(R.id.tvTaskMonthExp);
        cardFamilyTask = findViewById(R.id.cardFamilyTask);

        tvOnlineCount = findViewById(R.id.tvOnlineCount);
        rvOnlineMembers = findViewById(R.id.rvOnlineMembers);
        if (rvOnlineMembers != null) {
            rvOnlineMembers.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2, androidx.recyclerview.widget.GridLayoutManager.HORIZONTAL, false));
            onlineAdapter = new OnlineMemberAdapter(onlineMemberList);
            rvOnlineMembers.setAdapter(onlineAdapter);
        }

        tabContribution = findViewById(R.id.tabContribution);
        tabRoom = findViewById(R.id.tabRoom);
        indicatorContribution = findViewById(R.id.indicatorContribution);
        indicatorRoom = findViewById(R.id.indicatorRoom);
        layoutContributionContent = findViewById(R.id.layoutContributionContent);
        layoutRoomContent = findViewById(R.id.layoutRoomContent);
        cardFamilyRoomContainer = findViewById(R.id.cardFamilyRoomContainer);
        tvFamilyRoomTitle = findViewById(R.id.tvFamilyRoomTitle);

        if (tabContribution != null) {
            tabContribution.setOnClickListener(v -> selectSubTab("Contribution"));
        }
        if (tabRoom != null) {
            tabRoom.setOnClickListener(v -> selectSubTab("Room"));
        }

        if (cardFamilyRoomContainer != null) {
            cardFamilyRoomContainer.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(familyId)) {
                    Intent intent = new Intent(FamilyDetailActivity.this, RoomChatActivity.class);
                    intent.putExtra("roomID", familyId);
                    intent.putExtra("roomTitle", familyName != null ? familyName : "Family Room");
                    startActivity(intent);
                }
            });
        }

        btnDetailFilterDaily = findViewById(R.id.btnDetailFilterDaily);
        btnDetailFilterWeekly = findViewById(R.id.btnDetailFilterWeekly);
        btnDetailFilterSeason = findViewById(R.id.btnDetailFilterSeason);

        tvDetailSelectedDate = findViewById(R.id.tvDetailSelectedDate);
        btnDetailPrevDate = findViewById(R.id.btnDetailPrevDate);
        btnDetailNextDate = findViewById(R.id.btnDetailNextDate);

        podium1 = findViewById(R.id.podium1);
        podium2 = findViewById(R.id.podium2);
        podium3 = findViewById(R.id.podium3);

        tvPodium1Name = findViewById(R.id.tvPodium1Name);
        tvPodium1Exp = findViewById(R.id.tvPodium1Exp);
        ivPodium1Avatar = findViewById(R.id.ivPodium1Avatar);

        tvPodium2Name = findViewById(R.id.tvPodium2Name);
        tvPodium2Exp = findViewById(R.id.tvPodium2Exp);
        ivPodium2Avatar = findViewById(R.id.ivPodium2Avatar);

        tvPodium3Name = findViewById(R.id.tvPodium3Name);
        tvPodium3Exp = findViewById(R.id.tvPodium3Exp);
        ivPodium3Avatar = findViewById(R.id.ivPodium3Avatar);

        rvMemberContributionList = findViewById(R.id.rvMemberContributionList);
        if (rvMemberContributionList != null) {
            rvMemberContributionList.setLayoutManager(new LinearLayoutManager(this));
            memberAdapter = new MemberContributionAdapter(memberList);
            rvMemberContributionList.setAdapter(memberAdapter);
        }

        layoutBottomJoinBar = findViewById(R.id.layoutBottomJoinBar);
        layoutBottomChatBar = findViewById(R.id.layoutBottomChatBar);
        ivBottomUserAvatar = findViewById(R.id.ivBottomUserAvatar);
        tvBottomUserMessage = findViewById(R.id.tvBottomUserMessage);
        btnBottomQuickChat = findViewById(R.id.btnBottomQuickChat);

        if (layoutBottomChatBar != null) {
            layoutBottomChatBar.setOnClickListener(v -> openFamilyGroupChat());
        }
        if (btnBottomQuickChat != null) {
            btnBottomQuickChat.setOnClickListener(v -> openFamilyGroupChat());
        }

        btnJoinFamily = findViewById(R.id.btnJoinFamily);
        if (btnJoinFamily != null) {
            btnJoinFamily.setOnClickListener(v -> joinFamily());
        }
    }

    private void bindIntentData() {
        if (!TextUtils.isEmpty(familyName)) {
            if (tvFamilyTitle != null) tvFamilyTitle.setText(familyName);
        }

        if (!TextUtils.isEmpty(familyLogo)) {
            if (ivFamilyCrest != null) {
                Glide.with(this).load(familyLogo).into(ivFamilyCrest);
            }
            if (ivHeaderBackground != null) {
                Glide.with(this)
                        .load(familyLogo)
                        .transform(new BlurTransformation(this, 25f))
                        .into(ivHeaderBackground);
            }
        }
    }

    private void selectSubTab(String subTab) {
        if ("Room".equalsIgnoreCase(subTab)) {
            if (tabRoom != null) tabRoom.setTextColor(Color.WHITE);
            if (indicatorRoom != null) indicatorRoom.setVisibility(View.VISIBLE);
            if (tabContribution != null) tabContribution.setTextColor(Color.parseColor("#70889B"));
            if (indicatorContribution != null) indicatorContribution.setVisibility(View.GONE);
            if (layoutRoomContent != null) layoutRoomContent.setVisibility(View.VISIBLE);
            if (layoutContributionContent != null) layoutContributionContent.setVisibility(View.GONE);
        } else {
            if (tabContribution != null) tabContribution.setTextColor(Color.WHITE);
            if (indicatorContribution != null) indicatorContribution.setVisibility(View.VISIBLE);
            if (tabRoom != null) tabRoom.setTextColor(Color.parseColor("#70889B"));
            if (indicatorRoom != null) indicatorRoom.setVisibility(View.GONE);
            if (layoutContributionContent != null) layoutContributionContent.setVisibility(View.VISIBLE);
            if (layoutRoomContent != null) layoutRoomContent.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        if (btnDetailFilterDaily != null) {
            btnDetailFilterDaily.setOnClickListener(v -> selectPeriod("Daily"));
        }
        if (btnDetailFilterWeekly != null) {
            btnDetailFilterWeekly.setOnClickListener(v -> selectPeriod("Weekly"));
        }
        if (btnDetailFilterSeason != null) {
            btnDetailFilterSeason.setOnClickListener(v -> selectPeriod("Season"));
        }

        if (btnDetailPrevDate != null) {
            btnDetailPrevDate.setOnClickListener(v -> {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadFamilyData();
            });
        }

        if (btnDetailNextDate != null) {
            btnDetailNextDate.setOnClickListener(v -> {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadFamilyData();
            });
        }
    }

    private void selectPeriod(String period) {
        activePeriod = period;

        int activeColor = Color.parseColor("#3D2600");
        int inactiveColor = Color.parseColor("#1A232A");

        if (btnDetailFilterDaily != null) {
            boolean active = "Daily".equals(period);
            btnDetailFilterDaily.setBackgroundResource(active ? R.drawable.bg_family_filter_active : R.drawable.bg_family_filter_inactive);
            btnDetailFilterDaily.setTextColor(active ? activeColor : inactiveColor);
        }

        if (btnDetailFilterWeekly != null) {
            boolean active = "Weekly".equals(period);
            btnDetailFilterWeekly.setBackgroundResource(active ? R.drawable.bg_family_filter_active : R.drawable.bg_family_filter_inactive);
            btnDetailFilterWeekly.setTextColor(active ? activeColor : inactiveColor);
        }

        if (btnDetailFilterSeason != null) {
            boolean active = "Season".equals(period);
            btnDetailFilterSeason.setBackgroundResource(active ? R.drawable.bg_family_filter_active : R.drawable.bg_family_filter_inactive);
            btnDetailFilterSeason.setTextColor(active ? activeColor : inactiveColor);
        }

        loadFamilyData();
    }

    private void updateDateDisplay() {
        if (tvDetailSelectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            tvDetailSelectedDate.setText(sdf.format(selectedCalendar.getTime()));
        }
    }

    private void loadFamilyDetailData() {
        if (TextUtils.isEmpty(familyId)) return;

        DatabaseReference familyRef = FirebaseDatabase.getInstance().getReference("families").child(familyId);
        familyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("familyName").getValue(String.class);
                    String logo = snapshot.child("familyLogo").getValue(String.class);
                    String notice = snapshot.child("familyNotice").getValue(String.class);
                    String tag = snapshot.child("familyTag").getValue(String.class);
                    String ownerUid = snapshot.child("ownerUid").getValue(String.class);
                    familyOwnerUid = ownerUid;

                    String frame = snapshot.child("familyFrame").getValue(String.class);
                    if (frame == null) frame = snapshot.child("frameUrl").getValue(String.class);
                    if (frame == null) frame = snapshot.child("frame").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        if (tvFamilyTitle != null) tvFamilyTitle.setText(name);
                        if (tvFamilyRoomTitle != null) tvFamilyRoomTitle.setText(name);
                    }
                    if (!TextUtils.isEmpty(logo)) {
                        if (ivFamilyCrest != null) {
                            Glide.with(FamilyDetailActivity.this).load(logo).into(ivFamilyCrest);
                        }
                        if (ivHeaderBackground != null) {
                            Glide.with(FamilyDetailActivity.this)
                                    .load(logo)
                                    .transform(new BlurTransformation(FamilyDetailActivity.this, 25f))
                                    .into(ivHeaderBackground);
                        }
                    }
                    if (!TextUtils.isEmpty(frame)) {
                        if (frame.startsWith("http://") || frame.startsWith("https://")) {
                            if (ivFamilyFrame != null) {
                                FrameUtils.clearFrame(ivFamilyFrame, svgaFamilyFrame);
                                ivFamilyFrame.setVisibility(View.VISIBLE);
                                Glide.with(FamilyDetailActivity.this)
                                        .load(frame)
                                        .into(ivFamilyFrame);
                            }
                        } else {
                            FrameUtils.displayFrame(FamilyDetailActivity.this, frame, ivFamilyFrame, svgaFamilyFrame);
                        }
                    } else {
                        FrameUtils.clearFrame(ivFamilyFrame, svgaFamilyFrame);
                    }
                    if (!TextUtils.isEmpty(notice) && tvFamilyNoticeText != null) {
                        tvFamilyNoticeText.setText(notice);
                    }
                    if (tvFamilyTagBadge != null) {
                        tvFamilyTagBadge.setText(!TextUtils.isEmpty(tag) ? "1 " + tag : "1 " + name);
                    }

                    Long expScore = snapshot.child("score").getValue(Long.class);
                    if (expScore == null) expScore = snapshot.child("exp").getValue(Long.class);
                    long currentExp = expScore != null ? expScore : 0;

                    Long trophiesVal = snapshot.child("trophies").getValue(Long.class);
                    int trophies = trophiesVal != null ? trophiesVal.intValue() : 0;

                    updateFamilyLevelAndExp(currentExp, trophies);

                    if (!TextUtils.isEmpty(ownerUid)) {
                        loadLeaderInfo(ownerUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Query real member count from "family_members"
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId);
        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.exists() ? snapshot.getChildrenCount() : 1;
                if (tvMemberCount != null) {
                    tvMemberCount.setText("👥 " + count + "/60 ›");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        checkMembershipState();
        loadFamilyData();
    }

    private void updateFamilyLevelAndExp(long exp, int trophies) {
        String levelName;
        long maxExp;
        int shieldDrawableRes;

        if (exp < 2000) {
            // Level 1 - Bronze
            levelName = "Bronze I";
            maxExp = 2000;
            shieldDrawableRes = R.drawable.ic_family_shield_bronze;
        } else if (exp < 5000) {
            // Level 2 - Silver
            levelName = "Silver I";
            maxExp = 5000;
            shieldDrawableRes = R.drawable.ic_family_shield_silver;
        } else if (exp < 10000) {
            // Level 3 - Gold
            levelName = "Gold I";
            maxExp = 10000;
            shieldDrawableRes = R.drawable.ic_family_shield_gold;
        } else if (exp < 20000) {
            // Level 4 - Platinum
            levelName = "Platinum I";
            maxExp = 20000;
            shieldDrawableRes = R.drawable.ic_family_shield_gold;
        } else {
            // Level 5 - Diamond
            levelName = "Diamond I";
            maxExp = 50000;
            shieldDrawableRes = R.drawable.ic_family_shield_gold;
        }

        long needExp = Math.max(0, maxExp - exp);
        int progressPercent = (int) Math.min(100, (exp * 100) / maxExp);

        if (tvLevelTitle != null) {
            tvLevelTitle.setText(levelName + "  🏆 " + trophies);
        }
        if (tvLevelNeed != null) {
            tvLevelNeed.setText("Need " + needExp + " EXP to next level");
        }
        if (pbExpProgress != null) {
            pbExpProgress.setProgress(progressPercent);
        }
        if (tvExpText != null) {
            tvExpText.setText(exp + "/" + maxExp);
        }
        if (ivLevelShield != null) {
            ivLevelShield.setImageResource(shieldDrawableRes);
        }
    }

    private void checkMembershipState() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String myUid = currentUser.getUid();
        DatabaseReference myUserRef = FirebaseDatabase.getInstance().getReference("users").child(myUid);
        myUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String myFamilyId = snapshot.child("familyId").getValue(String.class);
                String myAvatar = snapshot.child("profilePic").getValue(String.class);
                if (myAvatar == null) myAvatar = snapshot.child("avatar").getValue(String.class);
                String myName = snapshot.child("name").getValue(String.class);
                if (myName == null) myName = snapshot.child("username").getValue(String.class);

                boolean isOwner = (!TextUtils.isEmpty(familyOwnerUid) && familyOwnerUid.equals(myUid));
                boolean isMember = (!TextUtils.isEmpty(myFamilyId) && myFamilyId.equals(familyId)) || isOwner;

                if (isMember) {
                    if (isOwner && TextUtils.isEmpty(myFamilyId) && !TextUtils.isEmpty(familyId)) {
                        myUserRef.child("familyId").setValue(familyId);
                        if (!TextUtils.isEmpty(familyName)) myUserRef.child("familyName").setValue(familyName);
                    }

                    if (cardFamilyTask != null) cardFamilyTask.setVisibility(View.VISIBLE);
                    if (layoutBottomJoinBar != null) layoutBottomJoinBar.setVisibility(View.GONE);
                    if (layoutBottomChatBar != null) {
                        layoutBottomChatBar.setVisibility(View.VISIBLE);
                    }
                    startGroupMessageTicker();
                } else {
                    if (cardFamilyTask != null) cardFamilyTask.setVisibility(View.GONE);
                    if (layoutBottomChatBar != null) layoutBottomChatBar.setVisibility(View.GONE);
                    if (layoutBottomJoinBar != null) layoutBottomJoinBar.setVisibility(View.VISIBLE);
                    
                    if (!TextUtils.isEmpty(myFamilyId)) {
                        if (btnJoinFamily != null) {
                            btnJoinFamily.setText("Joined another family");
                            btnJoinFamily.setAlpha(0.6f);
                            btnJoinFamily.setOnClickListener(v -> Toast.makeText(FamilyDetailActivity.this, "Already in another family", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        checkPendingJoinRequest(myUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkPendingJoinRequest(String uid) {
        if (TextUtils.isEmpty(familyId) || TextUtils.isEmpty(uid)) return;

        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("family_join_requests").child(familyId).child(uid);
        reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equalsIgnoreCase(status)) {
                        if (btnJoinFamily != null) {
                            btnJoinFamily.setText("Requested");
                            btnJoinFamily.setAlpha(0.6f);
                            btnJoinFamily.setOnClickListener(v -> Toast.makeText(FamilyDetailActivity.this, "Join request pending leader approval!", Toast.LENGTH_SHORT).show());
                        }
                        return;
                    }
                }

                if (btnJoinFamily != null) {
                    btnJoinFamily.setText("Join");
                    btnJoinFamily.setAlpha(1.0f);
                    btnJoinFamily.setOnClickListener(v -> requestJoinFamily());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (btnJoinFamily != null) {
                    btnJoinFamily.setText("Join");
                    btnJoinFamily.setAlpha(1.0f);
                    btnJoinFamily.setOnClickListener(v -> requestJoinFamily());
                }
            }
        });
    }

    private void loadLeaderInfo(String ownerUid) {
        DatabaseReference leaderRef = FirebaseDatabase.getInstance().getReference("users").child(ownerUid);
        leaderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name == null) name = snapshot.child("username").getValue(String.class);

                    String pic = snapshot.child("profilePic").getValue(String.class);
                    if (pic == null) pic = snapshot.child("avatar").getValue(String.class);
                    if (pic == null) pic = snapshot.child("image").getValue(String.class);

                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    if (equippedFrame == null) equippedFrame = snapshot.child("equippedFrame").getValue(String.class);
                    if (equippedFrame == null) equippedFrame = snapshot.child("frame").getValue(String.class);

                    if (!TextUtils.isEmpty(name) && tvLeaderName != null) {
                        tvLeaderName.setText(name);
                    }

                    // Bind Leader Profile Pic to ivFamilyCrest & ivLeaderAvatar
                    if (!TextUtils.isEmpty(pic)) {
                        if (ivLeaderAvatar != null) {
                            Glide.with(FamilyDetailActivity.this)
                                    .load(pic)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .circleCrop()
                                    .into(ivLeaderAvatar);
                        }
                        if (ivFamilyCrest != null) {
                            Glide.with(FamilyDetailActivity.this)
                                    .load(pic)
                                    .circleCrop()
                                    .into(ivFamilyCrest);
                        }
                    }

                    // Bind Leader Equipped Frame/Banner on both Crest and Leader Avatar
                    if (!TextUtils.isEmpty(equippedFrame)) {
                        FrameUtils.displayFrame(FamilyDetailActivity.this, equippedFrame, ivFamilyFrame, svgaFamilyFrame);
                        FrameUtils.displayFrame(FamilyDetailActivity.this, equippedFrame, ivLeaderFrame, svgaLeaderFrame);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadFamilyData() {
        if (TextUtils.isEmpty(familyId)) {
            populateDefaultMembers();
            return;
        }

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId);
        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    List<String> memberUids = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uid = ds.getKey();
                        if (uid != null && !uid.trim().isEmpty()) {
                            memberUids.add(uid);
                        }
                    }

                    if (!memberUids.isEmpty()) {
                        fetchMemberDetailsAndBind(memberUids);
                    } else {
                        populateDefaultMembers();
                    }
                } else {
                    populateDefaultMembers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                populateDefaultMembers();
            }
        });
    }

    private void fetchMemberDetailsAndBind(List<String> memberUids) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = currentUser != null ? currentUser.getUid() : null;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onlineMemberList.clear();
                List<MemberContributionModel> allMembers = new ArrayList<>();

                for (String uid : memberUids) {
                    if (snapshot.hasChild(uid)) {
                        DataSnapshot uSnap = snapshot.child(uid);
                        String name = uSnap.child("name").getValue(String.class);
                        if (name == null) name = uSnap.child("username").getValue(String.class);
                        if (name == null) name = "Member";

                        String pic = uSnap.child("profilePic").getValue(String.class);
                        if (pic == null) pic = uSnap.child("avatar").getValue(String.class);
                        if (pic == null) pic = uSnap.child("image").getValue(String.class);
                        if (pic == null) pic = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200";

                        Boolean onlineVal = uSnap.child("isOnline").getValue(Boolean.class);
                        if (onlineVal == null) onlineVal = uSnap.child("online").getValue(Boolean.class);
                        String statusStr = uSnap.child("status").getValue(String.class);

                        boolean isOnline = (onlineVal != null && onlineVal) || "online".equalsIgnoreCase(statusStr);
                        if (myUid != null && myUid.equals(uid)) {
                            isOnline = true; // Current user active on device
                        }

                        Long expVal = uSnap.child("score").getValue(Long.class);
                        if (expVal == null) expVal = uSnap.child("exp").getValue(Long.class);
                        if (expVal == null) expVal = uSnap.child("contribution").getValue(Long.class);
                        long exp = expVal != null ? expVal : 0;

                        if (isOnline) {
                            onlineMemberList.add(new OnlineMemberModel(name, pic, true));
                        }

                        allMembers.add(new MemberContributionModel(uid, name, pic, 0, exp));
                    }
                }

                if (onlineMemberList.isEmpty()) {
                    onlineMemberList.add(new OnlineMemberModel("+DEVIL😈", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", true));
                    onlineMemberList.add(new OnlineMemberModel("+ANGEL✨", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200", true));
                }

                if (tvOnlineCount != null) {
                    tvOnlineCount.setText(onlineMemberList.size() + " Online ›");
                }

                if (onlineAdapter != null) {
                    onlineAdapter.notifyDataSetChanged();
                }

                // Sort members by contribution score
                Collections.sort(allMembers, (a, b) -> Long.compare(b.getExp(), a.getExp()));

                // Bind Manager Avatars
                if (ivManager1 != null && allMembers.size() > 0) Glide.with(FamilyDetailActivity.this).load(allMembers.get(0).getAvatar()).circleCrop().into(ivManager1);
                if (ivManager2 != null && allMembers.size() > 1) Glide.with(FamilyDetailActivity.this).load(allMembers.get(1).getAvatar()).circleCrop().into(ivManager2);
                if (ivManager3 != null && allMembers.size() > 2) Glide.with(FamilyDetailActivity.this).load(allMembers.get(2).getAvatar()).circleCrop().into(ivManager3);
                if (ivManager4 != null && allMembers.size() > 3) Glide.with(FamilyDetailActivity.this).load(allMembers.get(3).getAvatar()).circleCrop().into(ivManager4);

                // Bind Top 3 Podium
                if (!allMembers.isEmpty()) {
                    MemberContributionModel m1 = allMembers.get(0);
                    if (tvPodium1Name != null) tvPodium1Name.setText(m1.getName());
                    if (tvPodium1Exp != null) tvPodium1Exp.setText(m1.getExp() + " EXP");
                    if (ivPodium1Avatar != null && !TextUtils.isEmpty(m1.getAvatar())) {
                        Glide.with(FamilyDetailActivity.this).load(m1.getAvatar()).circleCrop().into(ivPodium1Avatar);
                    }
                    if (podium1 != null) {
                        podium1.setOnClickListener(v -> openUserProfile(m1.getId()));
                    }
                }

                if (allMembers.size() > 1) {
                    MemberContributionModel m2 = allMembers.get(1);
                    if (tvPodium2Name != null) tvPodium2Name.setText(m2.getName());
                    if (tvPodium2Exp != null) tvPodium2Exp.setText(m2.getExp() + " EXP");
                    if (ivPodium2Avatar != null && !TextUtils.isEmpty(m2.getAvatar())) {
                        Glide.with(FamilyDetailActivity.this).load(m2.getAvatar()).circleCrop().into(ivPodium2Avatar);
                    }
                    if (podium2 != null) {
                        podium2.setOnClickListener(v -> openUserProfile(m2.getId()));
                    }
                }

                if (allMembers.size() > 2) {
                    MemberContributionModel m3 = allMembers.get(2);
                    if (tvPodium3Name != null) tvPodium3Name.setText(m3.getName());
                    if (tvPodium3Exp != null) tvPodium3Exp.setText(m3.getExp() + " EXP");
                    if (ivPodium3Avatar != null && !TextUtils.isEmpty(m3.getAvatar())) {
                        Glide.with(FamilyDetailActivity.this).load(m3.getAvatar()).circleCrop().into(ivPodium3Avatar);
                    }
                    if (podium3 != null) {
                        podium3.setOnClickListener(v -> openUserProfile(m3.getId()));
                    }
                }

                // Ranks 4+
                memberList.clear();
                for (int i = 3; i < allMembers.size(); i++) {
                    MemberContributionModel m = allMembers.get(i);
                    memberList.add(new MemberContributionModel(m.getName(), m.getName(), m.getAvatar(), i + 1, m.getExp()));
                }

                if (memberList.isEmpty()) {
                    memberList.add(new MemberContributionModel("m4", "SHEER", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", 4, 33));
                    memberList.add(new MemberContributionModel("m5", "i m CASH 💸", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200", 5, 28));
                }

                if (memberAdapter != null) {
                    memberAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                populateDefaultMembers();
            }
        });
    }

    private void openUserProfile(String targetUid) {
        if (TextUtils.isEmpty(targetUid)) return;
        Intent intent = new Intent(FamilyDetailActivity.this, UserDetailActivity.class);
        intent.putExtra("selectedUserId", targetUid);
        intent.putExtra("uid", targetUid);
        startActivity(intent);
    }

    private void populateDefaultMembers() {
        // Online Members
        onlineMemberList.clear();
        onlineMemberList.add(new OnlineMemberModel("+DEVIL😈", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("+ANGEL✨", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("Sonu", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("{$}", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("😁🔥 Hot 🔥..", "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("SANIYA", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("SAJJU", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("🌻Aarohi🌵", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("i m CASH 💸", "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=200", true));
        onlineMemberList.add(new OnlineMemberModel("🕸️SHEER🕸️", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=200", true));

        if (onlineAdapter != null) {
            onlineAdapter.notifyDataSetChanged();
        }

        // Top 3 Podium
        if (tvPodium1Name != null) tvPodium1Name.setText("+ANGEL✨");
        if (tvPodium1Exp != null) tvPodium1Exp.setText("47 EXP");

        if (tvPodium2Name != null) tvPodium2Name.setText("+DEVIL😈");
        if (tvPodium2Exp != null) tvPodium2Exp.setText("41 EXP");

        if (tvPodium3Name != null) tvPodium3Name.setText("{$}");
        if (tvPodium3Exp != null) tvPodium3Exp.setText("40 EXP");

        // Ranks 4+
        memberList.clear();
        memberList.add(new MemberContributionModel("m4", "🕸️SHEER🕸️", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=200", 4, 33));
        memberList.add(new MemberContributionModel("m5", "i m CASH 💸", "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=200", 5, 28));
        memberList.add(new MemberContributionModel("m6", "SAJJU", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200", 6, 27));

        if (memberAdapter != null) {
            memberAdapter.notifyDataSetChanged();
        }
    }

    private void showFamilyOptionsMenuDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_family_options, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        TextView btnOptionRules = view.findViewById(R.id.btnOptionRules);
        TextView btnOptionInvite = view.findViewById(R.id.btnOptionInvite);
        TextView btnOptionRequests = view.findViewById(R.id.btnOptionRequests);
        View dividerOptionRequests = view.findViewById(R.id.dividerOptionRequests);
        TextView btnOptionDelete = view.findViewById(R.id.btnOptionDelete);
        View dividerOptionDelete = view.findViewById(R.id.dividerOptionDelete);
        TextView btnOptionQuit = view.findViewById(R.id.btnOptionQuit);
        TextView btnOptionCancel = view.findViewById(R.id.btnOptionCancel);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLeader = currentUser != null && familyOwnerUid != null && familyOwnerUid.equals(currentUser.getUid());

        if (isLeader) {
            if (btnOptionRequests != null) {
                btnOptionRequests.setVisibility(View.VISIBLE);
                btnOptionRequests.setOnClickListener(v -> {
                    dialog.dismiss();
                    showJoinRequestsDialog();
                });
            }
            if (dividerOptionRequests != null) dividerOptionRequests.setVisibility(View.VISIBLE);

            if (btnOptionDelete != null) {
                btnOptionDelete.setVisibility(View.VISIBLE);
                btnOptionDelete.setOnClickListener(v -> {
                    dialog.dismiss();
                    showDeleteFamilyConfirmationDialog();
                });
            }
            if (dividerOptionDelete != null) dividerOptionDelete.setVisibility(View.VISIBLE);
        }

        if (btnOptionRules != null) {
            btnOptionRules.setOnClickListener(v -> {
                dialog.dismiss();
                showFamilyRulesDialog();
            });
        }

        if (btnOptionInvite != null) {
            btnOptionInvite.setOnClickListener(v -> {
                dialog.dismiss();
                shareFamilyInvite();
            });
        }

        if (btnOptionQuit != null) {
            btnOptionQuit.setOnClickListener(v -> {
                dialog.dismiss();
                showQuitFamilyConfirmationDialog();
            });
        }

        if (btnOptionCancel != null) {
            btnOptionCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showFamilyRulesDialog() {
        String notice = tvFamilyNoticeText != null ? tvFamilyNoticeText.getText().toString() : "Follow all family rules.";
        new AlertDialog.Builder(this)
                .setTitle("Family Rules")
                .setMessage(notice)
                .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void shareFamilyInvite() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my family " + (familyName != null ? familyName : "") + " on Room Chat!");
        startActivity(Intent.createChooser(shareIntent, "Invite Member"));
    }

    private void showQuitFamilyConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Family")
                .setMessage("Are you sure you want to quit this family?")
                .setPositiveButton("Quit", (dialog, which) -> quitFamily())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDeleteFamilyConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Family")
                .setMessage("Are you sure you want to permanently delete this family? All members will be removed and this action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteFamily())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteFamily() {
        if (TextUtils.isEmpty(familyId)) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();
        if (TextUtils.isEmpty(familyOwnerUid) || !familyOwnerUid.equals(currentUid)) {
            Toast.makeText(this, "Only the family leader can delete the family!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId);
        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String mUid = ds.getKey();
                        if (mUid != null) {
                            usersRef.child(mUid).child("familyId").removeValue();
                            usersRef.child(mUid).child("familyName").removeValue();
                            usersRef.child(mUid).child("pendingFamilyJoinRequestId").removeValue();
                        }
                    }
                }
                usersRef.child(currentUid).child("familyId").removeValue();
                usersRef.child(currentUid).child("familyName").removeValue();
                usersRef.child(currentUid).child("pendingFamilyJoinRequestId").removeValue();

                FirebaseDatabase.getInstance().getReference("families").child(familyId).removeValue();
                FirebaseDatabase.getInstance().getReference("family_members").child(familyId).removeValue();
                FirebaseDatabase.getInstance().getReference("family_rankings").child(familyId).removeValue();
                FirebaseDatabase.getInstance().getReference("family_join_requests").child(familyId).removeValue();
                FirebaseDatabase.getInstance().getReference("family_member_contributions").child(familyId).removeValue();

                Toast.makeText(FamilyDetailActivity.this, "Family deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FamilyDetailActivity.this, "Failed to delete family", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void quitFamily() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("familyId").removeValue();
        userRef.child("familyName").removeValue();
        userRef.child("pendingFamilyJoinRequestId").removeValue();

        if (!TextUtils.isEmpty(familyId)) {
            DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId).child(uid);
            memberRef.removeValue();
        }

        Toast.makeText(this, "Left family successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void joinFamily() {
        requestJoinFamily();
    }

    private void requestJoinFamily() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        if (!TextUtils.isEmpty(familyOwnerUid) && familyOwnerUid.equals(uid)) {
            Toast.makeText(this, "You are the leader of this family!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(familyId)) {
            Toast.makeText(this, "Invalid family", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentFamilyId = snapshot.child("familyId").getValue(String.class);
                if (!TextUtils.isEmpty(currentFamilyId)) {
                    Toast.makeText(FamilyDetailActivity.this, "You are already in a family. Leave your current family first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                if (name == null) name = snapshot.child("username").getValue(String.class);
                if (name == null) name = "User";

                String avatar = snapshot.child("profilePic").getValue(String.class);
                if (avatar == null) avatar = snapshot.child("avatar").getValue(String.class);

                String prevPendingFamilyId = snapshot.child("pendingFamilyJoinRequestId").getValue(String.class);
                if (!TextUtils.isEmpty(prevPendingFamilyId) && !prevPendingFamilyId.equals(familyId)) {
                    // Cancel previous pending request
                    FirebaseDatabase.getInstance().getReference("family_join_requests").child(prevPendingFamilyId).child(uid).removeValue();
                }

                // Write new pending join request
                DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("family_join_requests").child(familyId).child(uid);
                Map<String, Object> reqMap = new HashMap<>();
                reqMap.put("applicantUid", uid);
                reqMap.put("applicantName", name);
                reqMap.put("applicantAvatar", avatar != null ? avatar : "");
                reqMap.put("familyId", familyId);
                reqMap.put("familyName", familyName != null ? familyName : "");
                reqMap.put("status", "pending");
                reqMap.put("timestamp", System.currentTimeMillis());

                reqRef.setValue(reqMap);
                userRef.child("pendingFamilyJoinRequestId").setValue(familyId);

                if (!TextUtils.isEmpty(familyOwnerUid)) {
                    NotificationHelper.sendNotification(
                            familyOwnerUid,
                            "Family Join Request 👥",
                            name + " requested to join " + (familyName != null ? familyName : "your family"),
                            "FAMILY_JOIN_REQUEST",
                            familyId
                    );
                }

                Toast.makeText(FamilyDetailActivity.this, "Join request sent! Previous pending request canceled.", Toast.LENGTH_SHORT).show();
                if (btnJoinFamily != null) {
                    btnJoinFamily.setText("Requested");
                    btnJoinFamily.setAlpha(0.6f);
                    btnJoinFamily.setOnClickListener(v -> Toast.makeText(FamilyDetailActivity.this, "Join request pending leader approval!", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showJoinRequestsDialog() {
        if (TextUtils.isEmpty(familyId)) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_family_join_requests, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        ImageView btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        TextView tvEmptyRequests = view.findViewById(R.id.tvEmptyRequests);
        RecyclerView rvRequests = view.findViewById(R.id.rvJoinRequestsList);
        List<FamilyJoinRequestModel> requestList = new ArrayList<>();

        if (rvRequests != null) {
            rvRequests.setLayoutManager(new LinearLayoutManager(this));
            FamilyJoinRequestAdapter reqAdapter = new FamilyJoinRequestAdapter(requestList, new JoinRequestListener() {
                @Override
                public void onAccept(FamilyJoinRequestModel item) {
                    acceptJoinRequest(item);
                    requestList.remove(item);
                    if (rvRequests.getAdapter() != null) rvRequests.getAdapter().notifyDataSetChanged();
                    if (requestList.isEmpty() && tvEmptyRequests != null) tvEmptyRequests.setVisibility(View.VISIBLE);
                }

                @Override
                public void onReject(FamilyJoinRequestModel item) {
                    rejectJoinRequest(item);
                    requestList.remove(item);
                    if (rvRequests.getAdapter() != null) rvRequests.getAdapter().notifyDataSetChanged();
                    if (requestList.isEmpty() && tvEmptyRequests != null) tvEmptyRequests.setVisibility(View.VISIBLE);
                }
            });
            rvRequests.setAdapter(reqAdapter);

            DatabaseReference reqsRef = FirebaseDatabase.getInstance().getReference("family_join_requests").child(familyId);
            reqsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    requestList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String status = child.child("status").getValue(String.class);
                            if ("pending".equalsIgnoreCase(status)) {
                                String appUid = child.child("applicantUid").getValue(String.class);
                                String appName = child.child("applicantName").getValue(String.class);
                                String appAvatar = child.child("applicantAvatar").getValue(String.class);
                                Long ts = child.child("timestamp").getValue(Long.class);
                                long timestamp = ts != null ? ts : System.currentTimeMillis();

                                requestList.add(new FamilyJoinRequestModel(appUid, appName, appAvatar, familyId, familyName, status, timestamp));
                            }
                        }
                    }

                    if (requestList.isEmpty()) {
                        if (tvEmptyRequests != null) tvEmptyRequests.setVisibility(View.VISIBLE);
                        if (rvRequests != null) rvRequests.setVisibility(View.GONE);
                    } else {
                        if (tvEmptyRequests != null) tvEmptyRequests.setVisibility(View.GONE);
                        if (rvRequests != null) rvRequests.setVisibility(View.VISIBLE);
                        reqAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        dialog.show();
    }

    private void openFamilyGroupChat() {
        if (TextUtils.isEmpty(familyId)) {
            Toast.makeText(this, "Family details loading, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(FamilyDetailActivity.this, ChatActivity.class);
        intent.putExtra("receiverId", "family_" + familyId);
        intent.putExtra("familyId", familyId);
        intent.putExtra("isGroup", true);
        intent.putExtra("receiverName", !TextUtils.isEmpty(familyName) ? familyName : "Family");
        startActivity(intent);
    }

    private void startGroupMessageTicker() {
        if (TextUtils.isEmpty(familyId)) return;

        if (groupMessagesRef != null && groupMessageTickerListener != null) {
            groupMessagesRef.removeEventListener(groupMessageTickerListener);
        }

        groupMessagesRef = FirebaseDatabase.getInstance().getReference("family_group_messages").child(familyId);
        groupMessageTickerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                groupTickerMessages.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String msg = ds.child("message").getValue(String.class);
                        String sUid = ds.child("senderId").getValue(String.class);
                        String sName = ds.child("senderName").getValue(String.class);
                        String sAvatar = ds.child("senderAvatar").getValue(String.class);

                        if (!TextUtils.isEmpty(msg)) {
                            groupTickerMessages.add(new GroupMessageTickerItem(
                                    sUid != null ? sUid : "",
                                    sName != null ? sName : "Member",
                                    sAvatar != null ? sAvatar : "",
                                    msg
                            ));
                        }
                    }
                }

                if (!groupTickerMessages.isEmpty()) {
                    currentTickerIndex = 0;
                    runTickerCycle();
                } else {
                    if (tvBottomUserMessage != null) {
                        tvBottomUserMessage.setText("Tap to open Family Group Chat 💬");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        groupMessagesRef.addValueEventListener(groupMessageTickerListener);
    }

    private void runTickerCycle() {
        if (tickerRunnable != null) {
            tickerHandler.removeCallbacks(tickerRunnable);
        }

        tickerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed() || groupTickerMessages.isEmpty()) return;

                if (currentTickerIndex >= groupTickerMessages.size()) {
                    currentTickerIndex = 0;
                }

                GroupMessageTickerItem item = groupTickerMessages.get(currentTickerIndex);
                if (tvBottomUserMessage != null) {
                    tvBottomUserMessage.setText(item.senderName + ": " + item.message);
                }

                if (ivBottomUserAvatar != null) {
                    if (!TextUtils.isEmpty(item.senderAvatar)) {
                        Glide.with(FamilyDetailActivity.this)
                                .load(item.senderAvatar)
                                .placeholder(R.drawable.logo_placeholder)
                                .circleCrop()
                                .into(ivBottomUserAvatar);
                    } else if (!TextUtils.isEmpty(item.senderUid)) {
                        FirebaseDatabase.getInstance().getReference("users").child(item.senderUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot uSnap) {
                                        if (isFinishing() || isDestroyed()) return;
                                        String pic = uSnap.child("profilePic").getValue(String.class);
                                        if (pic == null) pic = uSnap.child("avatar").getValue(String.class);
                                        if (!TextUtils.isEmpty(pic) && ivBottomUserAvatar != null) {
                                            item.senderAvatar = pic;
                                            Glide.with(FamilyDetailActivity.this)
                                                    .load(pic)
                                                    .placeholder(R.drawable.logo_placeholder)
                                                    .circleCrop()
                                                    .into(ivBottomUserAvatar);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                }

                currentTickerIndex++;
                tickerHandler.postDelayed(this, 3000);
            }
        };

        tickerHandler.post(tickerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tickerHandler != null && tickerRunnable != null) {
            tickerHandler.removeCallbacks(tickerRunnable);
        }
        if (groupMessagesRef != null && groupMessageTickerListener != null) {
            groupMessagesRef.removeEventListener(groupMessageTickerListener);
        }
    }

    public static class GroupMessageTickerItem {
        String senderUid;
        String senderName;
        String senderAvatar;
        String message;

        public GroupMessageTickerItem(String senderUid, String senderName, String senderAvatar, String message) {
            this.senderUid = senderUid;
            this.senderName = senderName;
            this.senderAvatar = senderAvatar;
            this.message = message;
        }
    }

    private void acceptJoinRequest(FamilyJoinRequestModel item) {
        if (item == null || TextUtils.isEmpty(item.getApplicantUid())) return;

        String appUid = item.getApplicantUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(appUid);
        userRef.child("familyId").setValue(familyId);
        if (!TextUtils.isEmpty(familyName)) {
            userRef.child("familyName").setValue(familyName);
        }
        userRef.child("pendingFamilyJoinRequestId").removeValue();

        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId).child(appUid);
        memberRef.setValue(true);

        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("family_join_requests").child(familyId).child(appUid);
        reqRef.child("status").setValue("accepted");

        // Initialize Family Group Chat entry for newly accepted member
        CreateFamilyActivity.initializeFamilyGroupChat(appUid, familyId, familyName, familyLogo);

        NotificationHelper.sendNotification(
                appUid,
                "Family Request Approved! 🎉",
                "Your request to join " + (familyName != null ? familyName : "family") + " was approved!",
                "FAMILY_JOIN_ACCEPTED",
                familyId
        );

        Toast.makeText(this, item.getApplicantName() + " accepted as family member!", Toast.LENGTH_SHORT).show();
    }

    private void rejectJoinRequest(FamilyJoinRequestModel item) {
        if (item == null || TextUtils.isEmpty(item.getApplicantUid())) return;

        String appUid = item.getApplicantUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(appUid);
        userRef.child("pendingFamilyJoinRequestId").removeValue();

        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("family_join_requests").child(familyId).child(appUid);
        reqRef.child("status").setValue("rejected");

        Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
    }

    public static class FamilyJoinRequestModel {
        private String applicantUid, applicantName, applicantAvatar, familyId, familyName, status;
        private long timestamp;

        public FamilyJoinRequestModel() {}

        public FamilyJoinRequestModel(String applicantUid, String applicantName, String applicantAvatar, String familyId, String familyName, String status, long timestamp) {
            this.applicantUid = applicantUid;
            this.applicantName = applicantName;
            this.applicantAvatar = applicantAvatar;
            this.familyId = familyId;
            this.familyName = familyName;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getApplicantUid() { return applicantUid; }
        public String getApplicantName() { return applicantName; }
        public String getApplicantAvatar() { return applicantAvatar; }
        public String getFamilyId() { return familyId; }
        public String getFamilyName() { return familyName; }
        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
    }

    public interface JoinRequestListener {
        void onAccept(FamilyJoinRequestModel item);
        void onReject(FamilyJoinRequestModel item);
    }

    public static class FamilyJoinRequestAdapter extends RecyclerView.Adapter<FamilyJoinRequestAdapter.ViewHolder> {
        private final List<FamilyJoinRequestModel> list;
        private final JoinRequestListener listener;

        public FamilyJoinRequestAdapter(List<FamilyJoinRequestModel> list, JoinRequestListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_join_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FamilyJoinRequestModel item = list.get(position);
            if (holder.tvName != null) holder.tvName.setText(item.getApplicantName());
            if (holder.ivAvatar != null && !TextUtils.isEmpty(item.getApplicantAvatar())) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getApplicantAvatar())
                        .placeholder(R.drawable.logo_placeholder)
                        .circleCrop()
                        .into(holder.ivAvatar);
            }
            if (holder.btnAccept != null) {
                holder.btnAccept.setOnClickListener(v -> {
                    if (listener != null) listener.onAccept(item);
                });
            }
            if (holder.btnReject != null) {
                holder.btnReject.setOnClickListener(v -> {
                    if (listener != null) listener.onReject(item);
                });
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, btnAccept, btnReject;
            ShapeableImageView ivAvatar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvApplicantName);
                btnAccept = itemView.findViewById(R.id.btnAcceptRequest);
                btnReject = itemView.findViewById(R.id.btnRejectRequest);
                ivAvatar = itemView.findViewById(R.id.ivApplicantAvatar);
            }
        }
    }

    // Inner Data Model & Adapter for Online Members
    public static class OnlineMemberModel {
        private String name, avatar;
        private boolean isOnline;

        public OnlineMemberModel(String name, String avatar, boolean isOnline) {
            this.name = name;
            this.avatar = avatar;
            this.isOnline = isOnline;
        }

        public String getName() { return name; }
        public String getAvatar() { return avatar; }
        public boolean isOnline() { return isOnline; }
    }

    public static class OnlineMemberAdapter extends RecyclerView.Adapter<OnlineMemberAdapter.ViewHolder> {
        private final List<OnlineMemberModel> list;

        public OnlineMemberAdapter(List<OnlineMemberModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnlineMemberModel item = list.get(position);
            if (holder.tvName != null) holder.tvName.setText(item.getName());
            if (holder.ivAvatar != null && !TextUtils.isEmpty(item.getAvatar())) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getAvatar())
                        .placeholder(R.drawable.logo_placeholder)
                        .circleCrop()
                        .into(holder.ivAvatar);
            }
            if (holder.vOnlineDot != null) {
                holder.vOnlineDot.setVisibility(item.isOnline() ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ShapeableImageView ivAvatar;
            View vOnlineDot;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvOnlineName);
                ivAvatar = itemView.findViewById(R.id.ivOnlineAvatar);
                vOnlineDot = itemView.findViewById(R.id.viewOnlineDot);
            }
        }
    }

    public static class BlurTransformation extends com.bumptech.glide.load.resource.bitmap.BitmapTransformation {
        private final android.content.Context context;
        private final float radius;

        public BlurTransformation(android.content.Context context, float radius) {
            this.context = context;
            this.radius = radius;
        }

        @Override
        protected android.graphics.Bitmap transform(@NonNull com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool pool, @NonNull android.graphics.Bitmap toTransform, int outWidth, int outHeight) {
            android.graphics.Bitmap sampled = android.graphics.Bitmap.createScaledBitmap(toTransform, Math.max(1, toTransform.getWidth() / 4), Math.max(1, toTransform.getHeight() / 4), true);
            android.graphics.Bitmap blurred = android.graphics.Bitmap.createBitmap(sampled.getWidth(), sampled.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);

            try {
                android.renderscript.RenderScript rs = android.renderscript.RenderScript.create(context);
                android.renderscript.Allocation input = android.renderscript.Allocation.createFromBitmap(rs, sampled);
                android.renderscript.Allocation output = android.renderscript.Allocation.createFromBitmap(rs, blurred);
                android.renderscript.ScriptIntrinsicBlur script = android.renderscript.ScriptIntrinsicBlur.create(rs, android.renderscript.Element.U8_4(rs));
                script.setRadius(Math.max(1f, Math.min(25f, radius)));
                script.setInput(input);
                script.forEach(output);
                output.copyTo(blurred);
                rs.destroy();
                return blurred;
            } catch (Exception e) {
                return sampled;
            }
        }

        @Override
        public void updateDiskCacheKey(@NonNull java.security.MessageDigest messageDigest) {
            messageDigest.update(("blur_" + radius).getBytes());
        }
    }

    // Inner Data Model & Adapter for Contribution List
    public static class MemberContributionModel {
        private String id, name, avatar;
        private int rank;
        private long exp;

        public MemberContributionModel(String id, String name, String avatar, int rank, long exp) {
            this.id = id;
            this.name = name;
            this.avatar = avatar;
            this.rank = rank;
            this.exp = exp;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAvatar() { return avatar; }
        public int getRank() { return rank; }
        public long getExp() { return exp; }
    }

    public static class MemberContributionAdapter extends RecyclerView.Adapter<MemberContributionAdapter.ViewHolder> {
        private final List<MemberContributionModel> list;

        public MemberContributionAdapter(List<MemberContributionModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_member_rank, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MemberContributionModel item = list.get(position);
            if (holder.tvRank != null) holder.tvRank.setText(String.valueOf(item.getRank()));
            if (holder.tvName != null) holder.tvName.setText(item.getName());
            if (holder.tvExp != null) holder.tvExp.setText(String.valueOf(item.getExp()));

            if (holder.ivAvatar != null && !TextUtils.isEmpty(item.getAvatar())) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getAvatar())
                        .placeholder(R.drawable.logo_placeholder)
                        .circleCrop()
                        .into(holder.ivAvatar);
            }

            holder.itemView.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(item.getId())) {
                    Intent intent = new Intent(v.getContext(), UserDetailActivity.class);
                    intent.putExtra("selectedUserId", item.getId());
                    intent.putExtra("uid", item.getId());
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvExp;
            ShapeableImageView ivAvatar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvMemberRank);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvExp = itemView.findViewById(R.id.tvMemberExp);
                ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            }
        }
    }
}

package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opensource.svgaplayer.SVGAImageView;
import com.roomchatapps.Pmishra.adapters.LeaderboardAdapter;
import com.roomchatapps.Pmishra.models.LeaderboardModel;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tabDaily, tabWeekly, tabMonthly;

    // Podium Views
    private View podiumContainer, podiumRank1, podiumRank2, podiumRank3;
    private ShapeableImageView ivAvatarRank1, ivAvatarRank2, ivAvatarRank3;
    private ImageView ivFrameRank1, ivFrameRank2, ivFrameRank3;
    private SVGAImageView svgaFrameRank1, svgaFrameRank2, svgaFrameRank3;
    private TextView tvNameRank1, tvNameRank2, tvNameRank3;
    private TextView tvCoinsRank1, tvCoinsRank2, tvCoinsRank3;

    // RecyclerView & Empty State
    private RecyclerView rvLeaderboard;
    private View llEmptyState;
    private ProgressBar progressBar;

    // Bottom Bar (My Rank)
    private TextView tvMyRank, tvMyName, tvMySpentCoins;
    private ShapeableImageView ivMyAvatar;
    private ImageView ivMyFrame;
    private SVGAImageView svgaMyFrame;

    private LeaderboardAdapter adapter;
    private final List<LeaderboardModel> listRank4Plus = new ArrayList<>();

    private String activeTab = "DAILY";
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_leaderboard);

        currentUid = FirebaseAuth.getInstance().getUid();

        initViews();
        setupAnimations();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        loadLeaderboardData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabDaily = findViewById(R.id.tabDaily);
        tabWeekly = findViewById(R.id.tabWeekly);
        tabMonthly = findViewById(R.id.tabMonthly);

        podiumContainer = findViewById(R.id.podiumContainer);
        podiumRank1 = findViewById(R.id.podiumRank1);
        podiumRank2 = findViewById(R.id.podiumRank2);
        podiumRank3 = findViewById(R.id.podiumRank3);

        ivAvatarRank1 = findViewById(R.id.ivAvatarRank1);
        ivAvatarRank2 = findViewById(R.id.ivAvatarRank2);
        ivAvatarRank3 = findViewById(R.id.ivAvatarRank3);

        ivFrameRank1 = findViewById(R.id.ivFrameRank1);
        ivFrameRank2 = findViewById(R.id.ivFrameRank2);
        ivFrameRank3 = findViewById(R.id.ivFrameRank3);

        svgaFrameRank1 = findViewById(R.id.svgaFrameRank1);
        svgaFrameRank2 = findViewById(R.id.svgaFrameRank2);
        svgaFrameRank3 = findViewById(R.id.svgaFrameRank3);

        tvNameRank1 = findViewById(R.id.tvNameRank1);
        tvNameRank2 = findViewById(R.id.tvNameRank2);
        tvNameRank3 = findViewById(R.id.tvNameRank3);

        tvCoinsRank1 = findViewById(R.id.tvCoinsRank1);
        tvCoinsRank2 = findViewById(R.id.tvCoinsRank2);
        tvCoinsRank3 = findViewById(R.id.tvCoinsRank3);

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);

        tvMyRank = findViewById(R.id.tvMyRank);
        tvMyName = findViewById(R.id.tvMyName);
        tvMySpentCoins = findViewById(R.id.tvMySpentCoins);
        ivMyAvatar = findViewById(R.id.ivMyAvatar);
        ivMyFrame = findViewById(R.id.ivMyFrame);
        svgaMyFrame = findViewById(R.id.svgaMyFrame);
    }

    private void setupAnimations() {
        if (podiumContainer != null) {
            AnimationHelper.fadeIn(podiumContainer, 600);
        }
    }

    private void setupRecyclerView() {
        if (rvLeaderboard != null) {
            rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
            adapter = new LeaderboardAdapter(listRank4Plus);
            rvLeaderboard.setAdapter(adapter);
        }
    }

    private void setupTabs() {
        if (tabDaily != null) tabDaily.setOnClickListener(v -> selectTab("DAILY", tabDaily));
        if (tabWeekly != null) tabWeekly.setOnClickListener(v -> selectTab("WEEKLY", tabWeekly));
        if (tabMonthly != null) tabMonthly.setOnClickListener(v -> selectTab("MONTHLY", tabMonthly));
    }

    private void selectTab(String tabKey, TextView selectedTab) {
        if (activeTab.equalsIgnoreCase(tabKey)) return;
        activeTab = tabKey;

        TextView[] tabs = {tabDaily, tabWeekly, tabMonthly};
        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setBackgroundResource(R.drawable.bg_leaderboard_tab_unselected);
                tab.setTextColor(Color.parseColor("#A0A0C0"));
            }
        }

        if (selectedTab != null) {
            selectedTab.setBackgroundResource(R.drawable.bg_leaderboard_tab_selected);
            selectedTab.setTextColor(Color.WHITE);
            AnimationHelper.bounceAnimation(selectedTab);
        }

        loadLeaderboardData();
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private long getStartTimestampForTab() {
        Calendar cal = Calendar.getInstance();
        if ("DAILY".equalsIgnoreCase(activeTab)) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } else if ("WEEKLY".equalsIgnoreCase(activeTab)) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
            return cal.getTimeInMillis();
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -30);
            return cal.getTimeInMillis();
        }
    }

    private void loadLeaderboardData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        long startTimestamp = getStartTimestampForTab();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                if (!usersSnapshot.exists()) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    updateUI(new ArrayList<>());
                    return;
                }

                Map<String, LeaderboardModel> userMap = new HashMap<>();

                for (DataSnapshot ds : usersSnapshot.getChildren()) {
                    String uid = ds.getKey();
                    if (uid == null) continue;

                    String name = ds.child("name").getValue(String.class);
                    String profileId = ds.child("profileId").getValue(String.class);
                    String avatar = ds.child("avtar").getValue(String.class);

                    LeaderboardModel model = new LeaderboardModel(
                            uid,
                            name != null ? name : "User",
                            profileId != null ? profileId : "100000",
                            avatar,
                            0,
                            0
                    );
                    userMap.put(uid, model);
                }

                // Query transactions to calculate total spending per user
                txRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot txSnapshot) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);

                        if (txSnapshot.exists()) {
                            for (DataSnapshot userTxSnap : txSnapshot.getChildren()) {
                                String uid = userTxSnap.getKey();
                                LeaderboardModel model = userMap.get(uid);

                                for (DataSnapshot txItem : userTxSnap.getChildren()) {
                                    Long ts = txItem.child("timestamp").getValue(Long.class);
                                    String type = txItem.child("type").getValue(String.class);
                                    Long coinAmount = txItem.child("coinAmount").getValue(Long.class);

                                    if (ts != null && ts >= startTimestamp) {
                                        if ("GIFT_SENT".equalsIgnoreCase(type) && coinAmount != null) {
                                            long spent = Math.abs(coinAmount);
                                            if (model != null) {
                                                model.setSpentCoins(model.getSpentCoins() + spent);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        List<LeaderboardModel> resultList = new ArrayList<>(userMap.values());
                        // Sort by spent coins descending
                        resultList.sort((u1, u2) -> Long.compare(u2.getSpentCoins(), u1.getSpentCoins()));

                        // Assign Ranks
                        for (int i = 0; i < resultList.size(); i++) {
                            resultList.get(i).setRank(i + 1);
                        }

                        updateUI(resultList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        List<LeaderboardModel> resultList = new ArrayList<>(userMap.values());
                        updateUI(resultList);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<LeaderboardModel> fullList) {
        listRank4Plus.clear();

        // Populate Top 3 Podium
        if (fullList.size() >= 1) {
            LeaderboardModel top1 = fullList.get(0);
            bindPodiumSlot(top1, podiumRank1, tvNameRank1, tvCoinsRank1, ivAvatarRank1, ivFrameRank1, svgaFrameRank1);
            if (podiumRank1 != null) podiumRank1.setVisibility(View.VISIBLE);
        } else {
            if (podiumRank1 != null) podiumRank1.setVisibility(View.INVISIBLE);
        }

        if (fullList.size() >= 2) {
            LeaderboardModel top2 = fullList.get(1);
            bindPodiumSlot(top2, podiumRank2, tvNameRank2, tvCoinsRank2, ivAvatarRank2, ivFrameRank2, svgaFrameRank2);
            if (podiumRank2 != null) podiumRank2.setVisibility(View.VISIBLE);
        } else {
            if (podiumRank2 != null) podiumRank2.setVisibility(View.INVISIBLE);
        }

        if (fullList.size() >= 3) {
            LeaderboardModel top3 = fullList.get(2);
            bindPodiumSlot(top3, podiumRank3, tvNameRank3, tvCoinsRank3, ivAvatarRank3, ivFrameRank3, svgaFrameRank3);
            if (podiumRank3 != null) podiumRank3.setVisibility(View.VISIBLE);
        } else {
            if (podiumRank3 != null) podiumRank3.setVisibility(View.INVISIBLE);
        }

        // Ranks 4 and below
        for (int i = 3; i < fullList.size(); i++) {
            listRank4Plus.add(fullList.get(i));
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (llEmptyState != null) {
            llEmptyState.setVisibility(fullList.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // Update Bottom My Rank Bar
        updateMyRankBar(fullList);
    }

    private void bindPodiumSlot(LeaderboardModel model, View container, TextView tvName, TextView tvCoins, ShapeableImageView ivAvatar, ImageView ivFrame, SVGAImageView svgaFrame) {
        if (container == null) return;

        if (tvName != null) tvName.setText(model.getName() != null ? model.getName() : "User");
        if (tvCoins != null) tvCoins.setText(LeaderboardAdapter.formatCoins(model.getSpentCoins()));

        if (ivAvatar != null) {
            if (model.getAvatar() != null && !model.getAvatar().isEmpty()) {
                Glide.with(this)
                        .load(model.getAvatar())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
            }
        }

        if (model.getUid() != null && !model.getUid().trim().isEmpty()) {
            UserProfileCache.getUserProfile(model.getUid(), profile -> {
                if (profile != null) {
                    FrameUtils.displayFrame(LeaderboardActivity.this, profile.equippedFrame, ivFrame, svgaFrame);
                } else {
                    FrameUtils.clearFrame(ivFrame, svgaFrame);
                }
            });
        } else {
            FrameUtils.clearFrame(ivFrame, svgaFrame);
        }

        container.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(container);
            if (model.getUid() != null) {
                Intent intent = new Intent(LeaderboardActivity.this, UserDetailActivity.class);
                intent.putExtra("uid", model.getUid());
                startActivity(intent);
            }
        });
    }

    private void updateMyRankBar(List<LeaderboardModel> fullList) {
        if (currentUid == null) return;

        LeaderboardModel myModel = null;
        for (LeaderboardModel m : fullList) {
            if (currentUid.equals(m.getUid())) {
                myModel = m;
                break;
            }
        }

        if (myModel != null) {
            if (tvMyRank != null) tvMyRank.setText("Rank: #" + myModel.getRank());
            if (tvMyName != null) tvMyName.setText(myModel.getName());
            if (tvMySpentCoins != null) tvMySpentCoins.setText(LeaderboardAdapter.formatCoins(myModel.getSpentCoins()));

            if (ivMyAvatar != null) {
                if (myModel.getAvatar() != null && !myModel.getAvatar().isEmpty()) {
                    Glide.with(this)
                            .load(myModel.getAvatar())
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(ivMyAvatar);
                } else {
                    ivMyAvatar.setImageResource(R.drawable.ic_person);
                }
            }

            UserProfileCache.getUserProfile(currentUid, profile -> {
                if (profile != null) {
                    FrameUtils.displayFrame(LeaderboardActivity.this, profile.equippedFrame, ivMyFrame, svgaMyFrame);
                } else {
                    FrameUtils.clearFrame(ivMyFrame, svgaMyFrame);
                }
            });
        } else {
            if (tvMyRank != null) tvMyRank.setText("Rank: --");
            if (tvMyName != null) tvMyName.setText("You");
            if (tvMySpentCoins != null) tvMySpentCoins.setText("0");
            FrameUtils.clearFrame(ivMyFrame, svgaMyFrame);
        }
    }
}

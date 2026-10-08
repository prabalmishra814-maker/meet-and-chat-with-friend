package com.roomchatapps.Pmishra;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.GuardRankAdapter;
import com.roomchatapps.Pmishra.databinding.ActivityGuardRankBinding;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;
import com.roomchatapps.Pmishra.models.GuardRankModel;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuardRankActivity extends AppCompatActivity {

    private ActivityGuardRankBinding binding;
    private GuardRankAdapter adapter;
    private final List<GuardRankModel> rankList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String currentUid;
    private String currentUserName = "Prabal Mishra";
    private String currentUserAvatar = "";

    private String targetUid;
    private String targetUserName;
    private String targetUserAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);

        binding = ActivityGuardRankBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.headerContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    0
            );
            return insets;
        });

        targetUid = getIntent().getStringExtra("targetUid");
        targetUserName = getIntent().getStringExtra("userName");
        targetUserAvatar = getIntent().getStringExtra("userAvatar");

        if (TextUtils.isEmpty(targetUserName)) {
            targetUserName = "User";
        }
        binding.tvTitle.setText(targetUserName + " Guard Rank");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        }

        setupToolbar();
        setupRecyclerView();
        loadPodiumAndRankData();
        loadCurrentUserData();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnHelp.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Guard Rank Rules")
                    .setMessage("1. Guard rank is based on the intimacy & gift points received by the target user.\n\n" +
                            "2. Top guards unlock exclusive crowns, frames, and special entrance effects.\n\n" +
                            "3. Protect this user by sending gifts to increase your guard rank!")
                    .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        binding.btnProtectAction.setOnClickListener(v -> showGiftStoreDialog());
    }

    private void setupRecyclerView() {
        adapter = new GuardRankAdapter(this, rankList);
        binding.rvGuardRankList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGuardRankList.setAdapter(adapter);
    }

    private void showGiftStoreDialog() {
        if (isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_gift_store, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        RecyclerView rvGiftRecipients = dialogView.findViewById(R.id.rvGiftRecipients);
        RecyclerView rvGifts = dialogView.findViewById(R.id.rvGifts);
        View btnSendAction = dialogView.findViewById(R.id.btnSendGiftAction);
        TextView tvGiftDialogCoins = dialogView.findViewById(R.id.tvGiftDialogCoins);
        View llCoinBalance = dialogView.findViewById(R.id.llCoinBalance);

        if (tvGiftDialogCoins != null && currentUid != null) {
            WalletManager.getUserCoins(currentUid, balance -> runOnUiThread(() ->
                    tvGiftDialogCoins.setText(String.valueOf(balance))
            ));
        }

        if (llCoinBalance != null) {
            llCoinBalance.setOnClickListener(v -> {
                startActivity(new Intent(GuardRankActivity.this, CoinRechargeActivity.class));
            });
        }

        // Setup Recipients List (target user)
        if (rvGiftRecipients != null) {
            List<GiftRecipientModel> recipientList = new ArrayList<>();
            GiftRecipientModel targetItem = new GiftRecipientModel(
                    !TextUtils.isEmpty(targetUid) ? targetUid : "target_user",
                    !TextUtils.isEmpty(targetUserName) ? targetUserName : "Target User",
                    !TextUtils.isEmpty(targetUserAvatar) ? targetUserAvatar : "",
                    "1",
                    true,
                    false
            );
            recipientList.add(targetItem);
            GiftRecipientAdapter recipientAdapter = new GiftRecipientAdapter(recipientList);
            rvGiftRecipients.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvGiftRecipients.setAdapter(recipientAdapter);
        }

        // Setup Gifts List
        List<GiftStoreAdapter.GiftStoreItem> allGifts = GiftCatalog.getAllGifts();
        final GiftStoreAdapter.GiftStoreItem[] selectedGift = {allGifts != null && !allGifts.isEmpty() ? allGifts.get(0) : null};

        if (rvGifts != null && allGifts != null) {
            GiftStoreAdapter giftAdapter = new GiftStoreAdapter(allGifts);
            giftAdapter.setOnGiftSelectedListener((item, position, isReSelected) -> selectedGift[0] = item);
            rvGifts.setLayoutManager(new GridLayoutManager(this, 4));
            rvGifts.setAdapter(giftAdapter);
        }

        if (btnSendAction != null) {
            btnSendAction.setOnClickListener(v -> {
                if (selectedGift[0] == null) {
                    Toast.makeText(this, "Please select a gift", Toast.LENGTH_SHORT).show();
                    return;
                }

                long giftCost = selectedGift[0].cost;
                if (currentUid == null) {
                    Toast.makeText(this, "Please login to send gifts", Toast.LENGTH_SHORT).show();
                    return;
                }

                String keyTarget = !TextUtils.isEmpty(targetUid) ? targetUid : "target_user";

                WalletManager.spendCoinsForGift(currentUid, keyTarget, giftCost, selectedGift[0].name, new WalletManager.WalletCallback() {
                    @Override
                    public void onSuccess(String message, long newCoinBalance) {
                        runOnUiThread(() -> {
                            sendProtectGuardPoints(giftCost, selectedGift[0].name);
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(GuardRankActivity.this, error, Toast.LENGTH_SHORT).show();
                            if (error != null && error.toLowerCase().contains("insufficient")) {
                                startActivity(new Intent(GuardRankActivity.this, CoinRechargeActivity.class));
                            }
                        });
                    }
                });
            });
        }

        dialog.show();
    }

    private void sendProtectGuardPoints(long giftCost, String giftName) {
        if (currentUid == null) return;

        String keyTarget = !TextUtils.isEmpty(targetUid) ? targetUid : "target_user";

        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values")
                .child(keyTarget).child(currentUid);

        String currentUserNameVal = currentUserName != null ? currentUserName : "Prabal Mishra";
        String currentUserIconVal = currentUserAvatar != null ? currentUserAvatar : "";

        guardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentPoints = 0;
                if (snapshot.exists()) {
                    Long val = snapshot.child("value").getValue(Long.class);
                    if (val != null) currentPoints = val;
                }

                long newPoints = currentPoints + giftCost;

                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("value", newPoints);
                updateMap.put("userName", currentUserNameVal);
                updateMap.put("userIcon", currentUserIconVal);
                updateMap.put("timestamp", System.currentTimeMillis());

                guardRef.setValue(updateMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(GuardRankActivity.this,
                                "Sent " + giftName + "! +" + giftCost + " Guard Points to " + targetUserName + "!",
                                Toast.LENGTH_LONG).show();
                        loadPodiumAndRankData(); // Refresh Guard Rank Leaderboard
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPodiumAndRankData() {
        List<GuardRankModel> masterList = new ArrayList<>();

        String keyTarget = !TextUtils.isEmpty(targetUid) ? targetUid : "target_user";
        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values").child(keyTarget);

        guardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                masterList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Long scoreVal = ds.child("value").getValue(Long.class);
                        String uName = ds.child("userName").getValue(String.class);
                        String avatar = ds.child("userIcon").getValue(String.class);
                        String pid = ds.getKey();

                        if (scoreVal != null && scoreVal > 0) {
                            String scoreFormatted = formatGuardScore(scoreVal);
                            masterList.add(new GuardRankModel(0, !TextUtils.isEmpty(uName) ? uName : "User", pid, avatar, scoreFormatted));
                        }
                    }
                }

                if (masterList.isEmpty()) {
                    loadGlobalRealGuardHolders();
                } else {
                    processAndDisplayMasterRankList(masterList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadGlobalRealGuardHolders();
            }
        });
    }

    private void loadGlobalRealGuardHolders() {
        List<GuardRankModel> masterList = new ArrayList<>();
        DatabaseReference globalGuardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values");

        globalGuardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot targetSnap : snapshot.getChildren()) {
                        for (DataSnapshot supporterSnap : targetSnap.getChildren()) {
                            Long scoreVal = supporterSnap.child("value").getValue(Long.class);
                            String uName = supporterSnap.child("userName").getValue(String.class);
                            String avatar = supporterSnap.child("userIcon").getValue(String.class);
                            String pid = supporterSnap.getKey();

                            if (scoreVal != null && scoreVal > 0) {
                                boolean exists = false;
                                for (GuardRankModel m : masterList) {
                                    if (pid != null && pid.equals(m.getProfileId())) {
                                        long currentLong = parseGuardScoreToLong(m.getGuardScore());
                                        m.setGuardScore(formatGuardScore(currentLong + scoreVal));
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    String scoreFormatted = formatGuardScore(scoreVal);
                                    masterList.add(new GuardRankModel(0, !TextUtils.isEmpty(uName) ? uName : "User", pid, avatar, scoreFormatted));
                                }
                            }
                        }
                    }
                }
                processAndDisplayMasterRankList(masterList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                processAndDisplayMasterRankList(masterList);
            }
        });
    }

    private void processAndDisplayMasterRankList(List<GuardRankModel> masterList) {
        if (isFinishing() || binding == null) return;

        // Sort descending by guard value
        masterList.sort((a, b) -> Long.compare(parseGuardScoreToLong(b.getGuardScore()), parseGuardScoreToLong(a.getGuardScore())));

        // Assign ranks 1, 2, 3...
        for (int i = 0; i < masterList.size(); i++) {
            masterList.get(i).setRank(i + 1);
        }

        // Rank 1
        if (masterList.size() > 0) {
            GuardRankModel r1 = masterList.get(0);
            binding.tvRank1Name.setText(r1.getUserName());
            binding.tvRank1Score.setText("💎 " + r1.getGuardScore());
            Glide.with(this).load(r1.getAvatarUrl()).placeholder(R.drawable.img_20260904_135725).into(binding.ivRank1Avatar);
        } else {
            binding.tvRank1Name.setText("No Guard");
            binding.tvRank1Score.setText("💎 0");
            binding.ivRank1Avatar.setImageResource(R.drawable.img_20260904_135725);
        }

        // Rank 2
        if (masterList.size() > 1) {
            GuardRankModel r2 = masterList.get(1);
            binding.tvRank2Name.setText(r2.getUserName());
            binding.tvRank2Score.setText("💎 " + r2.getGuardScore());
            Glide.with(this).load(r2.getAvatarUrl()).placeholder(R.drawable.img_20260904_135725).into(binding.ivRank2Avatar);
        } else {
            binding.tvRank2Name.setText("No Guard");
            binding.tvRank2Score.setText("💎 0");
            binding.ivRank2Avatar.setImageResource(R.drawable.img_20260904_135725);
        }

        // Rank 3
        if (masterList.size() > 2) {
            GuardRankModel r3 = masterList.get(2);
            binding.tvRank3Name.setText(r3.getUserName());
            binding.tvRank3Score.setText("💎 " + r3.getGuardScore());
            Glide.with(this).load(r3.getAvatarUrl()).placeholder(R.drawable.img_20260904_135725).into(binding.ivRank3Avatar);
        } else {
            binding.tvRank3Name.setText("No Guard");
            binding.tvRank3Score.setText("💎 0");
            binding.ivRank3Avatar.setImageResource(R.drawable.img_20260904_135725);
        }

        // Rank 4+
        rankList.clear();
        if (masterList.size() > 3) {
            for (int i = 3; i < masterList.size(); i++) {
                rankList.add(masterList.get(i));
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // My Rank in bottom bar
        boolean foundMyRank = false;
        if (currentUid != null) {
            for (GuardRankModel m : masterList) {
                if (currentUid.equals(m.getProfileId()) || currentUid.equals(m.getUserName())) {
                    binding.tvMyRank.setText(String.valueOf(m.getRank()));
                    binding.tvMySubtitle.setText("Protect: " + m.getGuardScore());
                    foundMyRank = true;
                    break;
                }
            }
        }
        if (!foundMyRank) {
            binding.tvMyRank.setText("--");
            binding.tvMySubtitle.setText("Protect: 0");
        }
    }

    private String formatGuardScore(long score) {
        if (score >= 1000000) {
            return String.format("%.1fM", score / 1000000.0);
        } else if (score >= 1000) {
            return String.format("%.1fK", score / 1000.0);
        }
        return String.valueOf(score);
    }

    private long parseGuardScoreToLong(String scoreStr) {
        if (TextUtils.isEmpty(scoreStr)) return 0;
        try {
            String s = scoreStr.trim().toUpperCase().replace("💎", "").trim();
            if (s.endsWith("K")) {
                double val = Double.parseDouble(s.replace("K", "").trim());
                return (long) (val * 1000);
            } else if (s.endsWith("M")) {
                double val = Double.parseDouble(s.replace("M", "").trim());
                return (long) (val * 1000000);
            } else {
                return Long.parseLong(s);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private void loadCurrentUserData() {
        if (currentUid == null || userRef == null) return;

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || binding == null) return;

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("avatar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("photoUrl").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("image").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("userIcon").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        currentUserName = name;
                        binding.tvMyName.setText(name);
                    }
                    if (!TextUtils.isEmpty(avatar)) {
                        currentUserAvatar = avatar;
                        Glide.with(GuardRankActivity.this)
                                .load(avatar)
                                .placeholder(R.drawable.img_20260904_135725)
                                .error(R.drawable.img_20260904_135725)
                                .into(binding.ivMyAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

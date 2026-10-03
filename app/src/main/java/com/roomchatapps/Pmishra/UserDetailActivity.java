package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.CollectionAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.databinding.ActivityUserDetailBinding;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.CoinUtils;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.LevelUtils;
import com.roomchatapps.Pmishra.utils.NotificationHelper;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;
import com.roomchatapps.Pmishra.utils.StoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    private ActivityUserDetailBinding binding;
    private String targetUid;
    private String currentUid;
    private DatabaseReference userRef;

    // COLLECTION INLINE FIELDS
    private CollectionAdapter collectionAdapter;
    private final List<CollectionItemModel> allCollectionItems = new ArrayList<>();
    private final List<CollectionItemModel> filteredCollectionItems = new ArrayList<>();
    private String activeTabCategory = "ALL";

    private DatabaseReference txRef;
    private ValueEventListener txListener;
    private DatabaseReference userGiftsRef;
    private ValueEventListener userGiftsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.headerLayout.setPadding(
                    binding.headerLayout.getPaddingLeft(),
                    systemBars.top + (int) (8 * getResources().getDisplayMetrics().density),
                    binding.headerLayout.getPaddingRight(),
                    binding.headerLayout.getPaddingBottom()
            );
            return insets;
        });

        targetUid = getIntent().getStringExtra("uid");
        currentUid = FirebaseAuth.getInstance().getUid();

        if (targetUid == null) {
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);
        
        setupClickListeners();
        loadUserData();
        loadFollowStats();
        checkFollowStatus();
        setupCollection();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        if (currentUid != null && currentUid.equals(targetUid)) {
            binding.bottomBar.setVisibility(View.GONE);
        }
        
        binding.btnMessage.setOnClickListener(v -> {
            String name = binding.userName.getText().toString();
            Intent intent = new Intent(UserDetailActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", targetUid);
            intent.putExtra("receiverName", name);
            startActivity(intent);
        });

        binding.btnFollow.setOnClickListener(v -> {
            AnimationHelper.animateFollowButton(binding.btnFollow, this::toggleFollow);
        });

        View.OnClickListener openFollowers = v -> {
            Intent intent = new Intent(UserDetailActivity.this, FollowListActivity.class);
            intent.putExtra("uid", targetUid);
            intent.putExtra("type", "followers");
            startActivity(intent);
        };

        View.OnClickListener openFollowing = v -> {
            Intent intent = new Intent(UserDetailActivity.this, FollowListActivity.class);
            intent.putExtra("uid", targetUid);
            intent.putExtra("type", "following");
            startActivity(intent);
        };

        if (binding.tvFollowCount != null && binding.tvFollowCount.getParent() instanceof View) {
            ((View) binding.tvFollowCount.getParent()).setOnClickListener(openFollowers);
        }
        if (binding.tvFansCount != null && binding.tvFansCount.getParent() instanceof View) {
            ((View) binding.tvFansCount.getParent()).setOnClickListener(openFollowing);
        }
    }

    private void setupCollection() {
        if (binding.rvCollection != null) {
            binding.rvCollection.setLayoutManager(new GridLayoutManager(this, 2));
            collectionAdapter = new CollectionAdapter(filteredCollectionItems, item -> {
                String msg = item.getItemName();
                if (item.getItemType() == CollectionItemModel.ItemType.GIFT_RECEIVED) {
                    msg += " (Received x" + item.getReceivedCount() + ")";
                } else if (item.isEquipped()) {
                    msg += " (Equipped)";
                } else {
                    msg += " (Unlocked)";
                }
                Toast.makeText(UserDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            });
            binding.rvCollection.setAdapter(collectionAdapter);
        }

        setupTabs();
        loadSelectedUserCollection();
    }

    private void setupTabs() {
        if (binding.tabAll != null) binding.tabAll.setOnClickListener(v -> selectTab("ALL", binding.tabAll));
        if (binding.tabEntrances != null) binding.tabEntrances.setOnClickListener(v -> selectTab("ENTRY", binding.tabEntrances));
        if (binding.tabFrames != null) binding.tabFrames.setOnClickListener(v -> selectTab("FRAME", binding.tabFrames));
        if (binding.tabGifts != null) binding.tabGifts.setOnClickListener(v -> selectTab("GIFT", binding.tabGifts));
    }

    private void selectTab(String category, TextView selectedTab) {
        if (activeTabCategory.equalsIgnoreCase(category)) return;
        activeTabCategory = category;

        TextView[] tabs = {binding.tabAll, binding.tabEntrances, binding.tabFrames, binding.tabGifts};
        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setBackgroundResource(R.drawable.chip_room_bg);
                tab.setTextColor(Color.parseColor("#88FFFFFF"));
                tab.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
            }
        }

        if (selectedTab != null) {
            selectedTab.setBackgroundResource(R.drawable.chip_charm_bg);
            selectedTab.setTextColor(Color.WHITE);
            selectedTab.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(220)
                    .setInterpolator(new OvershootInterpolator(1.4f))
                    .start();
        }

        applyCategoryFilter();
    }

    private void loadSelectedUserCollection() {
        if (binding.progressBarCollection != null) binding.progressBarCollection.setVisibility(View.VISIBLE);

        StoreManager.getStoreCatalog(targetUid, "ALL", new StoreManager.CatalogCallback() {
            @Override
            public void onCatalogLoaded(List<StoreItemModel> catalog) {
                List<CollectionItemModel> storeOwnedItems = new ArrayList<>();

                for (StoreItemModel item : catalog) {
                    if (item.isOwned()) {
                        if ("ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                            storeOwnedItems.add(new CollectionItemModel(CollectionItemModel.ItemType.ENTRY_EFFECT, item));
                        } else if ("FRAME".equalsIgnoreCase(item.getCategory())) {
                            storeOwnedItems.add(new CollectionItemModel(CollectionItemModel.ItemType.FRAME, item));
                        }
                    }
                }

                loadSelectedUserReceivedGifts(storeOwnedItems);
            }

            @Override
            public void onError(String error) {
                loadSelectedUserReceivedGifts(new ArrayList<>());
            }
        });
    }

    private void loadSelectedUserReceivedGifts(List<CollectionItemModel> storeOwnedItems) {
        txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(targetUid);
        userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid).child("received_gifts");

        txListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> giftCountMap = new HashMap<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String type = ds.child("type").getValue(String.class);
                        if ("GIFT_RECEIVED".equalsIgnoreCase(type)) {
                            String giftName = ds.child("giftName").getValue(String.class);

                            if (giftName == null || giftName.trim().isEmpty()) {
                                String title = ds.child("title").getValue(String.class);
                                if (title != null) {
                                    if (title.startsWith("Received Gift: ")) {
                                        giftName = title.substring("Received Gift: ".length()).trim();
                                    } else if (title.startsWith("Earned Energy: ")) {
                                        giftName = title.substring("Earned Energy: ".length()).trim();
                                    }
                                }
                            }

                            if (giftName == null || giftName.trim().isEmpty()) {
                                String desc = ds.child("description").getValue(String.class);
                                if (desc != null && desc.contains("Received ") && desc.contains(" from ")) {
                                    int start = desc.indexOf("Received ") + "Received ".length();
                                    int end = desc.indexOf(" from ");
                                    if (start < end) {
                                        giftName = desc.substring(start, end).trim();
                                    }
                                }
                            }

                            if (giftName != null && !giftName.trim().isEmpty()) {
                                String cleanName = giftName.replaceAll("\\s*\\(.*?\\)", "").trim();

                                int qty = 1;
                                Long qObj = ds.child("quantity").getValue(Long.class);
                                if (qObj != null && qObj > 0) {
                                    qty = qObj.intValue();
                                }

                                int currentCount = giftCountMap.containsKey(cleanName) ? giftCountMap.get(cleanName) : 0;
                                giftCountMap.put(cleanName, currentCount + qty);
                            }
                        }
                    }
                }

                userGiftsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot giftsSnapshot) {
                        if (giftsSnapshot.exists()) {
                            for (DataSnapshot ds : giftsSnapshot.getChildren()) {
                                String gKey = ds.getKey();
                                Long countVal = ds.getValue(Long.class);
                                if (gKey != null && countVal != null && countVal > 0) {
                                    String cleanKey = gKey.replaceAll("\\s*\\(.*?\\)", "").trim();
                                    int existing = giftCountMap.containsKey(cleanKey) ? giftCountMap.get(cleanKey) : 0;
                                    giftCountMap.put(cleanKey, Math.max(existing, countVal.intValue()));
                                }
                            }
                        }

                        buildFinalCollection(storeOwnedItems, giftCountMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        buildFinalCollection(storeOwnedItems, giftCountMap);
                    }
                };

                userGiftsRef.addListenerForSingleValueEvent(userGiftsListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                buildFinalCollection(storeOwnedItems, new HashMap<>());
            }
        };

        txRef.limitToLast(200).addValueEventListener(txListener);
    }

    private void buildFinalCollection(List<CollectionItemModel> storeOwnedItems, Map<String, Integer> giftCountMap) {
        allCollectionItems.clear();
        allCollectionItems.addAll(storeOwnedItems);

        for (Map.Entry<String, Integer> entry : giftCountMap.entrySet()) {
            String giftName = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                GiftStoreAdapter.GiftStoreItem catalogGift = GiftCatalog.findGiftByName(giftName);
                if (catalogGift != null) {
                    allCollectionItems.add(new CollectionItemModel(catalogGift, count));
                } else {
                    allCollectionItems.add(new CollectionItemModel(giftName, R.drawable.gift_icon, 100000L, count));
                }
            }
        }

        if (binding.progressBarCollection != null) binding.progressBarCollection.setVisibility(View.GONE);
        applyCategoryFilter();
    }

    private void applyCategoryFilter() {
        filteredCollectionItems.clear();

        for (CollectionItemModel item : allCollectionItems) {
            if ("ALL".equalsIgnoreCase(activeTabCategory)) {
                filteredCollectionItems.add(item);
            } else if ("ENTRY".equalsIgnoreCase(activeTabCategory) && item.getItemType() == CollectionItemModel.ItemType.ENTRY_EFFECT) {
                filteredCollectionItems.add(item);
            } else if ("FRAME".equalsIgnoreCase(activeTabCategory) && item.getItemType() == CollectionItemModel.ItemType.FRAME) {
                filteredCollectionItems.add(item);
            } else if ("GIFT".equalsIgnoreCase(activeTabCategory) && item.getItemType() == CollectionItemModel.ItemType.GIFT_RECEIVED) {
                filteredCollectionItems.add(item);
            }
        }

        if (collectionAdapter != null) {
            collectionAdapter.updateList(filteredCollectionItems);
        }

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredCollectionItems.isEmpty()) {
            if (binding.llEmptyState != null) binding.llEmptyState.setVisibility(View.VISIBLE);
            if (binding.rvCollection != null) binding.rvCollection.setVisibility(View.GONE);

            if (binding.tvEmptyTitle != null && binding.tvEmptySubtitle != null) {
                if ("ENTRY".equalsIgnoreCase(activeTabCategory)) {
                    binding.tvEmptyTitle.setText("No Entry Effects");
                    binding.tvEmptySubtitle.setText("This user does not own any entry effects.");
                } else if ("FRAME".equalsIgnoreCase(activeTabCategory)) {
                    binding.tvEmptyTitle.setText("No Frames");
                    binding.tvEmptySubtitle.setText("This user does not own any frames.");
                } else if ("GIFT".equalsIgnoreCase(activeTabCategory)) {
                    binding.tvEmptyTitle.setText("No gifts received yet");
                    binding.tvEmptySubtitle.setText("Gifts sent to this user will appear here.");
                } else {
                    binding.tvEmptyTitle.setText("User's Collection is Empty");
                    binding.tvEmptySubtitle.setText("This user has no items or gifts in their collection.");
                }
            }
        } else {
            if (binding.llEmptyState != null) binding.llEmptyState.setVisibility(View.GONE);
            if (binding.rvCollection != null) binding.rvCollection.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed() || binding == null) return;
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileId = snapshot.child("profileId").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);

                    binding.userName.setText(name != null ? name : "User");
                    binding.userId.setText("ID: " + (profileId != null ? profileId : targetUid));
                    if (bio != null && !bio.isEmpty()) {
                        binding.tvAbout.setText(bio);
                    }

                    Glide.with(UserDetailActivity.this)
                            .load(avatar)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileImage);

                    // Check for equipped frame
                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    FrameUtils.displayFrame(UserDetailActivity.this, equippedFrame, binding.ivProfileFrame, binding.svgaProfileFrame);

                    // Level & XP Progress
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
                    int currentXpInLevel = LevelUtils.calculateCurrentXpInLevel(coinsSpent);
                    long maxXpInLevel = LevelUtils.getXpNeededForNextLevelFromStart(level);
                    int xpProgressPct = LevelUtils.calculateXpPercentageInLevel(coinsSpent);

                    binding.tvUserLevelCard.setText("🛡️ Lv." + level);
                    binding.tvUserLevelCard.setBackgroundResource(LevelUtils.getLevelBadgeDrawable(level));

                    binding.tvLevelTitle.setText("Level " + level + " Member");
                    binding.pbLevelXp.setMax(100);
                    binding.pbLevelXp.setProgress(xpProgressPct);
                    binding.tvXpProgress.setText(currentXpInLevel + " / " + maxXpInLevel + " XP");
                    binding.tvTotalCoinsSpent.setText("Spent: " + CoinUtils.formatCoins(coinsSpent) + " Coins");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadFollowStats() {
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow").child(targetUid);
        
        followRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.tvFollowCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        followRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.tvFansCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkFollowStatus() {
        if (currentUid == null) return;
        
        FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUid).child("following").child(targetUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            binding.btnFollow.setText("Following");
                            binding.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A3447")));
                            binding.btnFollow.setTextColor(Color.parseColor("#A0AEC0"));
                        } else {
                            binding.btnFollow.setText("+ Follow");
                            binding.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FFC2")));
                            binding.btnFollow.setTextColor(Color.parseColor("#050E1E"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void toggleFollow() {
        if (currentUid == null || currentUid.equals(targetUid)) return;

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUid).child("following").child(targetUid);
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(targetUid).child("followers").child(currentUid);

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followingRef.removeValue();
                    followersRef.removeValue();
                } else {
                    followingRef.setValue(true);
                    followersRef.setValue(true);
                    NotificationHelper.sendFollowNotification(targetUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (txRef != null && txListener != null) {
            txRef.removeEventListener(txListener);
        }
        if (userGiftsRef != null && userGiftsListener != null) {
            userGiftsRef.removeEventListener(userGiftsListener);
        }
    }
}

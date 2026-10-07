package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.GiftGridAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.HorizontalCollectionAdapter;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.NotificationHelper;

import com.roomchatapps.Pmishra.utils.StatusBarUtils;
import com.roomchatapps.Pmishra.utils.StoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherCollectionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, btnMoreOptions;
    private View tabProfile, tabIntimacy;
    private TextView tvTabProfile, tvTabIntimacy;
    private View indicatorProfile, indicatorIntimacy;
    private RecyclerView rvPhotos, rvFrames, rvVehicles, rvGiftsReceived, rvCollectionHall;
    private TextView tvCollectionHallCount;
    private Button btnAddFriend, btnFollowUser;
    private ProgressBar progressBar;

    private String selectedUserId;
    private String selectedUserName;
    private String currentUid;

    private final List<CollectionItemModel> photoList = new ArrayList<>();
    private final List<CollectionItemModel> frameList = new ArrayList<>();
    private final List<CollectionItemModel> vehicleList = new ArrayList<>();
    private final List<CollectionItemModel> giftList = new ArrayList<>();
    private final List<CollectionItemModel> collectionHallList = new ArrayList<>();

    private HorizontalCollectionAdapter photoAdapter;
    private HorizontalCollectionAdapter frameAdapter;
    private HorizontalCollectionAdapter vehicleAdapter;
    private HorizontalCollectionAdapter collectionHallAdapter;
    private GiftGridAdapter giftAdapter;

    private DatabaseReference txRef;
    private ValueEventListener txListener;
    private DatabaseReference userGiftsRef;
    private ValueEventListener userGiftsListener;
    private DatabaseReference userProfileRef;
    private ValueEventListener userProfileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_other_collection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View header = findViewById(R.id.header);
            if (header != null) {
                header.setPadding(
                        header.getPaddingLeft(),
                        systemBars.top + (int) (4 * getResources().getDisplayMetrics().density),
                        header.getPaddingRight(),
                        header.getPaddingBottom()
                );
            }
            return insets;
        });

        currentUid = FirebaseAuth.getInstance().getUid();
        parseIntentData();

        initViews();
        setupRecyclerViews();
        setupTabs();
        setupClickListeners();
        setupActionButtons();

        if (selectedUserId != null && !selectedUserId.trim().isEmpty()) {
            loadSelectedUserProfile();
            loadSelectedUserCollection();
        } else {
            Toast.makeText(this, "Unable to load this user's collection.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void parseIntentData() {
        if (getIntent() != null) {
            selectedUserId = getIntent().getStringExtra("selectedUserId");
            if (selectedUserId == null || selectedUserId.trim().isEmpty()) {
                selectedUserId = getIntent().getStringExtra("uid");
            }
            if (selectedUserId == null || selectedUserId.trim().isEmpty()) {
                selectedUserId = getIntent().getStringExtra("userId");
            }

            selectedUserName = getIntent().getStringExtra("selectedUserName");
            if (selectedUserName == null || selectedUserName.trim().isEmpty()) {
                selectedUserName = getIntent().getStringExtra("userName");
            }
            if (selectedUserName == null || selectedUserName.trim().isEmpty()) {
                selectedUserName = getIntent().getStringExtra("name");
            }
        }
    }

    private View llProfileContent, llIntimacyContent;
    private TextView tvGenderAge, tvUserMeta, tvUserLevel;
    private View btnGoLoveStore, btnAddCouplePartner;
    private ImageView ivUserProfileAvatar, ivCoupleLeftAvatar, ivCoupleRightAvatar, ivUserProfileFrame;
    private com.opensource.svgaplayer.SVGAImageView svgaUserProfileFrame;
    private TextView tvCoupleLeftName, tvCoupleRightName;

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        tabProfile = findViewById(R.id.tabProfile);
        tabIntimacy = findViewById(R.id.tabIntimacy);
        tvTabProfile = findViewById(R.id.tvTabProfile);
        tvTabIntimacy = findViewById(R.id.tvTabIntimacy);
        indicatorProfile = findViewById(R.id.indicatorProfile);
        indicatorIntimacy = findViewById(R.id.indicatorIntimacy);
        rvPhotos = findViewById(R.id.rvPhotos);
        rvFrames = findViewById(R.id.rvFrames);
        rvVehicles = findViewById(R.id.rvVehicles);
        rvGiftsReceived = findViewById(R.id.rvGiftsReceived);
        rvCollectionHall = findViewById(R.id.rvCollectionHall);
        tvCollectionHallCount = findViewById(R.id.tvCollectionHallCount);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnFollowUser = findViewById(R.id.btnFollowUser);
        progressBar = findViewById(R.id.progressBar);

        llProfileContent = findViewById(R.id.llProfileContent);
        llIntimacyContent = findViewById(R.id.llIntimacyContent);
        ivUserProfileAvatar = findViewById(R.id.ivUserProfileAvatar);
        ivUserProfileFrame = findViewById(R.id.ivUserProfileFrame);
        svgaUserProfileFrame = findViewById(R.id.svgaUserProfileFrame);
        tvGenderAge = findViewById(R.id.tvGenderAge);
        tvUserMeta = findViewById(R.id.tvUserMeta);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        btnGoLoveStore = findViewById(R.id.btnGoLoveStore);
        btnAddCouplePartner = findViewById(R.id.btnAddCouplePartner);
        ivCoupleLeftAvatar = findViewById(R.id.ivCoupleLeftAvatar);
        ivCoupleRightAvatar = findViewById(R.id.ivCoupleRightAvatar);
        tvCoupleLeftName = findViewById(R.id.tvCoupleLeftName);
        tvCoupleRightName = findViewById(R.id.tvCoupleRightName);

        if (tvTitle != null) {
            tvTitle.setText(selectedUserName != null && !selectedUserName.trim().isEmpty() ? selectedUserName.trim() : "User's Collection");
        }
    }

    private void setupRecyclerViews() {
        if (rvPhotos != null) {
            rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            photoAdapter = new HorizontalCollectionAdapter(photoList, item ->
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show());
            rvPhotos.setAdapter(photoAdapter);
        }

        if (rvCollectionHall != null) {
            rvCollectionHall.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            collectionHallAdapter = new HorizontalCollectionAdapter(collectionHallList, item ->
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show());
            rvCollectionHall.setAdapter(collectionHallAdapter);
        }

        if (rvFrames != null) {
            rvFrames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            frameAdapter = new HorizontalCollectionAdapter(frameList, item ->
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show());
            rvFrames.setAdapter(frameAdapter);
        }

        if (rvVehicles != null) {
            rvVehicles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            vehicleAdapter = new HorizontalCollectionAdapter(vehicleList, item ->
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show());
            rvVehicles.setAdapter(vehicleAdapter);
        }

        if (rvGiftsReceived != null) {
            rvGiftsReceived.setLayoutManager(new GridLayoutManager(this, 4));
            rvGiftsReceived.setNestedScrollingEnabled(false);
            giftAdapter = new GiftGridAdapter(giftList, item ->
                    Toast.makeText(this, item.getItemName() + " (Received x" + item.getReceivedCount() + ")", Toast.LENGTH_SHORT).show());
            rvGiftsReceived.setAdapter(giftAdapter);
        }
    }

    private void setupTabs() {
        if (tabProfile != null) {
            tabProfile.setOnClickListener(v -> selectTab(true));
        }
        if (tabIntimacy != null) {
            tabIntimacy.setOnClickListener(v -> selectTab(false));
        }
    }

    private void selectTab(boolean isProfile) {
        if (tvTabProfile != null) tvTabProfile.setTextColor(isProfile ? Color.WHITE : Color.parseColor("#70FFFFFF"));
        if (tvTabIntimacy != null) tvTabIntimacy.setTextColor(isProfile ? Color.parseColor("#70FFFFFF") : Color.WHITE);
        if (indicatorProfile != null) indicatorProfile.setBackgroundColor(isProfile ? Color.parseColor("#00FFC6") : Color.TRANSPARENT);
        if (indicatorIntimacy != null) indicatorIntimacy.setBackgroundColor(isProfile ? Color.TRANSPARENT : Color.parseColor("#00FFC6"));

        if (llProfileContent != null) llProfileContent.setVisibility(isProfile ? View.VISIBLE : View.GONE);
        if (llIntimacyContent != null) llIntimacyContent.setVisibility(isProfile ? View.GONE : View.VISIBLE);
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnGoLoveStore != null) {
            btnGoLoveStore.setOnClickListener(v -> {
                Intent intent = new Intent(OtherCollectionActivity.this, StoreActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddCouplePartner != null) {
            btnAddCouplePartner.setOnClickListener(v ->
                    Toast.makeText(this, "Send a Couple request to this user 💖", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupActionButtons() {
        if (currentUid == null || selectedUserId == null) return;

        // Follow Button State
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUid).child("following").child(selectedUserId);

        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (btnFollowUser != null) {
                    if (snapshot.exists()) {
                        btnFollowUser.setText("💛 Following");
                    } else {
                        btnFollowUser.setText("Follow");
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnFollowUser != null) {
            btnFollowUser.setOnClickListener(v -> {
                DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(currentUid).child("following").child(selectedUserId);
                DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(selectedUserId).child("followers").child(currentUid);

                followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            followingRef.removeValue();
                            followersRef.removeValue();
                            Toast.makeText(OtherCollectionActivity.this, "Unfollowed " + selectedUserName, Toast.LENGTH_SHORT).show();
                        } else {
                            followingRef.setValue(true);
                            followersRef.setValue(true);
                            NotificationHelper.sendFollowNotification(selectedUserId);
                            Toast.makeText(OtherCollectionActivity.this, "Now following " + selectedUserName + " ❤️", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        // Add Friend Button State
        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(selectedUserId).child(currentUid);

        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (btnAddFriend != null) {
                    if (snapshot.exists()) {
                        btnAddFriend.setText("Requested");
                    } else {
                        btnAddFriend.setText("+ Add");
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnAddFriend != null) {
            btnAddFriend.setOnClickListener(v -> {
                reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            reqRef.removeValue();
                            Toast.makeText(OtherCollectionActivity.this, "Friend request cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("senderId", currentUid);
                            map.put("timestamp", System.currentTimeMillis());
                            map.put("status", "pending");
                            reqRef.setValue(map);
                            Toast.makeText(OtherCollectionActivity.this, "Friend request sent to " + selectedUserName + "! 🤝", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }
    }

    private void loadSelectedUserProfile() {
        userProfileRef = FirebaseDatabase.getInstance().getReference("users").child(selectedUserId);
        userProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null && !name.trim().isEmpty()) {
                        selectedUserName = name.trim();
                        if (tvTitle != null) tvTitle.setText(selectedUserName);
                        if (tvCoupleLeftName != null) tvCoupleLeftName.setText(selectedUserName);
                    }

                    if (tvUserMeta != null) {
                        tvUserMeta.setText("ID: " + selectedUserId + "  |  1 followers  |  📍 India");
                    }

                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (avatar == null || avatar.trim().isEmpty()) avatar = snapshot.child("avatar").getValue(String.class);
                    if (avatar != null && !avatar.trim().isEmpty()) {
                        if (ivCoupleLeftAvatar != null) {
                            Glide.with(OtherCollectionActivity.this).load(avatar).placeholder(R.drawable.logo_placeholder).into(ivCoupleLeftAvatar);
                        }
                        if (ivUserProfileAvatar != null) {
                            Glide.with(OtherCollectionActivity.this).load(avatar).placeholder(R.drawable.logo_placeholder).into(ivUserProfileAvatar);
                        }
                    }

                    String frame = snapshot.child("equipped_frame").getValue(String.class);
                    if (frame != null && !frame.trim().isEmpty()) {
                        com.roomchatapps.Pmishra.utils.FrameUtils.displayFrame(OtherCollectionActivity.this, frame, ivUserProfileFrame, svgaUserProfileFrame);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userProfileRef.addListenerForSingleValueEvent(userProfileListener);
    }

    private void loadSelectedUserCollection() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Fetch User Photos
        if (selectedUserId != null) {
            DatabaseReference userNodeRef = FirebaseDatabase.getInstance().getReference("users").child(selectedUserId);
            userNodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    photoList.clear();
                    String activeAvatar = snapshot.child("avtar").getValue(String.class);
                    if (activeAvatar == null || activeAvatar.trim().isEmpty()) {
                        activeAvatar = snapshot.child("avatar").getValue(String.class);
                    }

                    List<String> loadedUrls = new ArrayList<>();
                    DataSnapshot pSnap = snapshot.child("photos");
                    if (pSnap.exists()) {
                        for (DataSnapshot ds : pSnap.getChildren()) {
                            String pKey = ds.getKey();
                            String pUrl = ds.getValue(String.class);
                            if (pUrl != null && !pUrl.trim().isEmpty()) {
                                loadedUrls.add(pUrl);
                                boolean isActive = pUrl.equalsIgnoreCase(activeAvatar);
                                photoList.add(new CollectionItemModel(pKey, pUrl, isActive));
                            }
                        }
                    }

                    if (activeAvatar != null && !activeAvatar.trim().isEmpty() && !loadedUrls.contains(activeAvatar)) {
                        photoList.add(new CollectionItemModel("active_avatar", activeAvatar, true));
                    }

                    if (photoAdapter != null) photoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Fetch Store Catalog
        StoreManager.getStoreCatalog(selectedUserId, "ALL", new StoreManager.CatalogCallback() {
            @Override
            public void onCatalogLoaded(List<StoreItemModel> catalog) {
                frameList.clear();
                vehicleList.clear();

                for (StoreItemModel item : catalog) {
                    if (item.isOwned()) {
                        if ("ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                            vehicleList.add(new CollectionItemModel(CollectionItemModel.ItemType.ENTRY_EFFECT, item));
                        } else if ("FRAME".equalsIgnoreCase(item.getCategory())) {
                            frameList.add(new CollectionItemModel(CollectionItemModel.ItemType.FRAME, item));
                        }
                    }
                }

                if (frameAdapter != null) frameAdapter.notifyDataSetChanged();
                if (vehicleAdapter != null) vehicleAdapter.notifyDataSetChanged();

                loadSelectedUserReceivedGifts();
            }

            @Override
            public void onError(String error) {
                loadSelectedUserReceivedGifts();
            }
        });
    }

    private void loadSelectedUserReceivedGifts() {
        txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(selectedUserId);
        userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(selectedUserId).child("received_gifts");

        txListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> giftCountMap = new HashMap<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String type = ds.child("type").getValue(String.class);
                        if (type == null) type = "";

                        String giftName = ds.child("giftName").getValue(String.class);
                        if (giftName == null || giftName.trim().isEmpty()) {
                            String title = ds.child("title").getValue(String.class);
                            if (title != null && title.startsWith("Received Gift: ")) {
                                giftName = title.substring("Received Gift: ".length()).trim();
                            } else if (title != null && title.startsWith("Earned Energy: ")) {
                                giftName = title.substring("Earned Energy: ".length()).trim();
                            } else if (title != null && title.startsWith("Gift: ")) {
                                giftName = title.substring("Gift: ".length()).trim();
                            }
                        }
                        if (giftName == null || giftName.trim().isEmpty()) {
                            String desc = ds.child("description").getValue(String.class);
                            if (desc != null && desc.contains("Received ") && desc.contains(" from ")) {
                                int start = desc.indexOf("Received ") + "Received ".length();
                                int end = desc.indexOf(" from ");
                                if (start < end) giftName = desc.substring(start, end).trim();
                            } else if (desc != null && desc.contains("Earned Energy (Targeted): ")) {
                                int start = desc.indexOf("Earned Energy (Targeted): ") + "Earned Energy (Targeted): ".length();
                                int end = desc.contains(" (") ? desc.indexOf(" (") : desc.length();
                                if (start < end) giftName = desc.substring(start, end).trim();
                            }
                        }

                        if ("GIFT_RECEIVED".equalsIgnoreCase(type) || "RECEIVED_GIFT".equalsIgnoreCase(type) || (giftName != null && !giftName.trim().isEmpty() && !type.equalsIgnoreCase("GIFT_SENT"))) {
                            if (giftName != null && !giftName.trim().isEmpty()) {
                                String cleanName = giftName.replaceAll("\\s*\\(.*?\\)", "").trim();
                                int qty = 1;
                                Long qObj = ds.child("quantity").getValue(Long.class);
                                if (qObj == null || qObj <= 0) qObj = ds.child("count").getValue(Long.class);
                                if (qObj != null && qObj > 0) qty = qObj.intValue();

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
                                Object val = ds.getValue();
                                long countVal = 0;
                                if (val instanceof Long) {
                                    countVal = (Long) val;
                                } else if (val instanceof Integer) {
                                    countVal = (Integer) val;
                                } else if (val instanceof String) {
                                    try { countVal = Long.parseLong((String) val); } catch (Exception ignored) {}
                                }

                                if (gKey != null && countVal > 0) {
                                    String cleanKey = gKey.replaceAll("\\s*\\(.*?\\)", "").trim();
                                    int existing = giftCountMap.containsKey(cleanKey) ? giftCountMap.get(cleanKey) : 0;
                                    giftCountMap.put(cleanKey, Math.max(existing, (int) countVal));
                                }
                            }
                        }

                        bindGifts(giftCountMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        bindGifts(giftCountMap);
                    }
                };

                userGiftsRef.addListenerForSingleValueEvent(userGiftsListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bindGifts(new HashMap<>());
            }
        };

        txRef.addValueEventListener(txListener);
    }

    private void bindGifts(Map<String, Integer> giftCountMap) {
        giftList.clear();
        int totalItemsCount = frameList.size() + vehicleList.size();

        for (Map.Entry<String, Integer> entry : giftCountMap.entrySet()) {
            String giftName = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                totalItemsCount += count;
                GiftStoreAdapter.GiftStoreItem catalogGift = GiftCatalog.findGiftByName(giftName);
                if (catalogGift != null) {
                    giftList.add(new CollectionItemModel(catalogGift, count));
                } else {
                    giftList.add(new CollectionItemModel(giftName, R.drawable.gift_icon, 100000L, count));
                }
            }
        }

        if (giftAdapter != null) giftAdapter.notifyDataSetChanged();

        // Populate Collection Hall with all owned items (Frames, Vehicles, Gifts)
        collectionHallList.clear();
        collectionHallList.addAll(frameList);
        collectionHallList.addAll(vehicleList);
        collectionHallList.addAll(giftList);
        if (collectionHallAdapter != null) collectionHallAdapter.notifyDataSetChanged();

        if (tvCollectionHallCount != null) {
            tvCollectionHallCount.setText(totalItemsCount + "/365 ›");
        }
        if (progressBar != null) progressBar.setVisibility(View.GONE);
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
        if (userProfileRef != null && userProfileListener != null) {
            userProfileRef.removeEventListener(userProfileListener);
        }
    }
}

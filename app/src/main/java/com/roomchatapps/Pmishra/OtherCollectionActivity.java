package com.roomchatapps.Pmishra;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.CollectionAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;
import com.roomchatapps.Pmishra.utils.StoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// OTHER COLLECTION
public class OtherCollectionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvSubtitle;
    private TextView tvSummaryEntryCount, tvSummaryFrameCount, tvSummaryGiftCount;
    private TextView tabAll, tabEntrances, tabFrames, tabGifts;
    private RecyclerView rvCollection;
    private LinearLayout llEmptyState;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private ProgressBar progressBar;

    private CollectionAdapter adapter;
    private final List<CollectionItemModel> allCollectionItems = new ArrayList<>();
    private final List<CollectionItemModel> filteredCollectionItems = new ArrayList<>();

    // SELECTED USER COLLECTION
    private String selectedUserId;
    private String selectedUserName;
    private String activeTabCategory = "ALL";

    private DatabaseReference txRef;
    private ValueEventListener txListener;
    private DatabaseReference userGiftsRef;
    private ValueEventListener userGiftsListener;
    private DatabaseReference userProfileRef;
    private ValueEventListener userProfileListener;

    private int totalEntryCount = 0;
    private int totalFrameCount = 0;
    private int totalGiftReceivedCount = 0;

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

        // SELECTED USER COLLECTION
        parseIntentData();

        initViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();

        if (selectedUserId != null && !selectedUserId.trim().isEmpty()) {
            loadSelectedUserProfile();
            loadSelectedUserCollection();
        } else {
            Toast.makeText(this, "Unable to load this user's collection.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // SELECTED USER COLLECTION
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

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvSummaryEntryCount = findViewById(R.id.tvSummaryEntryCount);
        tvSummaryFrameCount = findViewById(R.id.tvSummaryFrameCount);
        tvSummaryGiftCount = findViewById(R.id.tvSummaryGiftCount);
        tabAll = findViewById(R.id.tabAll);
        tabEntrances = findViewById(R.id.tabEntrances);
        tabFrames = findViewById(R.id.tabFrames);
        tabGifts = findViewById(R.id.tabGifts);
        rvCollection = findViewById(R.id.rvCollection);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        progressBar = findViewById(R.id.progressBar);

        if (tvTitle != null) {
            tvTitle.setText("Collection");
        }

        updateHeaderSubtitle();
    }

    private void updateHeaderSubtitle() {
        if (tvSubtitle != null) {
            if (selectedUserName != null && !selectedUserName.trim().isEmpty()) {
                tvSubtitle.setText(selectedUserName.trim() + "'s Collection");
            } else {
                tvSubtitle.setText("User's Collection");
            }
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
                        updateHeaderSubtitle();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userProfileRef.addListenerForSingleValueEvent(userProfileListener);
    }

    private void setupRecyclerView() {
        if (rvCollection != null) {
            rvCollection.setLayoutManager(new GridLayoutManager(this, 2));
            // Read-only click handler
            adapter = new CollectionAdapter(filteredCollectionItems, item -> {
                String msg = item.getItemName();
                if (item.getItemType() == CollectionItemModel.ItemType.GIFT_RECEIVED) {
                    msg += " (Received x" + item.getReceivedCount() + ")";
                } else if (item.isEquipped()) {
                    msg += " (Equipped)";
                } else {
                    msg += " (Unlocked)";
                }
                Toast.makeText(OtherCollectionActivity.this, msg, Toast.LENGTH_SHORT).show();
            });
            rvCollection.setAdapter(adapter);
        }
    }

    private void setupTabs() {
        if (tabAll != null) tabAll.setOnClickListener(v -> selectTab("ALL", tabAll));
        if (tabEntrances != null) tabEntrances.setOnClickListener(v -> selectTab("ENTRY", tabEntrances));
        if (tabFrames != null) tabFrames.setOnClickListener(v -> selectTab("FRAME", tabFrames));
        if (tabGifts != null) tabGifts.setOnClickListener(v -> selectTab("GIFT", tabGifts));
    }

    private void selectTab(String category, TextView selectedTab) {
        if (activeTabCategory.equalsIgnoreCase(category)) return;
        activeTabCategory = category;

        TextView[] tabs = {tabAll, tabEntrances, tabFrames, tabGifts};
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

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    // SELECTED USER COLLECTION
    private void loadSelectedUserCollection() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Fetch Store Catalog (Entry Effects & Frames owned by selected user)
        StoreManager.getStoreCatalog(selectedUserId, "ALL", new StoreManager.CatalogCallback() {
            @Override
            public void onCatalogLoaded(List<StoreItemModel> catalog) {
                List<CollectionItemModel> storeOwnedItems = new ArrayList<>();
                totalEntryCount = 0;
                totalFrameCount = 0;

                for (StoreItemModel item : catalog) {
                    if (item.isOwned()) {
                        if ("ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                            storeOwnedItems.add(new CollectionItemModel(CollectionItemModel.ItemType.ENTRY_EFFECT, item));
                            totalEntryCount++;
                        } else if ("FRAME".equalsIgnoreCase(item.getCategory())) {
                            storeOwnedItems.add(new CollectionItemModel(CollectionItemModel.ItemType.FRAME, item));
                            totalFrameCount++;
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

    // SELECTED USER COLLECTION
    private void loadSelectedUserReceivedGifts(List<CollectionItemModel> storeOwnedItems) {
        txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(selectedUserId);
        userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(selectedUserId).child("received_gifts");

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

        totalGiftReceivedCount = 0;

        for (Map.Entry<String, Integer> entry : giftCountMap.entrySet()) {
            String giftName = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                totalGiftReceivedCount += count;

                GiftStoreAdapter.GiftStoreItem catalogGift = GiftCatalog.findGiftByName(giftName);
                if (catalogGift != null) {
                    allCollectionItems.add(new CollectionItemModel(catalogGift, count));
                } else {
                    allCollectionItems.add(new CollectionItemModel(giftName, R.drawable.gift_icon, 100000L, count));
                }
            }
        }

        updateSummaryCard();

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        applyCategoryFilter();
    }

    private void updateSummaryCard() {
        if (tvSummaryEntryCount != null) tvSummaryEntryCount.setText(String.valueOf(totalEntryCount));
        if (tvSummaryFrameCount != null) tvSummaryFrameCount.setText(String.valueOf(totalFrameCount));
        if (tvSummaryGiftCount != null) tvSummaryGiftCount.setText(String.valueOf(totalGiftReceivedCount));
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

        if (adapter != null) {
            adapter.updateList(filteredCollectionItems);
        }

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredCollectionItems.isEmpty()) {
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
            if (rvCollection != null) rvCollection.setVisibility(View.GONE);

            if (tvEmptyTitle != null && tvEmptySubtitle != null) {
                if ("ENTRY".equalsIgnoreCase(activeTabCategory)) {
                    tvEmptyTitle.setText("No Entry Effects");
                    tvEmptySubtitle.setText("This user does not own any entry effects.");
                } else if ("FRAME".equalsIgnoreCase(activeTabCategory)) {
                    tvEmptyTitle.setText("No Frames");
                    tvEmptySubtitle.setText("This user does not own any frames.");
                } else if ("GIFT".equalsIgnoreCase(activeTabCategory)) {
                    tvEmptyTitle.setText("No gifts received yet");
                    tvEmptySubtitle.setText("Gifts sent to this user will appear here.");
                } else {
                    tvEmptyTitle.setText("User's Collection is Empty");
                    tvEmptySubtitle.setText("This user has no items or gifts in their collection.");
                }
            }
        } else {
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
            if (rvCollection != null) rvCollection.setVisibility(View.VISIBLE);
        }
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

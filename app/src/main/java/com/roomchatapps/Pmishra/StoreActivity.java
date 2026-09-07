package com.roomchatapps.Pmishra;

import android.content.Intent;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.StoreManager;

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvCoins;
    private LinearLayout llWalletBadge, llEmptyStore;
    private TextView tabAll, tabFrames, tabEntrances, tabBubbles, tabVip;
    private RecyclerView rvStoreItems;
    private ProgressBar progressBar;

    private StoreAdapter adapter;
    private final List<StoreItemModel> storeItemList = new ArrayList<>();
    private DatabaseReference userRef;
    private String currentUid;
    private String activeCategory = "ALL";
    private long lastCoinsVal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        currentUid = FirebaseAuth.getInstance().getUid();

        initViews();
        setupAnimations();
        setupTabs();
        setupRecyclerView();
        setupClickListeners();
        loadWalletBalance();
        loadCatalog();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvCoins = findViewById(R.id.tvCoins);
        llWalletBadge = findViewById(R.id.llWalletBadge);
        llEmptyStore = findViewById(R.id.llEmptyStore);
        tabAll = findViewById(R.id.tabAll);
        tabFrames = findViewById(R.id.tabFrames);
        tabEntrances = findViewById(R.id.tabEntrances);
        tabBubbles = findViewById(R.id.tabBubbles);
        tabVip = findViewById(R.id.tabVip);
        rvStoreItems = findViewById(R.id.rvStoreItems);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupAnimations() {
        View header = findViewById(R.id.header);
        View svTabs = findViewById(R.id.svTabs);

        if (header != null) AnimationHelper.fadeIn(header, 400);
        if (svTabs != null) AnimationHelper.scaleIn(svTabs, 500);
        if (llWalletBadge != null) AnimationHelper.pulseGlowAnimation(llWalletBadge);
    }

    private void setupTabs() {
        if (tabAll != null) tabAll.setOnClickListener(v -> selectTab("ALL", tabAll));
        if (tabFrames != null) tabFrames.setOnClickListener(v -> selectTab("FRAME", tabFrames));
        if (tabEntrances != null) tabEntrances.setOnClickListener(v -> selectTab("ENTRANCE", tabEntrances));
        if (tabBubbles != null) tabBubbles.setOnClickListener(v -> selectTab("BUBBLE", tabBubbles));
        if (tabVip != null) tabVip.setOnClickListener(v -> selectTab("VIP", tabVip));
    }

    private void selectTab(String category, TextView selectedTab) {
        if (activeCategory.equalsIgnoreCase(category)) return;
        activeCategory = category;

        TextView[] tabs = {tabAll, tabFrames, tabEntrances, tabBubbles, tabVip};
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

        loadCatalog();
    }

    private void setupRecyclerView() {
        if (rvStoreItems != null) {
            rvStoreItems.setLayoutManager(new GridLayoutManager(this, 2));
            adapter = new StoreAdapter(storeItemList, new StoreAdapter.OnStoreItemClickListener() {
                @Override
                public void onItemAction(StoreItemModel item, int position) {
                    handleItemAction(item, position);
                }
            });
            rvStoreItems.setAdapter(adapter);
        }
    }

    private void handleItemAction(StoreItemModel item, int position) {
        if (currentUid == null) {
            Toast.makeText(this, "Please login to buy items", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item.isEquipped()) {
            Toast.makeText(this, item.getName() + " is already equipped! ✓", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item.isOwned()) {
            // Equip owned item
            Toast.makeText(this, "Equipping " + item.getName() + "...", Toast.LENGTH_SHORT).show();
            StoreManager.equipItem(currentUid, item, new StoreManager.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(StoreActivity.this, "✨ " + message, Toast.LENGTH_SHORT).show();
                    loadCatalog(); // refresh states
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(StoreActivity.this, "Failed to equip: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Buy new item
            Toast.makeText(this, "Purchasing " + item.getName() + "...", Toast.LENGTH_SHORT).show();
            StoreManager.buyItem(currentUid, item, new StoreManager.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(StoreActivity.this, message, Toast.LENGTH_LONG).show();
                    loadCatalog(); // refresh catalog & equipped states
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(StoreActivity.this, "Purchase failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (llWalletBadge != null) {
            llWalletBadge.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                Intent intent = new Intent(StoreActivity.this, WalletActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadWalletBalance() {
        if (currentUid == null) return;

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object coinsObj = snapshot.child("coins").getValue();
                    long coinsVal = 0;
                    if (coinsObj != null) {
                        try {
                            coinsVal = Long.parseLong(String.valueOf(coinsObj));
                        } catch (Exception e) {}
                    }

                    if (tvCoins != null) {
                        if (lastCoinsVal >= 0 && lastCoinsVal != coinsVal) {
                            AnimationHelper.animateNumberCounter(tvCoins, lastCoinsVal, coinsVal);
                            AnimationHelper.bounceAnimation(tvCoins);
                        } else {
                            tvCoins.setText(String.valueOf(coinsVal));
                        }
                    }
                    lastCoinsVal = coinsVal;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCatalog() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        StoreManager.getStoreCatalog(currentUid, activeCategory, new StoreManager.CatalogCallback() {
            @Override
            public void onCatalogLoaded(List<StoreItemModel> items) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                storeItemList.clear();
                storeItemList.addAll(items);
                if (adapter != null) adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (storeItemList.isEmpty()) {
            if (llEmptyStore != null) llEmptyStore.setVisibility(View.VISIBLE);
            if (rvStoreItems != null) rvStoreItems.setVisibility(View.GONE);
        } else {
            if (llEmptyStore != null) llEmptyStore.setVisibility(View.GONE);
            if (rvStoreItems != null) rvStoreItems.setVisibility(View.VISIBLE);
        }
    }
}

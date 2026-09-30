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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.StoreManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvCoins;
    private LinearLayout llWalletBadge, llEmptyStore;
    private TextView tabFrames, tabEntrances;
    private RecyclerView rvStoreItems;
    private ProgressBar progressBar;

    private StoreAdapter adapter;
    private final List<StoreItemModel> storeItemList = new ArrayList<>();
    private DatabaseReference userRef;
    private String currentUid;
    private String activeCategory = "FRAME";
    private long lastCoinsVal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
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
        tabFrames = findViewById(R.id.tabFrames);
        tabEntrances = findViewById(R.id.tabEntrances);
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
        if (tabFrames != null) tabFrames.setOnClickListener(v -> selectTab("FRAME", tabFrames));
        if (tabEntrances != null) tabEntrances.setOnClickListener(v -> selectTab("ENTRANCE", tabEntrances));
    }

    private void selectTab(String category, TextView selectedTab) {
        if (activeCategory.equalsIgnoreCase(category)) return;
        activeCategory = category;

        TextView[] tabs = {tabFrames, tabEntrances};
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

                @Override
                public void onItemClick(StoreItemModel item, int position) {
                    showFramePreviewDialog(item);
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
            // Unequip item
            Toast.makeText(this, "Unequipping " + item.getName() + "...", Toast.LENGTH_SHORT).show();
            StoreManager.unequipItem(currentUid, item, new StoreManager.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(StoreActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadCatalog(); // refresh catalog & equipped states
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(StoreActivity.this, "Failed to unequip: " + error, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        if (item.isOwned()) {
            // Equip owned item
            Toast.makeText(this, "Equipping " + item.getName() + "...", Toast.LENGTH_SHORT).show();
            StoreManager.equipItem(currentUid, item, new StoreManager.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(StoreActivity.this, message, Toast.LENGTH_SHORT).show();
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

    private void showFramePreviewDialog(StoreItemModel item) {
        if (item == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_frame_preview, null, false);
        dialog.setContentView(view);

        boolean isEntrance = "ENTRANCE".equalsIgnoreCase(item.getCategory());

        TextView tvDialogTitle = view.findViewById(R.id.tvTitle);
        View ivPreviewAvatar = view.findViewById(R.id.ivPreviewAvatar);
        TextView tvFrameName = view.findViewById(R.id.tvPreviewFrameName);
        TextView tvBadge = view.findViewById(R.id.tvPreviewBadge);
        TextView tvDescription = view.findViewById(R.id.tvPreviewDescription);
        MaterialButton btnAction = view.findViewById(R.id.btnPreviewAction);
        ImageView btnClose = view.findViewById(R.id.btnClose);
        ImageView ivStaticFrame = view.findViewById(R.id.ivStaticFramePreview);
        SVGAImageView svgaFrame = view.findViewById(R.id.svgaFramePreview);

        if (tvDialogTitle != null) {
            tvDialogTitle.setText(isEntrance ? "Entrance Live Preview" : "Frame Live Preview");
        }

        // Hide profile avatar when previewing Entrance effects!
        if (ivPreviewAvatar != null) {
            ivPreviewAvatar.setVisibility(isEntrance ? View.GONE : View.VISIBLE);
        }

        if (tvFrameName != null) tvFrameName.setText(item.getName());
        if (tvBadge != null) tvBadge.setText(item.getBadgeText() != null ? item.getBadgeText() : "FRAME");
        if (tvDescription != null) tvDescription.setText(item.getDescription() != null ? item.getDescription() : "Avatar Frame Preview");

        if (btnAction != null) {
            if (item.isEquipped()) {
                btnAction.setText("Unequip");
                btnAction.setBackgroundColor(Color.parseColor("#FF6B6B"));
                btnAction.setTextColor(Color.parseColor("#FFFFFF"));
            } else if (item.isOwned()) {
                btnAction.setText(isEntrance ? "Equip Entrance" : "Equip Frame");
                btnAction.setBackgroundColor(Color.parseColor("#40E0D0"));
                btnAction.setTextColor(Color.parseColor("#050E1E"));
            } else {
                btnAction.setText("Buy for " + item.getPriceCoins());
                btnAction.setBackgroundColor(Color.parseColor("#FFD700"));
                btnAction.setTextColor(Color.parseColor("#050E1E"));
            }

            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                handleItemAction(item, -1);
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (item.getSvgaPath() != null && !item.getSvgaPath().isEmpty()) {
            if (ivStaticFrame != null) ivStaticFrame.setVisibility(View.GONE);
            if (svgaFrame != null) {
                svgaFrame.setVisibility(View.VISIBLE);
                svgaFrame.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                SVGAParser parser = new SVGAParser(this);
                parser.decodeFromAssets(item.getSvgaPath(), new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        runOnUiThread(() -> {
                            if (svgaFrame != null) {
                                svgaFrame.setVideoItem(videoItem);
                                svgaFrame.setLoops(0);
                                svgaFrame.stepToFrame(0, true);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        runOnUiThread(() -> {
                            if (svgaFrame != null) svgaFrame.setVisibility(View.GONE);
                            if (ivStaticFrame != null) {
                                ivStaticFrame.setVisibility(View.VISIBLE);
                                int resId = getResources().getIdentifier(item.getIconResName(), "drawable", getPackageName());
                                if (resId == 0) resId = R.drawable.family_owner_frame;
                                ivStaticFrame.setImageResource(resId);
                            }
                        });
                    }
                }, null);
            }
        } else {
            if (svgaFrame != null) svgaFrame.setVisibility(View.GONE);
            if (ivStaticFrame != null) {
                ivStaticFrame.setVisibility(View.VISIBLE);
                int resId = 0;
                if (item.getIconResName() != null) {
                    resId = getResources().getIdentifier(item.getIconResName(), "drawable", getPackageName());
                }
                if (resId == 0) resId = R.drawable.family_owner_frame;
                ivStaticFrame.setImageResource(resId);
            }
        }

        dialog.show();
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

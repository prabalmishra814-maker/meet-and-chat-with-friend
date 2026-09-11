package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.GiftCountAdapter;
import com.roomchatapps.Pmishra.models.GiftCountModel;
import com.roomchatapps.Pmishra.models.TransactionModel;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCoins, tvDiamonds, tvTxSummary;
    private View header, llBalances;
    private CardView cvCoins, cvDiamonds;
    private TextView tabTxAll, tabTxGiftCounts, tabTxReceived, tabTxSent, tabTxTopup;
    private RecyclerView rvTransactions, rvGiftCounts;
    private View llEmptyTransactions;
    private ProgressBar progressBar;

    private TransactionAdapter txAdapter;
    private GiftCountAdapter giftCountAdapter;

    private final List<TransactionModel> allTransactionList = new ArrayList<>();
    private final List<TransactionModel> filteredTransactionList = new ArrayList<>();
    private final List<GiftCountModel> giftCountList = new ArrayList<>();

    private DatabaseReference userRef;
    private String currentUid;
    private long lastCoinsVal = -1;
    private String activeTab = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        currentUid = FirebaseAuth.getInstance().getUid();

        initViews();
        setupAnimations();
        setupRecyclerViews();
        setupTabs();
        setupClickListeners();
        loadWalletData();
        loadTransactions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCoins = findViewById(R.id.tvCoins);
        tvDiamonds = findViewById(R.id.tvDiamonds);
        tvTxSummary = findViewById(R.id.tvTxSummary);
        header = findViewById(R.id.header);
        llBalances = findViewById(R.id.llBalances);
        cvCoins = findViewById(R.id.cvCoins);
        cvDiamonds = findViewById(R.id.cvDiamonds);

        tabTxAll = findViewById(R.id.tabTxAll);
        tabTxGiftCounts = findViewById(R.id.tabTxGiftCounts);
        tabTxReceived = findViewById(R.id.tabTxReceived);
        tabTxSent = findViewById(R.id.tabTxSent);
        tabTxTopup = findViewById(R.id.tabTxTopup);

        rvTransactions = findViewById(R.id.rvTransactions);
        rvGiftCounts = findViewById(R.id.rvGiftCounts);
        llEmptyTransactions = findViewById(R.id.llEmptyTransactions);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupAnimations() {
        if (header != null) AnimationHelper.fadeIn(header, 400);
        if (llBalances != null) AnimationHelper.scaleIn(llBalances, 500);

        if (cvCoins != null) AnimationHelper.pulseGlowAnimation(cvCoins);
        if (cvDiamonds != null) AnimationHelper.pulseGlowAnimation(cvDiamonds);
    }

    private void setupRecyclerViews() {
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            txAdapter = new TransactionAdapter(filteredTransactionList);
            rvTransactions.setAdapter(txAdapter);
        }

        if (rvGiftCounts != null) {
            rvGiftCounts.setLayoutManager(new GridLayoutManager(this, 2));
            giftCountAdapter = new GiftCountAdapter(giftCountList);
            rvGiftCounts.setAdapter(giftCountAdapter);
        }
    }

    private void setupTabs() {
        if (tabTxAll != null) tabTxAll.setOnClickListener(v -> selectTab("ALL", tabTxAll));
        if (tabTxGiftCounts != null) tabTxGiftCounts.setOnClickListener(v -> selectTab("GIFT_COUNTS", tabTxGiftCounts));
        if (tabTxReceived != null) tabTxReceived.setOnClickListener(v -> selectTab("GIFT_RECEIVED", tabTxReceived));
        if (tabTxSent != null) tabTxSent.setOnClickListener(v -> selectTab("GIFT_SENT", tabTxSent));
        if (tabTxTopup != null) tabTxTopup.setOnClickListener(v -> selectTab("TOPUP", tabTxTopup));
    }

    private void selectTab(String tabKey, TextView selectedTab) {
        if (activeTab.equalsIgnoreCase(tabKey)) return;
        activeTab = tabKey;

        TextView[] tabs = {tabTxAll, tabTxGiftCounts, tabTxReceived, tabTxSent, tabTxTopup};
        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setBackgroundResource(R.drawable.chip_room_bg);
                tab.setTextColor(Color.parseColor("#88FFFFFF"));
            }
        }

        if (selectedTab != null) {
            selectedTab.setBackgroundResource(R.drawable.chip_charm_bg);
            selectedTab.setTextColor(Color.WHITE);
        }

        filterAndDisplayData();
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void loadWalletData() {
        if (currentUid == null) return;

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object coinsObj = snapshot.child("coins").getValue();
                    Object diamondsObj = snapshot.child("diamonds").getValue();

                    long coinsVal = 0;
                    if (coinsObj != null) {
                        try {
                            coinsVal = Long.parseLong(String.valueOf(coinsObj));
                        } catch (Exception e) {}
                    }

                    String diamondsStr = diamondsObj != null ? String.valueOf(diamondsObj) : "0";

                    if (tvCoins != null) {
                        if (lastCoinsVal >= 0 && lastCoinsVal != coinsVal) {
                            AnimationHelper.animateNumberCounter(tvCoins, lastCoinsVal, coinsVal);
                            AnimationHelper.bounceAnimation(tvCoins);
                        } else {
                            tvCoins.setText(String.valueOf(coinsVal));
                        }
                    }
                    lastCoinsVal = coinsVal;

                    if (tvDiamonds != null) tvDiamonds.setText(diamondsStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTransactions() {
        if (currentUid == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            filterAndDisplayData();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        WalletManager.loadTransactionHistory(currentUid, new WalletManager.TransactionCallback() {
            @Override
            public void onTransactionsLoaded(List<TransactionModel> transactions) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                allTransactionList.clear();
                if (transactions != null) {
                    allTransactionList.addAll(transactions);
                }
                computeGiftCounts();
                filterAndDisplayData();
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                filterAndDisplayData();
            }
        });
    }

    private void computeGiftCounts() {
        // Initialize default 8 gifts
        Map<String, GiftCountModel> map = new HashMap<>();

        addGiftToMap(map, "Heart 💖", R.drawable._1000092377_removebg_preview, 0);
        addGiftToMap(map, "Rose 🌹", R.drawable._1000092341_removebg_preview, 0);
        addGiftToMap(map, "Crown 👑", R.drawable._1000092342_removebg_preview, 50);
        addGiftToMap(map, "Diamond 💎", R.drawable._1000092343_removebg_preview, 100);
        addGiftToMap(map, "Car 🚗", R.drawable._1000092344_removebg_preview, 200);
        addGiftToMap(map, "Cyber Ring 💍", R.drawable._1000092357_removebg_preview, 300);
        addGiftToMap(map, "Royal Ring 💎", R.drawable._1000092358_removebg_preview, 500);
        addGiftToMap(map, "Phoenix Wings 🦅", R.drawable._1000092363_removebg_preview, 800);

        for (TransactionModel tx : allTransactionList) {
            if (tx == null) continue;
            String type = tx.getType() != null ? tx.getType().toUpperCase() : "";
            String text = (tx.getTitle() + " " + tx.getDescription()).toLowerCase();

            String matchedKey = null;
            if (text.contains("heart")) matchedKey = "Heart 💖";
            else if (text.contains("rose")) matchedKey = "Rose 🌹";
            else if (text.contains("crown")) matchedKey = "Crown 👑";
            else if (text.contains("diamond")) matchedKey = "Diamond 💎";
            else if (text.contains("car")) matchedKey = "Car 🚗";
            else if (text.contains("cyber")) matchedKey = "Cyber Ring 💍";
            else if (text.contains("royal")) matchedKey = "Royal Ring 💎";
            else if (text.contains("phoenix") || text.contains("wing")) matchedKey = "Phoenix Wings 🦅";

            if (matchedKey != null && map.containsKey(matchedKey)) {
                GiftCountModel model = map.get(matchedKey);
                if (model != null) {
                    if ("GIFT_RECEIVED".equals(type)) {
                        model.setReceivedCount(model.getReceivedCount() + 1);
                    } else if ("GIFT_SENT".equals(type)) {
                        model.setSentCount(model.getSentCount() + 1);
                    }
                }
            }
        }

        giftCountList.clear();
        giftCountList.addAll(map.values());
    }

    private void addGiftToMap(Map<String, GiftCountModel> map, String name, int iconRes, long cost) {
        map.put(name, new GiftCountModel(name, iconRes, cost, 0, 0));
    }

    private void filterAndDisplayData() {
        filteredTransactionList.clear();

        if ("GIFT_COUNTS".equalsIgnoreCase(activeTab)) {
            if (rvTransactions != null) rvTransactions.setVisibility(View.GONE);
            if (rvGiftCounts != null) rvGiftCounts.setVisibility(View.VISIBLE);

            if (giftCountAdapter != null) giftCountAdapter.notifyDataSetChanged();

            int totalRec = 0;
            int totalSent = 0;
            for (GiftCountModel g : giftCountList) {
                totalRec += g.getReceivedCount();
                totalSent += g.getSentCount();
            }

            if (tvTxSummary != null) {
                tvTxSummary.setText("🎁 Total Gifts Received: " + totalRec + "  |  📤 Total Gifts Sent/Bought: " + totalSent);
                tvTxSummary.setVisibility(View.VISIBLE);
            }

            if (llEmptyTransactions != null) {
                llEmptyTransactions.setVisibility(giftCountList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            return;
        }

        if (rvGiftCounts != null) rvGiftCounts.setVisibility(View.GONE);
        if (rvTransactions != null) rvTransactions.setVisibility(View.VISIBLE);

        int totalCount = 0;
        long totalVal = 0;

        for (TransactionModel tx : allTransactionList) {
            String type = tx.getType() != null ? tx.getType().toUpperCase() : "";

            if ("ALL".equalsIgnoreCase(activeTab)) {
                filteredTransactionList.add(tx);
            } else if ("GIFT_RECEIVED".equalsIgnoreCase(activeTab) && "GIFT_RECEIVED".equals(type)) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += tx.getDiamondAmount();
            } else if ("GIFT_SENT".equalsIgnoreCase(activeTab) && "GIFT_SENT".equals(type)) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += Math.abs(tx.getCoinAmount());
            } else if ("TOPUP".equalsIgnoreCase(activeTab) && ("TOPUP".equals(type) || "WELCOME_BONUS".equals(type))) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += tx.getCoinAmount();
            }
        }

        if (txAdapter != null) txAdapter.notifyDataSetChanged();

        if (tvTxSummary != null) {
            if ("GIFT_RECEIVED".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("🎁 Received " + totalCount + " gifts (" + totalVal + " Diamonds Earned)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else if ("GIFT_SENT".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("📤 Sent/Bought " + totalCount + " gifts (" + totalVal + " Coins Spent)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else if ("TOPUP".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("🪙 Top-Up Total: " + totalVal + " Coins");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else {
                tvTxSummary.setVisibility(View.GONE);
            }
        }

        if (llEmptyTransactions != null) {
            llEmptyTransactions.setVisibility(filteredTransactionList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}

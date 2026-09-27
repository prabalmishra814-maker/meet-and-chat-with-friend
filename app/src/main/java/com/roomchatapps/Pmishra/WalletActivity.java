package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCoins, tvTxSummary;
    private View header;
    private View cvCoins;
    private TextView btnRechargeHeader, btnRechargeQuick;
    private TextView tabTxAll, tabTxTopup, tabTxSent, tabTxReceived, tabTxStore, tabTxSpin;
    private RecyclerView rvTransactions;
    private View llEmptyTransactions;
    private ProgressBar progressBar;

    private TransactionAdapter txAdapter;

    private final List<TransactionModel> allTransactionList = new ArrayList<>();
    private final List<TransactionModel> filteredTransactionList = new ArrayList<>();

    private DatabaseReference userRef;
    private DatabaseReference txRef;
    private ValueEventListener userWalletListener;
    private ValueEventListener txListener;

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
        tvTxSummary = findViewById(R.id.tvTxSummary);
        header = findViewById(R.id.header);
        cvCoins = findViewById(R.id.cvCoins);

        btnRechargeHeader = findViewById(R.id.btnRechargeHeader);
        btnRechargeQuick = findViewById(R.id.btnRechargeQuick);

        tabTxAll = findViewById(R.id.tabTxAll);
        tabTxTopup = findViewById(R.id.tabTxTopup);
        tabTxSent = findViewById(R.id.tabTxSent);
        tabTxReceived = findViewById(R.id.tabTxReceived);
        tabTxStore = findViewById(R.id.tabTxStore);
        tabTxSpin = findViewById(R.id.tabTxSpin);

        rvTransactions = findViewById(R.id.rvTransactions);
        llEmptyTransactions = findViewById(R.id.llEmptyTransactions);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupAnimations() {
        if (header != null) AnimationHelper.fadeIn(header, 400);
    }

    private void setupRecyclerViews() {
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            txAdapter = new TransactionAdapter(filteredTransactionList);
            rvTransactions.setAdapter(txAdapter);
        }
    }

    private void setupTabs() {
        if (tabTxAll != null) tabTxAll.setOnClickListener(v -> selectTab("ALL", tabTxAll));
        if (tabTxTopup != null) tabTxTopup.setOnClickListener(v -> selectTab("TOPUP", tabTxTopup));
        if (tabTxSent != null) tabTxSent.setOnClickListener(v -> selectTab("GIFT_SENT", tabTxSent));
        if (tabTxReceived != null) tabTxReceived.setOnClickListener(v -> selectTab("GIFT_RECEIVED", tabTxReceived));
        if (tabTxStore != null) tabTxStore.setOnClickListener(v -> selectTab("STORE_BUY", tabTxStore));
        if (tabTxSpin != null) tabTxSpin.setOnClickListener(v -> selectTab("GAME_SPIN", tabTxSpin));
    }

    private void selectTab(String tabKey, TextView selectedTab) {
        if (activeTab.equalsIgnoreCase(tabKey)) return;
        activeTab = tabKey;

        TextView[] tabs = {tabTxAll, tabTxTopup, tabTxSent, tabTxReceived, tabTxStore, tabTxSpin};
        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                tab.setTextColor(Color.parseColor("#90FFFFFF"));
            }
        }

        if (selectedTab != null) {
            selectedTab.setBackgroundResource(R.drawable.bg_wallet_chip_selected);
            selectedTab.setTextColor(Color.WHITE);
        }

        filterAndDisplayData();
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View.OnClickListener openRecharge = v -> {
            Intent intent = new Intent(WalletActivity.this, CoinRechargeActivity.class);
            startActivity(intent);
        };

        if (btnRechargeHeader != null) btnRechargeHeader.setOnClickListener(openRecharge);
        if (btnRechargeQuick != null) btnRechargeQuick.setOnClickListener(openRecharge);
    }

    private void loadWalletData() {
        if (currentUid == null) return;

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid).child("coins");
        userWalletListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                long coinsVal = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        coinsVal = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception ignored) {}
                }

                NumberFormat formatter = NumberFormat.getInstance();

                if (tvCoins != null) {
                    if (lastCoinsVal >= 0 && lastCoinsVal != coinsVal) {
                        AnimationHelper.animateNumberCounter(tvCoins, lastCoinsVal, coinsVal);
                        AnimationHelper.bounceAnimation(tvCoins);
                    } else {
                        tvCoins.setText(formatter.format(coinsVal));
                    }
                }
                lastCoinsVal = coinsVal;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userRef.addValueEventListener(userWalletListener);
    }

    private void loadTransactions() {
        if (currentUid == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            filterAndDisplayData();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(currentUid);
        txListener = WalletManager.loadTransactionHistory(currentUid, new WalletManager.TransactionCallback() {
            @Override
            public void onTransactionsLoaded(List<TransactionModel> transactions) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                allTransactionList.clear();
                if (transactions != null) {
                    allTransactionList.addAll(transactions);
                }
                filterAndDisplayData();
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                filterAndDisplayData();
            }
        });
    }

    private void filterAndDisplayData() {
        if (isFinishing() || isDestroyed()) return;
        filteredTransactionList.clear();

        if (rvTransactions != null) rvTransactions.setVisibility(View.VISIBLE);
        if (rvTransactions != null) rvTransactions.setVisibility(View.VISIBLE);

        int totalCount = 0;
        long totalVal = 0;
        NumberFormat formatter = NumberFormat.getInstance();

        for (TransactionModel tx : allTransactionList) {
            if (tx == null) continue;
            String type = tx.getType() != null ? tx.getType().toUpperCase() : "";

            if ("ALL".equalsIgnoreCase(activeTab)) {
                filteredTransactionList.add(tx);
            } else if ("TOPUP".equalsIgnoreCase(activeTab) && ("TOPUP".equals(type) || "WELCOME_BONUS".equals(type) || "REFERRAL".equals(type))) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += tx.getCoinAmount();
            } else if ("GIFT_SENT".equalsIgnoreCase(activeTab) && "GIFT_SENT".equals(type)) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += Math.abs(tx.getCoinAmount());
            } else if ("GIFT_RECEIVED".equalsIgnoreCase(activeTab) && "GIFT_RECEIVED".equals(type)) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += tx.getDiamondAmount() > 0 ? tx.getDiamondAmount() : Math.abs(tx.getCoinAmount());
            } else if ("STORE_BUY".equalsIgnoreCase(activeTab) && ("STORE_BUY".equals(type) || "THEME_BUY".equals(type))) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += Math.abs(tx.getCoinAmount());
            } else if ("GAME_SPIN".equalsIgnoreCase(activeTab) && "GAME_SPIN".equals(type)) {
                filteredTransactionList.add(tx);
                totalCount++;
                totalVal += Math.abs(tx.getCoinAmount());
            }
        }

        if (txAdapter != null) {
            txAdapter.resetAnimationState();
            txAdapter.notifyDataSetChanged();
        }

        if (tvTxSummary != null) {
            if ("TOPUP".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("Total Recharged: " + formatter.format(totalVal) + " Coins (" + totalCount + " Transactions)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else if ("GIFT_SENT".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("Total Spent on Gifts: " + formatter.format(totalVal) + " Coins (" + totalCount + " Gifts Sent)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else if ("GIFT_RECEIVED".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("Total Earned: " + formatter.format(totalVal) + " Diamonds (" + totalCount + " Gifts Received)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else if ("STORE_BUY".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("Total Store & Theme Purchases: " + formatter.format(totalVal) + " Coins (" + totalCount + " Purchases)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else if ("GAME_SPIN".equalsIgnoreCase(activeTab)) {
                tvTxSummary.setText("Total Games & Spin Transactions: " + formatter.format(totalVal) + " Coins (" + totalCount + " Games)");
                tvTxSummary.setVisibility(View.VISIBLE);
            } else {
                tvTxSummary.setVisibility(View.GONE);
            }
        }

        if (llEmptyTransactions != null) {
            llEmptyTransactions.setVisibility(filteredTransactionList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && userWalletListener != null) {
            userRef.removeEventListener(userWalletListener);
        }
        if (txRef != null && txListener != null) {
            txRef.removeEventListener(txListener);
        }
    }
}

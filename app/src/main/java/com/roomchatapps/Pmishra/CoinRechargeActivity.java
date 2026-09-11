package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.WalletManager;

public class CoinRechargeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCoinBalance, tvSelectedCoins, tvSelectedPrice;
    private View btnPayNow;

    private FrameLayout[] packCards;
    private final long[] coinAmounts = {100, 600, 1500, 4000, 9500, 21000};
    private final int[] prices = {10, 50, 100, 250, 500, 1000};
    private final String[] packageNames = {
            "Starter Pack",
            "Popular Pack (+100 Bonus)",
            "Value Pack (+300 Bonus)",
            "Super Saver (+1000 Bonus)",
            "VIP Mega Pack (+2500 Bonus)",
            "Ultimate Crown (+6000 Bonus)"
    };

    private int selectedIndex = 0;
    private String currentUid;
    private long lastCoinsVal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_recharge);

        currentUid = FirebaseAuth.getInstance().getUid();

        btnBack = findViewById(R.id.btnBack);
        tvCoinBalance = findViewById(R.id.tvCoinBalance);
        tvSelectedCoins = findViewById(R.id.tvSelectedCoins);
        tvSelectedPrice = findViewById(R.id.tvSelectedPrice);
        btnPayNow = findViewById(R.id.btnPayNow);

        packCards = new FrameLayout[]{
                findViewById(R.id.cardPack1),
                findViewById(R.id.cardPack2),
                findViewById(R.id.cardPack3),
                findViewById(R.id.cardPack4),
                findViewById(R.id.cardPack5),
                findViewById(R.id.cardPack6)
        };

        setupClickListeners();
        selectPack(0);
        loadUserCoins();
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        for (int i = 0; i < packCards.length; i++) {
            final int index = i;
            if (packCards[index] != null) {
                packCards[index].setOnClickListener(v -> selectPack(index));
            }
        }

        if (btnPayNow != null) {
            btnPayNow.setOnClickListener(v -> handlePayment());
        }
    }

    private void selectPack(int index) {
        selectedIndex = index;
        for (int i = 0; i < packCards.length; i++) {
            if (packCards[i] != null) {
                if (i == selectedIndex) {
                    packCards[i].setBackgroundResource(R.drawable.bg_recharge_card_selected);
                } else {
                    packCards[i].setBackgroundResource(R.drawable.bg_recharge_card_normal);
                }
            }
        }

        long coins = coinAmounts[selectedIndex];
        int price = prices[selectedIndex];

        if (tvSelectedCoins != null) {
            tvSelectedCoins.setText(coins + " Coins");
        }
        if (tvSelectedPrice != null) {
            tvSelectedPrice.setText("Total Price: ₹" + price);
        }
    }

    private void handlePayment() {
        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(this, "Please log in to top-up wallet", Toast.LENGTH_SHORT).show();
            return;
        }

        long coins = coinAmounts[selectedIndex];
        String packName = packageNames[selectedIndex];

        Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();

        WalletManager.addCoins(currentUid, coins, packName, new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                if (lastCoinsVal >= 0 && tvCoinBalance != null) {
                    AnimationHelper.animateNumberCounter(tvCoinBalance, lastCoinsVal, newCoinBalance);
                    AnimationHelper.bounceAnimation(tvCoinBalance);
                } else if (tvCoinBalance != null) {
                    tvCoinBalance.setText(String.valueOf(newCoinBalance));
                }
                lastCoinsVal = newCoinBalance;
                Toast.makeText(CoinRechargeActivity.this, "🎉 " + message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CoinRechargeActivity.this, "❌ Top-up failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserCoins() {
        if (currentUid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object coinsObj = snapshot.child("coins").getValue();
                    long coinsVal = 0;
                    if (coinsObj != null) {
                        try {
                            coinsVal = Long.parseLong(String.valueOf(coinsObj));
                        } catch (Exception ignored) {}
                    }

                    if (tvCoinBalance != null) {
                        if (lastCoinsVal >= 0 && lastCoinsVal != coinsVal) {
                            AnimationHelper.animateNumberCounter(tvCoinBalance, lastCoinsVal, coinsVal);
                            AnimationHelper.bounceAnimation(tvCoinBalance);
                        } else {
                            tvCoinBalance.setText(String.valueOf(coinsVal));
                        }
                    }
                    lastCoinsVal = coinsVal;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

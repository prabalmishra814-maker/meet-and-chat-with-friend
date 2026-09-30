package com.roomchatapps.Pmishra;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
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

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoinRechargeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCoinBalance, tvSelectedCoins, tvSelectedPrice;
    private View btnPayNow;
    private View cardBonusBanner;
    private TextView tvBannerTitle, tvBannerSubtitle;
    private View llAllOffersClaimed;

    private FrameLayout[] packCards;
    private TextView[] tvBadges;
    private TextView[] tvCoins;
    private TextView[] tvExtras;

    private final long[] baseCoinAmounts = {
            1800000L,      // ₹50
            4000000L,      // ₹100
            8000000L,      // ₹200
            22000000L,     // ₹500
            45000000L,     // ₹1,000
            250000000L     // ₹5,000
    };

    private final long[] offerCoinAmounts = {
            1800000L,      // ₹50 (First recharge)
            4200000L,      // ₹100 (4,000,000 + 200,000)
            8800000L,      // ₹200 (8,000,000 + 800,000)
            25300000L,     // ₹500 (22,000,000 + 3,300,000)
            54000000L,     // ₹1,000 (45,000,000 + 9,000,000)
            325000000L     // ₹5,000 (250,000,000 + 75,000,000)
    };

    private final int[] prices = {50, 100, 200, 500, 1000, 5000};

    private final String[] packageNames = {
            "₹50 Offer (1,800,000 Coins)",
            "₹100 Offer (4,200,000 Coins)",
            "₹200 Offer (8,800,000 Coins)",
            "₹500 Offer (25,300,000 Coins)",
            "₹1,000 Offer (54,000,000 Coins)",
            "₹5,000 Offer (325,000,000 Coins)"
    };

    private final String[] extraBonusText = {
            "Total Coins",
            "+ 200,000 Extra Coins",
            "+ 800,000 Extra Coins",
            "+ 3,300,000 Extra",
            "+ 9,000,000 Extra",
            "+ 75,000,000 Extra"
    };

    private final String[] bonusBadges = {
            null,
            "+5% Extra Coins",
            "+10% Extra Coins",
            "+15% Extra Coins",
            "+20% Extra Coins",
            "+30% Extra Coins"
    };

    private final String[] packageKeys = {
            "pack_0",
            "pack_1",
            "pack_2",
            "pack_3",
            "pack_4",
            "pack_5"
    };

    private int selectedIndex = -1;
    private String currentUid;
    private long lastCoinsVal = -1;
    private String currentUserName = "User";
    private String currentUserProfileId = "N/A";

    private final Set<String> claimedOfferKeys = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_coin_recharge);

        currentUid = FirebaseAuth.getInstance().getUid();

        btnBack = findViewById(R.id.btnBack);
        tvCoinBalance = findViewById(R.id.tvCoinBalance);
        tvSelectedCoins = findViewById(R.id.tvSelectedCoins);
        tvSelectedPrice = findViewById(R.id.tvSelectedPrice);
        btnPayNow = findViewById(R.id.btnPayNow);
        cardBonusBanner = findViewById(R.id.cardBonusBanner);
        tvBannerTitle = findViewById(R.id.tvBannerTitle);
        tvBannerSubtitle = findViewById(R.id.tvBannerSubtitle);
        llAllOffersClaimed = findViewById(R.id.llAllOffersClaimed);

        packCards = new FrameLayout[]{
                findViewById(R.id.cardPack1),
                findViewById(R.id.cardPack2),
                findViewById(R.id.cardPack3),
                findViewById(R.id.cardPack4),
                findViewById(R.id.cardPack5),
                findViewById(R.id.cardPack6)
        };

        tvBadges = new TextView[]{
                findViewById(R.id.tvBadgePack1),
                findViewById(R.id.tvBadgePack2),
                findViewById(R.id.tvBadgePack3),
                findViewById(R.id.tvBadgePack4),
                findViewById(R.id.tvBadgePack5),
                findViewById(R.id.tvBadgePack6)
        };

        tvCoins = new TextView[]{
                findViewById(R.id.tvCoinsPack1),
                findViewById(R.id.tvCoinsPack2),
                findViewById(R.id.tvCoinsPack3),
                findViewById(R.id.tvCoinsPack4),
                findViewById(R.id.tvCoinsPack5),
                findViewById(R.id.tvCoinsPack6)
        };

        tvExtras = new TextView[]{
                findViewById(R.id.tvExtraPack1),
                findViewById(R.id.tvExtraPack2),
                findViewById(R.id.tvExtraPack3),
                findViewById(R.id.tvExtraPack4),
                findViewById(R.id.tvExtraPack5),
                findViewById(R.id.tvExtraPack6)
        };

        setupClickListeners();
        loadUserData();
        loadClaimedOffers();
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

    private boolean isPackClaimed(int index) {
        if (index < 0 || index >= packageKeys.length) return false;
        String key1 = packageKeys[index];
        String key2 = sanitizeKey(packageNames[index]);
        return claimedOfferKeys.contains(key1) || claimedOfferKeys.contains(key2);
    }

    private String sanitizeKey(String name) {
        if (name == null) return "";
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private void selectPack(int index) {
        if (index < 0 || index >= packCards.length) return;

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

        boolean isClaimed = isPackClaimed(selectedIndex);
        long coins = isClaimed ? baseCoinAmounts[selectedIndex] : offerCoinAmounts[selectedIndex];
        int price = prices[selectedIndex];
        String formattedCoins = NumberFormat.getInstance().format(coins);
        String formattedPrice = NumberFormat.getInstance().format(price);

        if (tvSelectedCoins != null) {
            tvSelectedCoins.setText(formattedCoins + " Coins");
        }
        if (tvSelectedPrice != null) {
            tvSelectedPrice.setText("Total Price: ₹" + formattedPrice);
        }
    }

    private void updateOffersUI() {
        int unclaimedCount = 0;

        for (int i = 0; i < packCards.length; i++) {
            boolean isClaimed = isPackClaimed(i);
            if (packCards[i] != null) {
                packCards[i].setVisibility(View.VISIBLE);

                if (isClaimed) {
                    if (tvCoins[i] != null) {
                        tvCoins[i].setText(NumberFormat.getInstance().format(baseCoinAmounts[i]));
                    }
                    if (tvBadges[i] != null) {
                        tvBadges[i].setText("Offer Used");
                        tvBadges[i].setBackgroundResource(R.drawable.bg_offer_claimed_badge);
                        tvBadges[i].setTextColor(Color.parseColor("#9CA3AF"));
                        tvBadges[i].setVisibility(View.VISIBLE);
                    }
                    if (tvExtras[i] != null) {
                        tvExtras[i].setText("Standard Pack");
                        tvExtras[i].setTextColor(Color.parseColor("#9CA3AF"));
                    }
                } else {
                    unclaimedCount++;
                    if (tvCoins[i] != null) {
                        tvCoins[i].setText(NumberFormat.getInstance().format(offerCoinAmounts[i]));
                    }
                    if (tvBadges[i] != null) {
                        if (bonusBadges[i] != null) {
                            tvBadges[i].setText(bonusBadges[i]);
                            tvBadges[i].setBackgroundResource(R.drawable.bg_bonus_badge);
                            tvBadges[i].setTextColor(Color.parseColor("#FFFFFF"));
                            tvBadges[i].setVisibility(View.VISIBLE);
                        } else {
                            tvBadges[i].setVisibility(View.GONE);
                        }
                    }
                    if (tvExtras[i] != null) {
                        if (extraBonusText[i] != null) {
                            tvExtras[i].setText(extraBonusText[i]);
                            tvExtras[i].setTextColor(Color.parseColor("#40E0D0"));
                        } else {
                            tvExtras[i].setText("Total Coins");
                            tvExtras[i].setTextColor(Color.parseColor("#9CA3AF"));
                        }
                    }
                }
            }
        }

        if (cardBonusBanner != null) {
            cardBonusBanner.setVisibility(View.VISIBLE);
            if (unclaimedCount > 0) {
                if (tvBannerTitle != null) tvBannerTitle.setText("🔥 First Recharge Special Offer!");
                if (tvBannerSubtitle != null) tvBannerSubtitle.setText("Get up to +30% EXTRA Bonus Coins on available packages today!");
            } else {
                if (tvBannerTitle != null) tvBannerTitle.setText("⚡ Standard Top-Up Store Active");
                if (tvBannerSubtitle != null) tvBannerSubtitle.setText("Select any coin package below to top-up your wallet anytime!");
            }
        }

        if (llAllOffersClaimed != null) {
            llAllOffersClaimed.setVisibility(View.GONE);
        }

        if (btnPayNow != null) {
            btnPayNow.setEnabled(true);
            btnPayNow.setAlpha(1.0f);
        }

        if (selectedIndex < 0 || selectedIndex >= packCards.length) {
            selectPack(0);
        } else {
            selectPack(selectedIndex);
        }
    }

    private void loadClaimedOffers() {
        if (currentUid == null) {
            updateOffersUI();
            return;
        }

        DatabaseReference claimedRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUid).child("claimed_offers");

        claimedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                claimedOfferKeys.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object val = child.getValue();
                        if (val != null) {
                            if (val instanceof Boolean && (Boolean) val) {
                                claimedOfferKeys.add(child.getKey());
                            } else if ("true".equalsIgnoreCase(String.valueOf(val))) {
                                claimedOfferKeys.add(child.getKey());
                            }
                        }
                    }
                }
                checkPreviousRechargeRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateOffersUI();
            }
        });
    }

    private void checkPreviousRechargeRequests() {
        if (currentUid == null) {
            updateOffersUI();
            return;
        }

        DatabaseReference reqsRef = FirebaseDatabase.getInstance().getReference("recharge_requests");
        reqsRef.orderByChild("uid").equalTo(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String packName = ds.child("packageName").getValue(String.class);
                        String packKey = ds.child("packageKey").getValue(String.class);
                        String status = ds.child("status").getValue(String.class);

                        if (status == null || "PENDING".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                            if (packKey != null && !packKey.isEmpty()) {
                                claimedOfferKeys.add(packKey);
                            }
                            if (packName != null && !packName.isEmpty()) {
                                claimedOfferKeys.add(sanitizeKey(packName));
                                for (int i = 0; i < packageNames.length; i++) {
                                    if (packageNames[i].equalsIgnoreCase(packName)) {
                                        claimedOfferKeys.add(packageKeys[i]);
                                    }
                                }
                            }
                        }
                    }
                }
                updateOffersUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateOffersUI();
            }
        });
    }

    private void handlePayment() {
        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(this, "Please log in to top-up wallet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= packCards.length) {
            Toast.makeText(this, "Please select a coin package", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isClaimed = isPackClaimed(selectedIndex);
        long coins = isClaimed ? baseCoinAmounts[selectedIndex] : offerCoinAmounts[selectedIndex];
        int price = prices[selectedIndex];
        String packName = isClaimed ?
                ("₹" + price + " Standard (" + NumberFormat.getInstance().format(coins) + " Coins)") :
                packageNames[selectedIndex];

        showPaymentQrDialog(coins, price, packName);
    }

    private void showPaymentQrDialog(long coins, int price, String packName) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_qr);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvDialogCoins = dialog.findViewById(R.id.tvDialogCoins);
        TextView tvDialogPrice = dialog.findViewById(R.id.tvDialogPrice);
        TextView btnCopyUpi = dialog.findViewById(R.id.btnCopyUpi);
        ImageView btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        EditText etTransactionId = dialog.findViewById(R.id.etTransactionId);
        View btnSubmitPayment = dialog.findViewById(R.id.btnSubmitPayment);

        String formattedCoins = NumberFormat.getInstance().format(coins);
        String formattedPrice = NumberFormat.getInstance().format(price);

        if (tvDialogCoins != null) tvDialogCoins.setText(formattedCoins + " Coins");
        if (tvDialogPrice != null) tvDialogPrice.setText("Pay Amount: ₹" + formattedPrice);

        String upiString = "roomchat@upi";

        if (btnCopyUpi != null) {
            btnCopyUpi.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UPI ID", upiString);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(CoinRechargeActivity.this, "📋 UPI ID Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSubmitPayment != null) {
            btnSubmitPayment.setOnClickListener(v -> {
                String utr = etTransactionId != null ? etTransactionId.getText().toString().trim() : "";
                if (utr.isEmpty() || utr.length() < 6) {
                    Toast.makeText(CoinRechargeActivity.this, "Please enter a valid 12-digit Transaction ID / UTR!", Toast.LENGTH_SHORT).show();
                    return;
                }

                submitRechargeRequest(coins, price, packName, utr);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void submitRechargeRequest(long coins, int price, String packName, String utr) {
        if (currentUid == null) return;

        if (selectedIndex < 0 || selectedIndex >= packageKeys.length) {
            Toast.makeText(this, "Invalid package selection!", Toast.LENGTH_SHORT).show();
            return;
        }

        String packKey = packageKeys[selectedIndex];
        String sanitizedName = sanitizeKey(packName);

        boolean isFirstClaim = !isPackClaimed(selectedIndex);

        if (isFirstClaim) {
            DatabaseReference userClaimedRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(currentUid).child("claimed_offers");

            Map<String, Object> offerClaimMap = new HashMap<>();
            offerClaimMap.put(packKey, true);
            offerClaimMap.put(sanitizedName, true);
            userClaimedRef.updateChildren(offerClaimMap);

            claimedOfferKeys.add(packKey);
            claimedOfferKeys.add(sanitizedName);
            updateOffersUI();
        }

        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("recharge_requests").push();
        String reqId = reqRef.getKey();

        Map<String, Object> map = new HashMap<>();
        map.put("requestId", reqId);
        map.put("uid", currentUid);
        map.put("userName", currentUserName);
        map.put("userProfileId", currentUserProfileId);
        map.put("coinAmount", coins);
        map.put("priceAmount", "₹" + price);
        map.put("packageName", packName);
        map.put("packageKey", packKey);
        map.put("utrNumber", utr);
        map.put("status", "PENDING");
        map.put("timestamp", System.currentTimeMillis());

        reqRef.setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUid).push();
                Map<String, Object> notifMap = new HashMap<>();
                notifMap.put("title", "Recharge Pending ⏳");
                notifMap.put("message", "Your request for " + coins + " Coins (₹" + price + ") with UTR " + utr + " is pending admin verification.");
                notifMap.put("type", "RECHARGE_STATUS");
                notifMap.put("timestamp", System.currentTimeMillis());

                notifRef.setValue(notifMap);

                Toast.makeText(CoinRechargeActivity.this, "🎉 Payment Request Submitted! Pending admin verification.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CoinRechargeActivity.this, "❌ Error submitting request. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        if (currentUid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String pid = snapshot.child("profileId").getValue(String.class);
                    if (name != null) currentUserName = name;
                    if (pid != null) currentUserProfileId = pid;

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

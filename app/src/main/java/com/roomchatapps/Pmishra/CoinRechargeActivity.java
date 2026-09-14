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

import java.util.HashMap;
import java.util.Map;

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
    private String currentUserName = "User";
    private String currentUserProfileId = "N/A";

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
        loadUserData();
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
        int price = prices[selectedIndex];
        String packName = packageNames[selectedIndex];

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
        TextView tvUpiId = dialog.findViewById(R.id.tvUpiId);
        TextView btnCopyUpi = dialog.findViewById(R.id.btnCopyUpi);
        ImageView btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        EditText etTransactionId = dialog.findViewById(R.id.etTransactionId);
        View btnSubmitPayment = dialog.findViewById(R.id.btnSubmitPayment);

        if (tvDialogCoins != null) tvDialogCoins.setText(coins + " Coins");
        if (tvDialogPrice != null) tvDialogPrice.setText("Pay Amount: ₹" + price);

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
        map.put("utrNumber", utr);
        map.put("status", "PENDING");
        map.put("timestamp", System.currentTimeMillis());

        reqRef.setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Add notification to user
                DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUid).push();
                Map<String, Object> notifMap = new HashMap<>();
                notifMap.put("title", "Recharge Pending ⏳");
                notifMap.put("message", "Your request for " + coins + " Coins (₹" + price + ") with UTR " + utr + " is pending admin verification.");
                notifMap.put("type", "RECHARGE_STATUS");
                notifMap.put("timestamp", System.currentTimeMillis());

                notifRef.setValue(notifMap);

                Toast.makeText(CoinRechargeActivity.this, "🎉 Payment Request Submitted! Admin will verify shortly.", Toast.LENGTH_LONG).show();
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

package com.roomchatapps.Pmishra;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.CoinUtils;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.text.NumberFormat;

public class EnergyConvertActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEnergyBalance, tvCoinsBalance, tvCoinsToReceive;
    private TextView chip100, chip500, chip1000, chipAll;
    private EditText etEnergyInput;
    private MaterialButton btnConvert;

    private String currentUid;
    private long currentEnergyVal = 0;
    private long currentCoinsVal = 0;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_energy_convert);

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

        initViews();
        setupClickListeners();
        setupInputWatcher();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEnergyBalance = findViewById(R.id.tvEnergyBalance);
        tvCoinsBalance = findViewById(R.id.tvCoinsBalance);
        tvCoinsToReceive = findViewById(R.id.tvCoinsToReceive);

        chip100 = findViewById(R.id.chip100);
        chip500 = findViewById(R.id.chip500);
        chip1000 = findViewById(R.id.chip1000);
        chipAll = findViewById(R.id.chipAll);

        etEnergyInput = findViewById(R.id.etEnergyInput);
        btnConvert = findViewById(R.id.btnConvert);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (chip100 != null) chip100.setOnClickListener(v -> selectPreset(100, chip100));
        if (chip500 != null) chip500.setOnClickListener(v -> selectPreset(500, chip500));
        if (chip1000 != null) chip1000.setOnClickListener(v -> selectPreset(1000, chip1000));
        if (chipAll != null) chipAll.setOnClickListener(v -> selectPreset(currentEnergyVal, chipAll));

        if (btnConvert != null) {
            btnConvert.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(btnConvert);
                performConversion();
            });
        }
    }

    private void selectPreset(long amount, TextView selectedChip) {
        if (amount <= 0) {
            Toast.makeText(this, "No Energy available to convert!", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView[] chips = {chip100, chip500, chip1000, chipAll};
        for (TextView chip : chips) {
            if (chip != null) {
                chip.setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                chip.setTextColor(Color.parseColor("#D0FFFFFF"));
            }
        }

        if (selectedChip != null) {
            selectedChip.setBackgroundResource(R.drawable.bg_wallet_chip_selected);
            selectedChip.setTextColor(Color.WHITE);
        }

        if (etEnergyInput != null) {
            etEnergyInput.setText(String.valueOf(amount));
            etEnergyInput.setSelection(etEnergyInput.getText().length());
        }
    }

    private void setupInputWatcher() {
        if (etEnergyInput == null) return;

        etEnergyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLiveSummary();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateLiveSummary() {
        if (etEnergyInput == null || tvCoinsToReceive == null) return;

        String inputStr = etEnergyInput.getText().toString().trim();
        long amount = 0;
        if (!inputStr.isEmpty()) {
            try {
                amount = Long.parseLong(inputStr);
            } catch (Exception ignored) {}
        }

        if (amount <= 0) {
            tvCoinsToReceive.setText("0 Coins");
        } else {
            tvCoinsToReceive.setText(NumberFormat.getInstance().format(amount) + " Coins");
        }
    }

    private void loadUserData() {
        if (currentUid == null || currentUid.isEmpty()) return;

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                if (snapshot.exists()) {
                    Object energyObj = snapshot.child("energy").getValue();
                    currentEnergyVal = 0;
                    if (energyObj != null) {
                        try {
                            currentEnergyVal = Long.parseLong(String.valueOf(energyObj));
                        } catch (Exception e) {
                            try {
                                currentEnergyVal = (long) Double.parseDouble(String.valueOf(energyObj));
                            } catch (Exception ignored) {}
                        }
                    }

                    Object coinsObj = snapshot.child("coins").getValue();
                    currentCoinsVal = 0;
                    if (coinsObj != null) {
                        try {
                            currentCoinsVal = Long.parseLong(String.valueOf(coinsObj));
                        } catch (Exception e) {
                            try {
                                currentCoinsVal = (long) Double.parseDouble(String.valueOf(coinsObj));
                            } catch (Exception ignored) {}
                        }
                    }

                    if (tvEnergyBalance != null) {
                        tvEnergyBalance.setText(CoinUtils.formatCoins(currentEnergyVal));
                    }
                    if (tvCoinsBalance != null) {
                        tvCoinsBalance.setText(CoinUtils.formatCoins(currentCoinsVal));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userRef.addValueEventListener(userListener);
    }

    private void performConversion() {
        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(this, "User session invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        String inputStr = etEnergyInput != null ? etEnergyInput.getText().toString().trim() : "";
        if (inputStr.isEmpty()) {
            Toast.makeText(this, "Please enter energy amount to convert!", Toast.LENGTH_SHORT).show();
            return;
        }

        long amountToConvert;
        try {
            amountToConvert = Long.parseLong(inputStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid energy amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountToConvert <= 0) {
            Toast.makeText(this, "Amount must be greater than 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountToConvert > currentEnergyVal) {
            Toast.makeText(this, "Insufficient Energy! You have " + CoinUtils.formatCoins(currentEnergyVal) + " Energy.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnConvert != null) btnConvert.setEnabled(false);

        WalletManager.convertEnergyToCoins(currentUid, amountToConvert, new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                if (isFinishing() || isDestroyed()) return;
                if (btnConvert != null) btnConvert.setEnabled(true);

                if (etEnergyInput != null) etEnergyInput.setText("");
                Toast.makeText(EnergyConvertActivity.this, "🎉 Converted " + amountToConvert + " Energy to " + amountToConvert + " Coins!", Toast.LENGTH_LONG).show();

                if (tvCoinsBalance != null) {
                    AnimationHelper.bounceAnimation(tvCoinsBalance);
                }
                if (tvEnergyBalance != null) {
                    AnimationHelper.bounceAnimation(tvEnergyBalance);
                }
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                if (btnConvert != null) btnConvert.setEnabled(true);
                Toast.makeText(EnergyConvertActivity.this, "❌ " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }
}

package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.AgencyWithdrawalAdapter;
import com.roomchatapps.Pmishra.databinding.ActivityAgencyWalletBinding;
import com.roomchatapps.Pmishra.models.AgencyWalletModel;
import com.roomchatapps.Pmishra.models.AgencyWithdrawalModel;
import com.roomchatapps.Pmishra.utils.LoadingDialog;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AgencyWalletActivity extends AppCompatActivity {

    private ActivityAgencyWalletBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference walletRef;
    private DatabaseReference withdrawalsRef;
    private DatabaseReference agencyRef;

    private String currentUid;
    private String currentAgencyName = "Official Agency";
    private String currentOwnerName = "Owner";

    private double availableBalanceUsd = 0.0;
    private double totalEarningsUsd = 0.0;
    private double withdrawnBalanceUsd = 0.0;
    private double pendingWithdrawalUsd = 0.0;

    private AgencyWithdrawalAdapter historyAdapter;
    private final List<AgencyWithdrawalModel> historyList = new ArrayList<>();
    private LoadingDialog loadingDialog;

    private static final double MINIMUM_WITHDRAWAL_USD = 10.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);

        binding = ActivityAgencyWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.headerLayout.setPadding(
                    binding.headerLayout.getPaddingLeft(),
                    systemBars.top + 8,
                    binding.headerLayout.getPaddingRight(),
                    binding.headerLayout.getPaddingBottom()
            );
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getUid();

        if (currentUid == null) {
            Toast.makeText(this, "Please log in to access Agency Wallet", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingDialog = new LoadingDialog(this);

        walletRef = FirebaseDatabase.getInstance().getReference("agencies").child(currentUid).child("wallet");
        withdrawalsRef = FirebaseDatabase.getInstance().getReference("agencies").child(currentUid).child("withdrawals");
        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(currentUid);

        setupRecyclerView();
        setupClickListeners();
        loadAgencyOwnerDetails();
        loadWalletData();
        loadWithdrawalHistory();
    }

    private void setupRecyclerView() {
        binding.rvWithdrawalHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new AgencyWithdrawalAdapter(historyList);
        binding.rvWithdrawalHistory.setAdapter(historyAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnRefreshWallet.setOnClickListener(v -> {
            Toast.makeText(AgencyWalletActivity.this, "Refreshing Agency Wallet...", Toast.LENGTH_SHORT).show();
            loadWalletData();
        });

        binding.btnWithdrawFunds.setOnClickListener(v -> showWithdrawDialog());
    }

    private void loadAgencyOwnerDetails() {
        agencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("agencyName").getValue(String.class);
                    String owner = snapshot.child("ownerName").getValue(String.class);

                    if (name != null) currentAgencyName = name;
                    if (owner != null) currentOwnerName = owner;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadWalletData() {
        walletRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AgencyWalletModel wallet = snapshot.getValue(AgencyWalletModel.class);
                    if (wallet != null) {
                        availableBalanceUsd = wallet.getAvailableBalanceUsd();
                        totalEarningsUsd = wallet.getTotalEarningsUsd();
                        withdrawnBalanceUsd = wallet.getWithdrawnBalanceUsd();
                        pendingWithdrawalUsd = wallet.getPendingWithdrawalUsd();

                        updateWalletUi();
                        return;
                    }
                }

                // If no custom wallet node exists, initialize from hosts total coins calculation
                initializeDefaultWalletFromHosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                initializeDefaultWalletFromHosts();
            }
        });
    }

    private void initializeDefaultWalletFromHosts() {
        agencyRef.child("hosts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long aggregateCoins = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Long coins = child.child("monthlyCoins").getValue(Long.class);
                        if (coins == null || coins == 0) {
                            coins = child.child("totalCoins").getValue(Long.class);
                        }
                        if (coins != null) aggregateCoins += coins;
                    }
                }

                // e.g. 10,000 coins = $1.00 USD. Agency Commission Share = 10%
                double totalUsd = (aggregateCoins / 10000.0) * 0.10;

                availableBalanceUsd = totalUsd;
                totalEarningsUsd = totalUsd;
                withdrawnBalanceUsd = 0.0;
                pendingWithdrawalUsd = 0.0;

                updateWalletUi();

                // Save initialized wallet node to Firebase
                AgencyWalletModel newWallet = new AgencyWalletModel(
                        "AGY_" + currentUid.substring(0, 6).toUpperCase(),
                        currentUid,
                        totalEarningsUsd,
                        availableBalanceUsd,
                        withdrawnBalanceUsd,
                        pendingWithdrawalUsd,
                        aggregateCoins,
                        System.currentTimeMillis()
                );
                walletRef.setValue(newWallet);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateWalletUi();
            }
        });
    }

    private void updateWalletUi() {
        binding.tvAvailableBalance.setText(String.format(Locale.getDefault(), "₹%.2f INR", availableBalanceUsd));
        binding.tvTotalEarningsUsd.setText(String.format(Locale.getDefault(), "₹%.2f", totalEarningsUsd));
        binding.tvWithdrawnBalanceUsd.setText(String.format(Locale.getDefault(), "₹%.2f", withdrawnBalanceUsd));
        binding.tvPendingWithdrawalUsd.setText(String.format(Locale.getDefault(), "₹%.2f", pendingWithdrawalUsd));
    }

    private void loadWithdrawalHistory() {
        withdrawalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        AgencyWithdrawalModel item = child.getValue(AgencyWithdrawalModel.class);
                        if (item != null) {
                            historyList.add(item);
                        }
                    }
                }

                // Sort history by timestamp descending
                historyList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                binding.tvWithdrawalCountLabel.setText(historyList.size() + " Requests");

                if (historyList.isEmpty()) {
                    binding.layoutEmptyHistory.setVisibility(View.VISIBLE);
                    binding.rvWithdrawalHistory.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyHistory.setVisibility(View.GONE);
                    binding.rvWithdrawalHistory.setVisibility(View.VISIBLE);
                }

                if (historyAdapter != null) {
                    historyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showWithdrawDialog() {
        if (availableBalanceUsd < MINIMUM_WITHDRAWAL_USD) {
            Toast.makeText(this, String.format(Locale.getDefault(), "Insufficient balance! Minimum withdrawal is ₹%.2f INR", MINIMUM_WITHDRAWAL_USD), Toast.LENGTH_LONG).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agency_withdraw, null, false);
        dialog.setContentView(dialogView);

        TextView tvNotice = dialogView.findViewById(R.id.tvAvailableBalanceNotice);
        EditText etWithdrawAmount = dialogView.findViewById(R.id.etWithdrawAmount);
        Spinner spinnerPaymentMethod = dialogView.findViewById(R.id.spinnerPaymentMethod);
        EditText etAccountDetails = dialogView.findViewById(R.id.etAccountDetails);
        MaterialButton btnSubmitWithdrawConfirm = dialogView.findViewById(R.id.btnSubmitWithdrawConfirm);
        View btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        if (tvNotice != null) {
            tvNotice.setText(String.format(Locale.getDefault(), "Available Balance: ₹%.2f | Min Withdrawal: ₹%.2f", availableBalanceUsd, MINIMUM_WITHDRAWAL_USD));
        }

        if (spinnerPaymentMethod != null) {
            String[] methods = new String[]{"UPI Transfer (India)", "Bank Account Transfer", "PayPal", "Crypto USDT (TRC20)"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
            spinnerPaymentMethod.setAdapter(adapter);
        }

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmitWithdrawConfirm != null) {
            btnSubmitWithdrawConfirm.setOnClickListener(v -> {
                if (etWithdrawAmount == null || etAccountDetails == null || spinnerPaymentMethod == null) return;

                String amountStr = etWithdrawAmount.getText().toString().trim();
                String details = etAccountDetails.getText().toString().trim();
                String method = spinnerPaymentMethod.getSelectedItem().toString();

                if (amountStr.isEmpty()) {
                    etWithdrawAmount.setError("Enter withdrawal amount");
                    etWithdrawAmount.requestFocus();
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (Exception e) {
                    etWithdrawAmount.setError("Invalid amount format");
                    etWithdrawAmount.requestFocus();
                    return;
                }

                if (amount < MINIMUM_WITHDRAWAL_USD) {
                    etWithdrawAmount.setError(String.format(Locale.getDefault(), "Minimum withdrawal is ₹%.2f", MINIMUM_WITHDRAWAL_USD));
                    etWithdrawAmount.requestFocus();
                    return;
                }

                if (amount > availableBalanceUsd) {
                    etWithdrawAmount.setError(String.format(Locale.getDefault(), "Amount exceeds available balance (₹%.2f)", availableBalanceUsd));
                    etWithdrawAmount.requestFocus();
                    return;
                }

                if (details.isEmpty()) {
                    etAccountDetails.setError("Enter account or UPI details");
                    etAccountDetails.requestFocus();
                    return;
                }

                dialog.dismiss();
                processWithdrawalSubmission(amount, method, details);
            });
        }

        dialog.show();
    }

    private void processWithdrawalSubmission(double amount, String method, String accountDetails) {
        loadingDialog.show("Submitting withdrawal request...");

        String withdrawalId = "WTH_" + System.currentTimeMillis();
        long now = System.currentTimeMillis();

        AgencyWithdrawalModel withdrawalModel = new AgencyWithdrawalModel(
                withdrawalId,
                "AGY_" + currentUid.substring(0, 6).toUpperCase(),
                currentUid,
                currentAgencyName,
                currentOwnerName,
                amount,
                method,
                accountDetails,
                "PENDING",
                "Awaiting Admin Processing",
                now
        );

        // 1. Save withdrawal request in user agency node
        withdrawalsRef.child(withdrawalId).setValue(withdrawalModel);

        // 2. Save withdrawal request in global admin agency_withdrawals node for HTML Admin Panel!
        FirebaseDatabase.getInstance().getReference("agency_withdrawals").child(withdrawalId).setValue(withdrawalModel);

        // 3. Update wallet balances
        availableBalanceUsd -= amount;
        pendingWithdrawalUsd += amount;

        AgencyWalletModel updatedWallet = new AgencyWalletModel(
                "AGY_" + currentUid.substring(0, 6).toUpperCase(),
                currentUid,
                totalEarningsUsd,
                availableBalanceUsd,
                withdrawnBalanceUsd,
                pendingWithdrawalUsd,
                0,
                now
        );

        walletRef.setValue(updatedWallet).addOnCompleteListener(task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                updateWalletUi();
                Toast.makeText(AgencyWalletActivity.this, "🎉 Withdrawal Request Submitted! Admin will process your payout.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AgencyWalletActivity.this, "Failed to submit request", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

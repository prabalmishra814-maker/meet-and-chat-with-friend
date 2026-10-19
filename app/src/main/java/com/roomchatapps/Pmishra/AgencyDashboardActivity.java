package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.AgencyHostAdapter;
import com.roomchatapps.Pmishra.databinding.ActivityAgencyDashboardBinding;
import com.roomchatapps.Pmishra.models.AgencyHostModel;
import com.roomchatapps.Pmishra.models.AgencyModel;
import com.roomchatapps.Pmishra.utils.EconomyConfig;
import com.roomchatapps.Pmishra.utils.LoadingDialog;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgencyDashboardActivity extends AppCompatActivity {

    private ActivityAgencyDashboardBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference agencyRef;
    private DatabaseReference hostsRef;

    private String currentUid;
    private String currentAgencyId = "";
    private String currentAgencyName = "";

    private AgencyHostAdapter hostAdapter;
    private final List<AgencyHostModel> hostList = new ArrayList<>();
    private LoadingDialog loadingDialog;

    private double currentCommissionRate = EconomyConfig.DEFAULT_AGENCY_COMMISSION_PERCENTAGE;
    private long currentAggregateCoins = 0;
    private int currentTotalHosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);

        binding = ActivityAgencyDashboardBinding.inflate(getLayoutInflater());
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
            Toast.makeText(this, "Please log in to access Agency Panel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingDialog = new LoadingDialog(this);

        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(currentUid);
        hostsRef = agencyRef.child("hosts");

        setupRecyclerView();
        setupClickListeners();
        loadCommissionConfig();
        loadAgencyInfo();
        loadAgencyHosts();
    }

    private void loadCommissionConfig() {
        FirebaseDatabase.getInstance().getReference("app_config").child("agencyCommissionPercentage")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Double rate = snapshot.getValue(Double.class);
                            if (rate != null) currentCommissionRate = rate;
                        }
                        updateDashboardStats(currentTotalHosts, currentAggregateCoins);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupRecyclerView() {
        binding.rvHosts.setLayoutManager(new LinearLayoutManager(this));
        hostAdapter = new AgencyHostAdapter(hostList, new AgencyHostAdapter.OnHostActionListener() {
            @Override
            public void onHostClick(AgencyHostModel host) {
                if (host.getHostUid() != null) {
                    Intent intent = new Intent(AgencyDashboardActivity.this, UserDetailActivity.class);
                    intent.putExtra("userId", host.getHostUid());
                    startActivity(intent);
                }
            }

            @Override
            public void onHostOptionsClick(AgencyHostModel host, View anchorView) {
                showHostPopupMenu(host, anchorView);
            }
        });
        binding.rvHosts.setAdapter(hostAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        View.OnClickListener addHostListener = v -> showAddHostDialog();
        binding.btnAddHostHeader.setOnClickListener(addHostListener);
        binding.tvAddHostTextLink.setOnClickListener(addHostListener);

        binding.btnOpenAgencyWalletCard.setOnClickListener(v -> {
            Intent intent = new Intent(AgencyDashboardActivity.this, AgencyWalletActivity.class);
            startActivity(intent);
        });
    }

    private void loadAgencyInfo() {
        agencyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    AgencyModel agency = null;
                    try {
                        agency = snapshot.getValue(AgencyModel.class);
                    } catch (Exception ignored) {}

                    if (agency == null) {
                        agency = new AgencyModel();
                    }

                    String name = snapshot.child("agencyName").getValue(String.class);
                    if (name == null || name.isEmpty()) name = agency.getAgencyName();
                    if (name == null || name.isEmpty()) name = "Official Agency";
                    currentAgencyName = name;

                    String id = snapshot.child("agencyId").getValue(String.class);
                    if (id == null || id.isEmpty()) id = agency.getAgencyId();
                    if (id == null || id.isEmpty()) id = "AGY_" + (currentUid.length() >= 6 ? currentUid.substring(0, 6).toUpperCase() : currentUid.toUpperCase());
                    currentAgencyId = id;

                    String owner = snapshot.child("ownerName").getValue(String.class);
                    if (owner == null || owner.isEmpty()) owner = agency.getOwnerName();
                    if (owner == null || owner.isEmpty()) owner = "Owner";

                    String logo = snapshot.child("agencyLogo").getValue(String.class);
                    if (logo == null || logo.isEmpty()) logo = agency.getAgencyLogo();

                    binding.tvAgencyName.setText(currentAgencyName);
                    binding.tvAgencyId.setText("Agency ID: " + currentAgencyId);
                    binding.tvOwnerDetails.setText("Owner: " + owner);

                    if (logo != null && !logo.isEmpty()) {
                        Glide.with(AgencyDashboardActivity.this)
                                .load(logo)
                                .placeholder(R.drawable.app_create_room_ic)
                                .error(R.drawable.app_create_room_ic)
                                .into(binding.ivAgencyLogo);
                    }
                } else {
                    // Fallback to agency_applications node if direct agency node doesn't exist
                    loadAgencyFromApplications();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAgencyFromApplications() {
        FirebaseDatabase.getInstance().getReference("agency_applications").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isFinishing() || isDestroyed()) return;
                        if (snapshot.exists()) {
                            AgencyModel app = null;
                            try {
                                app = snapshot.getValue(AgencyModel.class);
                            } catch (Exception ignored) {}

                            if (app == null) {
                                app = new AgencyModel();
                            }

                            String name = snapshot.child("agencyName").getValue(String.class);
                            if (name == null || name.isEmpty()) name = app.getAgencyName();
                            if (name == null || name.isEmpty()) name = "Official Agency";
                            currentAgencyName = name;

                            String id = snapshot.child("agencyId").getValue(String.class);
                            if (id == null || id.isEmpty()) id = app.getAgencyId();
                            if (id == null || id.isEmpty()) id = "AGY_" + (currentUid.length() >= 6 ? currentUid.substring(0, 6).toUpperCase() : currentUid.toUpperCase());
                            currentAgencyId = id;

                            String owner = snapshot.child("ownerName").getValue(String.class);
                            if (owner == null || owner.isEmpty()) owner = app.getOwnerName();
                            if (owner == null || owner.isEmpty()) owner = "Owner";

                            String logo = snapshot.child("agencyLogo").getValue(String.class);
                            if (logo == null || logo.isEmpty()) logo = app.getAgencyLogo();

                            binding.tvAgencyName.setText(currentAgencyName);
                            binding.tvAgencyId.setText("Agency ID: " + currentAgencyId);
                            binding.tvOwnerDetails.setText("Owner: " + owner);

                            if (logo != null && !logo.isEmpty()) {
                                Glide.with(AgencyDashboardActivity.this)
                                        .load(logo)
                                        .placeholder(R.drawable.app_create_room_ic)
                                        .error(R.drawable.app_create_room_ic)
                                        .into(binding.ivAgencyLogo);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadAgencyHosts() {
        hostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hostList.clear();
                long aggregateCoins = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        AgencyHostModel host = child.getValue(AgencyHostModel.class);
                        if (host != null) {
                            hostList.add(host);
                            aggregateCoins += host.getMonthlyCoins() > 0 ? host.getMonthlyCoins() : host.getTotalCoins();
                        }
                    }
                }

                updateDashboardStats(hostList.size(), aggregateCoins);

                if (hostList.isEmpty()) {
                    binding.layoutEmptyHosts.setVisibility(View.VISIBLE);
                    binding.rvHosts.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyHosts.setVisibility(View.GONE);
                    binding.rvHosts.setVisibility(View.VISIBLE);
                }

                if (hostAdapter != null) {
                    hostAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateDashboardStats(int totalHosts, long aggregateCoins) {
        currentTotalHosts = totalHosts;
        currentAggregateCoins = aggregateCoins;

        binding.tvTotalHostsCount.setText(String.valueOf(totalHosts));

        // Commission calculation: e.g. Host Income = 100,000 Coins @ 10% = 10,000 Agency Commission Coins
        long agencyCommissionCoins = EconomyConfig.calculateAgencyCommissionCoins(aggregateCoins, currentCommissionRate);
        binding.tvTotalCoinsCount.setText(String.format(Locale.getDefault(), "%,d", agencyCommissionCoins));

        // Income calculation: e.g. 100 Coins = ₹1 INR
        double estimatedInr = agencyCommissionCoins / 100.0;
        binding.tvTotalIncomeAmount.setText(String.format(Locale.getDefault(), "₹%.2f", estimatedInr));

        binding.tvMonthlyCommission.setText(String.format(Locale.getDefault(), "%.1f%% Rate", currentCommissionRate));

        // Monthly Report Calculations
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.tvReportMonthLabel.setText(monthFormat.format(new Date()));

        binding.tvReportMonthlyCoins.setText(String.format(Locale.getDefault(), "%,d Coins", agencyCommissionCoins));

        // Agency Payout
        binding.tvReportPayout.setText(String.format(Locale.getDefault(), "₹%.2f", estimatedInr));

        // Progress bar towards 10,000,000 monthly coin target (₹1,00,000)
        long targetCoins = 10000000L;
        int progress = (int) Math.min(100, (agencyCommissionCoins * 100) / targetCoins);
        binding.pbMonthlyTarget.setProgress(progress);
        binding.tvMonthlyProgressText.setText(String.format(Locale.getDefault(), "Monthly Target: %d%% achieved towards ₹1,00,000 milestone", progress));
    }

    private void showAddHostDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_agency_host, null, false);
        dialog.setContentView(dialogView);

        EditText etHostProfileId = dialogView.findViewById(R.id.etHostProfileId);
        MaterialButton btnAddHostConfirm = dialogView.findViewById(R.id.btnAddHostConfirm);
        View btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        if (btnAddHostConfirm != null) {
            btnAddHostConfirm.setOnClickListener(v -> {
                if (etHostProfileId == null) return;
                String queryInput = etHostProfileId.getText().toString().trim();

                if (queryInput.isEmpty()) {
                    Toast.makeText(AgencyDashboardActivity.this, "Please enter User Profile ID or UID", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                searchAndAddHost(queryInput);
            });
        }

        dialog.show();
    }

    private void searchAndAddHost(String input) {
        loadingDialog.show("Searching for user...");

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Try searching by direct UID first
        usersRef.child(input).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    addHostToAgency(snapshot);
                } else {
                    // Search by profileId property
                    usersRef.orderByChild("profileId").equalTo(input)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot querySnapshot) {
                                    if (querySnapshot.exists() && querySnapshot.getChildren().iterator().hasNext()) {
                                        DataSnapshot userSnap = querySnapshot.getChildren().iterator().next();
                                        addHostToAgency(userSnap);
                                    } else {
                                        loadingDialog.dismiss();
                                        Toast.makeText(AgencyDashboardActivity.this, "User not found with ID: " + input, Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loadingDialog.dismiss();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void addHostToAgency(DataSnapshot userSnap) {
        String hostUid = userSnap.getKey();
        if (hostUid == null) {
            loadingDialog.dismiss();
            return;
        }

        String hostName = userSnap.child("name").getValue(String.class);
        if (hostName == null) hostName = userSnap.child("userName").getValue(String.class);
        if (hostName == null) hostName = "Host User";

        String hostAvatar = userSnap.child("avtar").getValue(String.class);
        String profileId = userSnap.child("profileId").getValue(String.class);
        profileId = UserProfileCache.getDisplayNumericId(hostUid, profileId);

        long coins = 0;
        if (userSnap.child("coins").exists()) {
            Long c = userSnap.child("coins").getValue(Long.class);
            if (c != null) coins = c;
        }

        AgencyHostModel hostModel = new AgencyHostModel(
                hostUid,
                hostName,
                hostAvatar,
                profileId,
                coins / 10,
                coins,
                coins,
                12.5,
                System.currentTimeMillis(),
                "ACTIVE"
        );

        final String finalHostName = hostName;

        // 1. Add host to agency node
        hostsRef.child(hostUid).setValue(hostModel).addOnCompleteListener(task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                // 2. Update user profile node
                FirebaseDatabase.getInstance().getReference("users").child(hostUid)
                        .child("agencyId").setValue(currentAgencyId);

                FirebaseDatabase.getInstance().getReference("users").child(hostUid)
                        .child("agencyName").setValue(currentAgencyName);

                Toast.makeText(AgencyDashboardActivity.this, "🎉 " + finalHostName + " added to Agency successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AgencyDashboardActivity.this, "Failed to add host", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showHostPopupMenu(AgencyHostModel host, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add("👁️ View Host Profile");
        popup.getMenu().add("❌ Remove Host From Agency");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle() != null && item.getTitle().toString().contains("View Host")) {
                Intent intent = new Intent(AgencyDashboardActivity.this, UserDetailActivity.class);
                intent.putExtra("userId", host.getHostUid());
                startActivity(intent);
                return true;
            } else if (item.getTitle() != null && item.getTitle().toString().contains("Remove")) {
                confirmRemoveHost(host);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void confirmRemoveHost(AgencyHostModel host) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Host")
                .setMessage("Are you sure you want to remove '" + host.getHostName() + "' from your agency?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (host.getHostUid() != null) {
                        hostsRef.child(host.getHostUid()).removeValue();
                        DatabaseReference hostUserRef = FirebaseDatabase.getInstance().getReference("users").child(host.getHostUid());
                        hostUserRef.child("agencyId").removeValue();
                        hostUserRef.child("agencyName").removeValue();
                        hostUserRef.child("cooldownUntil").removeValue();
                        Toast.makeText(AgencyDashboardActivity.this, host.getHostName() + " removed from agency", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

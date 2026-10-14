package com.roomchatapps.Pmishra.dialogs;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.AgencyListAdapter;
import com.roomchatapps.Pmishra.databinding.DialogJoinAgencyBinding;
import com.roomchatapps.Pmishra.models.AgencyModel;
import com.roomchatapps.Pmishra.utils.AgencyManager;

import java.util.ArrayList;
import java.util.List;

public class JoinAgencyDialog {

    private final Context context;
    private final String userId;
    private final BottomSheetDialog dialog;
    private final DialogJoinAgencyBinding binding;

    private String currentOwnerUid = "";
    private AgencyListAdapter agencyAdapter;
    private final List<AgencyModel> allAgenciesList = new ArrayList<>();

    public JoinAgencyDialog(@NonNull Context context, @NonNull String userId) {
        this.context = context;
        this.userId = userId;

        dialog = new BottomSheetDialog(context);
        binding = DialogJoinAgencyBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        setupListeners();
        checkMembershipStatus();
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void setupListeners() {
        binding.btnCloseDialog.setOnClickListener(v -> dismiss());

        // Setup RecyclerView for Agency List
        binding.rvAgencies.setLayoutManager(new LinearLayoutManager(context));
        agencyAdapter = new AgencyListAdapter(allAgenciesList, this::joinSelectedAgency);
        binding.rvAgencies.setAdapter(agencyAdapter);

        // Realtime Search Filter Listener
        binding.etTargetAgencyId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (agencyAdapter != null) {
                    agencyAdapter.filter(s.toString());
                    if (agencyAdapter.getItemCount() == 0) {
                        binding.tvEmptyAgencies.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyAgencies.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Direct Join via entered ID
        binding.btnJoinAgencyConfirm.setOnClickListener(v -> {
            String inputCode = binding.etTargetAgencyId.getText().toString().trim();
            if (inputCode.isEmpty()) {
                binding.etTargetAgencyId.setError("Please enter or select an Agency");
                binding.etTargetAgencyId.requestFocus();
                return;
            }

            binding.btnJoinAgencyConfirm.setEnabled(false);
            binding.layoutLoadingStatus.setVisibility(View.VISIBLE);

            AgencyManager.joinAgency(userId, inputCode, new AgencyManager.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    binding.btnJoinAgencyConfirm.setEnabled(true);
                    binding.layoutLoadingStatus.setVisibility(View.GONE);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    binding.btnJoinAgencyConfirm.setEnabled(true);
                    binding.layoutLoadingStatus.setVisibility(View.GONE);
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.btnLeaveAgency.setOnClickListener(v -> confirmLeaveAgency());
    }

    private void checkMembershipStatus() {
        binding.layoutLoadingStatus.setVisibility(View.VISIBLE);
        binding.layoutJoinedState.setVisibility(View.GONE);
        binding.layoutCooldownState.setVisibility(View.GONE);
        binding.layoutJoinFormState.setVisibility(View.GONE);

        AgencyManager.checkUserAgencyStatus(userId, new AgencyManager.AgencyCheckCallback() {
            @Override
            public void onResult(boolean inAgency, String agencyId, String agencyName, long cooldownUntil) {
                binding.layoutLoadingStatus.setVisibility(View.GONE);

                if (inAgency) {
                    currentOwnerUid = agencyId;
                    binding.layoutJoinedState.setVisibility(View.VISIBLE);
                    binding.tvJoinedAgencyName.setText(agencyName != null ? agencyName : "Official Agency");
                    binding.tvJoinedAgencyId.setText("Agency ID: " + (agencyId != null ? agencyId : "N/A"));
                } else {
                    binding.layoutJoinFormState.setVisibility(View.VISIBLE);
                    loadAgenciesList();
                }
            }

            @Override
            public void onError(String error) {
                binding.layoutLoadingStatus.setVisibility(View.GONE);
                binding.layoutJoinFormState.setVisibility(View.VISIBLE);
                loadAgenciesList();
            }
        });
    }

    private void loadAgenciesList() {
        binding.pbLoadingAgencies.setVisibility(View.VISIBLE);
        binding.tvEmptyAgencies.setVisibility(View.GONE);

        DatabaseReference agenciesRef = FirebaseDatabase.getInstance().getReference("agencies");
        agenciesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAgenciesList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        AgencyModel agency = parseAgencySnapshot(child);
                        if (agency != null) {
                            String status = agency.getStatus();
                            if (status == null || status.isEmpty() || "APPROVED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status)) {
                                allAgenciesList.add(agency);
                            }
                        }
                    }
                }

                loadApprovedApplications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadApprovedApplications();
            }
        });
    }

    private AgencyModel parseAgencySnapshot(DataSnapshot child) {
        if (!child.exists()) return null;

        AgencyModel agency = child.getValue(AgencyModel.class);
        if (agency == null) {
            agency = new AgencyModel();
        }

        String ownerUid = child.getKey();
        if (agency.getUserId() == null || agency.getUserId().isEmpty()) {
            agency.setUserId(ownerUid);
        }

        String agencyName = child.child("agencyName").getValue(String.class);
        if (agencyName == null || agencyName.isEmpty()) {
            agencyName = agency.getAgencyName();
        }
        if (agencyName == null || agencyName.isEmpty()) {
            agencyName = child.child("name").getValue(String.class);
        }
        if (agencyName == null || agencyName.isEmpty()) {
            agencyName = "Official Agency";
        }
        agency.setAgencyName(agencyName);

        String agencyId = child.child("agencyId").getValue(String.class);
        if (agencyId == null || agencyId.isEmpty()) {
            agencyId = agency.getAgencyId();
        }
        if (agencyId == null || agencyId.isEmpty()) {
            agencyId = "AGY_" + (ownerUid != null && ownerUid.length() >= 6 ? ownerUid.substring(0, 6).toUpperCase() : "100000");
        }
        agency.setAgencyId(agencyId);

        String ownerName = child.child("ownerName").getValue(String.class);
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = agency.getOwnerName();
        }
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = "Official Owner";
        }
        agency.setOwnerName(ownerName);

        String agencyLogo = child.child("agencyLogo").getValue(String.class);
        if (agencyLogo == null || agencyLogo.isEmpty()) {
            agencyLogo = agency.getAgencyLogo();
        }
        agency.setAgencyLogo(agencyLogo);

        return agency;
    }

    private void loadApprovedApplications() {
        FirebaseDatabase.getInstance().getReference("agency_applications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                AgencyModel app = parseAgencySnapshot(child);
                                if (app != null && app.getAgencyName() != null && !app.getAgencyName().isEmpty()) {
                                    String status = app.getStatus();
                                    if (status == null || status.isEmpty() || "APPROVED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status)) {
                                        boolean exists = false;
                                        for (AgencyModel existing : allAgenciesList) {
                                            if ((existing.getAgencyId() != null && app.getAgencyId() != null &&
                                                    existing.getAgencyId().equalsIgnoreCase(app.getAgencyId())) ||
                                                    (existing.getUserId() != null && app.getUserId() != null &&
                                                            existing.getUserId().equalsIgnoreCase(app.getUserId()))) {
                                                exists = true;
                                                break;
                                            }
                                        }
                                        if (!exists) {
                                            allAgenciesList.add(app);
                                        }
                                    }
                                }
                            }
                        }

                        loadAgenciesFromUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadAgenciesFromUsers();
                    }
                });
    }

    private void loadAgenciesFromUsers() {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("agencyStatus").equalTo("APPROVED")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.pbLoadingAgencies.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String uid = child.getKey();
                                String agencyName = child.child("appliedAgencyName").getValue(String.class);
                                if (agencyName == null || agencyName.isEmpty()) {
                                    agencyName = child.child("agencyName").getValue(String.class);
                                }
                                if (agencyName != null && !agencyName.isEmpty()) {
                                    boolean exists = false;
                                    for (AgencyModel existing : allAgenciesList) {
                                        if (existing.getUserId() != null && existing.getUserId().equalsIgnoreCase(uid)) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        AgencyModel userAgency = new AgencyModel();
                                        userAgency.setUserId(uid);
                                        userAgency.setAgencyName(agencyName);
                                        userAgency.setAgencyId("AGY_" + (uid != null && uid.length() >= 6 ? uid.substring(0, 6).toUpperCase() : "100000"));
                                        String owner = child.child("name").getValue(String.class);
                                        if (owner == null) owner = child.child("userName").getValue(String.class);
                                        userAgency.setOwnerName(owner != null ? owner : "Official Owner");
                                        userAgency.setAgencyLogo(child.child("avtar").getValue(String.class));
                                        userAgency.setStatus("APPROVED");
                                        allAgenciesList.add(userAgency);
                                    }
                                }
                            }
                        }

                        if (agencyAdapter != null) {
                            agencyAdapter.updateData(allAgenciesList);
                            if (agencyAdapter.getItemCount() == 0) {
                                binding.tvEmptyAgencies.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvEmptyAgencies.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.pbLoadingAgencies.setVisibility(View.GONE);
                        if (agencyAdapter != null) {
                            agencyAdapter.updateData(allAgenciesList);
                            if (agencyAdapter.getItemCount() == 0) {
                                binding.tvEmptyAgencies.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvEmptyAgencies.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    private void joinSelectedAgency(AgencyModel agency) {
        String agencyIdToJoin = agency.getAgencyId();
        if (agencyIdToJoin == null || agencyIdToJoin.isEmpty()) {
            agencyIdToJoin = agency.getUserId();
        }

        binding.layoutLoadingStatus.setVisibility(View.VISIBLE);
        AgencyManager.joinAgency(userId, agencyIdToJoin, new AgencyManager.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                binding.layoutLoadingStatus.setVisibility(View.GONE);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                dismiss();
            }

            @Override
            public void onError(String error) {
                binding.layoutLoadingStatus.setVisibility(View.GONE);
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmLeaveAgency() {
        new AlertDialog.Builder(context)
                .setTitle("Leave Agency")
                .setMessage("Are you sure you want to leave your current agency?")
                .setPositiveButton("Leave Agency", (dialogInterface, which) -> {
                    binding.layoutLoadingStatus.setVisibility(View.VISIBLE);
                    AgencyManager.leaveAgency(userId, currentOwnerUid, new AgencyManager.ActionCallback() {
                        @Override
                        public void onSuccess(String message) {
                            binding.layoutLoadingStatus.setVisibility(View.GONE);
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            dismiss();
                        }

                        @Override
                        public void onError(String error) {
                            binding.layoutLoadingStatus.setVisibility(View.GONE);
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

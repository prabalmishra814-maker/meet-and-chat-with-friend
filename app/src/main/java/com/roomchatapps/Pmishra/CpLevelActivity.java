package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.CpPrivilegeAdapter;
import com.roomchatapps.Pmishra.models.CpPrivilegeModel;
import com.roomchatapps.Pmishra.utils.CpIntimacyManager;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

public class CpLevelActivity extends AppCompatActivity {

    private View headerBar;
    private ImageView btnBack;
    private ImageView btnHelp;
    private ImageView btnPrevLevel;
    private ImageView btnNextLevel;
    private TextView tvStageLevelText;
    private TextView tvStageStatus;
    private RecyclerView rvPrivileges;

    private CpPrivilegeAdapter adapter;
    private final List<CpPrivilegeModel> privilegeList = new ArrayList<>();

    private int currentDisplayLevel = 4;
    private int userActualLevel = 4;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_cp_level);

        initViews();

        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                headerBar.setPadding(
                        headerBar.getPaddingLeft(),
                        systemBars.top,
                        headerBar.getPaddingRight(),
                        headerBar.getPaddingBottom()
                );
                v.setPadding(
                        v.getPaddingLeft(),
                        0,
                        v.getPaddingRight(),
                        systemBars.bottom
                );
                return insets;
            });
        }

        setupToolbar();
        setupRecyclerView();
        loadUserIntimacyLevel();
        updateLevelUi();
    }

    private void initViews() {
        headerBar = findViewById(R.id.headerBar);
        btnBack = findViewById(R.id.btnBack);
        btnHelp = findViewById(R.id.btnHelp);
        btnPrevLevel = findViewById(R.id.btnPrevLevel);
        btnNextLevel = findViewById(R.id.btnNextLevel);
        tvStageLevelText = findViewById(R.id.tvStageLevelText);
        tvStageStatus = findViewById(R.id.tvStageStatus);
        rvPrivileges = findViewById(R.id.rvPrivileges);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());

        btnHelp.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("CP Level & Privileges")
                .setMessage("Send gifts and spend time together with your CP partner to increase Intimacy. Higher CP Levels unlock exclusive cards, notifications, emojis, badges, and house themes!")
                .setPositiveButton("Got It", null)
                .show());

        btnPrevLevel.setOnClickListener(v -> {
            if (currentDisplayLevel > 1) {
                currentDisplayLevel--;
                updateLevelUi();
            } else {
                Toast.makeText(this, "Minimum level reached", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextLevel.setOnClickListener(v -> {
            if (currentDisplayLevel < 10) {
                currentDisplayLevel++;
                updateLevelUi();
            } else {
                Toast.makeText(this, "Maximum level reached", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CpPrivilegeAdapter(privilegeList, this::showPrivilegeDetailDialog);
        rvPrivileges.setLayoutManager(new GridLayoutManager(this, 3));
        rvPrivileges.setAdapter(adapter);
    }

    private void loadUserIntimacyLevel() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference cpRef = FirebaseDatabase.getInstance().getReference("cp_bindings").child(user.getUid());
        cpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                if (snapshot.exists() && snapshot.hasChild("partnerUid")) {
                    String partnerUid = snapshot.child("partnerUid").getValue(String.class);
                    if (partnerUid != null) {
                        fetchGuardValue(user.getUid(), partnerUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchGuardValue(String currentUid, String partnerUid) {
        DatabaseReference cpGuardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values")
                .child(currentUid).child(partnerUid);

        cpGuardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;

                long intimacy = 0;
                if (snapshot.exists()) {
                    Long val = snapshot.child("value").getValue(Long.class);
                    if (val != null) intimacy = val;
                }

                userActualLevel = CpIntimacyManager.calculateCpLevel(intimacy);
                currentDisplayLevel = Math.max(currentDisplayLevel, userActualLevel);

                updateLevelUi();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateLevelUi() {
        tvStageLevelText.setText("LV." + currentDisplayLevel);

        if (currentDisplayLevel <= userActualLevel) {
            tvStageStatus.setText("Gained");
        } else {
            tvStageStatus.setText("Unlocks at LV." + currentDisplayLevel);
        }

        buildPrivilegesList();
        adapter.updateList(privilegeList);
    }

    private void buildPrivilegesList() {
        privilegeList.clear();

        privilegeList.add(new CpPrivilegeModel("1", "CP Card", R.drawable.ic_privilege_cp_card, 1, "Exclusive CP Card banner to showcase your bond in rooms and profiles.", currentDisplayLevel >= 1));
        privilegeList.add(new CpPrivilegeModel("2", "CP Online\nNotification", R.drawable.ic_privilege_online_notif, 1, "Special online notification broadcasted when your CP partner arrives.", currentDisplayLevel >= 1));
        privilegeList.add(new CpPrivilegeModel("3", "Unlock CP\nHouse", R.drawable.ic_privilege_cp_house, 2, "Unlock access to your shared CP Love House space.", currentDisplayLevel >= 2));
        privilegeList.add(new CpPrivilegeModel("4", "CP together\nprofile", R.drawable.ic_privilege_together_profile, 2, "Displays linked dual avatar profile badge on both user profiles.", currentDisplayLevel >= 2));
        privilegeList.add(new CpPrivilegeModel("5", "Unlock\nExclusive CP...", R.drawable.ic_privilege_exclusive_cp, 3, "Unlocks exclusive CP gift packs and intimate interaction items.", currentDisplayLevel >= 3));
        privilegeList.add(new CpPrivilegeModel("6", "Unlock\nProposal...", R.drawable.ic_privilege_proposal, 3, "Unlocks custom proposal envelopes and wedding proposal gifts.", currentDisplayLevel >= 3));
        privilegeList.add(new CpPrivilegeModel("7", "CP Online\nEffect", R.drawable.ic_privilege_online_effect, 4, "Glow trail and winged heart animation effect when CP joins.", currentDisplayLevel >= 4));
        privilegeList.add(new CpPrivilegeModel("8", "CP Sending\nNotification in...", R.drawable.ic_privilege_sending_notif, 4, "Special banner notification when sending CP gifts in room chat.", currentDisplayLevel >= 4));
        privilegeList.add(new CpPrivilegeModel("9", "CP Emoji", R.drawable.ic_privilege_cp_emoji, 4, "Exclusive romantic CP emoji pack usable in chat and room.", currentDisplayLevel >= 4));
        privilegeList.add(new CpPrivilegeModel("10", "CP Enter\nNotification", R.drawable.ic_privilege_enter_notif, 5, "Grand entrance banner when CP enters any chat room.", currentDisplayLevel >= 5));
        privilegeList.add(new CpPrivilegeModel("11", "Unlock CP\nHouse Theme", R.drawable.ic_privilege_house_theme, 5, "Customizable themes and decorations for your Love House.", currentDisplayLevel >= 5));
        privilegeList.add(new CpPrivilegeModel("12", "CP Medal", R.drawable.ic_privilege_cp_medal, 4, "Glowing winged heart CP Medal displayed on your profile card.", currentDisplayLevel >= 4));
        privilegeList.add(new CpPrivilegeModel("13", "CP Ring", R.drawable.ic_privilege_cp_ring, 6, "Exclusive crowned diamond CP Ring displayed on user info.", currentDisplayLevel >= 6));
    }

    private void showPrivilegeDetailDialog(CpPrivilegeModel model) {
        if (model == null || isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cp_privilege_detail, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        ImageView ivDetailIcon = dialogView.findViewById(R.id.ivDetailIcon);
        TextView tvDetailTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvDetailLevel = dialogView.findViewById(R.id.tvDetailLevel);
        TextView tvDetailDescription = dialogView.findViewById(R.id.tvDetailDescription);
        View btnCloseDetail = dialogView.findViewById(R.id.btnCloseDetail);

        if (ivDetailIcon != null) ivDetailIcon.setImageResource(model.getIconRes());
        if (tvDetailTitle != null) tvDetailTitle.setText(model.getTitle().replace("\n", " "));
        if (tvDetailLevel != null) {
            if (model.isUnlocked()) {
                tvDetailLevel.setText("Status: Unlocked (LV." + model.getUnlockLevel() + ")");
            } else {
                tvDetailLevel.setText("Requires: CP Level " + model.getUnlockLevel());
            }
        }
        if (tvDetailDescription != null) tvDetailDescription.setText(model.getDescription());

        if (btnCloseDetail != null) {
            btnCloseDetail.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}

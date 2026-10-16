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
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
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

        privilegeList.add(new CpPrivilegeModel(
                "0",
                "CP Banner",
                R.drawable.cp_banner,
                1,
                "Exclusive CP banner displayed for room and user profile.",
                userActualLevel >= 1
        ));

        privilegeList.add(new CpPrivilegeModel(
                "1",
                "CP Enter Effect",
                R.drawable.ic_privilege_enter_notif,
                1,
                "Exclusive CP entrance animation effect when entering a room.",
                userActualLevel >= 1,
                "cp_level/enter_1.svga"
        ));

        privilegeList.add(new CpPrivilegeModel(
                "2",
                "CP Level 5 Notification",
                R.drawable.ic_privilege_sending_notif,
                5,
                "Grand CP Level 5 banner notification effect.",
                userActualLevel >= 5,
                "cp_level/notification_level_5.svga"
        ));
    }

    private void showPrivilegeDetailDialog(CpPrivilegeModel model) {
        if (model == null || isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cp_privilege_detail, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        ImageView ivDetailIcon = dialogView.findViewById(R.id.ivDetailIcon);
        SVGAImageView svgaDetailPlayer = dialogView.findViewById(R.id.svgaDetailPlayer);
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

        if (model.getSvgaPath() != null && !model.getSvgaPath().isEmpty() && svgaDetailPlayer != null) {
            if (ivDetailIcon != null) {
                ivDetailIcon.setVisibility(View.GONE);
            }
            svgaDetailPlayer.setVisibility(View.VISIBLE);
            svgaDetailPlayer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            SVGAParser parser = new SVGAParser(this);
            parser.decodeFromAssets(model.getSvgaPath(), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity videoItem) {
                    runOnUiThread(() -> {
                        if (svgaDetailPlayer != null) {
                            svgaDetailPlayer.setVideoItem(videoItem);
                            svgaDetailPlayer.setLoops(0);
                            svgaDetailPlayer.stepToFrame(0, true);
                        }
                    });
                }

                @Override
                public void onError() {
                    runOnUiThread(() -> {
                        if (svgaDetailPlayer != null) {
                            svgaDetailPlayer.setVisibility(View.GONE);
                        }
                        if (ivDetailIcon != null) {
                            ivDetailIcon.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }, null);
        } else {
            if (svgaDetailPlayer != null) svgaDetailPlayer.setVisibility(View.GONE);
            if (ivDetailIcon != null) ivDetailIcon.setVisibility(View.VISIBLE);
        }

        dialog.setOnDismissListener(dialogInterface -> {
            if (svgaDetailPlayer != null) {
                svgaDetailPlayer.stopAnimation();
                svgaDetailPlayer.clear();
            }
        });

        if (btnCloseDetail != null) {
            btnCloseDetail.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}

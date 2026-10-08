package com.roomchatapps.Pmishra;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.roomchatapps.Pmishra.adapters.InviteTaskAdapter;
import com.roomchatapps.Pmishra.models.InviteTaskModel;
import com.roomchatapps.Pmishra.models.ReferralModel;
import com.roomchatapps.Pmishra.utils.ReferralManager;

import java.util.ArrayList;
import java.util.List;

public class ReferralActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCodePillText, tvRechargeRebateCount, tvGameRebateCount;
    private View btnCopyCodePill, btnBindInviterPill, llInviteTasksHeader, llNewUserTasksHeader;
    private Button btnInviteFriendsBottom, btnGoRoomLaunch;
    private TextView tabInviteTasks, tabNewUserTasks;
    private RecyclerView rvInviteTasks, rvInvitedAvatars;
    private ProgressBar progressBar;

    private InviteTaskAdapter taskAdapter;
    private final List<InviteTaskModel> taskList = new ArrayList<>();
    private String currentUid;
    private String currentReferralCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_referral);

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
        setupTabs();
        setupRecyclerView();
        setupClickListeners();
        loadReferralCode();
        loadDefaultTasks();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCodePillText = findViewById(R.id.tvCodePillText);
        btnCopyCodePill = findViewById(R.id.btnCopyCodePill);
        tvRechargeRebateCount = findViewById(R.id.tvRechargeRebateCount);
        tvGameRebateCount = findViewById(R.id.tvGameRebateCount);
        tabInviteTasks = findViewById(R.id.tabInviteTasks);
        tabNewUserTasks = findViewById(R.id.tabNewUserTasks);
        rvInviteTasks = findViewById(R.id.rvInviteTasks);
        rvInvitedAvatars = findViewById(R.id.rvInvitedAvatars);
        btnBindInviterPill = findViewById(R.id.btnBindInviterPill);
        btnInviteFriendsBottom = findViewById(R.id.btnInviteFriendsBottom);
        llInviteTasksHeader = findViewById(R.id.llInviteTasksHeader);
        llNewUserTasksHeader = findViewById(R.id.llNewUserTasksHeader);
        btnGoRoomLaunch = findViewById(R.id.btnGoRoomLaunch);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupTabs() {
        if (tabInviteTasks != null) {
            tabInviteTasks.setOnClickListener(v -> selectTab(true));
        }
        if (tabNewUserTasks != null) {
            tabNewUserTasks.setOnClickListener(v -> selectTab(false));
        }
    }

    private void selectTab(boolean isInviteTasks) {
        if (tabInviteTasks != null) {
            tabInviteTasks.setBackgroundResource(isInviteTasks ? R.drawable.chip_charm_bg : R.drawable.chip_room_bg);
            tabInviteTasks.setTextColor(isInviteTasks ? Color.WHITE : Color.parseColor("#80FFFFFF"));
        }

        if (tabNewUserTasks != null) {
            tabNewUserTasks.setBackgroundResource(isInviteTasks ? R.drawable.chip_room_bg : R.drawable.chip_charm_bg);
            tabNewUserTasks.setTextColor(isInviteTasks ? Color.parseColor("#80FFFFFF") : Color.WHITE);
        }

        if (llInviteTasksHeader != null) llInviteTasksHeader.setVisibility(isInviteTasks ? View.VISIBLE : View.GONE);
        if (llNewUserTasksHeader != null) llNewUserTasksHeader.setVisibility(isInviteTasks ? View.GONE : View.VISIBLE);

        if (isInviteTasks) {
            loadDefaultTasks();
        } else {
            loadNewUserTasks();
        }
    }

    private void setupRecyclerView() {
        if (rvInviteTasks != null) {
            rvInviteTasks.setLayoutManager(new LinearLayoutManager(this));
            taskAdapter = new InviteTaskAdapter(taskList);
            rvInviteTasks.setAdapter(taskAdapter);
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Copy Invitation Code
        if (btnCopyCodePill != null) {
            btnCopyCodePill.setOnClickListener(v -> {
                if (currentReferralCode.isEmpty()) return;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invitation Code", currentReferralCode);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ReferralActivity.this, "📋 Invitation code copied!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Floating "Bind Your Inviter"
        if (btnBindInviterPill != null) {
            btnBindInviterPill.setOnClickListener(v -> showBindInviterDialog());
        }

        // Bottom "Invite Friends" Button
        if (btnInviteFriendsBottom != null) {
            btnInviteFriendsBottom.setOnClickListener(v -> {
                if (!currentReferralCode.isEmpty()) {
                    ReferralManager.shareReferralInvite(ReferralActivity.this, currentReferralCode, "User");
                }
            });
        }

        // Room Launch Banner "Go" Button
        if (btnGoRoomLaunch != null) {
            btnGoRoomLaunch.setOnClickListener(v ->
                    Toast.makeText(ReferralActivity.this, "Joining Room ID: 61553...", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadReferralCode() {
        if (currentUid == null) {
            if (tvCodePillText != null) tvCodePillText.setText("100934032 📋");
            return;
        }

        ReferralManager.getOrCreateReferralCode(currentUid, new ReferralManager.CodeCallback() {
            @Override
            public void onCodeReady(String code) {
                currentReferralCode = code;
                if (tvCodePillText != null) {
                    tvCodePillText.setText(code + " 📋");
                }
            }

            @Override
            public void onError(String error) {
                currentReferralCode = "100934032";
                if (tvCodePillText != null) tvCodePillText.setText("100934032 📋");
            }
        });
    }

    private void loadDefaultTasks() {
        taskList.clear();

        // 7 Tasks matching design screenshots exactly:
        taskList.add(new InviteTaskModel("task_bind_code", "Friends bind your invitation code", "1 friend = 200000 🍀", R.drawable.ic_person, "CHIP", 0, false));
        taskList.add(new InviteTaskModel("task_first_recharge", "Invite friends for first recharge", "1 friend = 8000000 🍀", R.drawable.gift_icon, "CHIP", 0, true));
        taskList.add(new InviteTaskModel("task_recharge_5d", "Friend recharges 5000000 coins (5$)", "1 friend = 30000000 🪙", R.drawable.gift_icon, "COIN", 0, true));
        taskList.add(new InviteTaskModel("task_share_daily", "Share Room Chat once daily", "1 Time = 20000 🍀", R.drawable.ic_play, "CHIP", 0, false));
        taskList.add(new InviteTaskModel("task_spend_gifts", "Friends spend coins to send gifts", "Gift amount * 8% 🪙", R.drawable.gift_icon, "COIN", 0, true));
        taskList.add(new InviteTaskModel("task_send_gifts", "You send gifts to friends", "Gift amount * 4% 🪙", R.drawable.gift_icon, "COIN", 0, true));
        taskList.add(new InviteTaskModel("task_receive_gifts", "Friends send gifts to you", "Gift amount * 4% 🪙", R.drawable.gift_icon, "COIN", 0, true));

        if (taskAdapter != null) taskAdapter.notifyDataSetChanged();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void loadNewUserTasks() {
        taskList.clear();

        // New User Tasks matching screenshot
        taskList.add(new InviteTaskModel("new_user_profile", "Friends bind your invitation code", "1 friend = 200000 🍀", R.drawable.ic_person, "CHIP", 0, false));
        taskList.add(new InviteTaskModel("new_user_recharge", "Invite friends for first recharge", "1 friend = 8000000 🍀", R.drawable.gift_icon, "CHIP", 0, true));
        taskList.add(new InviteTaskModel("new_user_recharge_5d", "Friend recharges 5000000 coins (5$)", "1 friend = 30000000 🪙", R.drawable.gift_icon, "COIN", 0, true));
        taskList.add(new InviteTaskModel("new_user_share", "Share Hayi once daily", "1 Time = 20000 🍀", R.drawable.ic_play, "CHIP", 0, false));
        taskList.add(new InviteTaskModel("new_user_spend_gifts", "Friends spend coins to send gifts", "Gift amount * 8% 🪙", R.drawable.gift_icon, "COIN", 0, true));
        taskList.add(new InviteTaskModel("new_user_send_gifts", "You send gifts to friends", "Gift amount * 4% 🪙", R.drawable.gift_icon, "COIN", 0, true));
        taskList.add(new InviteTaskModel("new_user_receive_gifts", "Friends send gifts to you", "Gift amount * 4% 🪙", R.drawable.gift_icon, "COIN", 0, true));

        if (taskAdapter != null) taskAdapter.notifyDataSetChanged();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void showBindInviterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_bind_inviter, null, false);
        dialog.setContentView(dialogView);

        EditText etInviterCode = dialogView.findViewById(R.id.etInviterCode);
        MaterialButton btnConfirmBind = dialogView.findViewById(R.id.btnConfirmBind);
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnConfirmBind != null) {
            btnConfirmBind.setOnClickListener(v -> {
                if (etInviterCode == null) return;
                String inputCode = etInviterCode.getText().toString().trim();
                if (inputCode.isEmpty()) {
                    Toast.makeText(ReferralActivity.this, "Please enter an invitation code!", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnConfirmBind.setEnabled(false);
                Toast.makeText(ReferralActivity.this, "Binding inviter code...", Toast.LENGTH_SHORT).show();

                ReferralManager.applyReferralCode(currentUid, inputCode, new ReferralManager.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            dialog.dismiss();
                            Toast.makeText(ReferralActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            btnConfirmBind.setEnabled(true);
                            Toast.makeText(ReferralActivity.this, error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        }

        dialog.show();
    }
}

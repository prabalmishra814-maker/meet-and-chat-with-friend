package com.roomchatapps.Pmishra;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.roomchatapps.Pmishra.adapters.DailyCheckInAdapter;
import com.roomchatapps.Pmishra.models.DayRewardConfig;
import com.roomchatapps.Pmishra.models.UserCheckInState;
import com.roomchatapps.Pmishra.utils.DailyCheckInManager;

import java.util.List;

// DAILY CHECK-IN
public class DailyCheckInDialog extends BottomSheetDialog {

    private TextView tvTitle, tvSubtitle, tvStreakTitle, tvStreakNote;
    private ImageView btnClose;
    private RecyclerView rvCheckInDays;
    private ProgressBar progressBar;

    private DailyCheckInAdapter adapter;
    private List<DayRewardConfig> rewardList;
    private UserCheckInState currentState;
    private String currentUid;
    private boolean isClaiming = false;

    public DailyCheckInDialog(@NonNull Context context) {
        super(context, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_daily_checkin);

        // Remove default bottom sheet background and expand flush to bottom with 0 gap
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().getDecorView().setPadding(0, 0, 0, 0);

            View bottomSheet = getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(null);
                bottomSheet.setPadding(0, 0, 0, 0);
                
                ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) lp).setMargins(0, 0, 0, 0);
                    bottomSheet.setLayoutParams(lp);
                }

                ViewCompat.setOnApplyWindowInsetsListener(bottomSheet, (v, insets) -> {
                    v.setPadding(0, 0, 0, 0);
                    return insets;
                });

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }

        currentUid = FirebaseAuth.getInstance().getUid();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadState();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvStreakTitle = findViewById(R.id.tvStreakTitle);
        tvStreakNote = findViewById(R.id.tvStreakNote);
        btnClose = findViewById(R.id.btnClose);
        rvCheckInDays = findViewById(R.id.rvCheckInDays);
        progressBar = findViewById(R.id.progressBar);

        if (tvTitle != null) tvTitle.setText("Daily Check-In");
        if (tvSubtitle != null) tvSubtitle.setText("Check in every day to claim your rewards");
    }

    private void setupRecyclerView() {
        if (rvCheckInDays == null) return;

        rewardList = DailyCheckInManager.get7DayRewards();
        currentState = new UserCheckInState();

        rvCheckInDays.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        adapter = new DailyCheckInAdapter(getContext(), rewardList, currentState, new DailyCheckInAdapter.OnClaimClickListener() {
            @Override
            public void onClaimClick(DayRewardConfig reward, int position) {
                handleRewardClaim(reward);
            }
        });

        rvCheckInDays.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }

    private void loadState() {
        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(getContext(), "Please log in to claim daily rewards", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        DailyCheckInManager.loadUserCheckInState(currentUid, new DailyCheckInManager.StateCallback() {
            @Override
            public void onStateLoaded(UserCheckInState state) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                currentState = state;
                if (adapter != null) adapter.updateState(currentState);
                updateStreakUI(currentState);
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStreakUI(UserCheckInState state) {
        if (tvStreakTitle != null) {
            int day = state.getCurrentDay();
            if (state.isTodayClaimed()) {
                tvStreakTitle.setText("Today's Reward Claimed! 🎉 (Day " + day + "/7)");
            } else {
                tvStreakTitle.setText("Claim Day " + day + " Reward Today! 🔥");
            }
        }

        if (tvStreakNote != null) {
            tvStreakNote.setText(state.isTodayClaimed() ? "Come back tomorrow" : "Available Now");
        }
    }

    // DAILY CHECK-IN ANTI DUPLICATE
    private void handleRewardClaim(DayRewardConfig reward) {
        if (isClaiming) return; // Prevent double taps during active transaction

        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(getContext(), "Please log in to claim rewards", Toast.LENGTH_SHORT).show();
            return;
        }

        isClaiming = true;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        DailyCheckInManager.claimReward(currentUid, reward.getDayNumber(), new DailyCheckInManager.ClaimCallback() {
            @Override
            public void onSuccess(String message, DayRewardConfig claimedReward) {
                isClaiming = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                // Refresh state and reload UI
                loadState();
            }

            @Override
            public void onError(String error) {
                isClaiming = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                String displayError = (error != null && !error.isEmpty()) ? error : "Unable to claim reward. Please try again.";
                Toast.makeText(getContext(), displayError, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

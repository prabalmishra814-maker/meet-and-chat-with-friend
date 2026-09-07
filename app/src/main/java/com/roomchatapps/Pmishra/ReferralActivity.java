package com.roomchatapps.Pmishra;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.roomchatapps.Pmishra.models.ReferralModel;
import com.roomchatapps.Pmishra.utils.ReferralManager;

import java.util.ArrayList;
import java.util.List;

public class ReferralActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvReferralCode, tvTotalInvited, tvCoinsEarned;
    private MaterialButton btnCopyCode, btnShareInvite, btnClaimBonus;
    private EditText etReferralCode;
    private CardView cvCodeCard, cvClaimCard;
    private View llStatsRow;
    private RecyclerView rvReferrals;
    private LinearLayout llEmptyReferrals;
    private ProgressBar progressBar;

    private ReferralAdapter adapter;
    private final List<ReferralModel> referralList = new ArrayList<>();
    private String currentUid;
    private String currentReferralCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);

        currentUid = FirebaseAuth.getInstance().getUid();

        initViews();
        setupAnimations();
        setupRecyclerView();
        setupClickListeners();
        loadReferralCode();
        loadReferralStats();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvReferralCode = findViewById(R.id.tvReferralCode);
        tvTotalInvited = findViewById(R.id.tvTotalInvited);
        tvCoinsEarned = findViewById(R.id.tvCoinsEarned);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        btnShareInvite = findViewById(R.id.btnShareInvite);
        btnClaimBonus = findViewById(R.id.btnClaimBonus);
        etReferralCode = findViewById(R.id.etReferralCode);
        cvCodeCard = findViewById(R.id.cvCodeCard);
        cvClaimCard = findViewById(R.id.cvClaimCard);
        llStatsRow = findViewById(R.id.llStatsRow);
        rvReferrals = findViewById(R.id.rvReferrals);
        llEmptyReferrals = findViewById(R.id.llEmptyReferrals);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupAnimations() {
        View header = findViewById(R.id.header);
        if (header != null) AnimationHelper.fadeIn(header, 400);
        if (cvCodeCard != null) AnimationHelper.scaleIn(cvCodeCard, 500);
        if (llStatsRow != null) AnimationHelper.fadeIn(llStatsRow, 600);
        if (cvClaimCard != null) AnimationHelper.slideUp(cvClaimCard, 700);
    }

    private void setupRecyclerView() {
        if (rvReferrals != null) {
            rvReferrals.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ReferralAdapter(referralList);
            rvReferrals.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Copy Code to Clipboard
        if (btnCopyCode != null) {
            btnCopyCode.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                if (currentReferralCode.isEmpty() || currentReferralCode.equals("LOADING...")) return;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Referral Code", currentReferralCode);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ReferralActivity.this, "📋 Referral code copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Share Invite
        if (btnShareInvite != null) {
            btnShareInvite.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                if (currentReferralCode.isEmpty() || currentReferralCode.equals("LOADING...")) return;
                ReferralManager.shareReferralInvite(ReferralActivity.this, currentReferralCode, "User");
            });
        }

        // Claim Bonus
        if (btnClaimBonus != null) {
            btnClaimBonus.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                if (etReferralCode == null) return;
                String input = etReferralCode.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(ReferralActivity.this, "Please enter a referral code!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(ReferralActivity.this, "Claiming referral bonus...", Toast.LENGTH_SHORT).show();
                ReferralManager.applyReferralCode(currentUid, input, new ReferralManager.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(ReferralActivity.this, message, Toast.LENGTH_LONG).show();
                        etReferralCode.setText("");
                        etReferralCode.setEnabled(false);
                        btnClaimBonus.setEnabled(false);
                        btnClaimBonus.setText("Claimed ✓");
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ReferralActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    private void loadReferralCode() {
        if (currentUid == null) return;

        ReferralManager.getOrCreateReferralCode(currentUid, new ReferralManager.CodeCallback() {
            @Override
            public void onCodeReady(String code) {
                currentReferralCode = code;
                if (tvReferralCode != null) {
                    tvReferralCode.setText(code);
                    AnimationHelper.bounceAnimation(tvReferralCode);
                }
            }

            @Override
            public void onError(String error) {
                if (tvReferralCode != null) tvReferralCode.setText("ROOM88");
            }
        });
    }

    private void loadReferralStats() {
        if (currentUid == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            updateEmptyState();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        ReferralManager.loadUserReferrals(currentUid, new ReferralManager.ReferralsListCallback() {
            @Override
            public void onReferralsLoaded(List<ReferralModel> referrals, long totalCoinsEarned) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                referralList.clear();
                referralList.addAll(referrals);

                if (tvTotalInvited != null) tvTotalInvited.setText(String.valueOf(referrals.size()));
                if (tvCoinsEarned != null) tvCoinsEarned.setText(totalCoinsEarned + " 🪙");

                if (adapter != null) adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (referralList.isEmpty()) {
            if (llEmptyReferrals != null) llEmptyReferrals.setVisibility(View.VISIBLE);
            if (rvReferrals != null) rvReferrals.setVisibility(View.GONE);
        } else {
            if (llEmptyReferrals != null) llEmptyReferrals.setVisibility(View.GONE);
            if (rvReferrals != null) rvReferrals.setVisibility(View.VISIBLE);
        }
    }
}

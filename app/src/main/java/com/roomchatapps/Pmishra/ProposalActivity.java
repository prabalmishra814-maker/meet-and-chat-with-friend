package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;



import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProposalActivity extends AppCompatActivity {

    private View mainLayout;
    private View headerBar;
    private ImageView btnBack;
    private ImageView ivUserAvatar;
    private ImageView ivPartnerAvatar;
    private TextView tvPartnerName;
    private View layoutRingBox;
    private View layoutRingTooltip;
    private ImageView ivRingIcon;
    private EditText etProposalMessage;
    private TextView btnChooseDate;
    private TextView btnConfirmProposal;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference cpRef;
    private String currentUid;
    private String partnerUid;
    private String partnerName = "Partner";

    private int selectedRingRes = R.drawable.ic_proposal_ring_icon;
    private String selectedWeddingDate = "";

    private final ActivityResultLauncher<Intent> ringSelectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int iconRes = data.getIntExtra("ringIconRes", R.drawable.ic_ring_blue_winged);
                    selectedRingRes = iconRes;
                    ivRingIcon.setImageResource(selectedRingRes);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_proposal);

        initViews();
        setupWindowInsets();
        setupToolbar();
        setupClickListeners();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (getIntent() != null && getIntent().hasExtra("partnerUid")) {
            partnerUid = getIntent().getStringExtra("partnerUid");
        }

        if (currentUser != null) {
            currentUid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
            cpRef = FirebaseDatabase.getInstance().getReference("cp_bindings").child(currentUid);
            loadUserData();
            loadCpPartnerData();
        } else if (!TextUtils.isEmpty(partnerUid)) {
            loadPartnerUserProfile(partnerUid);
        }
    }

    private void initViews() {
        mainLayout = findViewById(R.id.mainLayout);
        headerBar = findViewById(R.id.headerBar);
        btnBack = findViewById(R.id.btnBack);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        ivPartnerAvatar = findViewById(R.id.ivPartnerAvatar);
        tvPartnerName = findViewById(R.id.tvPartnerName);
        layoutRingBox = findViewById(R.id.layoutRingBox);
        layoutRingTooltip = findViewById(R.id.layoutRingTooltip);
        ivRingIcon = findViewById(R.id.ivRingIcon);
        etProposalMessage = findViewById(R.id.etProposalMessage);
        btnChooseDate = findViewById(R.id.btnChooseDate);
        btnConfirmProposal = findViewById(R.id.btnConfirmProposal);
    }

    private void setupWindowInsets() {
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
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        View.OnClickListener ringClickListener = v -> showRingSelectionDialog();
        layoutRingBox.setOnClickListener(ringClickListener);
        layoutRingTooltip.setOnClickListener(ringClickListener);

        btnChooseDate.setOnClickListener(v -> showDatePicker());
        btnConfirmProposal.setOnClickListener(v -> sendProposal());
    }

    private void loadUserData() {
        if (userRef == null) return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                if (snapshot.exists()) {
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("avatar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("photoUrl").getValue(String.class);

                    if (!TextUtils.isEmpty(avatar)) {
                        Glide.with(ProposalActivity.this)
                                .load(avatar)
                                .placeholder(R.drawable.img_20260904_135725)
                                .error(R.drawable.img_20260904_135725)
                                .into(ivUserAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCpPartnerData() {
        if (!TextUtils.isEmpty(partnerUid)) {
            loadPartnerUserProfile(partnerUid);
            return;
        }

        if (cpRef == null) return;
        cpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                if (snapshot.exists() && snapshot.hasChild("partnerUid")) {
                    partnerUid = snapshot.child("partnerUid").getValue(String.class);
                    String name = snapshot.child("partnerName").getValue(String.class);
                    String avatar = snapshot.child("partnerAvatar").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        partnerName = name;
                        tvPartnerName.setText(partnerName);
                    }

                    if (!TextUtils.isEmpty(avatar)) {
                        Glide.with(ProposalActivity.this)
                                .load(avatar)
                                .placeholder(R.drawable.img_20260904_135725)
                                .error(R.drawable.img_20260904_135725)
                                .into(ivPartnerAvatar);
                    }

                    if (!TextUtils.isEmpty(partnerUid)) {
                        loadPartnerUserProfile(partnerUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPartnerUserProfile(String uid) {
        if (TextUtils.isEmpty(uid)) return;

        DatabaseReference partnerRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        partnerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("avatar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("photoUrl").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("image").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("userIcon").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        partnerName = name;
                        tvPartnerName.setText(partnerName);
                    }

                    if (!TextUtils.isEmpty(avatar) && !isFinishing()) {
                        Glide.with(ProposalActivity.this)
                                .load(avatar)
                                .placeholder(R.drawable.img_20260904_135725)
                                .error(R.drawable.img_20260904_135725)
                                .into(ivPartnerAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showRingSelectionDialog() {
        Intent intent = new Intent(this, RingSelectActivity.class);
        ringSelectLauncher.launch(intent);
    }

    private void showDatePicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_wedding_date_picker, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        NumberPicker pickerMonth = dialogView.findViewById(R.id.pickerMonth);
        NumberPicker pickerDay = dialogView.findViewById(R.id.pickerDay);
        NumberPicker pickerYear = dialogView.findViewById(R.id.pickerYear);
        NumberPicker pickerHour = dialogView.findViewById(R.id.pickerHour);
        NumberPicker pickerMinute = dialogView.findViewById(R.id.pickerMinute);
        View btnConfirmDate = dialogView.findViewById(R.id.btnConfirmDate);

        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH);
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMin = calendar.get(Calendar.MINUTE);

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        if (pickerMonth != null) {
            pickerMonth.setMinValue(0);
            pickerMonth.setMaxValue(11);
            pickerMonth.setDisplayedValues(months);
            pickerMonth.setValue(curMonth);
            styleNumberPicker(pickerMonth);
            pickerMonth.setOnValueChangedListener((p, o, n) -> styleNumberPicker(p));
        }

        if (pickerDay != null) {
            pickerDay.setMinValue(1);
            pickerDay.setMaxValue(31);
            pickerDay.setValue(curDay);
            styleNumberPicker(pickerDay);
            pickerDay.setOnValueChangedListener((p, o, n) -> styleNumberPicker(p));
        }

        if (pickerYear != null) {
            pickerYear.setMinValue(2026);
            pickerYear.setMaxValue(2035);
            pickerYear.setValue(Math.max(curYear, 2026));
            styleNumberPicker(pickerYear);
            pickerYear.setOnValueChangedListener((p, o, n) -> styleNumberPicker(p));
        }

        if (pickerHour != null) {
            pickerHour.setMinValue(0);
            pickerHour.setMaxValue(23);
            pickerHour.setValue(curHour);
            pickerHour.setFormatter(i -> String.format("%02d", i));
            styleNumberPicker(pickerHour);
            pickerHour.setOnValueChangedListener((p, o, n) -> styleNumberPicker(p));
        }

        if (pickerMinute != null) {
            pickerMinute.setMinValue(0);
            pickerMinute.setMaxValue(59);
            pickerMinute.setValue(curMin);
            pickerMinute.setFormatter(i -> String.format("%02d", i));
            styleNumberPicker(pickerMinute);
            pickerMinute.setOnValueChangedListener((p, o, n) -> styleNumberPicker(p));
        }

        if (btnConfirmDate != null) {
            btnConfirmDate.setOnClickListener(v -> {
                int selM = pickerMonth != null ? pickerMonth.getValue() : curMonth;
                int selD = pickerDay != null ? pickerDay.getValue() : curDay;
                int selY = pickerYear != null ? pickerYear.getValue() : curYear;
                int selH = pickerHour != null ? pickerHour.getValue() : curHour;
                int selMin = pickerMinute != null ? pickerMinute.getValue() : curMin;

                String monthName = (selM >= 0 && selM < months.length) ? months[selM] : "October";
                String formattedTime = String.format("%02d:%02d", selH, selMin);

                selectedWeddingDate = monthName + " " + selD + " " + selY + " " + formattedTime;
                btnChooseDate.setText("Wedding Date: " + selectedWeddingDate);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void styleNumberPicker(NumberPicker picker) {
        if (picker == null) return;
        int count = picker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    EditText editText = (EditText) child;
                    editText.setTextColor(Color.parseColor("#FFEA00"));
                    editText.setTextSize(18f);
                    editText.setTypeface(null, Typeface.BOLD);
                } catch (Exception ignored) {}
            }
        }
        picker.invalidate();
    }

    private void sendProposal() {
        String message = etProposalMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            message = "So lucky to meet you, hope I can marry you and stay together for a life time~";
        }

        if (TextUtils.isEmpty(selectedWeddingDate)) {
            Toast.makeText(this, "Please choose a wedding date first!", Toast.LENGTH_SHORT).show();
            showDatePicker();
            return;
        }

        if (TextUtils.isEmpty(currentUid) || TextUtils.isEmpty(partnerUid)) {
            Toast.makeText(this, "Proposal sent to " + partnerName + "!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference proposalRef = FirebaseDatabase.getInstance().getReference("cp_proposals").child(currentUid);
        Map<String, Object> proposalMap = new HashMap<>();
        proposalMap.put("senderUid", currentUid);
        proposalMap.put("partnerUid", partnerUid);
        proposalMap.put("partnerName", partnerName);
        proposalMap.put("message", message);
        proposalMap.put("weddingDate", selectedWeddingDate);
        proposalMap.put("ringRes", selectedRingRes);
        proposalMap.put("timestamp", System.currentTimeMillis());
        proposalMap.put("status", "pending");

        proposalRef.setValue(proposalMap).addOnCompleteListener(task -> {
            if (isFinishing()) return;
            if (task.isSuccessful()) {
                Toast.makeText(ProposalActivity.this, "Proposal sent to " + partnerName + "!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(ProposalActivity.this, "Failed to send proposal. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

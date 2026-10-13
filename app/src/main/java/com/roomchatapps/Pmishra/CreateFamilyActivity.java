package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.util.HashMap;
import java.util.Map;

public class CreateFamilyActivity extends AppCompatActivity {

    public static final int PICK_IMAGE_REQUEST = 100;
    private static final long CREATE_FAMILY_COST = 200000L;

    private ShapeableImageView ivFamilyLogo;
    private EditText etFamilyName, etFamilyTag, etFamilyNotice;
    private TextView tvNameCounter, tvTagCounter, tvNoticeCounter;
    private TextView tvTagPreviewText, tvJoinModeValue, tvLevelReqValue;

    private Uri imageUri;
    private String selectedJoinMode = "Free join";
    private String selectedLevelReq = "Level.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_create_family);

        setupWindowInsets();
        initViews();
        setupTextWatchers();
        setupListeners();
    }

    private void setupWindowInsets() {
        View topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ivFamilyLogo = findViewById(R.id.ivFamilyLogo);
        View btnPickLogo = findViewById(R.id.btnPickLogo);
        if (btnPickLogo != null) {
            btnPickLogo.setOnClickListener(v -> openGallery());
        }

        etFamilyName = findViewById(R.id.etFamilyName);
        etFamilyTag = findViewById(R.id.etFamilyTag);
        etFamilyNotice = findViewById(R.id.etFamilyNotice);

        tvNameCounter = findViewById(R.id.tvNameCounter);
        tvTagCounter = findViewById(R.id.tvTagCounter);
        tvNoticeCounter = findViewById(R.id.tvNoticeCounter);

        tvTagPreviewText = findViewById(R.id.tvTagPreviewText);
        tvJoinModeValue = findViewById(R.id.tvJoinModeValue);
        tvLevelReqValue = findViewById(R.id.tvLevelReqValue);

        View btnSubmit = findViewById(R.id.btnSubmitCreateFamily);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> validateAndCreateFamily());
        }
    }

    private void setupTextWatchers() {
        if (etFamilyName != null) {
            etFamilyName.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvNameCounter != null) tvNameCounter.setText(s.length() + "/20");
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (etFamilyTag != null) {
            etFamilyTag.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvTagCounter != null) tvTagCounter.setText(s.length() + "/8");
                    if (tvTagPreviewText != null) {
                        tvTagPreviewText.setText(s.length() > 0 ? s.toString().toUpperCase() : "TAG");
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (etFamilyNotice != null) {
            etFamilyNotice.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvNoticeCounter != null) tvNoticeCounter.setText(s.length() + "/500");
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupListeners() {
        View btnJoinMode = findViewById(R.id.btnJoinMode);
        if (btnJoinMode != null) {
            btnJoinMode.setOnClickListener(v -> showJoinModeDialog());
        }

        View btnLevelReq = findViewById(R.id.btnLevelReq);
        if (btnLevelReq != null) {
            btnLevelReq.setOnClickListener(v -> showLevelReqDialog());
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (ivFamilyLogo != null) {
                Glide.with(this).load(imageUri).into(ivFamilyLogo);
            }
        }
    }

    private void showJoinModeDialog() {
        String[] options = {"Free join", "Approval required"};
        new AlertDialog.Builder(this)
                .setTitle("Select Join Mode")
                .setItems(options, (dialog, which) -> {
                    selectedJoinMode = options[which];
                    if (tvJoinModeValue != null) tvJoinModeValue.setText(selectedJoinMode);
                })
                .show();
    }

    private void showLevelReqDialog() {
        String[] options = {"Level.0", "Level.5", "Level.10", "Level.20"};
        new AlertDialog.Builder(this)
                .setTitle("Select Level Requirement")
                .setItems(options, (dialog, which) -> {
                    selectedLevelReq = options[which];
                    if (tvLevelReqValue != null) tvLevelReqValue.setText(selectedLevelReq);
                })
                .show();
    }

    private void validateAndCreateFamily() {
        String name = etFamilyName != null ? etFamilyName.getText().toString().trim() : "";
        String tag = etFamilyTag != null ? etFamilyTag.getText().toString().trim() : "";
        String notice = etFamilyNotice != null ? etFamilyNotice.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a family name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(tag)) {
            Toast.makeText(this, "Please enter a family tag", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        Toast.makeText(this, "Creating family...", Toast.LENGTH_SHORT).show();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long coins = snapshot.child("coins").getValue(Long.class);
                if (coins == null) coins = snapshot.child("walletBalance").getValue(Long.class);
                long currentCoins = coins != null ? coins : 0;

                if (currentCoins < CREATE_FAMILY_COST) {
                    Toast.makeText(CreateFamilyActivity.this, "Insufficient coins! You need 200,000 coins.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Deduct coins & create family
                userRef.child("coins").setValue(currentCoins - CREATE_FAMILY_COST);

                DatabaseReference familiesRef = FirebaseDatabase.getInstance().getReference("families");
                String familyId = familiesRef.push().getKey();
                if (familyId == null) familyId = "f_" + System.currentTimeMillis();

                String logoUrl = imageUri != null ? imageUri.toString() : "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=200";

                Map<String, Object> familyMap = new HashMap<>();
                familyMap.put("familyId", familyId);
                familyMap.put("familyName", name);
                familyMap.put("familyTag", tag);
                familyMap.put("familyNotice", notice);
                familyMap.put("familyLogo", logoUrl);
                familyMap.put("ownerUid", uid);
                familyMap.put("joinMode", selectedJoinMode);
                familyMap.put("levelRequirement", selectedLevelReq);
                familyMap.put("score", 0L);
                familyMap.put("createdAt", System.currentTimeMillis());

                familiesRef.child(familyId).setValue(familyMap);

                // Update user family ID and Name
                userRef.child("familyId").setValue(familyId);
                userRef.child("familyName").setValue(name);

                Toast.makeText(CreateFamilyActivity.this, "Family Created Successfully! 🎉", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateFamilyActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

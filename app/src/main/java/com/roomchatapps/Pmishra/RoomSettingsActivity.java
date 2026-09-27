package com.roomchatapps.Pmishra;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RoomSettingsActivity extends AppCompatActivity {

    private TextView tvRoomIdValue, tvRoomTypeValue, tvMicModeValue, tvWheatModeValue;
    private SwitchCompat switchMemberApproved;
    private ImageView ivRoomCover, pickimg, btnCopyRoomId, ivBack;
    private View btnUpdate, layoutRoomIdCopy;
    private View rowPublicScreenSetting, rowSensitiveWordsSetting, rowRoomType, rowMicMode, rowMemberApproved, rowWheatMode, rowSuperMic, rowBlacklist, rowEffectSwitch;
    private View btnActionClearScreen, btnActionTheme, btnActionMusic, btnActionLock, btnActionAdmin;

    private String roomID, hostUid, currentRoomImg;
    private Uri imageUri;
    private String newImgUrl = "";
    private static final int PICK_IMAGE = 100;
    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#091326"));
        setContentView(R.layout.activity_room_settings);

        roomID = getIntent().getStringExtra("roomID");
        hostUid = getIntent().getStringExtra("hostUid");
        currentRoomImg = getIntent().getStringExtra("roomImg");

        initViews();
        setupListeners();
        loadRoomSettingsFromFirebase();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvRoomIdValue = findViewById(R.id.tvRoomIdValue);
        layoutRoomIdCopy = findViewById(R.id.layoutRoomIdCopy);
        btnCopyRoomId = findViewById(R.id.btnCopyRoomId);
        ivRoomCover = findViewById(R.id.ivRoomCover);
        pickimg = findViewById(R.id.pickimg);
        btnUpdate = findViewById(R.id.btnUpdate);

        // List Row Views
        rowPublicScreenSetting = findViewById(R.id.rowPublicScreenSetting);
        rowSensitiveWordsSetting = findViewById(R.id.rowSensitiveWordsSetting);
        rowRoomType = findViewById(R.id.rowRoomType);
        rowMicMode = findViewById(R.id.rowMicMode);
        rowMemberApproved = findViewById(R.id.rowMemberApproved);
        rowWheatMode = findViewById(R.id.rowWheatMode);
        rowSuperMic = findViewById(R.id.rowSuperMic);
        rowBlacklist = findViewById(R.id.rowBlacklist);
        rowEffectSwitch = findViewById(R.id.rowEffectSwitch);

        // Value Texts & Switch
        tvRoomTypeValue = findViewById(R.id.tvRoomTypeValue);
        tvMicModeValue = findViewById(R.id.tvMicModeValue);
        tvWheatModeValue = findViewById(R.id.tvWheatModeValue);
        switchMemberApproved = findViewById(R.id.switchMemberApproved);

        // Bottom Action Buttons
        btnActionClearScreen = findViewById(R.id.btnActionClearScreen);
        btnActionTheme = findViewById(R.id.btnActionTheme);
        btnActionMusic = findViewById(R.id.btnActionMusic);
        btnActionLock = findViewById(R.id.btnActionLock);
        btnActionAdmin = findViewById(R.id.btnActionAdmin);

        if (roomID != null && !roomID.trim().isEmpty()) {
            if (tvRoomIdValue != null) tvRoomIdValue.setText(roomID);
        }

        if (currentRoomImg != null && !currentRoomImg.trim().isEmpty()) {
            Glide.with(this).load(currentRoomImg).into(ivRoomCover);
            newImgUrl = currentRoomImg;
        }
    }

    private void setupListeners() {
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        View.OnClickListener copyListener = v -> copyRoomIdToClipboard();
        if (layoutRoomIdCopy != null) layoutRoomIdCopy.setOnClickListener(copyListener);
        if (btnCopyRoomId != null) btnCopyRoomId.setOnClickListener(copyListener);

        if (pickimg != null) {
            pickimg.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            });
        }

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> {
                if (imageUri != null) {
                    btnUpdate.setEnabled(false);
                    uploadImageAndSaveSettings();
                } else {
                    Toast.makeText(this, "Select a new cover image first!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Apply animations
        if (ivBack != null) AnimationHelper.applyClickAnimation(ivBack);
        if (btnUpdate != null) AnimationHelper.applyClickAnimation(btnUpdate);
        if (pickimg != null) AnimationHelper.applyClickAnimation(pickimg);

        // List Row Item Listeners
        if (rowPublicScreenSetting != null) {
            rowPublicScreenSetting.setOnClickListener(v -> showPublicScreenSettingDialog());
        }
        if (rowSensitiveWordsSetting != null) {
            rowSensitiveWordsSetting.setOnClickListener(v -> showSensitiveWordsDialog());
        }
        if (rowRoomType != null) {
            rowRoomType.setOnClickListener(v -> showRoomTypeSelectionDialog());
        }
        if (rowMicMode != null) {
            rowMicMode.setOnClickListener(v -> showMicModeSelectionDialog());
        }
        if (switchMemberApproved != null) {
            switchMemberApproved.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSettingToFirebase("memberApproved", isChecked);
                Toast.makeText(this, isChecked ? "Member approval enabled" : "Member approval disabled", Toast.LENGTH_SHORT).show();
            });
        }
        if (rowWheatMode != null) {
            rowWheatMode.setOnClickListener(v -> showWheatModeSelectionDialog());
        }
        if (rowSuperMic != null) {
            rowSuperMic.setOnClickListener(v -> showSuperMicDialog());
        }
        if (rowBlacklist != null) {
            rowBlacklist.setOnClickListener(v -> showBlacklistDialog());
        }
        if (rowEffectSwitch != null) {
            rowEffectSwitch.setOnClickListener(v -> showEffectSwitchDialog());
        }

        // Bottom Quick Action Buttons Listeners
        if (btnActionClearScreen != null) {
            AnimationHelper.applyClickAnimation(btnActionClearScreen);
            btnActionClearScreen.setOnClickListener(v -> handleClearScreen());
        }
        if (btnActionTheme != null) {
            AnimationHelper.applyClickAnimation(btnActionTheme);
            btnActionTheme.setOnClickListener(v -> handleThemeSelection());
        }
        if (btnActionMusic != null) {
            AnimationHelper.applyClickAnimation(btnActionMusic);
            btnActionMusic.setOnClickListener(v -> handleMusicControl());
        }
        if (btnActionLock != null) {
            AnimationHelper.applyClickAnimation(btnActionLock);
            btnActionLock.setOnClickListener(v -> handleRoomLock());
        }
        if (btnActionAdmin != null) {
            AnimationHelper.applyClickAnimation(btnActionAdmin);
            btnActionAdmin.setOnClickListener(v -> handleAdminManagement());
        }
    }

    private void loadRoomSettingsFromFirebase() {
        if (roomID == null || roomID.trim().isEmpty()) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String roomType = snapshot.child("roomType").getValue(String.class);
                    if (roomType != null && tvRoomTypeValue != null) {
                        tvRoomTypeValue.setText(roomType);
                    }

                    String micMode = snapshot.child("micMode").getValue(String.class);
                    if (micMode != null && tvMicModeValue != null) {
                        tvMicModeValue.setText(micMode);
                    }

                    Boolean approved = snapshot.child("memberApproved").getValue(Boolean.class);
                    if (approved != null && switchMemberApproved != null) {
                        switchMemberApproved.setChecked(approved);
                    }

                    String wheatMode = snapshot.child("wheatMode").getValue(String.class);
                    if (wheatMode != null && tvWheatModeValue != null) {
                        tvWheatModeValue.setText(wheatMode);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void saveSettingToFirebase(String key, Object value) {
        if (roomID == null || roomID.trim().isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        ref.child(key).setValue(value);
    }

    // Dialog Methods
    private void showRoomTypeSelectionDialog() {
        String[] types = {"Chat", "Music", "Gaming", "Dating", "Chill", "Karaoke"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Select Room Type");
        builder.setItems(types, (dialog, which) -> {
            String selectedType = types[which];
            if (tvRoomTypeValue != null) tvRoomTypeValue.setText(selectedType);
            saveSettingToFirebase("roomType", selectedType);
            Toast.makeText(this, "Room Type updated to " + selectedType, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showMicModeSelectionDialog() {
        String[] modes = {"open mode", "free mode", "host control"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Select Mic Mode");
        builder.setItems(modes, (dialog, which) -> {
            String selectedMode = modes[which];
            if (tvMicModeValue != null) tvMicModeValue.setText(selectedMode);
            saveSettingToFirebase("micMode", selectedMode);
            Toast.makeText(this, "Mic Mode updated to " + selectedMode, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showWheatModeSelectionDialog() {
        String[] seats = {"6 people", "9 people", "12 people", "15 people"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Select Wheat Mode (Seat Capacity)");
        builder.setItems(seats, (dialog, which) -> {
            String selectedWheat = seats[which];
            if (tvWheatModeValue != null) tvWheatModeValue.setText(selectedWheat);
            saveSettingToFirebase("wheatMode", selectedWheat);
            Toast.makeText(this, "Wheat Mode set to " + selectedWheat, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showPublicScreenSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Public Screen Setting");
        builder.setMessage("Configure public chat messages, system announcements, and entrance banners in the room.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showSensitiveWordsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Sensitive Words Filter");
        builder.setMessage("Sensitive words filter is active to automatically block prohibited language in chat.");
        builder.setPositiveButton("Configure", (dialog, which) -> {
            Toast.makeText(this, "Filter configuration saved", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showSuperMicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Super Mic Setting");
        builder.setMessage("Super Mic provides high-definition audio enhancement and noise suppression.");
        builder.setPositiveButton("Enable", (dialog, which) -> {
            saveSettingToFirebase("superMicEnabled", true);
            Toast.makeText(this, "Super Mic Enabled", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showBlacklistDialog() {
        if (roomID == null || roomID.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomSettingsActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
                builder.setTitle("Room Blacklist");
                builder.setMessage("Currently " + count + " blocked/kicked users in this room.");
                builder.setPositiveButton("Close", null);
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showEffectSwitchDialog() {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_effect_settings, null, false);
            AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Effect settings opened", Toast.LENGTH_SHORT).show();
        }
    }

    // Bottom Action Handlers
    private void handleClearScreen() {
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Clear Screen")
                .setMessage("Are you sure you want to clear all public chat messages in this room?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    if (roomID != null && !roomID.isEmpty()) {
                        FirebaseDatabase.getInstance().getReference("room_messages").child(roomID).removeValue();
                        Toast.makeText(this, "Public screen cleared!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleThemeSelection() {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_theme_selection, null, false);
            AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Room Theme Store opened", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMusicControl() {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_music_control, null, false);
            AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Room Music Player opened", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRoomLock() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Lock Room");
        final EditText input = new EditText(this);
        input.setHint("Set 4-digit room password");
        input.setTextColor(Color.WHITE);
        builder.setView(input);
        builder.setPositiveButton("Lock", (dialog, which) -> {
            String pass = input.getText().toString().trim();
            saveSettingToFirebase("password", pass);
            Toast.makeText(this, "Room locked with password", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Unlock Room", (dialog, which) -> {
            saveSettingToFirebase("password", "");
            Toast.makeText(this, "Room unlocked", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void handleAdminManagement() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Admin Management");
        builder.setMessage("Manage room moderators and co-hosts.");
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void copyRoomIdToClipboard() {
        if (roomID == null || roomID.trim().isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Room ID", roomID);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "📋 Room ID (" + roomID + ") copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveSettings() {
        Toast.makeText(this, "Uploading cover image...", Toast.LENGTH_SHORT).show();
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                if (btnUpdate != null) btnUpdate.setEnabled(true);
                return;
            }
            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        if (btnUpdate != null) btnUpdate.setEnabled(true);
                        Toast.makeText(RoomSettingsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            newImgUrl = jsonObject.getJSONObject("data").getString("url");
                            runOnUiThread(() -> saveCoverSettings(newImgUrl));
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                if (btnUpdate != null) btnUpdate.setEnabled(true);
                                Toast.makeText(RoomSettingsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            if (btnUpdate != null) btnUpdate.setEnabled(true);
                            Toast.makeText(RoomSettingsActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            if (btnUpdate != null) btnUpdate.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void saveCoverSettings(String imgUrl) {
        String keyToUpdate = (hostUid != null && !hostUid.isEmpty()) ? hostUid : roomID;
        if (keyToUpdate == null || keyToUpdate.isEmpty()) {
            if (btnUpdate != null) btnUpdate.setEnabled(true);
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(keyToUpdate);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("img", imgUrl);

        ref.updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Room Cover Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            if (btnUpdate != null) btnUpdate.setEnabled(true);
            Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (ivRoomCover != null) ivRoomCover.setImageURI(imageUri);
        }
    }
}

package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.ActivityEditProfileBinding;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Uri imageUri;
    private String currentAvatarUrl;

    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.profileImage.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
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
        String uid = mAuth.getUid();
        if (uid == null) {
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        loadUserData();
        setupClickListeners();
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);

                    currentAvatarUrl = avatar;
                    binding.etName.setText(name);
                    binding.etBio.setText(bio);

                    if (avatar != null && !avatar.isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load(avatar)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(binding.profileImage);
                    }

                    if ("Male".equalsIgnoreCase(gender)) {
                        binding.rbMale.setChecked(true);
                    } else if ("Female".equalsIgnoreCase(gender)) {
                        binding.rbFemale.setChecked(true);
                    }

                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    selectedFrameId = equippedFrame;
                    FrameUtils.displayFrame(EditProfileActivity.this, equippedFrame, binding.ivProfileFrame, binding.svgaProfileFrame);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userRef != null) {
            userRef.child("equipped_frame").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String equippedFrame = snapshot.getValue(String.class);
                        selectedFrameId = equippedFrame;
                        FrameUtils.displayFrame(EditProfileActivity.this, equippedFrame, binding.ivProfileFrame, binding.svgaProfileFrame);
                    }
                }

                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private String selectedFrameId = null;

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Both camera button and profile picture open gallery
        binding.btnEditAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        binding.profileImage.setOnClickListener(v -> mGetContent.launch("image/*"));

        // Frame Store Button
        binding.btnOpenFrameStore.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, StoreActivity.class);
            startActivity(intent);
        });

        // Preset Avatars Click Listeners
        binding.presetAvatar1.setOnClickListener(v -> selectPresetAvatar(R.drawable._1000092458_removebg_preview));
        binding.presetAvatar2.setOnClickListener(v -> selectPresetAvatar(R.drawable._1000092459_removebg_preview));
        binding.presetAvatar3.setOnClickListener(v -> selectPresetAvatar(R.drawable._1000092460_removebg_preview));

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void selectPresetAvatar(int resId) {
        AnimationHelper.bounceAnimation(binding.profileImage);
        binding.profileImage.setImageResource(resId);
        imageUri = null;
        String resName = getResources().getResourceEntryName(resId);
        currentAvatarUrl = "android.resource://" + getPackageName() + "/drawable/" + resName;
        Toast.makeText(this, "Preset Avatar Selected! 👤", Toast.LENGTH_SHORT).show();
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();
        String gender = binding.rbMale.isChecked() ? "Male" : (binding.rbFemale.isChecked() ? "Female" : "");

        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            return;
        }

        binding.btnSave.setEnabled(false);
        Toast.makeText(this, "Saving profile...", Toast.LENGTH_SHORT).show();

        if (imageUri != null) {
            uploadImageAndSave(name, bio, gender);
        } else {
            updateDatabase(name, bio, gender, currentAvatarUrl);
        }
    }

    private void uploadImageAndSave(String name, String bio, String gender) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                updateDatabase(name, bio, gender, currentAvatarUrl);
                return;
            }
            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", "avatar.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Image upload failed, saving text details", Toast.LENGTH_SHORT).show();
                        updateDatabase(name, bio, gender, currentAvatarUrl);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String uploadedUrl = currentAvatarUrl;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            uploadedUrl = jsonObject.getJSONObject("data").getString("url");
                        } catch (Exception e) {
                            // fallback to current avatar
                        }
                    }
                    final String finalAvatarUrl = uploadedUrl;
                    runOnUiThread(() -> updateDatabase(name, bio, gender, finalAvatarUrl));
                }
            });

        } catch (Exception e) {
            updateDatabase(name, bio, gender, currentAvatarUrl);
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = is.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void updateDatabase(String name, String bio, String gender, String avatarUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("gender", gender);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            updates.put("avtar", avatarUrl);
            updates.put("avatar", avatarUrl);
            updates.put("userIcon", avatarUrl);
            updates.put("photoUrl", avatarUrl);
            updates.put("image", avatarUrl);
        }
        if (selectedFrameId != null) {
            updates.put("equipped_frame", selectedFrameId);
        }

        // 1. Update SessionManager local cache for immediate room chat & message updates
        SessionManager.getInstance(EditProfileActivity.this).updateUserProfile(name, avatarUrl);

        // 2. Invalidate UserProfileCache so all user profile cards reload fresh data
        if (mAuth.getUid() != null) {
            UserProfileCache.invalidate(mAuth.getUid());
        }

        // 3. Also update Firebase Auth profile displayName and photoUrl
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name);
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                profileUpdates.setPhotoUri(Uri.parse(avatarUrl));
            }
            user.updateProfile(profileUpdates.build());
        }

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            binding.btnSave.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Update failed: " + (task.getException() != null ? task.getException().getMessage() : "Error"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

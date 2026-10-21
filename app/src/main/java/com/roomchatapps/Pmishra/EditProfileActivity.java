package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.roomchatapps.Pmishra.dialogs.ImageCropDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.roomchatapps.Pmishra.adapters.GiftGridAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.HorizontalCollectionAdapter;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.StoreManager;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final List<CollectionItemModel> frameList = new ArrayList<>();
    private final List<CollectionItemModel> vehicleList = new ArrayList<>();
    private final List<CollectionItemModel> giftList = new ArrayList<>();

    private HorizontalCollectionAdapter frameAdapter;
    private HorizontalCollectionAdapter vehicleAdapter;
    private GiftGridAdapter giftAdapter;

    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageCropDialog cropDialog = new ImageCropDialog(EditProfileActivity.this, uri, (croppedBitmap, croppedUri) -> {
                        if (croppedUri != null) {
                            imageUri = croppedUri;
                            binding.profileImage.setImageURI(croppedUri);
                        } else if (croppedBitmap != null) {
                            try {
                                File file = new File(getCacheDir(), "cropped_avatar_" + System.currentTimeMillis() + ".jpg");
                                try (FileOutputStream out = new FileOutputStream(file)) {
                                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out);
                                    imageUri = Uri.fromFile(file);
                                    binding.profileImage.setImageURI(imageUri);
                                }
                            } catch (Exception e) {
                                binding.profileImage.setImageBitmap(croppedBitmap);
                            }
                        }
                    });
                    cropDialog.show();
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
        setupRecyclerViews();
        setupClickListeners();
        loadCollectionSections(uid);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (avatar == null || avatar.trim().isEmpty()) avatar = snapshot.child("avatar").getValue(String.class);
                    if (avatar == null || avatar.trim().isEmpty()) avatar = snapshot.child("photoUrl").getValue(String.class);
                    if (avatar == null || avatar.trim().isEmpty()) avatar = snapshot.child("image").getValue(String.class);
                    if (avatar == null || avatar.trim().isEmpty()) avatar = snapshot.child("userIcon").getValue(String.class);
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
        String uid = mAuth != null ? mAuth.getUid() : null;
        if (uid != null && userRef != null) {
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
            loadCollectionSections(uid);
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

        if (binding.btnViewAllFrames != null) {
            binding.btnViewAllFrames.setOnClickListener(v -> startActivity(new Intent(EditProfileActivity.this, StoreActivity.class)));
        }
        if (binding.btnViewAllVehicles != null) {
            binding.btnViewAllVehicles.setOnClickListener(v -> startActivity(new Intent(EditProfileActivity.this, StoreActivity.class)));
        }

        // Preset Avatars Click Listeners
        binding.presetAvatar1.setOnClickListener(v -> selectPresetAvatar(R.drawable._1000092458_removebg_preview));
        binding.presetAvatar2.setOnClickListener(v -> selectPresetAvatar(R.drawable._1000092459_removebg_preview));
        binding.presetAvatar3.setOnClickListener(v -> selectPresetAvatar(R.drawable._1000092460_removebg_preview));

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupRecyclerViews() {
        if (binding.rvFrames != null) {
            binding.rvFrames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            frameAdapter = new HorizontalCollectionAdapter(frameList, item -> {
                if (item != null && item.getStoreItem() != null) {
                    StoreItemModel storeItem = item.getStoreItem();
                    if (storeItem.isEquipped()) {
                        Toast.makeText(this, storeItem.getName() + " is already equipped ✨", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Equipping " + storeItem.getName() + "...", Toast.LENGTH_SHORT).show();
                        String uid = mAuth != null ? mAuth.getUid() : null;
                        StoreManager.equipItem(uid, storeItem, new StoreManager.ActionCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                                selectedFrameId = storeItem.getId();
                                FrameUtils.displayFrame(EditProfileActivity.this, storeItem.getId(), binding.ivProfileFrame, binding.svgaProfileFrame);
                                loadCollectionSections(uid);
                            }
                            @Override
                            public void onError(String err) {
                                Toast.makeText(EditProfileActivity.this, err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            binding.rvFrames.setAdapter(frameAdapter);
        }

        if (binding.rvVehicles != null) {
            binding.rvVehicles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            vehicleAdapter = new HorizontalCollectionAdapter(vehicleList, item -> {
                if (item != null && item.getStoreItem() != null) {
                    StoreItemModel storeItem = item.getStoreItem();
                    if (storeItem.isEquipped()) {
                        Toast.makeText(this, storeItem.getName() + " is already equipped 🚗", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Equipping " + storeItem.getName() + "...", Toast.LENGTH_SHORT).show();
                        String uid = mAuth != null ? mAuth.getUid() : null;
                        StoreManager.equipItem(uid, storeItem, new StoreManager.ActionCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                                loadCollectionSections(uid);
                            }
                            @Override
                            public void onError(String err) {
                                Toast.makeText(EditProfileActivity.this, err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            binding.rvVehicles.setAdapter(vehicleAdapter);
        }

        if (binding.rvGiftsReceived != null) {
            binding.rvGiftsReceived.setLayoutManager(new GridLayoutManager(this, 4));
            binding.rvGiftsReceived.setNestedScrollingEnabled(false);
            giftAdapter = new GiftGridAdapter(giftList, item ->
                    Toast.makeText(this, item.getItemName() + " (Received x" + item.getReceivedCount() + ")", Toast.LENGTH_SHORT).show());
            binding.rvGiftsReceived.setAdapter(giftAdapter);
        }
    }

    private void loadCollectionSections(String uid) {
        if (uid == null) return;

        StoreManager.getStoreCatalog(uid, "ALL", new StoreManager.CatalogCallback() {
            @Override
            public void onCatalogLoaded(List<StoreItemModel> catalog) {
                frameList.clear();
                vehicleList.clear();

                for (StoreItemModel item : catalog) {
                    if (item.isOwned()) {
                        if ("ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                            vehicleList.add(new CollectionItemModel(CollectionItemModel.ItemType.ENTRY_EFFECT, item));
                        } else if ("FRAME".equalsIgnoreCase(item.getCategory())) {
                            frameList.add(new CollectionItemModel(CollectionItemModel.ItemType.FRAME, item));
                        }
                    }
                }

                if (frameAdapter != null) frameAdapter.notifyDataSetChanged();
                if (vehicleAdapter != null) vehicleAdapter.notifyDataSetChanged();

                loadReceivedGifts(uid);
            }

            @Override
            public void onError(String error) {
                loadReceivedGifts(uid);
            }
        });
    }

    private void loadReceivedGifts(String uid) {
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
        DatabaseReference userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("received_gifts");

        txRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> giftCountMap = new HashMap<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String type = ds.child("type").getValue(String.class);
                        if (type == null) type = "";

                        String giftName = ds.child("giftName").getValue(String.class);
                        if (giftName == null || giftName.trim().isEmpty()) {
                            String title = ds.child("title").getValue(String.class);
                            if (title != null && title.startsWith("Received Gift: ")) {
                                giftName = title.substring("Received Gift: ".length()).trim();
                            }
                        }
                        if ("GIFT_RECEIVED".equalsIgnoreCase(type) || "RECEIVED_GIFT".equalsIgnoreCase(type)) {
                            if (giftName != null && !giftName.trim().isEmpty()) {
                                String cleanName = giftName.replaceAll("\\s*\\(.*?\\)", "").trim();
                                int qty = 1;
                                Long qObj = ds.child("quantity").getValue(Long.class);
                                if (qObj != null && qObj > 0) qty = qObj.intValue();

                                int currentCount = giftCountMap.containsKey(cleanName) ? giftCountMap.get(cleanName) : 0;
                                giftCountMap.put(cleanName, currentCount + qty);
                            }
                        }
                    }
                }

                userGiftsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot giftsSnapshot) {
                        if (giftsSnapshot.exists()) {
                            for (DataSnapshot ds : giftsSnapshot.getChildren()) {
                                String gKey = ds.getKey();
                                Object val = ds.getValue();
                                long countVal = 0;
                                if (val instanceof Long) countVal = (Long) val;
                                else if (val instanceof Integer) countVal = (Integer) val;
                                else if (val instanceof String) {
                                    try { countVal = Long.parseLong((String) val); } catch (Exception ignored) {}
                                }

                                if (gKey != null && countVal > 0) {
                                    String cleanKey = gKey.replaceAll("\\s*\\(.*?\\)", "").trim();
                                    int existing = giftCountMap.containsKey(cleanKey) ? giftCountMap.get(cleanKey) : 0;
                                    giftCountMap.put(cleanKey, Math.max(existing, (int) countVal));
                                }
                            }
                        }

                        bindGifts(giftCountMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        bindGifts(giftCountMap);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bindGifts(new HashMap<>());
            }
        });
    }

    private void bindGifts(Map<String, Integer> giftCountMap) {
        giftList.clear();
        int totalGifts = 0;

        for (Map.Entry<String, Integer> entry : giftCountMap.entrySet()) {
            String giftName = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                totalGifts += count;
                GiftStoreAdapter.GiftStoreItem catalogGift = GiftCatalog.findGiftByName(giftName);
                if (catalogGift != null) {
                    giftList.add(new CollectionItemModel(catalogGift, count));
                } else {
                    giftList.add(new CollectionItemModel(giftName, R.drawable.gift_icon, 100000L, count));
                }
            }
        }

        if (giftAdapter != null) giftAdapter.notifyDataSetChanged();
        if (binding.tvGiftCount != null) {
            binding.tvGiftCount.setText(totalGifts + " Items");
        }
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

            // Base64 data URI fallback ensures new cropped image is NEVER lost even if ImgBB fails
            String base64Fallback = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);

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
                        updateDatabase(name, bio, gender, base64Fallback);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String uploadedUrl = null;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            uploadedUrl = jsonObject.getJSONObject("data").getString("url");
                        } catch (Exception ignored) {}
                    }
                    final String finalAvatarUrl = (uploadedUrl != null && !uploadedUrl.trim().isEmpty()) ? uploadedUrl : base64Fallback;
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

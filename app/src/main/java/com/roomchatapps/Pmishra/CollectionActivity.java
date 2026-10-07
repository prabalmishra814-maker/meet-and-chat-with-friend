package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.GiftGridAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.HorizontalCollectionAdapter;
import com.roomchatapps.Pmishra.dialogs.ImageCropDialog;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;
import com.roomchatapps.Pmishra.utils.StoreManager;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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

public class CollectionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, btnMoreOptions;
    private View tabProfile, tabIntimacy;
    private TextView tvTabProfile, tvTabIntimacy;
    private View indicatorProfile, indicatorIntimacy;
    private RecyclerView rvPhotos, rvFrames, rvVehicles, rvGiftsReceived, rvCollectionHall;
    private TextView tvCollectionHallCount;
    private ProgressBar progressBar;

    private View llProfileContent, llIntimacyContent;
    private TextView tvGenderAge, tvUserMeta, tvUserLevel;
    private View btnGoLoveStore, btnAddCouplePartner;
    private ImageView ivUserProfileAvatar, ivCoupleLeftAvatar, ivCoupleRightAvatar, ivUserProfileFrame;
    private com.opensource.svgaplayer.SVGAImageView svgaUserProfileFrame;
    private TextView tvCoupleLeftName, tvCoupleRightName;

    private String currentUid;
    private String currentUserName;

    private final List<CollectionItemModel> photoList = new ArrayList<>();
    private final List<CollectionItemModel> frameList = new ArrayList<>();
    private final List<CollectionItemModel> vehicleList = new ArrayList<>();
    private final List<CollectionItemModel> giftList = new ArrayList<>();
    private final List<CollectionItemModel> collectionHallList = new ArrayList<>();

    private HorizontalCollectionAdapter photoAdapter;
    private HorizontalCollectionAdapter frameAdapter;
    private HorizontalCollectionAdapter vehicleAdapter;
    private HorizontalCollectionAdapter collectionHallAdapter;
    private GiftGridAdapter giftAdapter;

    private DatabaseReference txRef;
    private ValueEventListener txListener;
    private DatabaseReference userGiftsRef;
    private ValueEventListener userGiftsListener;

    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageCropDialog cropDialog = new ImageCropDialog(CollectionActivity.this, uri, (croppedBitmap, croppedUri) -> {
                        Uri targetUri = croppedUri != null ? croppedUri : uri;
                        uploadPhotoAndSave(targetUri);
                    });
                    cropDialog.show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_collection);

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
        currentUserName = SessionManager.getInstance(this).getName();

        initViews();
        setupRecyclerViews();
        setupTabs();
        setupClickListeners();

        if (currentUid != null) {
            loadCollectionData();
        } else {
            Toast.makeText(this, "Please login to view your collection", Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        tabProfile = findViewById(R.id.tabProfile);
        tabIntimacy = findViewById(R.id.tabIntimacy);
        tvTabProfile = findViewById(R.id.tvTabProfile);
        tvTabIntimacy = findViewById(R.id.tvTabIntimacy);
        indicatorProfile = findViewById(R.id.indicatorProfile);
        indicatorIntimacy = findViewById(R.id.indicatorIntimacy);
        rvPhotos = findViewById(R.id.rvPhotos);
        rvFrames = findViewById(R.id.rvFrames);
        rvVehicles = findViewById(R.id.rvVehicles);
        rvGiftsReceived = findViewById(R.id.rvGiftsReceived);
        rvCollectionHall = findViewById(R.id.rvCollectionHall);
        tvCollectionHallCount = findViewById(R.id.tvCollectionHallCount);
        progressBar = findViewById(R.id.progressBar);

        llProfileContent = findViewById(R.id.llProfileContent);
        llIntimacyContent = findViewById(R.id.llIntimacyContent);
        ivUserProfileAvatar = findViewById(R.id.ivUserProfileAvatar);
        ivUserProfileFrame = findViewById(R.id.ivUserProfileFrame);
        svgaUserProfileFrame = findViewById(R.id.svgaUserProfileFrame);
        tvGenderAge = findViewById(R.id.tvGenderAge);
        tvUserMeta = findViewById(R.id.tvUserMeta);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        btnGoLoveStore = findViewById(R.id.btnGoLoveStore);
        btnAddCouplePartner = findViewById(R.id.btnAddCouplePartner);
        ivCoupleLeftAvatar = findViewById(R.id.ivCoupleLeftAvatar);
        ivCoupleRightAvatar = findViewById(R.id.ivCoupleRightAvatar);
        tvCoupleLeftName = findViewById(R.id.tvCoupleLeftName);
        tvCoupleRightName = findViewById(R.id.tvCoupleRightName);

        if (tvTitle != null) {
            tvTitle.setText(currentUserName != null && !currentUserName.isEmpty() ? currentUserName : "My Collection");
        }
    }

    private void setupRecyclerViews() {
        if (rvPhotos != null) {
            rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            photoAdapter = new HorizontalCollectionAdapter(photoList, item -> {
                if (item.isAddButton()) {
                    mGetContent.launch("image/*");
                } else if (item.getPhotoUrl() != null) {
                    showPhotoOptionsDialog(item);
                }
            });
            rvPhotos.setAdapter(photoAdapter);
        }

        if (rvCollectionHall != null) {
            rvCollectionHall.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            collectionHallAdapter = new HorizontalCollectionAdapter(collectionHallList, item ->
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show());
            rvCollectionHall.setAdapter(collectionHallAdapter);
        }

        if (rvFrames != null) {
            rvFrames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            frameAdapter = new HorizontalCollectionAdapter(frameList, item -> {
                if (item != null && item.getStoreItem() != null) {
                    StoreItemModel storeItem = item.getStoreItem();
                    if (storeItem.isEquipped()) {
                        Toast.makeText(this, storeItem.getName() + " is equipped ✨", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Equipping " + storeItem.getName() + "...", Toast.LENGTH_SHORT).show();
                        StoreManager.equipItem(currentUid, storeItem, new StoreManager.ActionCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                Toast.makeText(CollectionActivity.this, msg, Toast.LENGTH_SHORT).show();
                                loadCollectionData();
                            }
                            @Override
                            public void onError(String err) {
                                Toast.makeText(CollectionActivity.this, err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else if (item != null) {
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show();
                }
            });
            rvFrames.setAdapter(frameAdapter);
        }

        if (rvVehicles != null) {
            rvVehicles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            vehicleAdapter = new HorizontalCollectionAdapter(vehicleList, item -> {
                if (item != null && item.getStoreItem() != null) {
                    StoreItemModel storeItem = item.getStoreItem();
                    if (storeItem.isEquipped()) {
                        Toast.makeText(this, storeItem.getName() + " is equipped 🚗", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Equipping " + storeItem.getName() + "...", Toast.LENGTH_SHORT).show();
                        StoreManager.equipItem(currentUid, storeItem, new StoreManager.ActionCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                Toast.makeText(CollectionActivity.this, msg, Toast.LENGTH_SHORT).show();
                                loadCollectionData();
                            }
                            @Override
                            public void onError(String err) {
                                Toast.makeText(CollectionActivity.this, err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else if (item != null) {
                    Toast.makeText(this, item.getItemName(), Toast.LENGTH_SHORT).show();
                }
            });
            rvVehicles.setAdapter(vehicleAdapter);
        }

        if (rvGiftsReceived != null) {
            rvGiftsReceived.setLayoutManager(new GridLayoutManager(this, 4));
            rvGiftsReceived.setNestedScrollingEnabled(false);
            giftAdapter = new GiftGridAdapter(giftList, item ->
                    Toast.makeText(this, item.getItemName() + " (Received x" + item.getReceivedCount() + ")", Toast.LENGTH_SHORT).show());
            rvGiftsReceived.setAdapter(giftAdapter);
        }
    }

    private void showPhotoOptionsDialog(CollectionItemModel item) {
        if (item == null || item.getPhotoUrl() == null) return;

        String[] options = item.isCurrentAvatar()
                ? new String[]{"View Full Photo", "Delete Photo"}
                : new String[]{"Set as Active Profile Picture", "View Full Photo", "Delete Photo"};

        new AlertDialog.Builder(this)
                .setTitle("Profile Photo Options")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (selected.equals("Set as Active Profile Picture")) {
                        savePhotoToFirebase(item.getPhotoUrl());
                    } else if (selected.equals("View Full Photo")) {
                        Toast.makeText(this, "Profile Photo", Toast.LENGTH_SHORT).show();
                    } else if (selected.equals("Delete Photo")) {
                        deletePhotoFromFirebase(item);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePhotoFromFirebase(CollectionItemModel item) {
        if (currentUid == null || item == null || item.getPhotoKey() == null) return;
        DatabaseReference pRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUid).child("photos").child(item.getPhotoKey());

        pRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CollectionActivity.this, "Photo deleted.", Toast.LENGTH_SHORT).show();
                loadCollectionData();
            }
        });
    }

    private void setupTabs() {
        if (tabProfile != null) {
            tabProfile.setOnClickListener(v -> selectTab(true));
        }
        if (tabIntimacy != null) {
            tabIntimacy.setOnClickListener(v -> selectTab(false));
        }
    }

    private void selectTab(boolean isProfile) {
        if (tvTabProfile != null) tvTabProfile.setTextColor(isProfile ? Color.WHITE : Color.parseColor("#70FFFFFF"));
        if (tvTabIntimacy != null) tvTabIntimacy.setTextColor(isProfile ? Color.parseColor("#70FFFFFF") : Color.WHITE);
        if (indicatorProfile != null) indicatorProfile.setBackgroundColor(isProfile ? Color.parseColor("#00FFC6") : Color.TRANSPARENT);
        if (indicatorIntimacy != null) indicatorIntimacy.setBackgroundColor(isProfile ? Color.TRANSPARENT : Color.parseColor("#00FFC6"));

        if (llProfileContent != null) llProfileContent.setVisibility(isProfile ? View.VISIBLE : View.GONE);
        if (llIntimacyContent != null) llIntimacyContent.setVisibility(isProfile ? View.GONE : View.VISIBLE);
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnGoLoveStore != null) {
            btnGoLoveStore.setOnClickListener(v -> {
                Intent intent = new Intent(CollectionActivity.this, StoreActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddCouplePartner != null) {
            btnAddCouplePartner.setOnClickListener(v ->
                    Toast.makeText(this, "Select a friend to invite as Couple partner 💖", Toast.LENGTH_SHORT).show());
        }
    }

    private void uploadPhotoAndSave(Uri uri) {
        if (currentUid == null || uri == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Uploading profile photo...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                return;
            }
            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            String base64Fallback = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", "photo.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> savePhotoToFirebase(base64Fallback));
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
                    final String finalUrl = (uploadedUrl != null && !uploadedUrl.trim().isEmpty()) ? uploadedUrl : base64Fallback;
                    runOnUiThread(() -> savePhotoToFirebase(finalUrl));
                }
            });

        } catch (Exception e) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to read photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePhotoToFirebase(String photoUrl) {
        if (currentUid == null || photoUrl == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        DatabaseReference photosRef = userRef.child("photos");
        String photoKey = photosRef.push().getKey();

        Map<String, Object> updates = new HashMap<>();
        if (photoKey != null) {
            updates.put("photos/" + photoKey, photoUrl);
        }
        updates.put("avtar", photoUrl);
        updates.put("avatar", photoUrl);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                SessionManager.getInstance(CollectionActivity.this).updateUserProfile(null, photoUrl);
                UserProfileCache.invalidate(currentUid);
                Toast.makeText(CollectionActivity.this, "Profile photo updated! 📸", Toast.LENGTH_SHORT).show();
                loadCollectionData();
            } else {
                Toast.makeText(CollectionActivity.this, "Failed to save photo.", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void loadCollectionData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Fetch User Profile info
        if (currentUid != null) {
            UserProfileCache.getUserProfile(currentUid, profile -> {
                if (isFinishing() || isDestroyed()) return;
                if (profile != null) {
                    if (tvTitle != null && profile.name != null && !profile.name.isEmpty()) {
                        tvTitle.setText(profile.name);
                    }
                    if (tvUserMeta != null) {
                        tvUserMeta.setText("ID: " + currentUid + "  |  1 followers  |  📍 India");
                    }
                    if (tvCoupleLeftName != null && profile.name != null) {
                        tvCoupleLeftName.setText(profile.name);
                    }
                    if (ivCoupleLeftAvatar != null && profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                        Glide.with(CollectionActivity.this).load(profile.avatarUrl).placeholder(R.drawable.logo_placeholder).into(ivCoupleLeftAvatar);
                    }
                    if (ivUserProfileAvatar != null && profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                        Glide.with(CollectionActivity.this).load(profile.avatarUrl).placeholder(R.drawable.logo_placeholder).into(ivUserProfileAvatar);
                    }
                    if (profile.equippedFrame != null && !profile.equippedFrame.isEmpty()) {
                        FrameUtils.displayFrame(CollectionActivity.this, profile.equippedFrame, ivUserProfileFrame, svgaUserProfileFrame);
                    }
                }
            });
        }

        // Load Photos from Firebase
        if (currentUid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
            userRef.child("photos").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    photoList.clear();
                    photoList.add(CollectionItemModel.createAddPhotoButton());

                    String currentAvatar = SessionManager.getInstance(CollectionActivity.this).getAvatar();
                    List<String> loadedUrls = new ArrayList<>();

                    if (snapshot.exists()) {
                        for (DataSnapshot pSnap : snapshot.getChildren()) {
                            String pKey = pSnap.getKey();
                            String pUrl = pSnap.getValue(String.class);
                            if (pUrl != null && !pUrl.trim().isEmpty()) {
                                loadedUrls.add(pUrl);
                                boolean isActive = pUrl.equalsIgnoreCase(currentAvatar);
                                photoList.add(new CollectionItemModel(pKey, pUrl, isActive));
                            }
                        }
                    }

                    if (currentAvatar != null && !currentAvatar.trim().isEmpty() && !loadedUrls.contains(currentAvatar)) {
                        photoList.add(new CollectionItemModel("current_avatar", currentAvatar, true));
                    }

                    if (photoAdapter != null) photoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Step 1: Fetch Store Catalog for Frames & Vehicles
        StoreManager.getStoreCatalog(currentUid, "ALL", new StoreManager.CatalogCallback() {
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

                // Step 2: Fetch Received Gifts
                loadReceivedGifts();
            }

            @Override
            public void onError(String error) {
                loadReceivedGifts();
            }
        });
    }

    private void loadReceivedGifts() {
        txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(currentUid);
        userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid).child("received_gifts");

        txListener = new ValueEventListener() {
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
                            } else if (title != null && title.startsWith("Earned Energy: ")) {
                                giftName = title.substring("Earned Energy: ".length()).trim();
                            } else if (title != null && title.startsWith("Gift: ")) {
                                giftName = title.substring("Gift: ".length()).trim();
                            }
                        }
                        if (giftName == null || giftName.trim().isEmpty()) {
                            String desc = ds.child("description").getValue(String.class);
                            if (desc != null && desc.contains("Received ") && desc.contains(" from ")) {
                                int start = desc.indexOf("Received ") + "Received ".length();
                                int end = desc.indexOf(" from ");
                                if (start < end) giftName = desc.substring(start, end).trim();
                            } else if (desc != null && desc.contains("Earned Energy (Targeted): ")) {
                                int start = desc.indexOf("Earned Energy (Targeted): ") + "Earned Energy (Targeted): ".length();
                                int end = desc.contains(" (") ? desc.indexOf(" (") : desc.length();
                                if (start < end) giftName = desc.substring(start, end).trim();
                            }
                        }

                        if ("GIFT_RECEIVED".equalsIgnoreCase(type) || "RECEIVED_GIFT".equalsIgnoreCase(type) || (giftName != null && !giftName.trim().isEmpty() && !type.equalsIgnoreCase("GIFT_SENT"))) {
                            if (giftName != null && !giftName.trim().isEmpty()) {
                                String cleanName = giftName.replaceAll("\\s*\\(.*?\\)", "").trim();
                                int qty = 1;
                                Long qObj = ds.child("quantity").getValue(Long.class);
                                if (qObj == null || qObj <= 0) qObj = ds.child("count").getValue(Long.class);
                                if (qObj != null && qObj > 0) qty = qObj.intValue();

                                int currentCount = giftCountMap.containsKey(cleanName) ? giftCountMap.get(cleanName) : 0;
                                giftCountMap.put(cleanName, currentCount + qty);
                            }
                        }
                    }
                }

                userGiftsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot giftsSnapshot) {
                        if (giftsSnapshot.exists()) {
                            for (DataSnapshot ds : giftsSnapshot.getChildren()) {
                                String gKey = ds.getKey();
                                Object val = ds.getValue();
                                long countVal = 0;
                                if (val instanceof Long) {
                                    countVal = (Long) val;
                                } else if (val instanceof Integer) {
                                    countVal = (Integer) val;
                                } else if (val instanceof String) {
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
                };

                userGiftsRef.addListenerForSingleValueEvent(userGiftsListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bindGifts(new HashMap<>());
            }
        };

        txRef.addValueEventListener(txListener);
    }

    private void bindGifts(Map<String, Integer> giftCountMap) {
        giftList.clear();
        int totalItemsCount = frameList.size() + vehicleList.size();

        for (Map.Entry<String, Integer> entry : giftCountMap.entrySet()) {
            String giftName = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                totalItemsCount += count;
                GiftStoreAdapter.GiftStoreItem catalogGift = GiftCatalog.findGiftByName(giftName);
                if (catalogGift != null) {
                    giftList.add(new CollectionItemModel(catalogGift, count));
                } else {
                    giftList.add(new CollectionItemModel(giftName, R.drawable.gift_icon, 100000L, count));
                }
            }
        }

        if (giftAdapter != null) giftAdapter.notifyDataSetChanged();

        // Populate Collection Hall with all owned items (Frames, Vehicles, Gifts)
        collectionHallList.clear();
        collectionHallList.addAll(frameList);
        collectionHallList.addAll(vehicleList);
        collectionHallList.addAll(giftList);
        if (collectionHallAdapter != null) collectionHallAdapter.notifyDataSetChanged();

        if (tvCollectionHallCount != null) {
            tvCollectionHallCount.setText(totalItemsCount + "/365 ›");
        }
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (txRef != null && txListener != null) {
            txRef.removeEventListener(txListener);
        }
        if (userGiftsRef != null && userGiftsListener != null) {
            userGiftsRef.removeEventListener(userGiftsListener);
        }
    }
}

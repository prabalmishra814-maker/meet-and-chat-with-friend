package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateRoomActivity extends AppCompatActivity {
    Button btnCreate;
    EditText etRoomTitle;
    ImageView pickimg;
    ImageView ivRoomCover;
    TextView tvTitle;
    public static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private String imgUrl = "";

    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    // Indian Girl Photos (Both Village / Traditional & City / Modern looks)
    private static final String[] INDIAN_GIRL_PHOTOS = {
            // Village / Traditional / Deshi Indian Girls
            "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1609357605129-26f69add5d6e?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1621784563330-caac0b162981?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1616091216791-a5360b5fc78a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1604537466158-719b1972feb8?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1588516903720-8ceb67f9ef84?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1614030424754-24d1e285a854?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1617922001439-4a2e6562f328?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1618151313441-bc79b11e5090?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1601288496920-b6154fe3626a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1611042553365-9b101441c135?auto=format&fit=crop&w=800&q=80",

            // City / Modern / Urban Indian Girls
            "https://images.unsplash.com/photo-1607746882042-944635dfe10e?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1597223557154-721c1cecc4b0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1503104896436-594329385108?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1532074205216-d0e1f4b87368?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1520512202623-51c5c53957df?auto=format&fit=crop&w=800&q=80"
    };

    private String getNextUniqueGirlPhotoUrl() {
        SharedPreferences sp = getSharedPreferences("room_cover_prefs", MODE_PRIVATE);
        int lastIndex = sp.getInt("last_girl_photo_index", -1);

        int nextIndex;
        if (lastIndex < 0) {
            nextIndex = new Random().nextInt(INDIAN_GIRL_PHOTOS.length);
        } else {
            nextIndex = (lastIndex + 1 + new Random().nextInt(INDIAN_GIRL_PHOTOS.length - 1)) % INDIAN_GIRL_PHOTOS.length;
        }

        sp.edit().putInt("last_girl_photo_index", nextIndex).apply();
        return INDIAN_GIRL_PHOTOS[nextIndex];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_create_room);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());
        btnCreate = findViewById(R.id.btnCreate);
        etRoomTitle = findViewById(R.id.etRoomTitle);
        pickimg = findViewById(R.id.pickimg);
        ivRoomCover = findViewById(R.id.ivRoomCover);
        tvTitle = findViewById(R.id.tvTitle);

        // Entrance animations
        AnimationHelper.fadeIn(findViewById(R.id.ivBack), 500);
        AnimationHelper.fadeIn(tvTitle, 600);
        AnimationHelper.scaleIn(findViewById(R.id.clRoomCover), 700);
        AnimationHelper.slideUp(btnCreate, 800);
        
        // Image preview subtle animation on open
        ivRoomCover.setAlpha(0f);
        ivRoomCover.setScaleX(0.9f);
        ivRoomCover.setScaleY(0.9f);
        ivRoomCover.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1000).start();

        // Click animations
        AnimationHelper.applyClickAnimation(btnCreate);
        AnimationHelper.applyClickAnimation(pickimg);
        AnimationHelper.applyClickAnimation(ivBack);

        pickimg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Tap cover image to refresh to a new Indian girl photo if desired
        ivRoomCover.setOnClickListener(v -> {
            if (imageUri == null) {
                fetchRandomGirlPhotoBanner(true);
            }
        });

        // Load existing room for current user if available, or fetch random Indian girl photo banner
        String userId = getCurrentUserId();
        if (userId != null) {
            loadExistingRoom(userId);
        } else {
            fetchRandomGirlPhotoBanner(false);
        }

        btnCreate.setOnClickListener(v -> {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreateRoomActivity.this, LoginActivity.class));
                finish();
                return;
            }

            String roomTitle = etRoomTitle != null ? etRoomTitle.getText().toString().trim() : "";
            if (roomTitle.isEmpty()) {
                if (etRoomTitle != null) {
                    etRoomTitle.setError("Please enter room title");
                }
                Toast.makeText(this, "Please enter room title", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);
            if (imageUri != null) {
                uploadImageAndSaveRoom();
            } else if (imgUrl != null && !imgUrl.isEmpty()) {
                saveRoom();
            } else {
                fetchGirlPhotoAndSaveRoom();
            }
        });
    }

    private boolean isFetchingBanner = false;

    private void fetchRandomGirlPhotoBanner(boolean showToast) {
        if (isFetchingBanner) return;
        isFetchingBanner = true;

        if (showToast) {
            Toast.makeText(this, "Loading Indian girl photo cover...", Toast.LENGTH_SHORT).show();
        }

        imgUrl = getNextUniqueGirlPhotoUrl();
        imageUri = null;

        if (!isFinishing() && !isDestroyed() && ivRoomCover != null) {
            Glide.with(CreateRoomActivity.this)
                    .load(imgUrl)
                    .placeholder(R.drawable.app_create_room_ic)
                    .into(ivRoomCover);
        }
        isFetchingBanner = false;
    }

    private void fetchGirlPhotoAndSaveRoom() {
        if (imgUrl == null || imgUrl.isEmpty()) {
            imgUrl = getNextUniqueGirlPhotoUrl();
        }
        saveRoom();
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (currentUser != null) {
            return currentUser.getUid();
        } else if (account != null) {
            return account.getId();
        }
        return null;
    }

    private String getCurrentUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail() != null ? currentUser.getEmail().split("@")[0] : "User";
            }
            return userName;
        } else if (account != null) {
            String userName = account.getDisplayName();
            return (userName != null && !userName.isEmpty()) ? userName : "Host";
        }
        return "Host";
    }

    private void loadExistingRoom(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    RoomModel existingRoom = snapshot.getValue(RoomModel.class);
                    if (existingRoom != null) {
                        if (existingRoom.getRoomId() != null && is6DigitNumber(existingRoom.getRoomId())) {
                            existing6DigitRoomId = existingRoom.getRoomId();
                        }
                        if (existingRoom.getRoom_name() != null && etRoomTitle != null) {
                            etRoomTitle.setText(existingRoom.getRoom_name());
                        }
                        if (existingRoom.getImg() != null && !existingRoom.getImg().isEmpty()) {
                            imgUrl = existingRoom.getImg();
                            Glide.with(CreateRoomActivity.this)
                                    .load(imgUrl)
                                    .placeholder(R.drawable.app_create_room_ic)
                                    .into(ivRoomCover);
                        } else {
                            fetchRandomGirlPhotoBanner(false);
                        }
                        if (btnCreate != null) {
                            btnCreate.setText("Update Room");
                        }
                        if (tvTitle != null) {
                            tvTitle.setText("Update Room");
                        }
                    } else {
                        fetchRandomGirlPhotoBanner(false);
                    }
                } else {
                    fetchRandomGirlPhotoBanner(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchRandomGirlPhotoBanner(false);
            }
        });
    }

    private void uploadImageAndSaveRoom() {
        Toast.makeText(this, "Uploading cover image...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                if (imgUrl == null || imgUrl.isEmpty()) {
                    imgUrl = getNextUniqueGirlPhotoUrl();
                }
                saveRoom();
                return;
            }
            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", "image.jpg",
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
                        Toast.makeText(CreateRoomActivity.this, "Image upload failed, setting girl photo cover.", Toast.LENGTH_SHORT).show();
                        if (imgUrl == null || imgUrl.isEmpty()) {
                            imgUrl = getNextUniqueGirlPhotoUrl();
                        }
                        saveRoom();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            imgUrl = jsonObject.getJSONObject("data").getString("url");
                        } catch (Exception e) {
                            if (imgUrl == null || imgUrl.isEmpty()) {
                                imgUrl = getNextUniqueGirlPhotoUrl();
                            }
                        }
                    } else {
                        if (imgUrl == null || imgUrl.isEmpty()) {
                            imgUrl = getNextUniqueGirlPhotoUrl();
                        }
                    }
                    runOnUiThread(() -> saveRoom());
                }
            });

        } catch (Exception e) {
            if (imgUrl == null || imgUrl.isEmpty()) {
                imgUrl = getNextUniqueGirlPhotoUrl();
            }
            saveRoom();
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

    private void saveRoom() {
        String userId = getCurrentUserId();
        String userName = getCurrentUserName();

        if (userId == null) {
            btnCreate.setEnabled(true);
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateRoomActivity.this, LoginActivity.class));
            finish();
            return;
        }

        String roomTitle = etRoomTitle != null ? etRoomTitle.getText().toString().trim() : "";
        if (roomTitle.isEmpty()) {
            btnCreate.setEnabled(true);
            if (etRoomTitle != null) {
                etRoomTitle.setError("Please enter room title");
            }
            Toast.makeText(this, "Please enter room title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName == null || userName.isEmpty()) {
            userName = "Host";
        }

        if (imgUrl == null || imgUrl.isEmpty()) {
            imgUrl = getNextUniqueGirlPhotoUrl();
        }

        final String finalUserId = userId;
        final String finalUserName = userName;
        final String finalRoomTitle = roomTitle;

        ensure6DigitRoomId(generatedRoomId -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms");
            String roomId = generatedRoomId;

            HashMap<String, Object> map = new HashMap<>();
            map.put("room_name", finalRoomTitle);
            map.put("img", imgUrl);
            map.put("uid", finalUserId);
            map.put("roomId", roomId);

            ref.child(userId).setValue(map)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Room Saved Successfully! Room ID: " + roomId, Toast.LENGTH_SHORT).show();
                        
                        // Start RoomChatActivity as Host
                        Intent intent = new Intent(CreateRoomActivity.this, RoomChatActivity.class);
                        intent.putExtra("roomID", roomId);
                        intent.putExtra("room_name", finalRoomTitle);
                        intent.putExtra("username", finalUserName);
                        intent.putExtra("userID", finalUserId);
                        intent.putExtra("uid", finalUserId);
                        intent.putExtra("img", imgUrl);
                        intent.putExtra("host", true); // Creator is the Host
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnCreate.setEnabled(true);
                        Toast.makeText(this, "Failed to save room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private String existing6DigitRoomId = null;

    private void ensure6DigitRoomId(OnRoomIdGeneratedListener listener) {
        if (existing6DigitRoomId != null && is6DigitNumber(existing6DigitRoomId)) {
            listener.onGenerated(existing6DigitRoomId);
            return;
        }
        generateUniqueRoomId(listener);
    }

    private void generateUniqueRoomId(OnRoomIdGeneratedListener listener) {
        String random6Digit = String.valueOf(100000 + new Random().nextInt(900000));
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = false;
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        RoomModel room = child.getValue(RoomModel.class);
                        if (room != null && random6Digit.equals(room.getRoomId())) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (exists) {
                    generateUniqueRoomId(listener);
                } else {
                    existing6DigitRoomId = random6Digit;
                    listener.onGenerated(random6Digit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                existing6DigitRoomId = random6Digit;
                listener.onGenerated(random6Digit);
            }
        });
    }

    private boolean is6DigitNumber(String str) {
        return str != null && str.matches("\\d{6}");
    }

    private interface OnRoomIdGeneratedListener {
        void onGenerated(String roomId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivRoomCover.setImageURI(imageUri);
        }
    }
}

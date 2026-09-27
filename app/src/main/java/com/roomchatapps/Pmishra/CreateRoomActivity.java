package com.roomchatapps.Pmishra;

import android.content.Intent;
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
    EditText etRoomDescription;
    ImageView pickimg;
    ImageView ivRoomCover;
    TextView tvTitle;
    public static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private String imgUrl = "";

    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";
    private static final String DEFAULT_ROOM_IMG = "https://i.ibb.co/Q7dp3r5h/IMG-20260705-WA0006.jpg";

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
        etRoomDescription = findViewById(R.id.etRoomDescription);
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

        // Load existing room for current user if available
        String userId = getCurrentUserId();
        if (userId != null) {
            loadExistingRoom(userId);
        }

        btnCreate.setOnClickListener(v -> {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreateRoomActivity.this, LoginActivity.class));
                finish();
                return;
            }

            btnCreate.setEnabled(false);
            if (imageUri != null) {
                uploadImageAndSaveRoom();
            } else {
                saveRoom();
            }
        });
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
                        if (existingRoom.getImg() != null && !existingRoom.getImg().isEmpty()) {
                            imgUrl = existingRoom.getImg();
                            Glide.with(CreateRoomActivity.this)
                                    .load(imgUrl)
                                    .placeholder(R.drawable.app_create_room_ic)
                                    .into(ivRoomCover);
                        }
                        if (btnCreate != null) {
                            btnCreate.setText("Update Room");
                        }
                        if (tvTitle != null) {
                            tvTitle.setText("Update Room");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void uploadImageAndSaveRoom() {
        Toast.makeText(this, "Uploading cover image...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                if (imgUrl == null || imgUrl.isEmpty()) {
                    imgUrl = DEFAULT_ROOM_IMG;
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
                        Toast.makeText(CreateRoomActivity.this, "Image upload failed, using default cover.", Toast.LENGTH_SHORT).show();
                        if (imgUrl == null || imgUrl.isEmpty()) {
                            imgUrl = DEFAULT_ROOM_IMG;
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
                                imgUrl = DEFAULT_ROOM_IMG;
                            }
                        }
                    } else {
                        if (imgUrl == null || imgUrl.isEmpty()) {
                            imgUrl = DEFAULT_ROOM_IMG;
                        }
                    }
                    runOnUiThread(() -> saveRoom());
                }
            });

        } catch (Exception e) {
            if (imgUrl == null || imgUrl.isEmpty()) {
                imgUrl = DEFAULT_ROOM_IMG;
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

        if (userName == null || userName.isEmpty()) {
            userName = "Host";
        }

        if (imgUrl == null || imgUrl.isEmpty()) {
            imgUrl = DEFAULT_ROOM_IMG;
        }

        final String finalUserId = userId;
        final String finalUserName = userName;

        ensure6DigitRoomId(generatedRoomId -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms");
            String roomId = generatedRoomId;
            // Room Title is automatically set to the same unique 6-digit Room ID number
            String roomName = roomId;

            HashMap<String, Object> map = new HashMap<>();
            map.put("room_name", roomName);
            map.put("img", imgUrl);
            map.put("uid", finalUserId);
            map.put("roomId", roomId);

            ref.child(userId).setValue(map)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Room Saved Successfully! Room ID: " + roomId, Toast.LENGTH_SHORT).show();
                        
                        // Start RoomChatActivity as Host
                        Intent intent = new Intent(CreateRoomActivity.this, RoomChatActivity.class);
                        intent.putExtra("roomID", roomId);
                        intent.putExtra("room_name", roomName);
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

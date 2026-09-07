package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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

        // Entrance animations
        AnimationHelper.fadeIn(findViewById(R.id.ivBack), 500);
        AnimationHelper.fadeIn(findViewById(R.id.tvTitle), 600);
        AnimationHelper.scaleIn(findViewById(R.id.clRoomCover), 700);
        AnimationHelper.slideUp(etRoomDescription, 600);
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

        btnCreate.setOnClickListener(v -> {
            String roomName = etRoomDescription.getText().toString().trim();
            if (roomName.isEmpty()) {
                Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);
            if (imageUri != null) {
                uploadImageAndSaveRoom();
            } else {
                imgUrl = DEFAULT_ROOM_IMG;
                saveRoom();
            }
        });
    }

    private void uploadImageAndSaveRoom() {
        Toast.makeText(this, "Uploading cover image...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                imgUrl = DEFAULT_ROOM_IMG;
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
                        imgUrl = DEFAULT_ROOM_IMG;
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
                            imgUrl = DEFAULT_ROOM_IMG;
                        }
                    } else {
                        imgUrl = DEFAULT_ROOM_IMG;
                    }
                    runOnUiThread(() -> saveRoom());
                }
            });

        } catch (Exception e) {
            imgUrl = DEFAULT_ROOM_IMG;
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(CreateRoomActivity.this);

        String userId = null;
        String userName = null;

        if (currentUser != null) {
            userId = currentUser.getUid();
            userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail() != null ? currentUser.getEmail().split("@")[0] : "User";
            }
        } else if (account != null) {
            userId = account.getId();
            userName = account.getDisplayName();
        }

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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms");
        String roomId = ref.push().getKey();

        if (roomId == null) {
            btnCreate.setEnabled(true);
            Toast.makeText(this, "Failed to generate room ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String roomName = etRoomDescription.getText().toString().trim();

        HashMap<String, Object> map = new HashMap<>();
        map.put("room_name", roomName);
        map.put("img", imgUrl);
        map.put("uid", userId);
        map.put("roomId", roomId);

        final String finalUserId = userId;
        final String finalUserName = userName;

        ref.child(roomId).setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Room Created Successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Start RoomChatActivity as Host
                    Intent intent = new Intent(CreateRoomActivity.this, RoomChatActivity.class);
                    intent.putExtra("roomID", roomId);
                    intent.putExtra("room_name", roomName);
                    intent.putExtra("username", finalUserName);
                    intent.putExtra("userID", finalUserId);
                    intent.putExtra("img", imgUrl);
                    intent.putExtra("host", true); // Creator is the Host
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "Failed to create room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

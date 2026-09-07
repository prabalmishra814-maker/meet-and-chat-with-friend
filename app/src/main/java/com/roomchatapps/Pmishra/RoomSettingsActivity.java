package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roomchatapps.Pmishra.AnimationHelper;

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

    private EditText etRoomName;
    private ImageView ivRoomCover, pickimg;
    private View btnUpdate;
    private String roomID, currentRoomName, currentRoomImg;
    private Uri imageUri;
    private String newImgUrl = "";
    private static final int PICK_IMAGE = 100;
    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_room_settings);

        roomID = getIntent().getStringExtra("roomID");
        currentRoomName = getIntent().getStringExtra("roomName");
        currentRoomImg = getIntent().getStringExtra("roomImg");

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());
        
        etRoomName = findViewById(R.id.etRoomName);
        ivRoomCover = findViewById(R.id.ivRoomCover);
        pickimg = findViewById(R.id.pickimg);
        btnUpdate = findViewById(R.id.btnUpdate);

        if (currentRoomName != null) etRoomName.setText(currentRoomName);
        if (currentRoomImg != null) {
            Glide.with(this).load(currentRoomImg).into(ivRoomCover);
            newImgUrl = currentRoomImg;
        }

        AnimationHelper.applyClickAnimation(btnUpdate);
        AnimationHelper.applyClickAnimation(pickimg);
        AnimationHelper.applyClickAnimation(ivBack);

        pickimg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnUpdate.setOnClickListener(v -> {
            String newName = etRoomName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Room name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            btnUpdate.setEnabled(false);
            if (imageUri != null) {
                uploadImageAndSaveSettings(newName);
            } else {
                saveSettings(newName, newImgUrl);
            }
        });
    }

    private ImageView ivBack; // Defined here to avoid errors if referenced in onCreate later

    private void uploadImageAndSaveSettings(String newName) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytes(inputStream);
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
                        btnUpdate.setEnabled(true);
                        Toast.makeText(RoomSettingsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            newImgUrl = jsonObject.getJSONObject("data").getString("url");
                            runOnUiThread(() -> saveSettings(newName, newImgUrl));
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                btnUpdate.setEnabled(true);
                                Toast.makeText(RoomSettingsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            btnUpdate.setEnabled(true);
                            Toast.makeText(RoomSettingsActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            btnUpdate.setEnabled(true);
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

    private void saveSettings(String newName, String imgUrl) {
        if (roomID == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("room_name", newName);
        updates.put("img", imgUrl);

        ref.updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Settings Updated!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnUpdate.setEnabled(true);
            Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivRoomCover.setImageURI(imageUri);
        }
    }
}
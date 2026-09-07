package com.roomchatapps.Pmishra;

import android.content.Intent;
import java.util.Locale;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostCreateActivity extends AppCompatActivity {
    EditText etWord;
    ImageView img_upload, btnClose;
    TextView btnSend;
    FrameLayout cardUpload;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String imgUrl = "";
    public static final int PICK_IMAGE = 100;

    // Replace with your actual ImgBB API key
    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_postcreate);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        mAuth = FirebaseAuth.getInstance();

        etWord = findViewById(R.id.etWord);
        img_upload = findViewById(R.id.img_upload);
        btnSend = findViewById(R.id.btnSend);
        btnClose = findViewById(R.id.btnClose);
        cardUpload = findViewById(R.id.cardUpload);

        btnClose.setOnClickListener(v -> finish());

        cardUpload.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSend.setOnClickListener(view -> {
            if (imageUri != null) {
                uploadImage();
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage() {
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to open image file", Toast.LENGTH_SHORT).show();
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
                    runOnUiThread(() -> Toast.makeText(PostCreateActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            imgUrl = jsonObject.getJSONObject("data").getString("url");
                            runOnUiThread(() -> savePost());
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(PostCreateActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(PostCreateActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void savePost() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String postId = databaseReference.push().getKey();
        String uid = user.getUid();
        //country name
        Locale locale = Locale.getDefault();

        String country = locale.getDisplayCountry();
        //upload to firebase

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("title", etWord.getText().toString().trim());
        postMap.put("poster", imgUrl);
        postMap.put("uid", uid);
        postMap.put("datetime", dateTime);
        postMap.put("postId", postId);
        postMap.put("commentCount", 0);
        postMap.put("Country",country);

        if (postId != null) {
            databaseReference.child(postId).setValue(postMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(PostCreateActivity.this, "Post shared", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PostCreateActivity.this, "Failed to share post", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            img_upload.setImageURI(imageUri);
        }
    }
}

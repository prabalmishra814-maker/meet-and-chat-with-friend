package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.util.Log;
import java.io.InputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailloginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etName;
    private CircleImageView ivProfile;
    private TextView tvTitle, tvToggleMode;
    private Button btnNext;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private boolean isLoginMode = false;
    private Uri imageUri;

    // ImgBB API key from CreateRoomActivity
    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfile.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emaillogin);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivProfile = findViewById(R.id.ivProfile);
        tvTitle = findViewById(R.id.tvTitle);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        ivProfile.setOnClickListener(v -> {
            if (!isLoginMode) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });

        tvToggleMode.setOnClickListener(v -> toggleMode());

        btnNext.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (isLoginMode) {
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginUser(email, password);
            } else {
                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerUser(name, email, password);
            }
        });
        toggleMode();
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText("Email Login");
            etName.setVisibility(View.GONE);
            ivProfile.setVisibility(View.GONE);
            btnNext.setText("Login");
            tvToggleMode.setText("Don't have an account? Register");
        } else {
            tvTitle.setText("Create Account");
            etName.setVisibility(View.VISIBLE);
            ivProfile.setVisibility(View.VISIBLE);
            btnNext.setText("Register");
            tvToggleMode.setText("Already have an account? Login");
        }
    }

    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        String err = (task.getException() != null && task.getException().getMessage() != null) ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(EmailloginActivity.this, "Login failed: " + err, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String name, String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (imageUri != null) {
                            uploadAvatarToImgBB(user, name);
                        } else {
                            saveUserToDatabase(user, name, null);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EmailloginActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadAvatarToImgBB(FirebaseUser user, String name) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                saveUserToDatabase(user, name, null);
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
                        saveUserToDatabase(user, name, null);
                        Toast.makeText(EmailloginActivity.this, "Avatar upload failed, continuing with default", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            String avatarUrl = jsonObject.getJSONObject("data").getString("url");
                            runOnUiThread(() -> saveUserToDatabase(user, name, avatarUrl));
                        } catch (Exception e) {
                            runOnUiThread(() -> saveUserToDatabase(user, name, null));
                        }
                    } else {
                        runOnUiThread(() -> saveUserToDatabase(user, name, null));
                    }
                }
            });

        } catch (Exception e) {
            saveUserToDatabase(user, name, null);
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

    private void saveUserToDatabase(FirebaseUser user, String name, String avatarUrl) {
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid());

            String generatedProfileId = String.valueOf(100000 + Math.abs((long) user.getUid().hashCode()) % 900000);

            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("email", user.getEmail());
            map.put("uid", user.getUid());
            map.put("profileId", generatedProfileId);
            map.put("level", "1");
            map.put("premium", "no");
            if (avatarUrl != null) {
                map.put("avtar", avatarUrl);
            }

            userRef.setValue(map).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    navigateToMainActivity();
                } else {
                    Toast.makeText(EmailloginActivity.this, "Data upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(EmailloginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

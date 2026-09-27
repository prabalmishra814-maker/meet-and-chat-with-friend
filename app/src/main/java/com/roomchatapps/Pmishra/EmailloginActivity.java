package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.LoadingDialog;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.WalletManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private ImageView ivTogglePassword;
    private TextView tvTitle, tvToggleMode;
    private Button btnNext;
    private LinearLayout llForgetPassword;
    
    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;
    private SessionManager sessionManager;
    
    private boolean isLoginMode = true;
    private boolean isPasswordVisible = false;
    private Uri imageUri;

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
        loadingDialog = new LoadingDialog(this);
        sessionManager = SessionManager.getInstance(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivProfile = findViewById(R.id.ivProfile);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        tvTitle = findViewById(R.id.tvTitle);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        btnNext = findViewById(R.id.btnNext);
        llForgetPassword = findViewById(R.id.llForgetPassword);

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

        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        tvToggleMode.setOnClickListener(v -> toggleMode());

        llForgetPassword.setOnClickListener(v -> showForgotPasswordDialog());

        btnNext.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                loginUser(email, password);
            } else {
                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerUser(name, email, password);
            }
        });

        // Initialize mode to Login
        isLoginMode = false; // toggling will set it to true
        toggleMode();
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_eye_visible);
        } else {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText("Email Login");
            etName.setVisibility(View.GONE);
            ivProfile.setVisibility(View.GONE);
            btnNext.setText("Login");
            tvToggleMode.setText("Don't have an account? Register");
            llForgetPassword.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText("Create Account");
            etName.setVisibility(View.VISIBLE);
            ivProfile.setVisibility(View.VISIBLE);
            btnNext.setText("Register");
            tvToggleMode.setText("Already have an account? Login");
            llForgetPassword.setVisibility(View.GONE);
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your registered email to receive password reset instructions:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        String currentEmail = etEmail.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            input.setText(currentEmail);
        }
        input.setHint("email@example.com");
        
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(paddingPx, 0, paddingPx, 0);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(EmailloginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordReset(email);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendPasswordReset(String email) {
        loadingDialog.show("Sending reset link...");
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(EmailloginActivity.this, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(EmailloginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser(String email, String password) {
        loadingDialog.show("Logging in...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserAndCreateSession(user);
                        } else {
                            loadingDialog.dismiss();
                            navigateToMainActivity();
                        }
                    } else {
                        loadingDialog.dismiss();
                        String err = (task.getException() != null && task.getException().getMessage() != null)
                                ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(EmailloginActivity.this, "Login failed: " + err, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserAndCreateSession(FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                String name = snapshot.child("name").getValue(String.class);
                String profileId = snapshot.child("profileId").getValue(String.class);
                String avatar = snapshot.child("avtar").getValue(String.class);

                if (name == null) name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                if (profileId == null) profileId = String.valueOf(100000 + Math.abs((long) user.getUid().hashCode()) % 900000);

                sessionManager.createLoginSession(user.getUid(), name, user.getEmail(), profileId, avatar, "email");
                navigateToMainActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                sessionManager.createLoginSession(user.getUid(), "User", user.getEmail(), "", "", "email");
                navigateToMainActivity();
            }
        });
    }

    private void registerUser(String name, String email, String password) {
        loadingDialog.show("Creating account...");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (imageUri != null) {
                            loadingDialog.setMessage("Uploading avatar...");
                            uploadAvatarToImgBB(user, name);
                        } else {
                            loadingDialog.setMessage("Setting up profile...");
                            saveUserToDatabase(user, name, null);
                        }
                    } else {
                        loadingDialog.dismiss();
                        String err = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(EmailloginActivity.this, "Registration failed: " + err, Toast.LENGTH_SHORT).show();
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
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        saveUserToDatabase(user, name, null);
                        Toast.makeText(EmailloginActivity.this, "Avatar upload failed, continuing with default", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
            map.put("email", user.getEmail() != null ? user.getEmail() : "");
            map.put("uid", user.getUid());
            map.put("profileId", generatedProfileId);
            map.put("level", "0");
            map.put("coinsSpent", 0);
            map.put("xp", 0);
            map.put("premium", "no");
            map.put("Followers", "0");
            map.put("Following", "0");
            map.put("money", 0);
            map.put("coins", 500); // 500 Welcome Bonus Coins
            if (avatarUrl != null) {
                map.put("avtar", avatarUrl);
            }

            userRef.setValue(map).addOnCompleteListener(task -> {
                loadingDialog.dismiss();
                if (task.isSuccessful()) {
                    WalletManager.logTransaction(
                            user.getUid(), "WELCOME_BONUS", 500, 0,
                            "Welcome Signup Bonus", "Received 500 Free Signup Coins!"
                    );
                    sessionManager.createLoginSession(user.getUid(), name, user.getEmail(), generatedProfileId, avatarUrl, "email");
                    navigateToMainActivity();
                } else {
                    Toast.makeText(EmailloginActivity.this, "Data upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadingDialog.dismiss();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(EmailloginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}

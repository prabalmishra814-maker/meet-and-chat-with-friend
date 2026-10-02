package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log; // LOGIN FIX
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

import com.google.android.material.snackbar.Snackbar; // LOGIN UI FEEDBACK FIX
import com.google.firebase.FirebaseNetworkException; // LOGIN FIX
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; // LOGIN FIX
import com.google.firebase.auth.FirebaseAuthInvalidUserException; // LOGIN FIX
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // LOGIN FIX
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
    private AlertDialog resetPasswordDialog;
    
    private boolean isLoginMode = true;
    private boolean isPasswordVisible = false;
    private boolean isAuthenticating = false; // LOGIN FIX
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
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
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

        findViewById(R.id.ivBack).setOnClickListener(v -> {
            if (!isAuthenticating) { // LOGIN FIX
                finish();
            }
        });

        ivProfile.setOnClickListener(v -> {
            if (!isLoginMode && !isAuthenticating) { // LOGIN FIX
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });

        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        tvToggleMode.setOnClickListener(v -> {
            if (!isAuthenticating) { // LOGIN FIX
                toggleMode();
            }
        });

        llForgetPassword.setOnClickListener(v -> {
            if (!isAuthenticating) { // LOGIN FIX
                showForgotPasswordDialog();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isAuthenticating) return; // LOGIN FIX - Rapid multiple click protection

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // LOGIN FIX - Validation before calling Firebase
            if (TextUtils.isEmpty(email)) { // LOGIN FIX
                showFeedback("Please enter your email address"); // LOGIN UI FEEDBACK FIX
                return; // LOGIN FIX
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // LOGIN FIX
                showFeedback("Please enter a valid email address"); // LOGIN UI FEEDBACK FIX
                return; // LOGIN FIX
            }

            if (TextUtils.isEmpty(password)) { // LOGIN FIX
                showFeedback("Please enter your password"); // LOGIN UI FEEDBACK FIX
                return; // LOGIN FIX
            }

            if (password.length() < 6) { // LOGIN FIX
                showFeedback("Password must be at least 6 characters"); // LOGIN UI FEEDBACK FIX
                return; // LOGIN FIX
            }

            if (isLoginMode) {
                loginUser(email, password);
            } else {
                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) { // LOGIN FIX
                    showFeedback("Please enter your name"); // LOGIN UI FEEDBACK FIX
                    return; // LOGIN FIX
                }
                registerUser(name, email, password);
            }
        });

        // Initialize mode to Login
        isLoginMode = false; // toggling will set it to true
        toggleMode();
    }

    // LOGIN FIX - Loading state & click protection helper
    private void setLoadingState(boolean loading, String message) {
        isAuthenticating = loading; // LOGIN FIX
        if (btnNext != null) btnNext.setEnabled(!loading); // LOGIN FIX
        if (tvToggleMode != null) tvToggleMode.setEnabled(!loading); // LOGIN FIX
        if (llForgetPassword != null) llForgetPassword.setEnabled(!loading); // LOGIN FIX
        ImageView ivBack = findViewById(R.id.ivBack); // LOGIN FIX
        if (ivBack != null) ivBack.setEnabled(!loading); // LOGIN FIX

        if (loading) { // LOGIN FIX
            if (message != null) { // LOGIN FIX
                loadingDialog.show(message); // LOGIN FIX
            }
        } else {
            loadingDialog.dismiss(); // LOGIN FIX
        }
    }

    // LOGIN UI FEEDBACK FIX - Toast and Professional Snackbar feedback
    private void showFeedback(String message) {
        if (message == null || message.isEmpty()) return;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            Snackbar snackbar = Snackbar.make(mainView, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    // LOGIN FIX - Map Firebase exceptions to user-friendly messages
    private String getFriendlyAuthErrorMessage(Exception exception) {
        if (exception == null) { // LOGIN FIX
            return "Unable to sign in. Please try again."; // LOGIN UI FEEDBACK FIX
        }
        if (exception instanceof FirebaseNetworkException) { // LOGIN FIX
            return "No internet connection. Please check your connection."; // LOGIN UI FEEDBACK FIX
        }
        if (exception instanceof FirebaseAuthInvalidUserException ||
            exception instanceof FirebaseAuthInvalidCredentialsException) { // LOGIN FIX
            return "Incorrect email or password"; // LOGIN UI FEEDBACK FIX
        }
        if (exception instanceof FirebaseAuthUserCollisionException) { // LOGIN FIX
            return "An account with this email already exists."; // LOGIN UI FEEDBACK FIX
        }
        return "Unable to sign in. Please try again."; // LOGIN UI FEEDBACK FIX
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

    // LOGIN FIX - Map Firebase exceptions to user-friendly messages for password reset
    private String getFriendlyResetPasswordErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unable to send password reset email. Please try again.";
        }
        if (exception instanceof FirebaseNetworkException) {
            return "No internet connection. Please check your network connection.";
        }
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "No account found with this email address. Please check your email or register.";
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Please enter a valid email address.";
        }
        return "Unable to send reset link. Please try again.";
    }

    private void showForgotPasswordDialog() {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        EditText etResetEmail = dialogView.findViewById(R.id.etResetEmail);
        TextView tvResetError = dialogView.findViewById(R.id.tvResetError);
        Button btnResetCancel = dialogView.findViewById(R.id.btnResetCancel);
        Button btnResetSend = dialogView.findViewById(R.id.btnResetSend);

        if (etResetEmail != null) {
            etResetEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            String currentEmail = etEmail.getText().toString().trim();
            if (!currentEmail.isEmpty()) {
                etResetEmail.setText(currentEmail);
                etResetEmail.setSelection(currentEmail.length());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        resetPasswordDialog = builder.create();
        if (resetPasswordDialog.getWindow() != null) {
            resetPasswordDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (btnResetCancel != null) {
            btnResetCancel.setOnClickListener(v -> resetPasswordDialog.dismiss());
        }

        if (btnResetSend != null) {
            btnResetSend.setOnClickListener(v -> {
                if (etResetEmail == null) return;
                String email = etResetEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    if (tvResetError != null) {
                        tvResetError.setText("Please enter your email address.");
                        tvResetError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (tvResetError != null) {
                        tvResetError.setText("Please enter a valid email address.");
                        tvResetError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if (tvResetError != null) {
                    tvResetError.setVisibility(View.GONE);
                }
                resetPasswordDialog.dismiss();
                sendPasswordReset(email);
            });
        }

        resetPasswordDialog.show();
    }

    private void sendPasswordReset(String email) {
        setLoadingState(true, "Sending reset link..."); // LOGIN FIX
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoadingState(false, null); // LOGIN FIX
                    if (task.isSuccessful()) {
                        if (etEmail.getText().toString().trim().isEmpty()) {
                            etEmail.setText(email);
                        }
                        showFeedback("Password reset email sent to " + email + ". Check your inbox and spam folder.");
                    } else {
                        Log.e("EmailloginActivity", "Password reset failed", task.getException()); // LOGIN FIX
                        String error = getFriendlyResetPasswordErrorMessage(task.getException());
                        showFeedback(error);
                    }
                });
    }

    private void loginUser(String email, String password) {
        setLoadingState(true, "Logging in..."); // LOGIN FIX
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserAndCreateSession(user);
                        } else {
                            setLoadingState(false, null); // LOGIN FIX
                            navigateToMainActivity();
                        }
                    } else {
                        setLoadingState(false, null); // LOGIN FIX
                        Log.e("EmailloginActivity", "Login failed", task.getException()); // LOGIN FIX
                        String err = getFriendlyAuthErrorMessage(task.getException()); // LOGIN FIX
                        showFeedback(err); // LOGIN UI FEEDBACK FIX
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
                setLoadingState(false, null); // LOGIN FIX
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
                setLoadingState(false, null); // LOGIN FIX
                Log.e("EmailloginActivity", "Database error fetching user", error.toException()); // LOGIN FIX
                sessionManager.createLoginSession(user.getUid(), "User", user.getEmail(), "", "", "email");
                navigateToMainActivity();
            }
        });
    }

    private void registerUser(String name, String email, String password) {
        setLoadingState(true, "Creating account..."); // LOGIN FIX
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
                        setLoadingState(false, null); // LOGIN FIX
                        Log.e("EmailloginActivity", "Registration failed", task.getException()); // LOGIN FIX
                        String err = getFriendlyAuthErrorMessage(task.getException()); // LOGIN FIX
                        showFeedback(err); // LOGIN UI FEEDBACK FIX
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
                        showFeedback("Avatar upload failed, continuing with default profile"); // LOGIN UI FEEDBACK FIX
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
                setLoadingState(false, null); // LOGIN FIX
                if (task.isSuccessful()) {
                    WalletManager.logTransaction(
                            user.getUid(), "WELCOME_BONUS", 500, 0,
                            "Welcome Signup Bonus", "Received 500 Free Signup Coins!"
                    );
                    sessionManager.createLoginSession(user.getUid(), name, user.getEmail(), generatedProfileId, avatarUrl, "email");
                    navigateToMainActivity();
                } else {
                    Log.e("EmailloginActivity", "Data upload failed", task.getException()); // LOGIN FIX
                    showFeedback("Unable to set up profile. Please try again."); // LOGIN UI FEEDBACK FIX
                }
            });
        } else {
            setLoadingState(false, null); // LOGIN FIX
            showFeedback("Unable to register. Please try again."); // LOGIN UI FEEDBACK FIX
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
        if (resetPasswordDialog != null && resetPasswordDialog.isShowing()) {
            resetPasswordDialog.dismiss();
        }
        super.onDestroy();
    }
}

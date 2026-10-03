package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // LOGIN FIX
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes; // LOGIN FIX
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar; // LOGIN UI FEEDBACK FIX
import com.google.firebase.FirebaseNetworkException; // LOGIN FIX
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; // LOGIN FIX
import com.google.firebase.auth.FirebaseAuthInvalidUserException; // LOGIN FIX
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.LoadingDialog;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.WalletManager;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1000;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;
    private SessionManager sessionManager;
    private boolean isAuthenticating = false; // LOGIN FIX
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            TextView tvFeedback = findViewById(R.id.tvFeedback);
            if (tvFeedback != null) {
                tvFeedback.setPadding(
                        tvFeedback.getPaddingLeft(),
                        systemBars.top + (int) (8 * getResources().getDisplayMetrics().density),
                        tvFeedback.getPaddingRight(),
                        tvFeedback.getPaddingBottom()
                );
            }
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);
        sessionManager = SessionManager.getInstance(this);

        // Check if user is already signed in via Firebase and SessionManager
        if (mAuth.getCurrentUser() != null && sessionManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        LinearLayout llEmail = findViewById(R.id.llEmail);
        LinearLayout llGoogle = findViewById(R.id.llGoogle);
        TextView tvFeedback = findViewById(R.id.tvFeedback);
        TextView tvTerms = findViewById(R.id.tvTerms);
        TextView tvPrivacy = findViewById(R.id.tvPrivacy);
        ImageView ivLogo = findViewById(R.id.ivLogo);
        LinearLayout llTerms = findViewById(R.id.llTermsAndPrivacy);

        // Apply animations
        AnimationHelper.scaleIn(ivLogo, 800);
        
        llEmail.setAlpha(0f);
        llGoogle.setAlpha(0f);
        llTerms.setAlpha(0f);

        llEmail.postDelayed(() -> AnimationHelper.slideUp(llEmail, 500), 300);
        llGoogle.postDelayed(() -> AnimationHelper.slideUp(llGoogle, 500), 450);
        llTerms.postDelayed(() -> AnimationHelper.fadeIn(llTerms, 500), 600);

        // Apply click animations
        AnimationHelper.applyClickAnimation(llEmail);
        AnimationHelper.applyClickAnimation(llGoogle);
        AnimationHelper.applyClickAnimation(tvFeedback);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        llGoogle.setOnClickListener(v -> {
            if (!isAuthenticating) { // LOGIN FIX
                signIn();
            }
        });

        llEmail.setOnClickListener(v -> {
            if (!isAuthenticating) { // LOGIN FIX
                Intent intent = new Intent(LoginActivity.this, EmailloginActivity.class);
                startActivity(intent);
            }
        });

        tvFeedback.setOnClickListener(v -> {
            showFeedback("Feedback feature coming soon"); // LOGIN UI FEEDBACK FIX
        });

        tvTerms.setOnClickListener(v -> {
            showFeedback("Terms of Service"); // LOGIN UI FEEDBACK FIX
        });

        tvPrivacy.setOnClickListener(v -> {
            showFeedback("Privacy Policy"); // LOGIN UI FEEDBACK FIX
        });
    }

    // LOGIN FIX - Loading state & click protection helper
    private void setLoadingState(boolean loading, String message) {
        isAuthenticating = loading; // LOGIN FIX
        LinearLayout llEmail = findViewById(R.id.llEmail); // LOGIN FIX
        LinearLayout llGoogle = findViewById(R.id.llGoogle); // LOGIN FIX
        if (llEmail != null) llEmail.setEnabled(!loading); // LOGIN FIX
        if (llGoogle != null) llGoogle.setEnabled(!loading); // LOGIN FIX

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
        if (exception == null) {
            return "Unable to sign in. Please try again.";
        }
        if (exception instanceof FirebaseNetworkException) {
            return "No internet connection. Please check your connection.";
        }
        if (exception instanceof FirebaseAuthInvalidUserException ||
            exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Incorrect email or password";
        }
        return "Unable to sign in. Please try again.";
    }

    private void signIn() {
        if (isAuthenticating) return;
        setLoadingState(true, "Signing in with Google...");

        // Ensure clean account selection
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> launchGoogleSignInIntent());
        } else {
            launchGoogleSignInIntent();
        }
    }

    private void launchGoogleSignInIntent() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            setLoadingState(false, null);
            Log.e(TAG, "Error launching Google Sign In", e);
            showFeedback("Unable to open Google sign in: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (data == null) {
                setLoadingState(false, null);
                showFeedback("Google sign-in was cancelled");
                return;
            }

            loadingDialog.show("Processing Google sign-in...");

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null && !account.getIdToken().isEmpty()) {
                    loadingDialog.setMessage("Authenticating with Firebase...");
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    setLoadingState(false, null);
                    showFeedback("Google sign-in failed. Missing ID Token.");
                }
            } catch (ApiException e) {
                setLoadingState(false, null);
                int statusCode = e.getStatusCode();
                Log.e(TAG, "Google sign in failed code=" + statusCode, e);

                if (statusCode == GoogleSignInStatusCodes.DEVELOPER_ERROR || statusCode == 10 || statusCode == 12500) {
                    showSha1MismatchDialog();
                } else if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED || statusCode == 12501) {
                    showFeedback("Google sign-in was cancelled");
                } else if (statusCode == GoogleSignInStatusCodes.NETWORK_ERROR) {
                    showFeedback("No internet connection. Please check your network.");
                } else {
                    showFeedback("Google sign-in failed (Code " + statusCode + "). Verify SHA-1 in Firebase Console.");
                }
            } catch (Exception e) {
                setLoadingState(false, null);
                Log.e(TAG, "Unexpected error in Google Sign In result", e);
                showFeedback("Sign in failed: " + e.getLocalizedMessage());
            }
        }
    }

    private void showSha1MismatchDialog() {
        if (isFinishing() || isDestroyed()) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Google Sign-In Setup Required")
                .setMessage("Google Sign-In failed (Developer Error Code 10 / SHA-1 Mismatch).\n\n" +
                        "To fix this, add your app's SHA-1 fingerprint in Firebase Console:\n\n" +
                        "1. Open Firebase Console -> Project Settings\n" +
                        "2. Select your Android app (com.roomchatapps.amstudio)\n" +
                        "3. Click 'Add Fingerprint' and paste:\n\n" +
                        "5D:F5:B5:DC:F9:3A:DE:9E:98:E5:0C:C0:7C:3E:92:1C:2F:16:8F:C7\n\n" +
                        "4. Download updated google-services.json")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        loadingDialog.setMessage("Authenticating with Firebase...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        loadingDialog.setMessage("Syncing profile data...");
                        saveUserToDatabase(user);
                    } else {
                        setLoadingState(false, null);
                        Exception e = task.getException();
                        Log.e(TAG, "Firebase Authentication failed", e);
                        String userMessage = getFriendlyAuthErrorMessage(e);
                        if (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) {
                            userMessage = "Firebase Auth failed: " + e.getLocalizedMessage();
                        }
                        showFeedback(userMessage);
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        if (user != null) {
            loadingDialog.setMessage("Syncing profile data...");
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                        String email = user.getEmail() != null ? user.getEmail() : "";
                        String avatar = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                        String generatedProfileId;

                        if (!snapshot.exists()) {
                            // New User Registration
                            generatedProfileId = String.valueOf(100000 + Math.abs((long) user.getUid().hashCode()) % 900000);

                            userRef.child("name").setValue(name);
                            userRef.child("email").setValue(email);
                            userRef.child("uid").setValue(user.getUid());
                            userRef.child("profileId").setValue(generatedProfileId);
                            userRef.child("premium").setValue("no");
                            userRef.child("Followers").setValue("0");
                            userRef.child("Following").setValue("0");
                            userRef.child("level").setValue("0");
                            userRef.child("coinsSpent").setValue(0);
                            userRef.child("xp").setValue(0);
                            userRef.child("money").setValue(0);
                            userRef.child("coins").setValue(500);

                            if (!avatar.isEmpty()) {
                                userRef.child("avtar").setValue(avatar);
                            }

                            WalletManager.logTransaction(
                                    user.getUid(), "WELCOME_BONUS", 500, 0,
                                    "Welcome Signup Bonus", "Received 500 Free Signup Coins!"
                            );
                        } else {
                            // Existing User
                            String dbProfileId = snapshot.child("profileId").getValue(String.class);
                            generatedProfileId = dbProfileId != null ? dbProfileId : String.valueOf(100000 + Math.abs((long) user.getUid().hashCode()) % 900000);

                            String dbName = snapshot.child("name").getValue(String.class);
                            if (dbName != null && !dbName.isEmpty()) {
                                name = dbName;
                            } else if (!name.isEmpty()) {
                                userRef.child("name").setValue(name);
                            }

                            String dbAvatar = snapshot.child("avtar").getValue(String.class);
                            if (dbAvatar != null && !dbAvatar.isEmpty()) {
                                avatar = dbAvatar;
                            } else if (!avatar.isEmpty()) {
                                userRef.child("avtar").setValue(avatar);
                            }

                            if (!snapshot.hasChild("coins")) {
                                userRef.child("coins").setValue(500);
                            }
                        }

                        sessionManager.createLoginSession(user.getUid(), name, email, generatedProfileId, avatar, "google");
                        setLoadingState(false, null);
                        navigateToMainActivity();
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving user data", e);
                        setLoadingState(false, null);
                        showFeedback("Profile setup error: " + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error saving user", error.toException());
                    sessionManager.createLoginSession(user.getUid(), user.getDisplayName() != null ? user.getDisplayName() : "User", user.getEmail() != null ? user.getEmail() : "", "", "", "google");
                    setLoadingState(false, null);
                    navigateToMainActivity();
                }
            });
        } else {
            setLoadingState(false, null);
            showFeedback("Unable to sign in. User account is null.");
        }
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                intent.putExtra("user_name", user.getDisplayName());
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}

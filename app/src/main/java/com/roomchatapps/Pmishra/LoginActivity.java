package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1000;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in via Firebase
        if (mAuth.getCurrentUser() != null) {
            navigateToMainActivity();
            return; // Essential to prevent crash if layout views are missing
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

        llGoogle.setOnClickListener(v -> signIn());

        llEmail.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, EmailloginActivity.class);
            startActivity(intent);
        });

        tvFeedback.setOnClickListener(v -> {
            Toast.makeText(this, "Feedback feature coming soon", Toast.LENGTH_SHORT).show();
        });

        tvTerms.setOnClickListener(v -> {
            Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show();
        });

        tvPrivacy.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed code=" + e.getStatusCode(), e);
                String message = "Sign in failed";
                if (e.getStatusCode() == 10) {
                    message = "Developer Error (10): Check SHA-1 in Firebase Console";
                } else if (e.getStatusCode() == 12500) {
                    message = "Sign in failed (12500): Check Support Email in Firebase Settings";
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToDatabase(user);
                    } else {
                        Log.e(TAG, "Firebase Authentication failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Firebase Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid());

            String generatedProfileId = String.valueOf(100000 + Math.abs((long) user.getUid().hashCode()) % 900000);

            userRef.child("name").setValue(user.getDisplayName());
            userRef.child("email").setValue(user.getEmail());
            userRef.child("uid").setValue(user.getUid());
            userRef.child("profileId").setValue(generatedProfileId);
            userRef.child("premium").setValue("no");
            userRef.child("Followers").setValue("0");
            userRef.child("Following").setValue("0");
            userRef.child("level").setValue("1");
            userRef.child("money").setValue("0");
            if (user.getPhotoUrl() != null) {
                userRef.child("avtar").setValue(user.getPhotoUrl().toString());
            }

            navigateToMainActivity();
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
}

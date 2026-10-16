package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.ActivityApplyAgencyBinding;
import com.roomchatapps.Pmishra.dialogs.ImageCropDialog;
import com.roomchatapps.Pmishra.dialogs.JoinAgencyDialog;
import com.roomchatapps.Pmishra.models.AgencyModel;
import com.roomchatapps.Pmishra.utils.LoadingDialog;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplyAgencyActivity extends AppCompatActivity {

    private ActivityApplyAgencyBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference applicationRef;
    private DatabaseReference userRef;

    private String currentUid;
    private Uri selectedLogoUri = null;
    private String uploadedLogoUrl = "";

    private LoadingDialog loadingDialog;
    private AgencyModel currentApplication = null;

    private static final String IMGBB_API_KEY = "d909717479f29f4de1b6efc62ec33528";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageCropDialog cropDialog = new ImageCropDialog(ApplyAgencyActivity.this, uri, (croppedBitmap, croppedUri) -> {
                        if (croppedUri != null) {
                            selectedLogoUri = croppedUri;
                            binding.ivAgencyLogo.setImageURI(croppedUri);
                        } else if (croppedBitmap != null) {
                            binding.ivAgencyLogo.setImageBitmap(croppedBitmap);
                        }
                    });
                    cropDialog.show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);

        binding = ActivityApplyAgencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.headerLayout.setPadding(
                    binding.headerLayout.getPaddingLeft(),
                    systemBars.top + 8,
                    binding.headerLayout.getPaddingRight(),
                    binding.headerLayout.getPaddingBottom()
            );
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getUid();

        if (currentUid == null) {
            Toast.makeText(this, "Please log in to apply for an agency", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingDialog = new LoadingDialog(this);

        applicationRef = FirebaseDatabase.getInstance().getReference("agency_applications").child(currentUid);
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);

        setupClickListeners();
        loadUserProfileDefaults();
        checkApplicationStatus();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Logo Picker
        View.OnClickListener logoClickListener = v -> mGetContent.launch("image/*");
        binding.btnSelectLogo.setOnClickListener(logoClickListener);
        binding.ivAgencyLogo.setOnClickListener(logoClickListener);

        // Submit Button
        binding.btnSubmitApplication.setOnClickListener(v -> validateAndSubmitApplication());

        // Browse & Join Existing Agency
        View.OnClickListener joinAgencyListener = v -> {
            if (currentUid != null) {
                JoinAgencyDialog joinDialog = new JoinAgencyDialog(ApplyAgencyActivity.this, currentUid);
                joinDialog.show();
            }
        };
        binding.btnOpenJoinAgencyCard.setOnClickListener(joinAgencyListener);
        binding.btnJoinExistingAgency.setOnClickListener(joinAgencyListener);

        // Pending state action buttons
        binding.btnRefreshPending.setOnClickListener(v -> {
            Toast.makeText(ApplyAgencyActivity.this, "Refreshing application status...", Toast.LENGTH_SHORT).show();
            checkApplicationStatus();
        });

        binding.btnEditPending.setOnClickListener(v -> {
            binding.layoutPendingStatus.setVisibility(View.GONE);
            binding.layoutFormContainer.setVisibility(View.VISIBLE);
            if (currentApplication != null) {
                prefillFormWithApplication(currentApplication);
            }
        });

        binding.btnReApplyNow.setOnClickListener(v -> {
            binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0, binding.layoutFormContainer.getTop()));
            binding.etAgencyName.requestFocus();
        });

        // Approved state action buttons
        binding.btnGoAgencyCenter.setOnClickListener(v -> {
            Intent intent = new Intent(ApplyAgencyActivity.this, AgencyDashboardActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfileDefaults() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);

                    if (name != null && !name.isEmpty() && binding.etOwnerName.getText().toString().isEmpty()) {
                        binding.etOwnerName.setText(name);
                    }
                    if (email != null && !email.isEmpty() && binding.etOwnerEmail.getText().toString().isEmpty()) {
                        binding.etOwnerEmail.setText(email);
                    }
                    if (phone != null && !phone.isEmpty() && binding.etOwnerPhone.getText().toString().isEmpty()) {
                        binding.etOwnerPhone.setText(phone);
                    }
                    if (avatar != null && !avatar.isEmpty() && uploadedLogoUrl.isEmpty() && selectedLogoUri == null) {
                        uploadedLogoUrl = avatar;
                        Glide.with(ApplyAgencyActivity.this)
                                .load(avatar)
                                .placeholder(R.drawable.app_create_room_ic)
                                .into(binding.ivAgencyLogo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkApplicationStatus() {
        binding.layoutLoading.setVisibility(View.VISIBLE);

        applicationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutLoading.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    try {
                        currentApplication = snapshot.getValue(AgencyModel.class);
                    } catch (Exception ignored) {
                        currentApplication = null;
                    }
                    if (currentApplication == null) {
                        currentApplication = parseAgencyFromSnapshot(snapshot);
                    }
                    if (currentApplication != null) {
                        updateUiForStatus(currentApplication);
                    } else {
                        showFormForNewApplication();
                    }
                } else {
                    currentApplication = null;
                    showFormForNewApplication();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutLoading.setVisibility(View.GONE);
                showFormForNewApplication();
            }
        });
    }

    private AgencyModel parseAgencyFromSnapshot(DataSnapshot snapshot) {
        if (!snapshot.exists()) return null;
        AgencyModel model = new AgencyModel();
        model.setAgencyName(snapshot.child("agencyName").getValue(String.class));
        model.setAgencyId(snapshot.child("agencyId").getValue(String.class));
        model.setOwnerName(snapshot.child("ownerName").getValue(String.class));
        model.setOwnerPhone(snapshot.child("ownerPhone").getValue(String.class));
        model.setOwnerEmail(snapshot.child("ownerEmail").getValue(String.class));
        model.setAgencyLogo(snapshot.child("agencyLogo").getValue(String.class));
        model.setStatus(snapshot.child("status").getValue(String.class));
        model.setAdminNote(snapshot.child("adminNote").getValue(String.class));
        Long appliedAt = snapshot.child("appliedAt").getValue(Long.class);
        if (appliedAt != null) model.setAppliedAt(appliedAt);
        return model;
    }

    private void updateUiForStatus(AgencyModel app) {
        String status = app.getStatus();
        if (status == null) status = "PENDING";

        switch (status.toUpperCase()) {
            case "PENDING":
                binding.layoutPendingStatus.setVisibility(View.VISIBLE);
                binding.layoutApprovedStatus.setVisibility(View.GONE);
                binding.layoutRejectedStatus.setVisibility(View.GONE);
                binding.layoutFormContainer.setVisibility(View.GONE);

                binding.tvStatusBadgeHeader.setText("PENDING");
                binding.tvStatusBadgeHeader.setBackgroundResource(R.drawable.bg_referral_gold_pill);

                binding.tvPreviewAgencyName.setText("Agency Name: " + app.getAgencyName());
                binding.tvPreviewOwnerName.setText("Owner Name: " + app.getOwnerName());
                binding.tvPreviewOwnerPhone.setText("Owner Contact: " + app.getOwnerPhone());

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                binding.tvPreviewAppliedDate.setText("Applied On: " + sdf.format(new Date(app.getAppliedAt())));
                break;

            case "APPROVED":
                binding.layoutPendingStatus.setVisibility(View.GONE);
                binding.layoutApprovedStatus.setVisibility(View.VISIBLE);
                binding.layoutRejectedStatus.setVisibility(View.GONE);
                binding.layoutFormContainer.setVisibility(View.GONE);

                binding.tvStatusBadgeHeader.setText("APPROVED");
                binding.tvStatusBadgeHeader.setBackgroundResource(R.drawable.bg_referral_gold_pill);

                binding.tvApprovedAgencyName.setText(app.getAgencyName());
                binding.tvApprovedAgencyId.setText("Agency ID: " + app.getAgencyId());
                binding.tvApprovedOwnerName.setText("Owner: " + app.getOwnerName());

                if (app.getAgencyLogo() != null && !app.getAgencyLogo().isEmpty()) {
                    Glide.with(ApplyAgencyActivity.this)
                            .load(app.getAgencyLogo())
                            .placeholder(R.drawable.app_create_room_ic)
                            .into(binding.ivApprovedLogo);
                }
                break;

            case "REJECTED":
                binding.layoutPendingStatus.setVisibility(View.GONE);
                binding.layoutApprovedStatus.setVisibility(View.GONE);
                binding.layoutRejectedStatus.setVisibility(View.VISIBLE);
                binding.layoutFormContainer.setVisibility(View.VISIBLE);

                binding.tvStatusBadgeHeader.setText("REJECTED");
                binding.tvStatusBadgeHeader.setBackgroundResource(R.drawable.bg_agency_badge_rejected);

                String note = app.getAdminNote();
                if (note == null || note.isEmpty()) {
                    note = "Please update your details below and re-submit for admin review.";
                }
                binding.tvRejectedReason.setText("Reason: " + note);
                binding.btnSubmitApplication.setText("🔄 Re-Submit Agency Application");
                prefillFormWithApplication(app);
                break;

            case "BANNED":
                binding.layoutPendingStatus.setVisibility(View.GONE);
                binding.layoutApprovedStatus.setVisibility(View.GONE);
                binding.layoutRejectedStatus.setVisibility(View.VISIBLE);
                binding.layoutFormContainer.setVisibility(View.GONE);

                binding.tvStatusBadgeHeader.setText("BANNED");
                binding.tvStatusBadgeHeader.setBackgroundResource(R.drawable.chip_room_bg);

                binding.tvRejectedReason.setText("🚫 Your Agency has been BANNED by Admin. Contact Support for assistance.");
                break;

            default:
                showFormForNewApplication();
                break;
        }
    }

    private void showFormForNewApplication() {
        binding.layoutPendingStatus.setVisibility(View.GONE);
        binding.layoutApprovedStatus.setVisibility(View.GONE);
        binding.layoutRejectedStatus.setVisibility(View.GONE);
        binding.layoutFormContainer.setVisibility(View.VISIBLE);

        binding.tvStatusBadgeHeader.setText("NEW");
        binding.tvStatusBadgeHeader.setBackgroundResource(R.drawable.bg_referral_gold_pill);
    }

    private void prefillFormWithApplication(AgencyModel app) {
        if (app.getAgencyName() != null) binding.etAgencyName.setText(app.getAgencyName());
        if (app.getAgencyDescription() != null) binding.etAgencyDescription.setText(app.getAgencyDescription());
        if (app.getOwnerName() != null) binding.etOwnerName.setText(app.getOwnerName());
        if (app.getOwnerPhone() != null) binding.etOwnerPhone.setText(app.getOwnerPhone());
        if (app.getOwnerEmail() != null) binding.etOwnerEmail.setText(app.getOwnerEmail());
        if (app.getOwnerNationalId() != null) binding.etOwnerNationalId.setText(app.getOwnerNationalId());

        if (app.getAgencyLogo() != null && !app.getAgencyLogo().isEmpty()) {
            uploadedLogoUrl = app.getAgencyLogo();
            Glide.with(ApplyAgencyActivity.this)
                    .load(app.getAgencyLogo())
                    .placeholder(R.drawable.app_create_room_ic)
                    .into(binding.ivAgencyLogo);
        }
    }

    private void validateAndSubmitApplication() {
        String agencyName = binding.etAgencyName.getText().toString().trim();
        String agencyDescription = binding.etAgencyDescription.getText().toString().trim();
        String ownerName = binding.etOwnerName.getText().toString().trim();
        String ownerPhone = binding.etOwnerPhone.getText().toString().trim();
        String ownerEmail = binding.etOwnerEmail.getText().toString().trim();
        String ownerNationalId = binding.etOwnerNationalId.getText().toString().trim();

        if (agencyName.isEmpty()) {
            binding.etAgencyName.setError("Agency Name is required");
            binding.etAgencyName.requestFocus();
            return;
        }

        if (agencyDescription.isEmpty()) {
            binding.etAgencyDescription.setError("Agency Description is required");
            binding.etAgencyDescription.requestFocus();
            return;
        }

        if (ownerName.isEmpty()) {
            binding.etOwnerName.setError("Owner Name is required");
            binding.etOwnerName.requestFocus();
            return;
        }

        if (ownerPhone.isEmpty()) {
            binding.etOwnerPhone.setError("Owner Phone is required");
            binding.etOwnerPhone.requestFocus();
            return;
        }

        if (ownerEmail.isEmpty()) {
            binding.etOwnerEmail.setError("Owner Email is required");
            binding.etOwnerEmail.requestFocus();
            return;
        }

        loadingDialog.show("Submitting Agency Application...");

        if (selectedLogoUri != null) {
            uploadLogoAndSave(agencyName, agencyDescription, ownerName, ownerPhone, ownerEmail, ownerNationalId);
        } else if (!uploadedLogoUrl.isEmpty()) {
            saveApplicationToFirebase(uploadedLogoUrl, agencyName, agencyDescription, ownerName, ownerPhone, ownerEmail, ownerNationalId);
        } else {
            loadingDialog.dismiss();
            Toast.makeText(this, "Please select an Agency Logo!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadLogoAndSave(String agencyName, String agencyDescription, String ownerName,
                                  String ownerPhone, String ownerEmail, String ownerNationalId) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedLogoUri);
            if (inputStream == null) {
                loadingDialog.dismiss();
                Toast.makeText(this, "Failed to read logo image file", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", "agency_logo.jpg",
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
                        loadingDialog.dismiss();
                        Toast.makeText(ApplyAgencyActivity.this, "Logo upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            uploadedLogoUrl = jsonObject.getJSONObject("data").getString("url");

                            runOnUiThread(() -> saveApplicationToFirebase(uploadedLogoUrl, agencyName, agencyDescription,
                                    ownerName, ownerPhone, ownerEmail, ownerNationalId));
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                Toast.makeText(ApplyAgencyActivity.this, "Error parsing upload response", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(ApplyAgencyActivity.this, "Image server error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            loadingDialog.dismiss();
            Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void saveApplicationToFirebase(String logoUrl, String agencyName, String agencyDescription,
                                           String ownerName, String ownerPhone, String ownerEmail,
                                           String ownerNationalId) {
        long now = System.currentTimeMillis();
        String agencyId = "AGY_" + (currentUid.length() > 6 ? currentUid.substring(0, 6).toUpperCase() : currentUid.toUpperCase());

        AgencyModel agencyApp = new AgencyModel(
                agencyId,
                currentUid,
                agencyName,
                logoUrl,
                agencyDescription,
                ownerName,
                ownerPhone,
                ownerEmail,
                ownerNationalId,
                "PENDING",
                "",
                now,
                now
        );

        applicationRef.setValue(agencyApp).addOnCompleteListener(task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                // Update user reference agency status
                userRef.child("agencyStatus").setValue("PENDING");
                userRef.child("appliedAgencyName").setValue(agencyName);

                Toast.makeText(ApplyAgencyActivity.this, "🎉 Agency Application Submitted! Awaiting Admin Approval.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ApplyAgencyActivity.this, "Failed to submit application: " +
                        (task.getException() != null ? task.getException().getMessage() : "Error"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

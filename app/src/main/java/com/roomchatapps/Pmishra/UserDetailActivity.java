package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.ActivityUserDetailBinding;

public class UserDetailActivity extends AppCompatActivity {

    private ActivityUserDetailBinding binding;
    private String targetUid;
    private String currentUid;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        targetUid = getIntent().getStringExtra("uid");
        currentUid = FirebaseAuth.getInstance().getUid();

        if (targetUid == null) {
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);
        
        setupClickListeners();
        loadUserData();
        loadFollowStats();
        checkFollowStatus();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnMessage.setOnClickListener(v -> {
            String name = binding.userName.getText().toString();
            android.content.Intent intent = new android.content.Intent(UserDetailActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", targetUid);
            intent.putExtra("receiverName", name);
            startActivity(intent);
        });

        binding.btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed() || binding == null) return;
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileId = snapshot.child("profileId").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);

                    binding.userName.setText(name != null ? name : "User");
                    binding.userId.setText("ID: " + (profileId != null ? profileId : targetUid));
                    if (bio != null && !bio.isEmpty()) {
                        binding.tvAbout.setText(bio);
                    }

                    Glide.with(UserDetailActivity.this)
                            .load(avatar)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileImage);

                    // Check for equipped frame
                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    if (equippedFrame != null && !equippedFrame.isEmpty()) {
                        com.roomchatapps.Pmishra.utils.StoreManager.getStoreCatalog(targetUid, "FRAME", new com.roomchatapps.Pmishra.utils.StoreManager.CatalogCallback() {
                            @Override
                            public void onCatalogLoaded(java.util.List<com.roomchatapps.Pmishra.models.StoreItemModel> items) {
                                boolean found = false;
                                for (com.roomchatapps.Pmishra.models.StoreItemModel item : items) {
                                    if (item.getId().equals(equippedFrame)) {
                                        found = true;
                                        int resId = 0;
                                        try {
                                            resId = getResources().getIdentifier(item.getIconResName(), "drawable", getPackageName());
                                        } catch (Exception e) {}
                                        if (resId == 0) resId = R.drawable._1000092461_removebg_preview;
                                        if (binding.ivProfileFrame != null) {
                                            binding.ivProfileFrame.setImageResource(resId);
                                            binding.ivProfileFrame.setVisibility(View.VISIBLE);
                                            AnimationHelper.pulseGlowAnimation(binding.ivProfileFrame);
                                        }
                                        break;
                                    }
                                }
                                if (!found && binding.ivProfileFrame != null) {
                                    binding.ivProfileFrame.setImageResource(R.drawable._1000092461_removebg_preview);
                                    binding.ivProfileFrame.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (binding.ivProfileFrame != null) {
                                    binding.ivProfileFrame.setImageResource(R.drawable._1000092461_removebg_preview);
                                    binding.ivProfileFrame.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } else {
                        if (binding.ivProfileFrame != null) {
                            binding.ivProfileFrame.setImageResource(R.drawable._1000092461_removebg_preview);
                            binding.ivProfileFrame.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadFollowStats() {
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow").child(targetUid);
        
        followRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.tvFollowCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        followRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.tvFansCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkFollowStatus() {
        if (currentUid == null) return;
        
        FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUid).child("following").child(targetUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            binding.btnFollow.setText("Following");
                            binding.btnFollow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                        } else {
                            binding.btnFollow.setText("Follow");
                            binding.btnFollow.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void toggleFollow() {
        if (currentUid == null) return;

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUid).child("following").child(targetUid);
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(targetUid).child("followers").child(currentUid);

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followingRef.removeValue();
                    followersRef.removeValue();
                } else {
                    followingRef.setValue(true);
                    followersRef.setValue(true);
                    com.roomchatapps.Pmishra.utils.NotificationHelper.sendFollowNotification(targetUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

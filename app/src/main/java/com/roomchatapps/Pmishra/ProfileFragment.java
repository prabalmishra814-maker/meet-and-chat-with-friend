package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Profile Avatar Scale + Fade-In entrance animation
        AnimationHelper.scaleIn(binding.profileImage, 600);
        AnimationHelper.fadeIn(binding.userName, 500);
        AnimationHelper.fadeIn(binding.userId, 600);
        
        // Staggered fade-in for statistics and cards
        if (binding.statsLayout != null) {
            binding.statsLayout.setAlpha(0f);
            binding.statsLayout.setTranslationY(20f);
            binding.statsLayout.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start();
        }
        
        if (binding.cardWallet != null) {
            binding.cardWallet.setAlpha(0f);
            binding.cardWallet.setTranslationY(30f);
            binding.cardWallet.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(350).start();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserData();
            loadFollowStats(uid);
        }

        setupClickListeners();
    }

    private void loadFollowStats(String uid) {
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow").child(uid);
        
        // Followers count
        followRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    binding.tvFollowCount.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Following count
        followRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    binding.tvFansCount.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUserData() {
        if (userRef == null) return;

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String uid = snapshot.child("uid").getValue(String.class);
                    String profileId = snapshot.child("profileId").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);

                    if (bio != null && !bio.isEmpty()) {
                        binding.tvBio.setText(bio);
                    } else {
                        binding.tvBio.setText("No bio available.");
                    }

                    if ((profileId == null || profileId.isEmpty()) && uid != null) {
                        profileId = String.valueOf(100000 + Math.abs((long) uid.hashCode()) % 900000);
                        userRef.child("profileId").setValue(profileId);
                    }
                    
                    String level = snapshot.child("level").getValue(String.class);
                    if (level == null || level.isEmpty() || "0".equals(level)) {
                        level = "1";
                        userRef.child("level").setValue("1");
                    }
                    if (binding.tvUserLevel != null) {
                        binding.tvUserLevel.setText("Lv." + level);
                    }

                    String gender = snapshot.child("gender").getValue(String.class);
                    if (binding.ivUserGender != null) {
                        if ("Male".equalsIgnoreCase(gender)) {
                            binding.ivUserGender.setVisibility(View.VISIBLE);
                            binding.ivUserGender.setImageResource(R.drawable.common_gender_male_blue_16_ic);
                        } else if ("Female".equalsIgnoreCase(gender)) {
                            binding.ivUserGender.setVisibility(View.VISIBLE);
                            binding.ivUserGender.setImageResource(R.drawable.common_gender_female_pink_16_ic);
                        } else {
                            binding.ivUserGender.setVisibility(View.GONE);
                        }
                    }

                    Object coinsObj = snapshot.child("coins").getValue();
                    String coins = coinsObj != null ? String.valueOf(coinsObj) : "0";

                    binding.userName.setText(name != null ? name : "User");
                    binding.userId.setText("ID: " + (profileId != null ? profileId : "N/A"));

                    String currentCoins = binding.tvCoins.getText().toString();
                    if (!currentCoins.isEmpty() && !currentCoins.equals(coins)) {
                        AnimationHelper.animateCoinUpdate(null, binding.tvCoins, coins);
                    } else {
                        binding.tvCoins.setText(coins);
                    }

                    if (avatar != null && !avatar.isEmpty()) {
                        Glide.with(ProfileFragment.this)
                                .load(avatar)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(binding.profileImage);
                    }

                    // Check for equipped frame
                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    if (equippedFrame != null && !equippedFrame.isEmpty()) {
                        // We need to resolve the iconResName from StoreManager
                        com.roomchatapps.Pmishra.utils.StoreManager.getStoreCatalog(uid, "FRAME", new com.roomchatapps.Pmishra.utils.StoreManager.CatalogCallback() {
                            @Override
                            public void onCatalogLoaded(java.util.List<com.roomchatapps.Pmishra.models.StoreItemModel> items) {
                                if (!isAdded() || getContext() == null || binding == null) return;
                                boolean found = false;
                                for (com.roomchatapps.Pmishra.models.StoreItemModel item : items) {
                                    if (item.getId().equals(equippedFrame)) {
                                        found = true;
                                        int resId = 0;
                                        try {
                                            if (getContext() != null) {
                                                resId = getResources().getIdentifier(item.getIconResName(), "drawable", getContext().getPackageName());
                                            }
                                        } catch (Exception e) {}
                                        if (resId == 0) resId = R.drawable._1000092519_removebg_preview;
                                        if (binding.ivProfileFrame != null) {
                                            binding.ivProfileFrame.setImageResource(resId);
                                            binding.ivProfileFrame.setVisibility(View.VISIBLE);
                                            AnimationHelper.pulseGlowAnimation(binding.ivProfileFrame);
                                        }
                                        break;
                                    }
                                }
                                if (!found && binding.ivProfileFrame != null) {
                                    binding.ivProfileFrame.setImageResource(R.drawable._1000092519_removebg_preview);
                                    binding.ivProfileFrame.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (binding.ivProfileFrame != null) {
                                    binding.ivProfileFrame.setImageResource(R.drawable._1000092519_removebg_preview);
                                    binding.ivProfileFrame.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } else {
                        if (binding.ivProfileFrame != null) {
                            binding.ivProfileFrame.setImageResource(R.drawable._1000092519_removebg_preview);
                            binding.ivProfileFrame.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.layoutFollowers.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(getActivity(), FollowListActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("type", "followers");
                startActivity(intent);
            }
        });

        binding.layoutFollowing.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(getActivity(), FollowListActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("type", "following");
                startActivity(intent);
            }
        });

        View.OnClickListener openWallet = v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), WalletActivity.class);
                startActivity(intent);
            }
        };

        binding.cardWallet.setOnClickListener(openWallet);
        binding.cardBuyCoin.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(getActivity(), CoinRechargeActivity.class);
                startActivity(intent);
            }
        });
        binding.cardHistory.setOnClickListener(openWallet);
        binding.cardStore.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), StoreActivity.class);
                startActivity(intent);
            }
        });
        
        binding.cardSetting.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        binding.headerLayout.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(binding.headerLayout);
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        binding.btnRefer.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ReferralActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message + " feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.roomchatapps.Pmishra;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.CpHouseThemeAdapter;
import com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.databinding.FragmentLoveHouseBoundBinding;
import com.roomchatapps.Pmishra.models.CpHouseThemeModel;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;
import com.roomchatapps.Pmishra.utils.CpBindingManager;
import com.roomchatapps.Pmishra.utils.CpIntimacyManager;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.util.ArrayList;
import java.util.List;

public class LoveHouseBoundFragment extends Fragment {

    private FragmentLoveHouseBoundBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference cpRef;
    private String currentUid;
    private String partnerUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoveHouseBoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.headerContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    0
            );
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
            cpRef = FirebaseDatabase.getInstance().getReference("cp_bindings").child(currentUid);
        }

        setupToolbar();
        loadCurrentUserData();
        loadCpBoundData();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        if (binding.btnWardrobe != null) {
            binding.btnWardrobe.setOnClickListener(v -> showCpHouseThemeDialog());
        }

        if (binding.btnMenu != null) {
            binding.btnMenu.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Love House Options", Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.btnProposal.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), ProposalActivity.class);
                if (!TextUtils.isEmpty(partnerUid)) {
                    intent.putExtra("partnerUid", partnerUid);
                }
                startActivity(intent);
            }
        });

        View.OnClickListener openCpLevelListener = v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), CpLevelActivity.class));
            }
        };

        if (binding.layoutCpLevelBadge != null) {
            binding.layoutCpLevelBadge.setOnClickListener(openCpLevelListener);
        }
        if (binding.tvCpLevel != null) {
            binding.tvCpLevel.setOnClickListener(openCpLevelListener);
        }

        binding.layoutBottomSendGifts.setOnClickListener(v -> showGiftStoreDialog());
    }

    private void loadCurrentUserData() {
        if (currentUid == null || userRef == null) return;

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("avatar").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("photoUrl").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("image").getValue(String.class);
                    if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("userIcon").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        binding.tvUserName.setText(name);
                    } else {
                        binding.tvUserName.setText("User");
                    }

                    if (!TextUtils.isEmpty(avatar) && getContext() != null) {
                        Glide.with(getContext())
                                .load(avatar)
                                .placeholder(R.drawable.img_20260904_135725)
                                .error(R.drawable.img_20260904_135725)
                                .into(binding.ivUserAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCpBoundData() {
        if (cpRef == null) return;

        cpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                if (snapshot.exists() && snapshot.hasChild("partnerUid")) {
                    partnerUid = snapshot.child("partnerUid").getValue(String.class);
                    String partnerName = snapshot.child("partnerName").getValue(String.class);
                    String partnerAvatar = snapshot.child("partnerAvatar").getValue(String.class);
                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    if (timestamp == null || timestamp <= 0) {
                        timestamp = System.currentTimeMillis();
                        if (cpRef != null) {
                            cpRef.child("timestamp").setValue(timestamp);
                        }
                    }

                    long days = CpBindingManager.getBindingDays(timestamp);
                    binding.tvDaysCount.setText(String.valueOf(days));

                    if (!TextUtils.isEmpty(partnerName)) {
                        binding.tvPartnerName.setText(partnerName);
                    } else {
                        binding.tvPartnerName.setText("Partner");
                    }

                    if (!TextUtils.isEmpty(partnerAvatar) && getContext() != null) {
                        Glide.with(getContext())
                                .load(partnerAvatar)
                                .placeholder(R.drawable.img_20260904_135725)
                                .error(R.drawable.img_20260904_135725)
                                .into(binding.ivPartnerAvatar);
                    }

                    if (snapshot.hasChild("houseTheme")) {
                        String savedTheme = snapshot.child("houseTheme").getValue(String.class);
                        if (!TextUtils.isEmpty(savedTheme)) {
                            equippedThemeId = savedTheme;
                            if ("pink_aisle".equals(savedTheme)) {
                                binding.getRoot().setBackgroundResource(R.drawable.bg_theme_pink_aisle);
                            } else if ("purple_palace".equals(savedTheme)) {
                                binding.getRoot().setBackgroundResource(R.drawable.bg_theme_purple_palace);
                            } else {
                                binding.getRoot().setBackgroundResource(R.drawable.bg_theme_floral_arch);
                            }
                        }
                    }

                    loadCpIntimacyAndBlessingStats(partnerUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String equippedThemeId = "floral_arch";

    private void showCpHouseThemeDialog() {
        if (getContext() == null || getActivity() == null || getActivity().isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cp_house_theme, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        RecyclerView rvThemes = dialogView.findViewById(R.id.rvThemes);

        if (rvThemes != null) {
            List<CpHouseThemeModel> themeList = new ArrayList<>();

            // Card 1: Custom Upload Theme
            themeList.add(new CpHouseThemeModel("custom", "Custom Theme", 0, 10, true, true, false));

            // Card 2: Floral Arch Theme (LV.6)
            themeList.add(new CpHouseThemeModel("floral_arch", "Floral Arch", R.drawable.bg_theme_floral_arch, 6, false, false, "floral_arch".equals(equippedThemeId)));

            // Card 3: Pink Aisle Theme (LV.7)
            themeList.add(new CpHouseThemeModel("pink_aisle", "Pink Aisle", R.drawable.bg_theme_pink_aisle, 7, false, false, "pink_aisle".equals(equippedThemeId)));

            // Card 4: Purple Palace Theme (LV.8)
            themeList.add(new CpHouseThemeModel("purple_palace", "Purple Palace", R.drawable.bg_theme_purple_palace, 8, false, false, "purple_palace".equals(equippedThemeId)));

            CpHouseThemeAdapter themeAdapter = new CpHouseThemeAdapter(themeList, (theme, position) -> {
                if (theme.isCustomAdd() || theme.isLocked()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Requires CP Level " + theme.getUnlockLevel() + " to unlock!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                equippedThemeId = theme.getId();
                if (binding != null) {
                    binding.getRoot().setBackgroundResource(theme.getDrawableRes());
                }

                if (cpRef != null) {
                    cpRef.child("houseTheme").setValue(equippedThemeId);
                }

                if (getContext() != null) {
                    Toast.makeText(getContext(), "Equipped " + theme.getName() + " theme!", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            });

            rvThemes.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvThemes.setAdapter(themeAdapter);
        }

        dialog.show();
    }

    private void loadCpIntimacyAndBlessingStats(String pUid) {
        if (TextUtils.isEmpty(pUid) || TextUtils.isEmpty(currentUid)) return;

        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values")
                .child(currentUid).child(pUid);

        guardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                long intimacy = 0;
                if (snapshot.exists()) {
                    Long val = snapshot.child("value").getValue(Long.class);
                    if (val != null) {
                        intimacy = val;
                    }
                }

                binding.tvIntimacyScore.setText("💖 " + intimacy + " Intimacy");

                int level = CpIntimacyManager.calculateCpLevel(intimacy);
                binding.tvCpLevel.setText("LV." + level);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference blessingRef = FirebaseDatabase.getInstance().getReference("cp_blessing_values")
                .child(currentUid).child(pUid);

        blessingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                long blessing = 0;
                if (snapshot.exists()) {
                    Long val = snapshot.child("value").getValue(Long.class);
                    if (val != null) {
                        blessing = val;
                    }
                }

                binding.tvBlessingScore.setText("🎁 " + blessing + " Blessing");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showGiftStoreDialog() {
        if (getContext() == null || getActivity() == null || getActivity().isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_gift_store, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        RecyclerView rvGiftRecipients = dialogView.findViewById(R.id.rvGiftRecipients);
        RecyclerView rvGifts = dialogView.findViewById(R.id.rvGifts);
        View btnSendAction = dialogView.findViewById(R.id.btnSendGiftAction);
        TextView tvGiftDialogCoins = dialogView.findViewById(R.id.tvGiftDialogCoins);
        View llCoinBalance = dialogView.findViewById(R.id.llCoinBalance);

        if (tvGiftDialogCoins != null && currentUid != null) {
            WalletManager.getUserCoins(currentUid, balance -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvGiftDialogCoins.setText(String.valueOf(balance)));
                }
            });
        }

        if (llCoinBalance != null) {
            llCoinBalance.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), CoinRechargeActivity.class));
            });
        }

        if (rvGiftRecipients != null) {
            List<GiftRecipientModel> recipientList = new ArrayList<>();
            String partnerName = binding.tvPartnerName.getText().toString();
            GiftRecipientModel targetItem = new GiftRecipientModel(
                    !TextUtils.isEmpty(partnerUid) ? partnerUid : "cp_partner",
                    !TextUtils.isEmpty(partnerName) ? partnerName : "CP Partner",
                    "",
                    "1",
                    true,
                    false
            );
            recipientList.add(targetItem);
            GiftRecipientAdapter recipientAdapter = new GiftRecipientAdapter(recipientList);
            rvGiftRecipients.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvGiftRecipients.setAdapter(recipientAdapter);
        }

        List<GiftStoreAdapter.GiftStoreItem> allGifts = GiftCatalog.getAllGifts();
        final GiftStoreAdapter.GiftStoreItem[] selectedGift = {allGifts != null && !allGifts.isEmpty() ? allGifts.get(0) : null};

        if (rvGifts != null && allGifts != null) {
            GiftStoreAdapter giftAdapter = new GiftStoreAdapter(allGifts);
            giftAdapter.setOnGiftSelectedListener((item, position, isReSelected) -> selectedGift[0] = item);
            rvGifts.setLayoutManager(new GridLayoutManager(getContext(), 4));
            rvGifts.setAdapter(giftAdapter);
        }

        if (btnSendAction != null) {
            btnSendAction.setOnClickListener(v -> {
                if (selectedGift[0] == null) {
                    if (getContext() != null) Toast.makeText(getContext(), "Please select a gift", Toast.LENGTH_SHORT).show();
                    return;
                }

                long giftCost = selectedGift[0].cost;
                if (currentUid == null) {
                    if (getContext() != null) Toast.makeText(getContext(), "Please login to send gifts", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pUid = !TextUtils.isEmpty(partnerUid) ? partnerUid : "cp_partner";

                WalletManager.spendCoinsForGift(currentUid, pUid, giftCost, selectedGift[0].name, new WalletManager.WalletCallback() {
                    @Override
                    public void onSuccess(String message, long newCoinBalance) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Sent " + selectedGift[0].name + "! Intimacy increased!", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                }
                                if (error != null && error.toLowerCase().contains("insufficient") && getContext() != null) {
                                    startActivity(new Intent(getContext(), CoinRechargeActivity.class));
                                }
                            });
                        }
                    }
                });
            });
        }

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

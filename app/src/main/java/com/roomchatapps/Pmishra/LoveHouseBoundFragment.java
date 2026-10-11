package com.roomchatapps.Pmishra;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.CpHouseThemeAdapter;
import com.roomchatapps.Pmishra.adapters.CpRecentGiftAdapter;
import com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.RingAdapter;
import com.roomchatapps.Pmishra.databinding.FragmentLoveHouseBoundBinding;
import com.roomchatapps.Pmishra.models.CpHouseThemeModel;
import com.roomchatapps.Pmishra.models.CpRecentGiftModel;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;
import com.roomchatapps.Pmishra.models.RingModel;
import com.roomchatapps.Pmishra.utils.CpBindingManager;
import com.roomchatapps.Pmishra.utils.CpIntimacyManager;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoveHouseBoundFragment extends Fragment {

    private FragmentLoveHouseBoundBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference cpRef;
    private DatabaseReference partnerUserRef;
    private ValueEventListener partnerUserListener;
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
        loadCpRingsData();
        loadCpRecentGiftsData();
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
            binding.btnMenu.setOnClickListener(v -> showLoveHousePopupMenu(v));
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

                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    FrameUtils.displayFrame(getContext(), equippedFrame, binding.ivUserFrame, binding.svgaUserFrame);
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

                    if (!TextUtils.isEmpty(partnerUid)) {
                        if (partnerUserRef != null && partnerUserListener != null) {
                            partnerUserRef.removeEventListener(partnerUserListener);
                        }
                        partnerUserRef = FirebaseDatabase.getInstance().getReference("users").child(partnerUid);
                        partnerUserListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot partnerSnap) {
                                if (!isAdded() || binding == null) return;
                                if (partnerSnap.exists()) {
                                    String pName = partnerSnap.child("name").getValue(String.class);
                                    String pAvatar = partnerSnap.child("avtar").getValue(String.class);
                                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerSnap.child("avatar").getValue(String.class);
                                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerSnap.child("photoUrl").getValue(String.class);
                                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerSnap.child("image").getValue(String.class);
                                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerSnap.child("userIcon").getValue(String.class);

                                    String pFrame = partnerSnap.child("equipped_frame").getValue(String.class);

                                    if (!TextUtils.isEmpty(pName)) {
                                        binding.tvPartnerName.setText(pName);
                                    }

                                    if (!TextUtils.isEmpty(pAvatar) && getContext() != null) {
                                        Glide.with(getContext())
                                                .load(pAvatar)
                                                .placeholder(R.drawable.img_20260904_135725)
                                                .error(R.drawable.img_20260904_135725)
                                                .into(binding.ivPartnerAvatar);
                                    }

                                    FrameUtils.displayFrame(getContext(), pFrame, binding.ivPartnerFrame, binding.svgaPartnerFrame);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        };
                        partnerUserRef.addValueEventListener(partnerUserListener);
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

    private void showLoveHousePopupMenu(View anchorView) {
        if (getContext() == null || anchorView == null) return;

        View popupView = getLayoutInflater().inflate(R.layout.popup_love_house_menu, null, false);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(16f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        View itemBreakUp = popupView.findViewById(R.id.itemCpBreakUp);
        View itemRule = popupView.findViewById(R.id.itemRule);

        if (itemBreakUp != null) {
            itemBreakUp.setOnClickListener(v -> {
                popupWindow.dismiss();
                confirmCpBreakUp();
            });
        }

        if (itemRule != null) {
            itemRule.setOnClickListener(v -> {
                popupWindow.dismiss();
                showCpHelpDialog();
            });
        }

        int xOffset = -dpToPx(100);
        int yOffset = dpToPx(8);
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }

    private void confirmCpBreakUp() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Break Up CP")
                .setMessage("Are you sure you want to break up your CP relationship?")
                .setPositiveButton("Break Up", (dialog, which) -> {
                    if (currentUid != null) {
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("cp_bindings");
                        db.child(currentUid).removeValue();
                        if (!TextUtils.isEmpty(partnerUid)) {
                            db.child(partnerUid).removeValue();
                        }

                        DatabaseReference inviteDb = FirebaseDatabase.getInstance().getReference("cp_invitations");
                        inviteDb.child(currentUid).removeValue();
                        if (!TextUtils.isEmpty(partnerUid)) {
                            inviteDb.child(partnerUid).removeValue();
                        }
                    }
                    Toast.makeText(getContext(), "CP relationship broken up", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showCpHelpDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Love House & CP Rules")
                .setMessage("1. Guard value between two friends must reach 600 or more to become a CP.\n\n" +
                        "2. Send gifts and interact together in voice chat rooms to increase your intimacy & guard value.\n\n" +
                        "3. When you bind with a CP, your CP status and special animations will be unlocked!\n\n" +
                        "4. You can break up your CP relationship at any time from this menu.")
                .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private int dpToPx(int dp) {
        if (getContext() == null) return dp;
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }

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

    private void loadCpRingsData() {
        if (currentUid == null) return;

        DatabaseReference proposalsRef = FirebaseDatabase.getInstance().getReference("cp_proposals");
        proposalsRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                List<RingModel> ringList = new ArrayList<>();
                if (snapshot.exists()) {
                    Integer ringRes = snapshot.child("ringRes").getValue(Integer.class);
                    if (ringRes != null && ringRes != 0) {
                        ringList.add(new RingModel("prop_ring", "Proposal Ring", ringRes, "", 0, true));
                    }
                }

                DatabaseReference cpRingsRef = FirebaseDatabase.getInstance().getReference("cp_rings").child(currentUid);
                cpRingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ringSnap) {
                        if (!isAdded() || binding == null) return;

                        if (ringSnap.exists()) {
                            for (DataSnapshot ds : ringSnap.getChildren()) {
                                try {
                                    String name = ds.child("name").getValue(String.class);
                                    Integer iconRes = ds.child("iconRes").getValue(Integer.class);
                                    if (iconRes != null && iconRes != 0) {
                                        ringList.add(new RingModel(ds.getKey(), !TextUtils.isEmpty(name) ? name : "CP Ring", iconRes, "", 0, false));
                                    }
                                } catch (Exception ignored) {}
                            }
                        }

                        if (!ringList.isEmpty()) {
                            binding.tvNoRingData.setVisibility(View.GONE);
                            binding.rvCpRings.setVisibility(View.VISIBLE);
                            RingAdapter ringAdapter = new RingAdapter(ringList, null);
                            binding.rvCpRings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                            binding.rvCpRings.setAdapter(ringAdapter);
                        } else {
                            binding.rvCpRings.setVisibility(View.GONE);
                            binding.tvNoRingData.setText("");
                            binding.tvNoRingData.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCpRecentGiftsData() {
        if (currentUid == null) return;

        DatabaseReference recentGiftsRef = FirebaseDatabase.getInstance().getReference("cp_recent_gifts").child(currentUid);
        recentGiftsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                List<CpRecentGiftModel> giftList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            CpRecentGiftModel gift = ds.getValue(CpRecentGiftModel.class);
                            if (gift != null) {
                                if (gift.getId() == null) gift.setId(ds.getKey());
                                giftList.add(0, gift);
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (!giftList.isEmpty()) {
                    binding.tvNoGiftData.setVisibility(View.GONE);
                    binding.rvCpRecentGifts.setVisibility(View.VISIBLE);
                    CpRecentGiftAdapter giftAdapter = new CpRecentGiftAdapter(giftList);
                    binding.rvCpRecentGifts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    binding.rvCpRecentGifts.setAdapter(giftAdapter);
                } else {
                    binding.rvCpRecentGifts.setVisibility(View.GONE);
                    binding.tvNoGiftData.setVisibility(View.VISIBLE);
                }
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

        TabLayout tabCategory = dialogView.findViewById(R.id.tabCategoryGifts);
        String[] categories = new String[]{"Gift", "Relationship", "Nation Flag", "Luxury", "Customization"};

        List<GiftStoreAdapter.GiftStoreItem> allGifts = GiftCatalog.getAllGifts();

        List<GiftStoreAdapter.GiftStoreItem> initialCategoryGifts = new ArrayList<>();
        if (allGifts != null) {
            for (GiftStoreAdapter.GiftStoreItem item : allGifts) {
                if ("Gift".equalsIgnoreCase(item.category)) {
                    initialCategoryGifts.add(item);
                }
            }
            if (initialCategoryGifts.isEmpty()) {
                initialCategoryGifts.addAll(allGifts);
            }
        }

        final GiftStoreAdapter.GiftStoreItem[] selectedGift = {!initialCategoryGifts.isEmpty() ? initialCategoryGifts.get(0) : null};

        // Quantity Selection Chips
        final int[] selectedQuantity = new int[]{1};
        TextView chipQty1 = dialogView.findViewById(R.id.chipQty1);
        TextView chipQty7 = dialogView.findViewById(R.id.chipQty7);
        TextView chipQty77 = dialogView.findViewById(R.id.chipQty77);
        TextView chipQty777 = dialogView.findViewById(R.id.chipQty777);
        TextView tvSendActionText = dialogView.findViewById(R.id.tvSendActionText);

        View.OnClickListener qtyClickListener = v -> {
            if (chipQty1 != null) chipQty1.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty7 != null) chipQty7.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty77 != null) chipQty77.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty777 != null) chipQty777.setBackgroundResource(R.drawable.bg_room_chat_pill);

            v.setBackgroundResource(R.drawable.bg_send_button_glow);
            int qty = 1;
            if (v.getId() == R.id.chipQty7) qty = 7;
            else if (v.getId() == R.id.chipQty77) qty = 77;
            else if (v.getId() == R.id.chipQty777) qty = 777;

            selectedQuantity[0] = qty;
            if (tvSendActionText != null) {
                tvSendActionText.setText(qty > 1 ? "Send (x" + qty + ")" : "Send");
            }
        };

        if (chipQty1 != null) chipQty1.setOnClickListener(qtyClickListener);
        if (chipQty7 != null) chipQty7.setOnClickListener(qtyClickListener);
        if (chipQty77 != null) chipQty77.setOnClickListener(qtyClickListener);
        if (chipQty777 != null) chipQty777.setOnClickListener(qtyClickListener);

        if (rvGifts != null && !initialCategoryGifts.isEmpty()) {
            GiftStoreAdapter giftAdapter = new GiftStoreAdapter(initialCategoryGifts);
            giftAdapter.setOnGiftSelectedListener((item, position, isReSelected) -> {
                if (isReSelected) {
                    selectedQuantity[0]++;
                } else {
                    selectedQuantity[0] = 1;
                }

                int qty = selectedQuantity[0];
                if (chipQty1 != null) chipQty1.setBackgroundResource(qty == 1 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);
                if (chipQty7 != null) chipQty7.setBackgroundResource(qty == 7 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);
                if (chipQty77 != null) chipQty77.setBackgroundResource(qty == 77 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);
                if (chipQty777 != null) chipQty777.setBackgroundResource(qty == 777 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);

                if (tvSendActionText != null) {
                    tvSendActionText.setText(qty > 1 ? "Send (x" + qty + ")" : "Send");
                }
                selectedGift[0] = item;
            });
            rvGifts.setLayoutManager(new GridLayoutManager(getContext(), 4));
            rvGifts.setAdapter(giftAdapter);

            if (tabCategory != null) {
                tabCategory.removeAllTabs();
                for (String cat : categories) {
                    tabCategory.addTab(tabCategory.newTab().setText(cat));
                }

                tabCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        String selectedCategory = categories[tab.getPosition()];
                        List<GiftStoreAdapter.GiftStoreItem> filtered = new ArrayList<>();
                        if (allGifts != null) {
                            for (GiftStoreAdapter.GiftStoreItem item : allGifts) {
                                if (item.category.equalsIgnoreCase(selectedCategory)) {
                                    filtered.add(item);
                                }
                            }
                        }
                        if (filtered.isEmpty() && allGifts != null) {
                            filtered.addAll(allGifts);
                        }
                        giftAdapter.updateItems(filtered);
                        rvGifts.scrollToPosition(0);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });
            }
        }

        if (btnSendAction != null) {
            btnSendAction.setOnClickListener(v -> {
                if (selectedGift[0] == null) {
                    if (getContext() != null) Toast.makeText(getContext(), "Please select a gift", Toast.LENGTH_SHORT).show();
                    return;
                }

                int qty = selectedQuantity[0];
                long totalCost = selectedGift[0].cost * qty;
                String giftDisplayName = selectedGift[0].name + (qty > 1 ? " (x" + qty + ")" : "");

                if (currentUid == null) {
                    if (getContext() != null) Toast.makeText(getContext(), "Please login to send gifts", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pUid = !TextUtils.isEmpty(partnerUid) ? partnerUid : "cp_partner";

                WalletManager.spendCoinsForGift(currentUid, pUid, totalCost, giftDisplayName, new WalletManager.WalletCallback() {
                    @Override
                    public void onSuccess(String message, long newCoinBalance) {
                        DatabaseReference recentRef = FirebaseDatabase.getInstance().getReference("cp_recent_gifts");
                        String giftKey = recentRef.child(currentUid).push().getKey();
                        if (giftKey != null) {
                            Map<String, Object> giftData = new HashMap<>();
                            giftData.put("id", giftKey);
                            giftData.put("giftName", giftDisplayName);
                            giftData.put("giftCost", totalCost);
                            giftData.put("senderUid", currentUid);
                            giftData.put("senderName", binding != null ? binding.tvUserName.getText().toString() : "Partner");
                            giftData.put("iconRes", selectedGift[0].iconRes);
                            giftData.put("timestamp", System.currentTimeMillis());

                            recentRef.child(currentUid).child(giftKey).setValue(giftData);
                            if (!TextUtils.isEmpty(partnerUid)) {
                                recentRef.child(partnerUid).child(giftKey).setValue(giftData);
                            }
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Sent " + giftDisplayName + "! Intimacy increased!", Toast.LENGTH_SHORT).show();
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
        if (partnerUserRef != null && partnerUserListener != null) {
            partnerUserRef.removeEventListener(partnerUserListener);
        }
        super.onDestroyView();
        binding = null;
    }
}

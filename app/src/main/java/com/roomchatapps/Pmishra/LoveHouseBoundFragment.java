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
import com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.databinding.FragmentLoveHouseBoundBinding;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;
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
            binding.btnWardrobe.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "CP Wardrobe & Outfits", Toast.LENGTH_SHORT).show();
                }
            });
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
                Toast.makeText(getContext(), "Proposal sent to CP partner!", Toast.LENGTH_SHORT).show();
            }
        });

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

                    long days = 96;
                    if (timestamp != null && timestamp > 0) {
                        long diff = (System.currentTimeMillis() - timestamp) / (1000L * 60 * 60 * 24);
                        if (diff > 0) days = diff;
                    }
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

                    loadCpIntimacyAndBlessingStats(partnerUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCpIntimacyAndBlessingStats(String pUid) {
        if (TextUtils.isEmpty(pUid) || TextUtils.isEmpty(currentUid)) return;

        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values")
                .child(currentUid).child(pUid);

        guardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                long intimacy = 35429;
                if (snapshot.exists()) {
                    Long val = snapshot.child("value").getValue(Long.class);
                    if (val != null && val > 0) {
                        intimacy = val;
                    }
                }

                binding.tvIntimacyScore.setText("💖 " + intimacy + " Intimacy");
                binding.tvBlessingScore.setText("🎁 0 Blessing");

                String levelStr = "LV.1";
                if (intimacy >= 30000) {
                    levelStr = "LV.4";
                } else if (intimacy >= 15000) {
                    levelStr = "LV.3";
                } else if (intimacy >= 5000) {
                    levelStr = "LV.2";
                }
                binding.tvCpLevel.setText(levelStr);
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

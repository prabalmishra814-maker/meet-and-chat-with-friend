package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.adapters.CpRewardAdapter;
import com.roomchatapps.Pmishra.adapters.IntimacyFriendAdapter;
import com.roomchatapps.Pmishra.adapters.SupporterAdapter;
import com.roomchatapps.Pmishra.models.CpLevelRewardModel;
import com.roomchatapps.Pmishra.models.IntimacyFriendModel;
import com.roomchatapps.Pmishra.models.SupporterModel;
import com.roomchatapps.Pmishra.utils.CpBindingManager;
import com.roomchatapps.Pmishra.utils.CpIntimacyManager;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IntimacyFragment extends Fragment {

    private static final String ARG_TARGET_UID = "target_uid";
    private static final String ARG_IS_SELF = "is_self";

    private String targetUid;
    private boolean isSelf = true;

    private ShapeableImageView ivCoupleLeftAvatar, ivCoupleRightAvatar;
    private TextView tvCoupleLeftName, tvCoupleRightName, tvCoupleLevelBadgeText, tvCoupleDuration;
    private View btnAddCouplePartner;
    private RecyclerView rvBestFriends, rvSupporters, rvIntimacyFriends, rvCpRewards;

    private SupporterAdapter supporterAdapter;
    private IntimacyFriendAdapter bestFriendAdapter, intimacyFriendAdapter;
    private CpRewardAdapter cpRewardAdapter;

    private final List<SupporterModel> supporterList = new ArrayList<>();
    private final List<IntimacyFriendModel> bestFriendsList = new ArrayList<>();
    private final List<IntimacyFriendModel> intimacyFriendsList = new ArrayList<>();
    private final List<CpLevelRewardModel> cpRewardList = new ArrayList<>();

    private SVGAImageView activePreviewSvgaPlayer;

    public IntimacyFragment() {
        // Required empty public constructor
    }

    public static IntimacyFragment newInstance(String targetUid, boolean isSelf) {
        IntimacyFragment fragment = new IntimacyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TARGET_UID, targetUid);
        args.putBoolean(ARG_IS_SELF, isSelf);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetUid = getArguments().getString(ARG_TARGET_UID);
            isSelf = getArguments().getBoolean(ARG_IS_SELF, true);
        } else {
            targetUid = FirebaseAuth.getInstance().getUid();
            isSelf = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intimacy, container, false);

        initViews(view);
        loadIntimacyData();

        return view;
    }

    private void initViews(View view) {
        View cardLoveStore = view.findViewById(R.id.cardLoveStore);
        View btnGoLoveStore = view.findViewById(R.id.btnGoLoveStore);
        View.OnClickListener openStore = v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), StoreActivity.class);
                startActivity(intent);
            }
        };
        if (cardLoveStore != null) cardLoveStore.setOnClickListener(openStore);
        if (btnGoLoveStore != null) btnGoLoveStore.setOnClickListener(openStore);

        View.OnClickListener openLoveHouse = v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoveHouseActivity.class);
                if (targetUid != null && !targetUid.isEmpty()) {
                    intent.putExtra("targetUid", targetUid);
                }
                startActivity(intent);
            }
        };

        ivCoupleLeftAvatar = view.findViewById(R.id.ivCoupleLeftAvatar);
        ivCoupleRightAvatar = view.findViewById(R.id.ivCoupleRightAvatar);
        tvCoupleLeftName = view.findViewById(R.id.tvCoupleLeftName);
        tvCoupleRightName = view.findViewById(R.id.tvCoupleRightName);
        tvCoupleLevelBadgeText = view.findViewById(R.id.tvCoupleLevelBadgeText);
        tvCoupleDuration = view.findViewById(R.id.tvCoupleDuration);
        btnAddCouplePartner = view.findViewById(R.id.btnAddCouplePartner);

        if (btnAddCouplePartner != null) {
            btnAddCouplePartner.setOnClickListener(openLoveHouse);
        }

        rvBestFriends = view.findViewById(R.id.rvBestFriends);
        if (rvBestFriends != null) {
            rvBestFriends.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            bestFriendAdapter = new IntimacyFriendAdapter(getContext(), bestFriendsList);
            rvBestFriends.setAdapter(bestFriendAdapter);
        }

        rvSupporters = view.findViewById(R.id.rvSupporters);
        if (rvSupporters != null) {
            rvSupporters.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            supporterAdapter = new SupporterAdapter(getContext(), supporterList);
            rvSupporters.setAdapter(supporterAdapter);
        }

        rvIntimacyFriends = view.findViewById(R.id.rvIntimacyFriends);
        if (rvIntimacyFriends != null) {
            rvIntimacyFriends.setLayoutManager(new GridLayoutManager(getContext(), 3));
            rvIntimacyFriends.setNestedScrollingEnabled(false);
            intimacyFriendAdapter = new IntimacyFriendAdapter(getContext(), intimacyFriendsList);
            rvIntimacyFriends.setAdapter(intimacyFriendAdapter);
        }

        // Initialize CP Level SVGA Rewards RecyclerView
        rvCpRewards = view.findViewById(R.id.rvCpRewards);
        if (rvCpRewards != null) {
            rvCpRewards.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            cpRewardAdapter = new CpRewardAdapter(getContext(), cpRewardList, this::showRewardPreviewDialog);
            rvCpRewards.setAdapter(cpRewardAdapter);
        }
    }

    private void loadIntimacyData() {
        if (targetUid == null || targetUid.isEmpty()) return;

        // 1. Fetch Supporters
        CpIntimacyManager.fetchSupporters(targetUid, supporters -> {
            if (!isAdded()) return;
            supporterList.clear();
            if (supporters != null) supporterList.addAll(supporters);
            if (supporterAdapter != null) supporterAdapter.notifyDataSetChanged();
        });

        // 2. Fetch Best Friends & Intimacy Friends
        CpIntimacyManager.fetchIntimacyFriends(targetUid, (best, friends) -> {
            if (!isAdded()) return;
            bestFriendsList.clear();
            if (best != null) bestFriendsList.addAll(best);
            if (bestFriendAdapter != null) bestFriendAdapter.notifyDataSetChanged();

            intimacyFriendsList.clear();
            if (friends != null) intimacyFriendsList.addAll(friends);
            if (intimacyFriendAdapter != null) intimacyFriendAdapter.notifyDataSetChanged();
        });

        // 3. Load User Profile for Left Partner (Current User)
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                String name = snapshot.child("name").getValue(String.class);
                String avatar = snapshot.child("avatar").getValue(String.class);
                if (TextUtils.isEmpty(avatar)) avatar = snapshot.child("avtar").getValue(String.class);

                if (tvCoupleLeftName != null) {
                    tvCoupleLeftName.setText((name != null && !name.isEmpty()) ? name : "User");
                }
                if (ivCoupleLeftAvatar != null && avatar != null && !avatar.isEmpty()) {
                    Glide.with(IntimacyFragment.this)
                            .load(avatar)
                            .placeholder(R.drawable.logo_placeholder)
                            .circleCrop()
                            .into(ivCoupleLeftAvatar);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 4. Load CP Couple Data (cp_bindings & cp_guard_values)
        DatabaseReference cpRef = FirebaseDatabase.getInstance().getReference("cp_bindings").child(targetUid);
        cpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                if (snapshot.exists() && snapshot.hasChild("partnerUid")) {
                    String partnerUid = snapshot.child("partnerUid").getValue(String.class);
                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    if (timestamp != null && timestamp > 0) {
                        long days = CpBindingManager.getBindingDays(timestamp);
                        if (tvCoupleDuration != null) {
                            String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
                            tvCoupleDuration.setText(startDate + "-now " + days + " days");
                        }
                    }

                    if (!TextUtils.isEmpty(partnerUid)) {
                        DatabaseReference cpGuardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values")
                                .child(targetUid).child(partnerUid);

                        cpGuardRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot guardSnap) {
                                if (!isAdded()) return;
                                long intimacy = 0;
                                if (guardSnap.exists()) {
                                    Long val = guardSnap.child("value").getValue(Long.class);
                                    if (val != null) intimacy = val;
                                }
                                int level = CpIntimacyManager.calculateCpLevel(intimacy);
                                if (tvCoupleLevelBadgeText != null) {
                                    tvCoupleLevelBadgeText.setText("LV." + level);
                                }
                            }

                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });

                        FirebaseDatabase.getInstance().getReference("users").child(partnerUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot pSnap) {
                                        if (!isAdded()) return;
                                        String pName = pSnap.child("name").getValue(String.class);
                                        String pAvatar = pSnap.child("avatar").getValue(String.class);
                                        if (TextUtils.isEmpty(pAvatar)) pAvatar = pSnap.child("avtar").getValue(String.class);

                                        if (tvCoupleRightName != null) {
                                            tvCoupleRightName.setText((pName != null && !pName.isEmpty()) ? pName : "Partner");
                                        }
                                        if (ivCoupleRightAvatar != null && pAvatar != null && !pAvatar.isEmpty()) {
                                            Glide.with(IntimacyFragment.this)
                                                    .load(pAvatar)
                                                    .placeholder(R.drawable.logo_placeholder)
                                                    .circleCrop()
                                                    .into(ivCoupleRightAvatar);
                                        }
                                    }

                                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 5. Load CP Level SVGA Rewards
        loadCpLevelRewards();
    }

    private void loadCpLevelRewards() {
        cpRewardList.clear();

        // Testing Mode: All 3 SVGA rewards from assets/cp_level/ are unlocked by default
        cpRewardList.add(new CpLevelRewardModel(
                "cp_enter_1",
                "CP Entrance 1",
                "Exclusive CP entrance effect animation displayed when entering chat rooms.",
                "cp_level/enter_1.svga",
                R.drawable.ic_privilege_enter_notif,
                1,
                "ENTRANCE"
        ));

        cpRewardList.add(new CpLevelRewardModel(
                "cp_notification5",
                "CP Notification 5",
                "Special online banner notification broadcast when your CP partner arrives.",
                "cp_level/notification5.svga",
                R.drawable.ic_privilege_online_notif,
                5,
                "NOTIFICATION"
        ));

        cpRewardList.add(new CpLevelRewardModel(
                "cp_notification_level_5",
                "CP Level 5 Notification",
                "Grand level 5 notification banner for CP gift sending and events.",
                "cp_level/notification_level_5.svga",
                R.drawable.ic_privilege_sending_notif,
                5,
                "NOTIFICATION"
        ));

        if (cpRewardAdapter != null) {
            cpRewardAdapter.updateList(cpRewardList);
        }
    }

    private void showRewardPreviewDialog(CpLevelRewardModel reward) {
        if (reward == null || getContext() == null || !isAdded()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cp_reward_preview, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvPreviewTitle);
        TextView tvBadge = dialogView.findViewById(R.id.tvPreviewBadge);
        TextView tvDescription = dialogView.findViewById(R.id.tvPreviewDescription);
        SVGAImageView svgaPlayer = dialogView.findViewById(R.id.svgaPreviewPlayer);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbPreviewLoading);
        TextView btnReplay = dialogView.findViewById(R.id.btnReplayAnim);
        MaterialButton btnAction = dialogView.findViewById(R.id.btnPreviewAction);
        View btnClose = dialogView.findViewById(R.id.btnClosePreview);

        activePreviewSvgaPlayer = svgaPlayer;

        if (tvTitle != null) tvTitle.setText(reward.getTitle());
        if (tvBadge != null) {
            if (reward.isEquipped()) {
                tvBadge.setText("EQUIPPED ✨");
                tvBadge.setBackgroundResource(R.drawable.chip_room_bg);
            } else {
                tvBadge.setText("UNLOCKED (TESTING) ✨");
                tvBadge.setBackgroundResource(R.drawable.bg_level_badge_cyan);
            }
        }
        if (tvDescription != null) tvDescription.setText(reward.getDescription());

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        // Load and Play SVGA Animation
        if (svgaPlayer != null && reward.getSvgaPath() != null && !reward.getSvgaPath().isEmpty()) {
            svgaPlayer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

            SVGAParser parser = new SVGAParser(getContext());
            parser.decodeFromAssets(reward.getSvgaPath(), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                            if (svgaPlayer != null) {
                                svgaPlayer.setVideoItem(videoItem);
                                svgaPlayer.setLoops(0);
                                svgaPlayer.stepToFrame(0, true);
                                svgaPlayer.startAnimation();
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Unable to load preview animation", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }, null);

            if (btnReplay != null) {
                btnReplay.setOnClickListener(v -> {
                    if (svgaPlayer != null) {
                        svgaPlayer.stepToFrame(0, true);
                        svgaPlayer.startAnimation();
                    }
                });
            }
        }

        if (btnAction != null) {
            if (reward.isEquipped()) {
                btnAction.setText("Unequip Reward");
                btnAction.setBackgroundColor(Color.parseColor("#FF6B6B"));
                btnAction.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                btnAction.setText("Equip Reward ✨");
                btnAction.setBackgroundColor(Color.parseColor("#FFD700"));
                btnAction.setTextColor(Color.parseColor("#050E1E"));
            }

            btnAction.setOnClickListener(v -> {
                boolean newEquippedState = !reward.isEquipped();
                reward.setEquipped(newEquippedState);

                // Unequip others in same category if equipping
                if (newEquippedState) {
                    for (CpLevelRewardModel item : cpRewardList) {
                        if (item != reward && item.getCategory().equalsIgnoreCase(reward.getCategory())) {
                            item.setEquipped(false);
                        }
                    }
                }

                if (cpRewardAdapter != null) cpRewardAdapter.notifyDataSetChanged();
                dialog.dismiss();

                String msg = newEquippedState ? "Equipped " + reward.getTitle() + " ✨" : "Unequipped " + reward.getTitle();
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            });
        }

        dialog.setOnDismissListener(d -> {
            if (activePreviewSvgaPlayer != null) {
                activePreviewSvgaPlayer.stopAnimation();
                activePreviewSvgaPlayer.clear();
                activePreviewSvgaPlayer = null;
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        if (activePreviewSvgaPlayer != null) {
            activePreviewSvgaPlayer.stopAnimation();
            activePreviewSvgaPlayer.clear();
            activePreviewSvgaPlayer = null;
        }
        super.onDestroyView();
    }
}

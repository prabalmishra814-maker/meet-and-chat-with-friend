package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.IntimacyFriendAdapter;
import com.roomchatapps.Pmishra.adapters.SupporterAdapter;
import com.roomchatapps.Pmishra.models.IntimacyFriendModel;
import com.roomchatapps.Pmishra.models.SupporterModel;
import com.roomchatapps.Pmishra.utils.CpBindingManager;
import com.roomchatapps.Pmishra.utils.CpIntimacyManager;

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
    private RecyclerView rvBestFriends, rvSupporters, rvIntimacyFriends;

    private SupporterAdapter supporterAdapter;
    private IntimacyFriendAdapter bestFriendAdapter, intimacyFriendAdapter;

    private final List<SupporterModel> supporterList = new ArrayList<>();
    private final List<IntimacyFriendModel> bestFriendsList = new ArrayList<>();
    private final List<IntimacyFriendModel> intimacyFriendsList = new ArrayList<>();

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
    }
}

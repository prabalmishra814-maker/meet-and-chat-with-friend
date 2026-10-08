package com.roomchatapps.Pmishra;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.CpFriendAdapter;
import com.roomchatapps.Pmishra.databinding.FragmentLoveHouseUnboundBinding;
import com.roomchatapps.Pmishra.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoveHouseUnboundFragment extends Fragment {

    private FragmentLoveHouseUnboundBinding binding;
    private CpFriendAdapter adapter;
    private final List<User> candidateList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoveHouseUnboundBinding.inflate(inflater, container, false);
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
            userRef = FirebaseDatabase.getInstance().getReference("users");
        }

        setupToolbar();
        setupRecyclerView();
        setupSearchAndActions();
        loadCurrentUserData();
        loadCandidateFriends();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        binding.btnHelp.setOnClickListener(v -> showCpHelpDialog());
    }

    private void showCpHelpDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Love House & CP Rules")
                .setMessage("1. Guard value between two friends must reach 600 or more to become a CP.\n\n" +
                        "2. Send gifts and interact together in voice chat rooms to increase your intimacy & guard value.\n\n" +
                        "3. When you bind with a CP, your CP status and special animations will be unlocked!")
                .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupRecyclerView() {
        candidateList.clear();
        candidateList.addAll(getSuggestedCandidates());
        adapter = new CpFriendAdapter(getContext(), candidateList);
        binding.rvCandidateFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCandidateFriends.setAdapter(adapter);

        adapter.setOnInviteClickListener((user, position) -> verifyGuardValueAndProceed(user));
    }

    private void verifyGuardValueAndProceed(User targetUser) {
        if (targetUser == null) return;

        String targetUid = targetUser.getUserId();
        String targetProfileId = targetUser.getProfileId();

        if (currentUid == null) {
            showCpInsufficientGuardDialog(targetUser);
            return;
        }

        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        guardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maxGuardValue = 0;

                if (snapshot.exists()) {
                    if (targetUid != null && snapshot.hasChild(targetUid) && snapshot.child(targetUid).hasChild(currentUid)) {
                        maxGuardValue = Math.max(maxGuardValue, parseGuardValueFromSnapshot(snapshot.child(targetUid).child(currentUid)));
                    }
                    if (targetProfileId != null && snapshot.hasChild(targetProfileId) && snapshot.child(targetProfileId).hasChild(currentUid)) {
                        maxGuardValue = Math.max(maxGuardValue, parseGuardValueFromSnapshot(snapshot.child(targetProfileId).child(currentUid)));
                    }
                    if (targetUid != null && snapshot.hasChild(currentUid) && snapshot.child(currentUid).hasChild(targetUid)) {
                        maxGuardValue = Math.max(maxGuardValue, parseGuardValueFromSnapshot(snapshot.child(currentUid).child(targetUid)));
                    }
                    if (targetProfileId != null && snapshot.hasChild(currentUid) && snapshot.child(currentUid).hasChild(targetProfileId)) {
                        maxGuardValue = Math.max(maxGuardValue, parseGuardValueFromSnapshot(snapshot.child(currentUid).child(targetProfileId)));
                    }
                    if (targetUid != null) {
                        String pairKey1 = currentUid + "_" + targetUid;
                        String pairKey2 = targetUid + "_" + currentUid;
                        if (snapshot.hasChild(pairKey1)) {
                            maxGuardValue = Math.max(maxGuardValue, parseGuardValueFromSnapshot(snapshot.child(pairKey1)));
                        }
                        if (snapshot.hasChild(pairKey2)) {
                            maxGuardValue = Math.max(maxGuardValue, parseGuardValueFromSnapshot(snapshot.child(pairKey2)));
                        }
                    }
                }

                if (maxGuardValue >= 600) {
                    bindCpPartnerDirectly(targetUser);
                } else {
                    final long guardFromCpValues = maxGuardValue;
                    usersRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                            long totalGuard = guardFromCpValues;

                            if (userSnap.exists()) {
                                if (userSnap.hasChild("guardValue")) {
                                    totalGuard = Math.max(totalGuard, parseGuardValueFromSnapshot(userSnap.child("guardValue")));
                                }
                                if (userSnap.hasChild("coinsSpent")) {
                                    totalGuard = Math.max(totalGuard, parseGuardValueFromSnapshot(userSnap.child("coinsSpent")));
                                }
                            }

                            if (totalGuard >= 600) {
                                bindCpPartnerDirectly(targetUser);
                            } else {
                                showCpInsufficientGuardDialog(targetUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showCpInsufficientGuardDialog(targetUser);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showCpInsufficientGuardDialog(targetUser);
            }
        });
    }

    private long parseGuardValueFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return 0;

        if (snapshot.hasChild("value")) {
            Object val = snapshot.child("value").getValue();
            if (val != null) {
                try {
                    return Long.parseLong(String.valueOf(val).trim());
                } catch (Exception ignored) {}
            }
        }

        Object rootVal = snapshot.getValue();
        if (rootVal != null) {
            try {
                return Long.parseLong(String.valueOf(rootVal).trim());
            } catch (Exception ignored) {}
        }

        return 0;
    }

    private void bindCpPartnerDirectly(User targetUser) {
        if (targetUser == null || getContext() == null) return;

        String targetUid = targetUser.getUserId() != null ? targetUser.getUserId() : targetUser.getProfileId();
        if (targetUid == null) return;

        if (currentUid != null) {
            DatabaseReference inviteRef = FirebaseDatabase.getInstance().getReference("cp_invitations")
                    .child(targetUid).child(currentUid);

            Map<String, Object> inviteData = new HashMap<>();
            inviteData.put("fromUid", currentUid);
            inviteData.put("timestamp", System.currentTimeMillis());
            inviteData.put("status", "accepted");
            inviteRef.setValue(inviteData);

            DatabaseReference bindingRef1 = FirebaseDatabase.getInstance().getReference("cp_bindings").child(currentUid);
            Map<String, Object> cpData1 = new HashMap<>();
            cpData1.put("partnerUid", targetUid);
            cpData1.put("partnerName", !TextUtils.isEmpty(targetUser.getUserName()) ? targetUser.getUserName() : "Partner");
            cpData1.put("partnerAvatar", !TextUtils.isEmpty(targetUser.getUserIcon()) ? targetUser.getUserIcon() : "");
            cpData1.put("timestamp", System.currentTimeMillis());
            bindingRef1.setValue(cpData1);

            DatabaseReference bindingRef2 = FirebaseDatabase.getInstance().getReference("cp_bindings").child(targetUid);
            Map<String, Object> cpData2 = new HashMap<>();
            cpData2.put("partnerUid", currentUid);
            cpData2.put("partnerName", !TextUtils.isEmpty(binding.tvUserName.getText().toString()) ? binding.tvUserName.getText().toString() : "Partner");
            cpData2.put("partnerAvatar", "");
            cpData2.put("timestamp", System.currentTimeMillis());
            bindingRef2.setValue(cpData2);
        }

        if (adapter != null) {
            adapter.markAsInvited(targetUser.getUserId());
            adapter.markAsInvited(targetUser.getProfileId());
        }

        Toast.makeText(getContext(), "Bound with " + targetUser.getUserName() + " as CP partner!", Toast.LENGTH_SHORT).show();
    }

    private void showCpInsufficientGuardDialog(User targetUser) {
        if (getContext() == null) return;
        com.roomchatapps.Pmishra.dialogs.CpInsufficientGuardDialog dialog = new com.roomchatapps.Pmishra.dialogs.CpInsufficientGuardDialog(getContext(), targetUser);
        dialog.setOnProtectClickListener(() -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Send gifts or chat together in voice rooms to reach 600 Guard Value!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    private void setupSearchAndActions() {
        binding.etSearchId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (adapter != null) {
                    adapter.filter(query);
                }
                searchFirebaseUsers(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.containerCpAdd.setOnClickListener(v -> {
            binding.etSearchId.requestFocus();
            if (getContext() != null) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.etSearchId, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    private void searchFirebaseUsers(String query) {
        if (TextUtils.isEmpty(query) || userRef == null) return;

        String lowerQuery = query.toLowerCase().trim();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean foundNew = false;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String name = ds.child("name").getValue(String.class);
                        String profileId = ds.child("profileId").getValue(String.class);
                        String uid = ds.getKey();

                        if (uid != null && uid.equals(currentUid)) continue;

                        String nameLower = name != null ? name.toLowerCase() : "";
                        String profileIdLower = profileId != null ? profileId.toLowerCase() : "";
                        String uidLower = uid != null ? uid.toLowerCase() : "";

                        if (nameLower.contains(lowerQuery) || profileIdLower.contains(lowerQuery) || uidLower.contains(lowerQuery)) {
                            if (addFirebaseUserToCandidateList(ds)) {
                                foundNew = true;
                            }
                        }
                    }
                    if (foundNew && adapter != null) {
                        adapter.filter(query);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean addFirebaseUserToCandidateList(DataSnapshot ds) {
        String uid = ds.getKey();
        if (uid == null || (currentUid != null && currentUid.equals(uid))) return false;

        String profileId = ds.child("profileId").getValue(String.class);
        if (TextUtils.isEmpty(profileId)) {
            profileId = String.valueOf(100000 + Math.abs(uid.hashCode()) % 900000);
        }

        for (User u : candidateList) {
            if (uid.equals(u.getUserId()) || (profileId != null && profileId.equals(u.getProfileId()))) {
                return false;
            }
        }

        User user = new User();
        user.setUserId(uid);
        String name = ds.child("name").getValue(String.class);
        String avatar = ds.child("avtar").getValue(String.class);
        if (TextUtils.isEmpty(avatar)) avatar = ds.child("avatar").getValue(String.class);
        if (TextUtils.isEmpty(avatar)) avatar = ds.child("photoUrl").getValue(String.class);
        if (TextUtils.isEmpty(avatar)) avatar = ds.child("image").getValue(String.class);
        if (TextUtils.isEmpty(avatar)) avatar = ds.child("userIcon").getValue(String.class);

        user.setUserName(!TextUtils.isEmpty(name) ? name : "User");
        user.setProfileId(profileId);
        user.setUserIcon(avatar);

        candidateList.add(user);
        return true;
    }

    private void loadCurrentUserData() {
        if (currentUid == null || userRef == null) return;

        userRef.child(currentUid).addValueEventListener(new ValueEventListener() {
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

    private void loadCandidateFriends() {
        candidateList.clear();
        candidateList.addAll(getSuggestedCandidates());
        if (adapter != null) {
            adapter.setUsers(candidateList);
        }

        if (userRef == null) return;

        userRef.limitToFirst(50).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uid = ds.getKey();
                        if (currentUid != null && currentUid.equals(uid)) {
                            continue;
                        }

                        String profileId = ds.child("profileId").getValue(String.class);
                        if (TextUtils.isEmpty(profileId) && uid != null) {
                            profileId = String.valueOf(100000 + Math.abs(uid.hashCode()) % 900000);
                        }

                        boolean exists = false;
                        for (User u : candidateList) {
                            if ((uid != null && uid.equals(u.getUserId())) || (profileId != null && profileId.equals(u.getProfileId()))) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            User user = new User();
                            user.setUserId(uid);

                            String name = ds.child("name").getValue(String.class);
                            String avatar = ds.child("avtar").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = ds.child("avatar").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = ds.child("photoUrl").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = ds.child("image").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = ds.child("userIcon").getValue(String.class);

                            user.setUserName(!TextUtils.isEmpty(name) ? name : "User");
                            user.setProfileId(profileId);
                            user.setUserIcon(avatar);

                            candidateList.add(user);
                        }
                    }
                }

                if (adapter != null) {
                    adapter.setUsers(candidateList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private List<User> getSuggestedCandidates() {
        List<User> list = new ArrayList<>();

        User u1 = new User("zeenu_100098180", "🌸 i am ZeenU 🌸", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", false);
        u1.setProfileId("100098180");
        list.add(u1);

        User u2 = new User("user_100098181", "✨ Angel Queen ✨", "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=200", false);
        u2.setProfileId("100098181");
        list.add(u2);

        User u3 = new User("user_100098182", "👑 Royal Prince 👑", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=200", false);
        u3.setProfileId("100098182");
        list.add(u3);

        User u4 = new User("user_100098183", "💖 Cute Angel 💖", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=200", false);
        u4.setProfileId("100098183");
        list.add(u4);

        User u5 = new User("user_100098184", "🎀 Sweet Girl 🎀", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200", false);
        u5.setProfileId("100098184");
        list.add(u5);

        User u6 = new User("user_100098185", "🔥 Handsome Boy 🔥", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200", false);
        u6.setProfileId("100098185");
        list.add(u6);

        User u7 = new User("user_100098186", "🌟 Star Girl 🌟", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?q=80&w=200", false);
        u7.setProfileId("100098186");
        list.add(u7);

        User u8 = new User("user_100098187", "🌺 Princess Rose 🌺", "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?q=80&w=200", false);
        u8.setProfileId("100098187");
        list.add(u8);

        User u9 = new User("user_100098188", "🌙 Moon Knight 🌙", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200", false);
        u9.setProfileId("100098188");
        list.add(u9);

        User u10 = new User("user_100098189", "💎 Diamond Girl 💎", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200", false);
        u10.setProfileId("100098189");
        list.add(u10);

        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

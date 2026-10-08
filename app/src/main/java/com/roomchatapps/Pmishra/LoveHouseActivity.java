package com.roomchatapps.Pmishra;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.ActivityLoveHouseBinding;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

public class LoveHouseActivity extends AppCompatActivity {

    private ActivityLoveHouseBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference cpRef;
    private String currentUid;
    private String activeFragmentTag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);

        binding = ActivityLoveHouseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
            cpRef = FirebaseDatabase.getInstance().getReference("cp_bindings").child(currentUid);
            listenForCpBindingState();
        } else {
            showFragment(new LoveHouseUnboundFragment(), "UNBOUND");
        }
    }

    private void listenForCpBindingState() {
        if (cpRef == null) return;

        cpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;

                if (snapshot.exists() && snapshot.hasChild("partnerUid")) {
                    if (!"BOUND".equals(activeFragmentTag)) {
                        showFragment(new LoveHouseBoundFragment(), "BOUND");
                    }
                } else {
                    if (!"UNBOUND".equals(activeFragmentTag)) {
                        showFragment(new LoveHouseUnboundFragment(), "UNBOUND");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (activeFragmentTag.isEmpty()) {
                    showFragment(new LoveHouseUnboundFragment(), "UNBOUND");
                }
            }
        });
    }

    private void showFragment(Fragment fragment, String tag) {
        if (isFinishing() || fragment == null) return;
        activeFragmentTag = tag;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment, tag)
                .commitAllowingStateLoss();
    }
}

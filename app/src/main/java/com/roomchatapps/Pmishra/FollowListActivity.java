package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.FollowAdapter;
import com.roomchatapps.Pmishra.databinding.ActivityFollowListBinding;
import com.roomchatapps.Pmishra.models.User;
import java.util.ArrayList;
import java.util.List;

public class FollowListActivity extends AppCompatActivity {

    private ActivityFollowListBinding binding;
    private FollowAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private String type; // "followers" or "following"
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFollowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uid = getIntent().getStringExtra("uid");
        type = getIntent().getStringExtra("type");

        if (uid == null || type == null) {
            finish();
            return;
        }

        binding.tvTitle.setText(type.equalsIgnoreCase("followers") ? getString(R.string.followers) : getString(R.string.following));
        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadFollowList();
    }

    private void setupRecyclerView() {
        adapter = new FollowAdapter(userList);
        binding.rvFollowList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFollowList.setAdapter(adapter);
    }

    private void loadFollowList() {
        binding.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(uid).child(type);

        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (!snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }

                long totalCount = snapshot.getChildrenCount();
                final int[] fetchedCount = {0};

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userUid = ds.getKey();
                    fetchUserData(userUid, totalCount, fetchedCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchUserData(String userUid, long totalCount, int[] fetchedCount) {
        FirebaseDatabase.getInstance().getReference("users").child(userUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fetchedCount[0]++;
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }

                        if (fetchedCount[0] == totalCount) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (userList.isEmpty()) {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvEmpty.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        fetchedCount[0]++;
                        if (fetchedCount[0] == totalCount) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}

package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.FollowAdapter;
import com.roomchatapps.Pmishra.databinding.ActivitySearchBinding;
import com.roomchatapps.Pmishra.models.User;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private FollowAdapter adapter;
    private List<User> userList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userList = new ArrayList<>();
        adapter = new FollowAdapter(userList);
        
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(adapter);

        binding.ivBack.setOnClickListener(v -> finish());
        
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.tvSearchBtn.setOnClickListener(v -> searchUsers(binding.etSearch.getText().toString().trim()));
    }

    private void searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            userList.clear();
            adapter.notifyDataSetChanged();
            binding.tvPlaceholder.setVisibility(View.VISIBLE);
            binding.tvPlaceholder.setText("Search users by name or ID");
            return;
        }

        binding.tvPlaceholder.setVisibility(View.GONE);
        String q = query.trim().toLowerCase();
        String[] tokens = q.split("\\s+");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (user != null) {
                            String key = ds.getKey();
                            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                                user.setUserId(key);
                            }

                            String name = user.getUserName() != null ? user.getUserName().toLowerCase() : "";
                            String profileId = user.getProfileId() != null ? user.getProfileId().toLowerCase() : "";
                            String uid = user.getUserId() != null ? user.getUserId().toLowerCase() : "";

                            boolean matches = false;

                            if (name.contains(q) || profileId.contains(q) || uid.contains(q)) {
                                matches = true;
                            } else {
                                for (String token : tokens) {
                                    if (!token.isEmpty() && (name.contains(token) || profileId.contains(token))) {
                                        matches = true;
                                        break;
                                    }
                                }
                            }

                            if (matches) {
                                userList.add(user);
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                if (userList.isEmpty()) {
                    binding.tvPlaceholder.setVisibility(View.VISIBLE);
                    binding.tvPlaceholder.setText("No users found matching \"" + query + "\"");
                } else {
                    binding.tvPlaceholder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

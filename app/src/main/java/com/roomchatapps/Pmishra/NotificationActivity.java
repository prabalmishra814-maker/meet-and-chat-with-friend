package com.roomchatapps.Pmishra;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.ActivityNotificationBinding;
import com.roomchatapps.Pmishra.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private NotificationAdapter adapter;
    private final List<NotificationModel> allNotifications = new ArrayList<>();
    private final List<NotificationModel> filteredNotifications = new ArrayList<>();
    private DatabaseReference notifRef;
    private String currentUid;
    private String activeCategory = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUid = FirebaseAuth.getInstance().getUid();

        setupHeaderAnimations();
        setupTabs();
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void setupHeaderAnimations() {
        AnimationHelper.fadeIn(binding.header, 400);
        AnimationHelper.scaleIn(binding.svTabs, 500);
    }

    private void setupTabs() {
        binding.tabAll.setOnClickListener(v -> selectTab("ALL", binding.tabAll));
        binding.tabSystem.setOnClickListener(v -> selectTab("SYSTEM", binding.tabSystem));
        binding.tabSocial.setOnClickListener(v -> selectTab("SOCIAL", binding.tabSocial));
        binding.tabRooms.setOnClickListener(v -> selectTab("ROOMS", binding.tabRooms));
    }

    private void selectTab(String category, TextView selectedTab) {
        if (activeCategory.equals(category)) return;
        activeCategory = category;

        // Reset all tabs UI
        TextView[] tabs = {binding.tabAll, binding.tabSystem, binding.tabSocial, binding.tabRooms};
        for (TextView tab : tabs) {
            tab.setBackgroundResource(R.drawable.chip_room_bg);
            tab.setTextColor(Color.parseColor("#88FFFFFF"));
            tab.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
        }

        // Highlight selected tab with spring scale animation
        selectedTab.setBackgroundResource(R.drawable.chip_charm_bg);
        selectedTab.setTextColor(Color.WHITE);
        selectedTab.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(220)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        filterNotifications();
    }

    private void setupRecyclerView() {
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(filteredNotifications, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(NotificationModel notification, int position) {
                // Mark notification as read
                if (!notification.isRead() && currentUid != null && notification.getId() != null) {
                    notification.setRead(true);
                    adapter.notifyItemChanged(position);
                    notifRef.child(notification.getId()).child("read").setValue(true);
                }
                
                Toast.makeText(NotificationActivity.this, notification.getTitle() + ": " + notification.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotificationDismiss(NotificationModel notification, int position) {
                deleteNotification(notification, position);
            }
        });
        binding.rvNotifications.setAdapter(adapter);

        // Swipe-to-delete item animation
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < filteredNotifications.size()) {
                    NotificationModel item = filteredNotifications.get(position);
                    deleteNotification(item, position);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvNotifications);
    }

    private void deleteNotification(NotificationModel notification, int position) {
        if (position < 0 || position >= filteredNotifications.size()) return;
        filteredNotifications.remove(position);
        allNotifications.remove(notification);
        adapter.notifyItemRemoved(position);

        if (currentUid != null && notification.getId() != null) {
            notifRef.child(notification.getId()).removeValue();
        }

        updateEmptyState();
        Toast.makeText(this, "Notification cleared", Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnClearAll.setOnClickListener(v -> {
            if (allNotifications.isEmpty()) return;
            binding.rvNotifications.animate().alpha(0f).translationY(40f).setDuration(250).withEndAction(() -> {
                allNotifications.clear();
                filteredNotifications.clear();
                adapter.notifyDataSetChanged();
                binding.rvNotifications.setAlpha(1f);
                binding.rvNotifications.setTranslationY(0f);
                if (currentUid != null && notifRef != null) {
                    notifRef.removeValue();
                }
                updateEmptyState();
                Toast.makeText(NotificationActivity.this, "All notifications cleared", Toast.LENGTH_SHORT).show();
            }).start();
        });
    }

    private void loadNotifications() {
        if (currentUid == null) {
            binding.progressBar.setVisibility(View.GONE);
            updateEmptyState();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUid);

        notifRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allNotifications.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        NotificationModel model = ds.getValue(NotificationModel.class);
                        if (model != null) {
                            if (model.getId() == null) model.setId(ds.getKey());
                            allNotifications.add(model);
                        }
                    }
                } else {
                    // Seed initial sample notifications if database is empty for demo experience
                    seedSampleNotifications();
                }

                binding.progressBar.setVisibility(View.GONE);
                // Sort by timestamp descending
                java.util.Collections.sort(allNotifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                filterNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                updateEmptyState();
            }
        });
    }

    private void seedSampleNotifications() {
        long now = System.currentTimeMillis();
        List<NotificationModel> samples = new ArrayList<>();
        
        samples.add(new NotificationModel("sample1", "Welcome to Room Chat!", "Explore voice rooms, make friends, and chat live.", "SYSTEM", "sys1", "Room Chat", "", now - 120_000, false, null));
        samples.add(new NotificationModel("sample2", "New Gift Received 🎁", "A user sent you a Diamond Gift in Voice Room!", "GIFT", "usr1", "Alex", "", now - 1800_000, false, null));
        samples.add(new NotificationModel("sample3", "New Follower", "Sophia started following you.", "FOLLOW", "usr2", "Sophia", "", now - 7200_000, true, null));
        samples.add(new NotificationModel("sample4", "Room Party Invitation", "You were invited to join 'Chill Vibez Audio Party'.", "ROOM_INVITE", "usr3", "Chill Zone", "", now - 86400_000, false, null));

        if (currentUid != null && notifRef != null) {
            for (NotificationModel sample : samples) {
                notifRef.child(sample.getId()).setValue(sample);
            }
        }
    }

    private void filterNotifications() {
        filteredNotifications.clear();
        for (NotificationModel item : allNotifications) {
            String type = item.getType() != null ? item.getType().toUpperCase() : "SYSTEM";
            switch (activeCategory) {
                case "SYSTEM":
                    if ("SYSTEM".equals(type)) filteredNotifications.add(item);
                    break;
                case "SOCIAL":
                    if ("GIFT".equals(type) || "FOLLOW".equals(type) || "COMMENT".equals(type)) {
                        filteredNotifications.add(item);
                    }
                    break;
                case "ROOMS":
                    if ("ROOM_INVITE".equals(type)) filteredNotifications.add(item);
                    break;
                case "ALL":
                default:
                    filteredNotifications.add(item);
                    break;
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredNotifications.isEmpty()) {
            binding.llEmptyState.setVisibility(View.VISIBLE);
            binding.llEmptyState.setAlpha(0f);
            binding.llEmptyState.animate().alpha(1f).setDuration(300).start();
            binding.rvNotifications.setVisibility(View.GONE);
        } else {
            binding.llEmptyState.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
        }
    }
}

package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.FragmentMessageBinding;
import com.roomchatapps.Pmishra.databinding.ItemMessageBinding;
import com.roomchatapps.Pmishra.models.FriendRequestModel;
import com.roomchatapps.Pmishra.models.TransactionModel;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;

public class MessageFragment extends Fragment {

    private FragmentMessageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTabs();
        setupShortcuts();
        setupRecyclerView();
    }

    private void setupShortcuts() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        if (binding.llFriendRequest != null) {
            AnimationHelper.applyClickAnimation(binding.llFriendRequest);
            binding.llFriendRequest.setOnClickListener(v -> {
                showCategory("Friend Requests");
                loadFriendRequests(uid);
            });
        }

        if (binding.llGift != null) {
            AnimationHelper.applyClickAnimation(binding.llGift);
            binding.llGift.setOnClickListener(v -> {
                showCategory("Gift History");
                loadGiftHistory(uid);
            });
        }

        if (binding.llReward != null) {
            AnimationHelper.applyClickAnimation(binding.llReward);
            binding.llReward.setOnClickListener(v -> {
                showCategory("Rewards");
                loadRewards(uid);
            });
        }
    }

    private void showCategory(String title) {
        binding.llCategorySection.setVisibility(View.VISIBLE);
        binding.tvCategoryTitle.setText(title);
        binding.rvCategoryContent.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadFriendRequests(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("friend_requests").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                List<FriendRequestModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FriendRequestModel model = ds.getValue(FriendRequestModel.class);
                    if (model != null && "PENDING".equals(model.getStatus())) {
                        list.add(model);
                    }
                }
                updateCategoryUI(list.isEmpty());
                if (!list.isEmpty()) {
                    binding.rvCategoryContent.setAdapter(new FriendRequestAdapter(list, new FriendRequestAdapter.OnActionListener() {
                        @Override
                        public void onAccept(FriendRequestModel request) {
                            handleFriendRequest(uid, request, "ACCEPTED");
                        }

                        @Override
                        public void onReject(FriendRequestModel request) {
                            handleFriendRequest(uid, request, "REJECTED");
                        }
                    }));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleFriendRequest(String myUid, FriendRequestModel request, String newStatus) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("friend_requests")
                .child(myUid).child(request.getSenderUid());
        ref.child("status").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful() && "ACCEPTED".equals(newStatus)) {
                FirebaseDatabase.getInstance().getReference("friends").child(myUid).child(request.getSenderUid()).setValue(true);
                FirebaseDatabase.getInstance().getReference("friends").child(request.getSenderUid()).child(myUid).setValue(true);
                if (getContext() != null) Toast.makeText(getContext(), "Friend request accepted!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGiftHistory(String uid) {
        WalletManager.loadTransactionHistory(uid, new WalletManager.TransactionCallback() {
            @Override
            public void onTransactionsLoaded(List<TransactionModel> transactions) {
                if (binding == null) return;
                List<TransactionModel> gifts = new ArrayList<>();
                for (TransactionModel tx : transactions) {
                    if ("GIFT_SENT".equals(tx.getType()) || "GIFT_RECEIVED".equals(tx.getType())) {
                        gifts.add(tx);
                    }
                }
                updateCategoryUI(gifts.isEmpty());
                if (!gifts.isEmpty()) {
                    binding.rvCategoryContent.setAdapter(new TransactionAdapter(gifts));
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void loadRewards(String uid) {
        WalletManager.loadTransactionHistory(uid, new WalletManager.TransactionCallback() {
            @Override
            public void onTransactionsLoaded(List<TransactionModel> transactions) {
                if (binding == null) return;
                List<TransactionModel> rewards = new ArrayList<>();
                for (TransactionModel tx : transactions) {
                    if ("REWARD".equals(tx.getType()) || "TOPUP".equals(tx.getType())) {
                        rewards.add(tx);
                    }
                }
                updateCategoryUI(rewards.isEmpty());
                if (!rewards.isEmpty()) {
                    binding.rvCategoryContent.setAdapter(new TransactionAdapter(rewards));
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void updateCategoryUI(boolean isEmpty) {
        binding.tvEmptyCategory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvCategoryContent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupTabs() {
        binding.tvMessageTab.setAlpha(1f);

        binding.tvMessageTab.setOnClickListener(v -> {
            binding.tvMessageTab.setAlpha(1f);
            binding.activeTabIndicator.animate().translationX(0).setDuration(200);
        });
    }

    private void setupRecyclerView() {
        if (getContext() == null || binding == null) return;

        binding.rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        List<MessageItem> messages = new ArrayList<>();
        MessageAdapter adapter = new MessageAdapter(messages);
        binding.rvMessages.setAdapter(adapter);

        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        com.google.firebase.database.DatabaseReference recentRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("RecentChats").child(currentUid);

        recentRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null || binding == null) return;

                messages.clear();
                if (snapshot.exists()) {
                    for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                        com.roomchatapps.Pmishra.models.ChatMessage chat = ds.getValue(com.roomchatapps.Pmishra.models.ChatMessage.class);
                        if (chat != null && chat.getSenderId() != null && chat.getReceiverId() != null) {
                            String otherUserId = chat.getSenderId().equals(currentUid) ? chat.getReceiverId() : chat.getSenderId();
                            String lastMsg = chat.getMessage();
                            long timestamp = chat.getTimestamp();

                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
                            String time = sdf.format(new java.util.Date(timestamp));

                            int unread = (!chat.isRead() && !chat.getSenderId().equals(currentUid)) ? 1 : 0;

                            // Add placeholder MessageItem, details will be fetched in Adapter
                            messages.add(new MessageItem(otherUserId, "Loading...", lastMsg, time, timestamp, unread));
                        }
                    }
                }
                // Sort by timestamp descending (newest first)
                java.util.Collections.sort(messages, (m1, m2) -> Long.compare(m2.timestamp, m1.timestamp));
                if (adapter != null) adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class MessageItem {
        String userId, username, message, time;
        long timestamp;
        int unreadCount;

        MessageItem(String userId, String username, String message, String time, long timestamp, int unreadCount) {
            this.userId = userId;
            this.username = username;
            this.message = message;
            this.time = time;
            this.timestamp = timestamp;
            this.unreadCount = unreadCount;
        }
    }

    static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private final List<MessageItem> items;

        MessageAdapter(List<MessageItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMessageBinding b = ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int adapterPos = holder.getBindingAdapterPosition();
            int targetPos = (adapterPos != RecyclerView.NO_POSITION) ? adapterPos : position;
            if (targetPos < 0 || targetPos >= items.size()) return;

            MessageItem item = items.get(targetPos);

            // Reset UI for recycled view
            holder.binding.tvUsername.setText("Loading...");
            holder.binding.ivUserAvatar.setImageResource(R.drawable.ic_person);

            // Fetch User Details
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(item.userId)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            if (holder.itemView.getContext() == null) return;
                            if (holder.itemView.getContext() instanceof android.app.Activity) {
                                android.app.Activity act = (android.app.Activity) holder.itemView.getContext();
                                if (act.isFinishing() || act.isDestroyed()) return;
                            }
                            if (snapshot.exists()) {
                                String name = snapshot.child("name").getValue(String.class);
                                String avatar = snapshot.child("avtar").getValue(String.class);
                                if (holder.binding.tvUsername != null) holder.binding.tvUsername.setText(name != null ? name : "User");
                                if (holder.binding.ivUserAvatar != null) {
                                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                                            .load(avatar)
                                            .placeholder(R.drawable.ic_person)
                                            .into(holder.binding.ivUserAvatar);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                    });

            holder.binding.tvLastMsg.setText(item.message);
            holder.binding.tvMsgTime.setText(item.time);

            // Click listener
            holder.itemView.setOnClickListener(v -> {
                String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (currentUid != null) {
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("RecentChats")
                            .child(currentUid).child(item.userId).child("read").setValue(true);
                }

                android.content.Intent intent = new android.content.Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("receiverId", item.userId);
                intent.putExtra("receiverName", holder.binding.tvUsername.getText().toString());
                v.getContext().startActivity(intent);
            });

            // Unread count badge
            if (item.unreadCount > 0) {
                holder.binding.unreadBadge.setVisibility(View.VISIBLE);
                holder.binding.unreadBadge.setText("1");
                holder.binding.tvLastMsg.setTextColor(Color.WHITE);
                holder.binding.tvLastMsg.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.binding.unreadBadge.setVisibility(View.GONE);
                holder.binding.tvLastMsg.setTextColor(Color.parseColor("#99FFFFFF"));
                holder.binding.tvLastMsg.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Animation
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationX(30f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setStartDelay(Math.min(targetPos * 50L, 400L))
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemMessageBinding binding;
            ViewHolder(ItemMessageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

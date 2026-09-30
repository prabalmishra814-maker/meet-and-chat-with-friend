package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.ChatAdapter;
import com.roomchatapps.Pmishra.databinding.ActivityChatBinding;
import com.roomchatapps.Pmishra.models.ChatMessage;
import com.roomchatapps.Pmishra.utils.NotificationHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private String receiverId;
    private String senderId;
    private String chatRoomId;
    private DatabaseReference directChatRef;
    private DatabaseReference legacyChatRef;
    private LinearLayoutManager layoutManager;
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.roomchatapps.Pmishra.utils.StatusBarUtils.makeTransparent(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.chatHeader.setPadding(
                    binding.chatHeader.getPaddingLeft(),
                    systemBars.top + 8,
                    binding.chatHeader.getPaddingRight(),
                    binding.chatHeader.getPaddingBottom()
            );
            binding.inputLayout.setPadding(
                    binding.inputLayout.getPaddingLeft(),
                    binding.inputLayout.getPaddingTop(),
                    binding.inputLayout.getPaddingRight(),
                    systemBars.bottom + 8
            );
            return insets;
        });

        receiverId = getIntent().getStringExtra("receiverId");
        if (receiverId == null || receiverId.trim().isEmpty()) {
            receiverId = getIntent().getStringExtra("userId");
        }
        if (receiverId == null || receiverId.trim().isEmpty()) {
            receiverId = getIntent().getStringExtra("targetUid");
        }

        String receiverName = getIntent().getStringExtra("receiverName");
        if (receiverName == null || receiverName.trim().isEmpty()) {
            receiverName = "User";
        }

        senderId = FirebaseAuth.getInstance().getUid();
        if (senderId == null) senderId = "";

        chatRoomId = getChatRoomId(senderId, receiverId);

        binding.tvChatUserName.setText(receiverName);
        binding.ivBack.setOnClickListener(v -> finish());

        View.OnClickListener openProfile = v -> {
            if (receiverId != null && !receiverId.trim().isEmpty()) {
                Intent intent = new Intent(ChatActivity.this, UserDetailActivity.class);
                intent.putExtra("uid", receiverId);
                startActivity(intent);
            }
        };
        binding.ivChatUserAvatar.setOnClickListener(openProfile);
        binding.tvChatUserName.setOnClickListener(openProfile);

        if (binding.ivMoreOptions != null) {
            binding.ivMoreOptions.setOnClickListener(v -> showMoreOptionsMenu(v, openProfile));
        }

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(chatAdapter);

        // Scroll listener for "Scroll to Bottom" Floating Action Button
        binding.rvChatMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                int totalItems = chatAdapter.getItemCount();
                if (totalItems > 0 && lastVisible < totalItems - 3) {
                    if (binding.btnScrollBottom.getVisibility() != View.VISIBLE) {
                        binding.btnScrollBottom.show();
                    }
                } else {
                    if (binding.btnScrollBottom.getVisibility() == View.VISIBLE) {
                        binding.btnScrollBottom.hide();
                    }
                }
            }
        });

        binding.btnScrollBottom.setOnClickListener(v -> scrollToBottom(true));

        directChatRef = FirebaseDatabase.getInstance().getReference("DirectChats").child(chatRoomId);
        legacyChatRef = FirebaseDatabase.getInstance().getReference("Chats");

        loadMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void showMoreOptionsMenu(View view, View.OnClickListener openProfileListener) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("View Profile");
        popupMenu.getMenu().add("Clear Chat History");
        popupMenu.setOnMenuItemClickListener(item -> {
            CharSequence title = item.getTitle();
            if (title != null && "View Profile".contentEquals(title)) {
                if (openProfileListener != null) openProfileListener.onClick(view);
                return true;
            } else if (title != null && "Clear Chat History".contentEquals(title)) {
                confirmClearChat();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void confirmClearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to delete all messages in this conversation?")
                .setPositiveButton("Clear", (dialog, which) -> clearChatHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearChatHistory() {
        if (directChatRef != null) {
            directChatRef.removeValue();
        }
        if (senderId != null && receiverId != null) {
            FirebaseDatabase.getInstance().getReference("RecentChats")
                    .child(senderId).child(receiverId).removeValue();
        }
        messageMap.clear();
        chatMessages.clear();
        chatAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show();
    }

    private String getChatRoomId(String uid1, String uid2) {
        if (uid1 == null) uid1 = "";
        if (uid2 == null) uid2 = "";
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private final Map<String, ChatMessage> messageMap = new HashMap<>();

    private void loadMessages() {
        if (senderId.isEmpty() || receiverId == null || receiverId.trim().isEmpty()) return;

        // Mark recent chat conversation as read
        DatabaseReference userRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                .child(senderId).child(receiverId);
        userRecentRef.child("read").setValue(true);

        // Listen to permanent DirectChats/{chatRoomId}
        directChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean wasAtBottom = isAtBottom();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key == null) continue;

                        ChatMessage chat = ds.getValue(ChatMessage.class);
                        if (chat != null && chat.getSenderId() != null && chat.getReceiverId() != null) {
                            messageMap.put(key, chat);

                            // Mark received messages as read
                            if (chat.getSenderId().equals(receiverId) && !chat.isRead()) {
                                ds.getRef().child("read").setValue(true);
                            }
                        }
                    }
                }

                updateMessageList(wasAtBottom);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load legacy flat Chats for backwards compatibility and migrate to DirectChats
        legacyChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean wasAtBottom = isAtBottom();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key == null) continue;

                        ChatMessage chat = ds.getValue(ChatMessage.class);
                        if (chat != null && chat.getSenderId() != null && chat.getReceiverId() != null) {
                            boolean isSenderAndRecv = chat.getSenderId().equals(senderId) && chat.getReceiverId().equals(receiverId);
                            boolean isRecvAndSender = chat.getSenderId().equals(receiverId) && chat.getReceiverId().equals(senderId);

                            if (isSenderAndRecv || isRecvAndSender) {
                                messageMap.put(key, chat);
                                // Save permanently into DirectChats so it's never lost
                                directChatRef.child(key).setValue(chat);
                            }
                        }
                    }
                    updateMessageList(wasAtBottom);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateMessageList(boolean wasAtBottom) {
        chatMessages.clear();
        chatMessages.addAll(messageMap.values());
        Collections.sort(chatMessages, (c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()));
        chatAdapter.notifyDataSetChanged();

        if (isInitialLoad || wasAtBottom) {
            scrollToBottom(false);
            isInitialLoad = false;
        }
    }

    private boolean isAtBottom() {
        if (chatAdapter.getItemCount() == 0) return true;
        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
        return lastVisible >= chatAdapter.getItemCount() - 2;
    }

    private void scrollToBottom(boolean smooth) {
        if (chatAdapter.getItemCount() > 0) {
            int targetPos = chatAdapter.getItemCount() - 1;
            if (smooth) {
                binding.rvChatMessages.smoothScrollToPosition(targetPos);
            } else {
                binding.rvChatMessages.scrollToPosition(targetPos);
            }
        }
    }

    private void sendMessage() {
        String msg = binding.etMessage.getText().toString().trim();
        if (!msg.isEmpty() && !senderId.isEmpty() && receiverId != null && !receiverId.isEmpty()) {
            String msgId = directChatRef.push().getKey();
            long time = System.currentTimeMillis();
            ChatMessage chatMessage = new ChatMessage(senderId, receiverId, msg, time, false);

            if (msgId != null) {
                // Save permanently in DirectChats/{chatRoomId}/{msgId}
                directChatRef.child(msgId).setValue(chatMessage);

                // Update RecentChats for sender
                ChatMessage senderChatMessage = new ChatMessage(senderId, receiverId, msg, time, true);
                DatabaseReference senderRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(senderId).child(receiverId);
                senderRecentRef.setValue(senderChatMessage);

                // Update RecentChats for receiver
                ChatMessage receiverChatMessage = new ChatMessage(senderId, receiverId, msg, time, false);
                DatabaseReference receiverRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(receiverId).child(senderId);
                receiverRecentRef.setValue(receiverChatMessage);

                // Trigger in-app notification
                NotificationHelper.sendMessageNotification(receiverId, msg);

                binding.etMessage.setText("");
                scrollToBottom(true);
            }
        }
    }
}

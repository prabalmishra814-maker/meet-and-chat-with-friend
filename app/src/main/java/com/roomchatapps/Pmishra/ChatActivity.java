package com.roomchatapps.Pmishra;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

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
        StatusBarUtils.makeTransparent(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            binding.chatHeader.setPadding(
                    binding.chatHeader.getPaddingLeft(),
                    systemBars.top + 8,
                    binding.chatHeader.getPaddingRight(),
                    binding.chatHeader.getPaddingBottom()
            );

            int bottomPadding = Math.max(systemBars.bottom, ime.bottom) + 8;
            binding.inputLayout.setPadding(
                    binding.inputLayout.getPaddingLeft(),
                    binding.inputLayout.getPaddingTop(),
                    binding.inputLayout.getPaddingRight(),
                    bottomPadding
            );

            if (ime.bottom > 0) {
                binding.rvChatMessages.post(() -> scrollToBottom(false));
            }

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
        chatAdapter.setOnMessageLongClickListener(this::handleMessageLongClick);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(chatAdapter);

        binding.rvChatMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                binding.rvChatMessages.postDelayed(() -> scrollToBottom(false), 50);
            }
        });

        binding.etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.rvChatMessages.postDelayed(() -> scrollToBottom(false), 150);
            }
        });

        binding.etMessage.setOnClickListener(v -> {
            binding.rvChatMessages.postDelayed(() -> scrollToBottom(false), 150);
        });

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
    private ValueEventListener directChatListener;

    private void loadMessages() {
        if (senderId.isEmpty() || receiverId == null || receiverId.trim().isEmpty()) return;

        // Mark recent chat conversation as read
        DatabaseReference userRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                .child(senderId).child(receiverId);
        userRecentRef.child("read").setValue(true);

        if (directChatRef != null && directChatListener != null) {
            directChatRef.removeEventListener(directChatListener);
        }

        // Listen to permanent DirectChats/{chatRoomId}
        directChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                boolean wasAtBottom = isAtBottom();
                messageMap.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key == null) continue;

                        ChatMessage chat = ds.getValue(ChatMessage.class);
                        if (chat != null && chat.getSenderId() != null && chat.getReceiverId() != null) {
                            if (chat.getMessageId() == null || chat.getMessageId().isEmpty()) {
                                chat.setMessageId(key);
                            }
                            messageMap.put(key, chat);

                            // Mark received messages as read
                            if (receiverId.equals(chat.getSenderId()) && !chat.isRead()) {
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
        };
        directChatRef.addValueEventListener(directChatListener);

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
                chatMessage.setMessageId(msgId);
                // Save permanently in DirectChats/{chatRoomId}/{msgId}
                directChatRef.child(msgId).setValue(chatMessage);

                // Update RecentChats for sender
                ChatMessage senderChatMessage = new ChatMessage(senderId, receiverId, msg, time, true);
                senderChatMessage.setMessageId(msgId);
                DatabaseReference senderRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(senderId).child(receiverId);
                senderRecentRef.setValue(senderChatMessage);

                // Update RecentChats for receiver
                ChatMessage receiverChatMessage = new ChatMessage(senderId, receiverId, msg, time, false);
                receiverChatMessage.setMessageId(msgId);
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

    private void handleMessageLongClick(ChatMessage message, int position) {
        if (message == null || isFinishing() || isDestroyed()) return;
        boolean isSentByMe = senderId != null && senderId.equals(message.getSenderId());

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_message_options, null, false);
        if (dialogView == null) return;

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        View btnOptionUnsend = dialogView.findViewById(R.id.btnOptionUnsend);
        View dividerUnsend = dialogView.findViewById(R.id.dividerUnsend);
        View btnOptionCopy = dialogView.findViewById(R.id.btnOptionCopy);

        if (btnOptionUnsend != null) {
            btnOptionUnsend.setVisibility(isSentByMe ? View.VISIBLE : View.GONE);
            if (dividerUnsend != null) dividerUnsend.setVisibility(isSentByMe ? View.VISIBLE : View.GONE);
            btnOptionUnsend.setOnClickListener(v -> {
                dialog.dismiss();
                unsendMessage(message);
            });
        }

        if (btnOptionCopy != null) {
            btnOptionCopy.setOnClickListener(v -> {
                dialog.dismiss();
                copyMessageToClipboard(message);
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void copyMessageToClipboard(ChatMessage message) {
        if (message != null && message.getMessage() != null) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Message", message.getMessage());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Message copied to clipboard 📋", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void unsendMessage(ChatMessage message) {
        if (message == null || isFinishing() || isDestroyed()) return;
        String msgId = message.getMessageId();

        if (msgId == null || msgId.isEmpty()) {
            for (Map.Entry<String, ChatMessage> entry : messageMap.entrySet()) {
                ChatMessage val = entry.getValue();
                if (val != null && val.getTimestamp() == message.getTimestamp() && senderId.equals(val.getSenderId())) {
                    msgId = entry.getKey();
                    break;
                }
            }
        }

        if (msgId != null && !msgId.isEmpty()) {
            // 1. Instant local removal for 0ms fast & smooth UI update
            messageMap.remove(msgId);
            updateMessageList(isAtBottom());

            // 2. Remove node from Firebase DirectChats & legacy Chats asynchronously
            directChatRef.child(msgId).removeValue();
            legacyChatRef.child(msgId).removeValue();

            // 3. Update RecentChats for sender & receiver
            DatabaseReference senderRecent = FirebaseDatabase.getInstance().getReference("RecentChats").child(senderId).child(receiverId);
            DatabaseReference receiverRecent = FirebaseDatabase.getInstance().getReference("RecentChats").child(receiverId).child(senderId);

            if (!chatMessages.isEmpty()) {
                ChatMessage lastMsg = chatMessages.get(chatMessages.size() - 1);
                senderRecent.setValue(lastMsg);
                receiverRecent.setValue(lastMsg);
            } else {
                senderRecent.removeValue();
                receiverRecent.removeValue();
            }

            Toast.makeText(this, "Message un-sent 🗑️", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to unsend message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (directChatRef != null && directChatListener != null) {
            directChatRef.removeEventListener(directChatListener);
        }
    }
}

package com.roomchatapps.Pmishra;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
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
    private String receiverName = "User";
    private boolean isGroup = false;
    private String familyId = "";
    private String familyOwnerUid = "";
    private boolean isGroupOwner = false;
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

        isGroup = getIntent().getBooleanExtra("isGroup", false);
        familyId = getIntent().getStringExtra("familyId");
        if (familyId == null) familyId = "";

        senderId = FirebaseAuth.getInstance().getUid();
        if (senderId == null) senderId = "";

        if (isGroup || (familyId != null && !familyId.trim().isEmpty()) || (receiverId != null && receiverId.startsWith("family_"))) {
            isGroup = true;
            if ((familyId == null || familyId.trim().isEmpty()) && receiverId != null && receiverId.startsWith("family_")) {
                familyId = receiverId.substring("family_".length());
            }

            if (familyId == null || familyId.trim().isEmpty()) {
                Toast.makeText(this, "Group chat unavailable", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            directChatRef = FirebaseDatabase.getInstance().getReference("family_group_messages").child(familyId.trim());
            legacyChatRef = FirebaseDatabase.getInstance().getReference("family_group_messages").child(familyId.trim());

            // Fetch Family Owner UID to determine if current user is Leader/Owner
            FirebaseDatabase.getInstance().getReference("families").child(familyId.trim())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                familyOwnerUid = snapshot.child("ownerUid").getValue(String.class);
                                isGroupOwner = !TextUtils.isEmpty(senderId) && senderId.equals(familyOwnerUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            if (receiverId == null || receiverId.trim().isEmpty()) {
                Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            chatRoomId = getChatRoomId(senderId, receiverId);
            directChatRef = FirebaseDatabase.getInstance().getReference("DirectChats").child(chatRoomId);
            legacyChatRef = FirebaseDatabase.getInstance().getReference("Chats");
        }

        binding.tvChatUserName.setText(receiverName);
        binding.ivBack.setOnClickListener(v -> finish());

        View.OnClickListener openProfile = v -> {
            if (isGroup) {
                showGroupMembersDialog();
                return;
            }
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

        loadMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void showMoreOptionsMenu(View view, View.OnClickListener openProfileListener) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        if (isGroup) {
            popupMenu.getMenu().add("Group Members & Management");
        } else {
            popupMenu.getMenu().add("View Profile");
        }
        popupMenu.getMenu().add("Clear Chat History");
        popupMenu.setOnMenuItemClickListener(item -> {
            CharSequence title = item.getTitle();
            if (title != null && "View Profile".contentEquals(title)) {
                if (openProfileListener != null) openProfileListener.onClick(view);
                return true;
            } else if (title != null && "Group Members & Management".contentEquals(title)) {
                showGroupMembersDialog();
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
        if (senderId.isEmpty()) return;

        if (isGroup) {
            if (familyId == null || familyId.trim().isEmpty()) return;

            String groupKey = "family_" + familyId;
            DatabaseReference userRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                    .child(senderId).child(groupKey);
            userRecentRef.child("read").setValue(true);

            if (directChatRef != null && directChatListener != null) {
                directChatRef.removeEventListener(directChatListener);
            }

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
                            if (chat != null) {
                                if (chat.getMessageId() == null || chat.getMessageId().isEmpty()) {
                                    chat.setMessageId(key);
                                }
                                messageMap.put(key, chat);
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
            return;
        }

        if (receiverId == null || receiverId.trim().isEmpty()) return;

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
        if (msg.isEmpty() || senderId == null || senderId.isEmpty()) return;

        long time = System.currentTimeMillis();

        if (isGroup && !familyId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(senderId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot uSnap) {
                    String senderName = uSnap.child("name").getValue(String.class);
                    if (senderName == null) senderName = uSnap.child("username").getValue(String.class);
                    if (senderName == null) senderName = "Member";

                    String senderAvatar = uSnap.child("profilePic").getValue(String.class);
                    if (senderAvatar == null) senderAvatar = uSnap.child("avatar").getValue(String.class);
                    if (senderAvatar == null) senderAvatar = uSnap.child("image").getValue(String.class);

                    DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("family_group_messages").child(familyId);
                    String msgId = groupRef.push().getKey();
                    if (msgId != null) {
                        ChatMessage chatMessage = new ChatMessage(senderId, "family_" + familyId, msg, time, true);
                        chatMessage.setMessageId(msgId);
                        chatMessage.setSenderAvatar(senderAvatar);

                        Map<String, Object> msgMap = new HashMap<>();
                        msgMap.put("messageId", msgId);
                        msgMap.put("senderId", senderId);
                        msgMap.put("senderName", senderName);
                        msgMap.put("senderAvatar", senderAvatar != null ? senderAvatar : "");
                        msgMap.put("message", msg);
                        msgMap.put("timestamp", time);
                        msgMap.put("type", "text");

                        groupRef.child(msgId).setValue(msgMap);

                        // Broadcast recent chat update to all family members
                        String famGroupChatId = "family_" + familyId;
                        String fName = receiverName != null ? receiverName : "Family";

                        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId);
                        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                DatabaseReference recentRef = FirebaseDatabase.getInstance().getReference("RecentChats");
                                List<String> uids = new ArrayList<>();
                                if (snapshot.exists()) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        if (ds.getKey() != null) uids.add(ds.getKey());
                                    }
                                }
                                if (!uids.contains(senderId)) uids.add(senderId);

                                for (String mUid : uids) {
                                    DatabaseReference userRecentRef = recentRef.child(mUid).child(famGroupChatId);
                                    Map<String, Object> recentMap = new HashMap<>();
                                    recentMap.put("senderId", famGroupChatId);
                                    recentMap.put("receiverId", famGroupChatId);
                                    recentMap.put("message", msg);
                                    recentMap.put("timestamp", time);
                                    recentMap.put("isGroup", true);
                                    recentMap.put("familyId", familyId);
                                    recentMap.put("groupName", fName);
                                    recentMap.put("groupAvatar", getIntent().getStringExtra("familyLogo") != null ? getIntent().getStringExtra("familyLogo") : "");
                                    userRecentRef.setValue(recentMap);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                        binding.etMessage.setText("");
                        scrollToBottom(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else if (receiverId != null && !receiverId.isEmpty()) {
            String msgId = directChatRef.push().getKey();
            ChatMessage chatMessage = new ChatMessage(senderId, receiverId, msg, time, false);

            if (msgId != null) {
                chatMessage.setMessageId(msgId);
                directChatRef.child(msgId).setValue(chatMessage);

                ChatMessage senderChatMessage = new ChatMessage(senderId, receiverId, msg, time, true);
                senderChatMessage.setMessageId(msgId);
                DatabaseReference senderRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(senderId).child(receiverId);
                senderRecentRef.setValue(senderChatMessage);

                ChatMessage receiverChatMessage = new ChatMessage(senderId, receiverId, msg, time, false);
                receiverChatMessage.setMessageId(msgId);
                DatabaseReference receiverRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(receiverId).child(senderId);
                receiverRecentRef.setValue(receiverChatMessage);

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

    private void showGroupMembersDialog() {
        if (TextUtils.isEmpty(familyId)) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_group_members, null, false);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        View btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        RecyclerView rvGroupMembers = view.findViewById(R.id.rvGroupMembersList);
        List<GroupMemberModel> memberList = new ArrayList<>();

        if (rvGroupMembers != null) {
            rvGroupMembers.setLayoutManager(new LinearLayoutManager(this));
            GroupMemberAdapter adapter = new GroupMemberAdapter(memberList, isGroupOwner, (mUid, mName) -> {
                dialog.dismiss();
                confirmRemoveMember(mUid, mName);
            });
            rvGroupMembers.setAdapter(adapter);

            DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("family_members").child(familyId);
            membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    memberList.clear();
                    if (snapshot.exists()) {
                        List<String> uids = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey() != null) uids.add(child.getKey());
                        }

                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot uSnap) {
                                for (String mUid : uids) {
                                    if (uSnap.hasChild(mUid)) {
                                        DataSnapshot ds = uSnap.child(mUid);
                                        String name = ds.child("name").getValue(String.class);
                                        if (name == null) name = ds.child("username").getValue(String.class);
                                        if (name == null) name = "Member";

                                        String avatar = ds.child("profilePic").getValue(String.class);
                                        if (avatar == null) avatar = ds.child("avatar").getValue(String.class);

                                        boolean isLeader = (!TextUtils.isEmpty(familyOwnerUid) && familyOwnerUid.equals(mUid));
                                        memberList.add(new GroupMemberModel(mUid, name, avatar != null ? avatar : "", isLeader));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        dialog.show();
    }

    private void confirmRemoveMember(String targetUid, String targetName) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + targetName + " from the family and group chat?")
                .setPositiveButton("Remove", (dialog, which) -> removeMemberFromGroup(targetUid, targetName))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMemberFromGroup(String targetUid, String targetName) {
        if (TextUtils.isEmpty(familyId) || TextUtils.isEmpty(targetUid)) return;

        // Remove from family_members
        FirebaseDatabase.getInstance().getReference("family_members").child(familyId).child(targetUid).removeValue();

        // Clear user family details
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);
        userRef.child("familyId").removeValue();
        userRef.child("familyName").removeValue();

        // Remove RecentChat entry for removed user
        FirebaseDatabase.getInstance().getReference("RecentChats").child(targetUid).child("family_" + familyId).removeValue();

        // Send System message in group chat
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("family_group_messages").child(familyId);
        String msgId = groupRef.push().getKey();
        if (msgId != null) {
            long time = System.currentTimeMillis();
            ChatMessage sysMsg = new ChatMessage("SYSTEM", "family_" + familyId, targetName + " was removed from the family group by the Leader.", time, true);
            sysMsg.setMessageId(msgId);
            groupRef.child(msgId).setValue(sysMsg);
        }

        Toast.makeText(this, targetName + " removed from family group chat.", Toast.LENGTH_SHORT).show();
    }

    public static class GroupMemberModel {
        private String uid, name, avatar;
        private boolean isLeader;

        public GroupMemberModel(String uid, String name, String avatar, boolean isLeader) {
            this.uid = uid;
            this.name = name;
            this.avatar = avatar;
            this.isLeader = isLeader;
        }

        public String getUid() { return uid; }
        public String getName() { return name; }
        public String getAvatar() { return avatar; }
        public boolean isLeader() { return isLeader; }
    }

    public interface GroupMemberListener {
        void onRemove(String uid, String name);
    }

    public static class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
        private final List<GroupMemberModel> list;
        private final boolean isGroupOwner;
        private final GroupMemberListener listener;

        public GroupMemberAdapter(List<GroupMemberModel> list, boolean isGroupOwner, GroupMemberListener listener) {
            this.list = list;
            this.isGroupOwner = isGroupOwner;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroupMemberModel item = list.get(position);
            if (holder.tvName != null) holder.tvName.setText(item.getName());
            if (holder.tvRole != null) {
                holder.tvRole.setText(item.isLeader() ? "Leader / Owner" : "Member");
                holder.tvRole.setTextColor(item.isLeader() ? Color.parseColor("#FFD700") : Color.parseColor("#A0B4C8"));
            }
            if (holder.ivAvatar != null && !TextUtils.isEmpty(item.getAvatar())) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getAvatar())
                        .placeholder(R.drawable.logo_placeholder)
                        .circleCrop()
                        .into(holder.ivAvatar);
            }

            if (holder.btnRemove != null) {
                boolean showRemove = isGroupOwner && !item.isLeader();
                holder.btnRemove.setVisibility(showRemove ? View.VISIBLE : View.GONE);
                holder.btnRemove.setOnClickListener(v -> {
                    if (listener != null) listener.onRemove(item.getUid(), item.getName());
                });
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, btnRemove;
            ShapeableImageView ivAvatar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvRole = itemView.findViewById(R.id.tvMemberRole);
                btnRemove = itemView.findViewById(R.id.btnRemoveMember);
                ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            }
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

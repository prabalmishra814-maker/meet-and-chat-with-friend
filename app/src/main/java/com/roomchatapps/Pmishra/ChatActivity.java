package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private String receiverId;
    private String senderId;
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        binding.rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChatMessages.setAdapter(chatAdapter);

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        loadMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        if (senderId.isEmpty() || receiverId == null || receiverId.trim().isEmpty()) return;

        // Mark current recent chat conversation as read when opened
        DatabaseReference userRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                .child(senderId).child(receiverId);
        userRecentRef.child("read").setValue(true);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ChatMessage chat = ds.getValue(ChatMessage.class);
                        if (chat != null && chat.getSenderId() != null && chat.getReceiverId() != null) {
                            boolean isSenderAndRecv = chat.getSenderId().equals(senderId) && chat.getReceiverId().equals(receiverId);
                            boolean isRecvAndSender = chat.getSenderId().equals(receiverId) && chat.getReceiverId().equals(senderId);
                            if (isSenderAndRecv || isRecvAndSender) {
                                chatMessages.add(chat);
                                // Mark received messages as read
                                if (isRecvAndSender && !chat.isRead()) {
                                    ds.getRef().child("read").setValue(true);
                                }
                            }
                        }
                    }
                }
                Collections.sort(chatMessages, (c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()));
                chatAdapter.notifyDataSetChanged();
                if (!chatMessages.isEmpty()) {
                    binding.rvChatMessages.smoothScrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessage() {
        String msg = binding.etMessage.getText().toString().trim();
        if (!msg.isEmpty() && !senderId.isEmpty() && receiverId != null && !receiverId.isEmpty()) {
            String msgId = chatRef.push().getKey();
            long time = System.currentTimeMillis();
            ChatMessage chatMessage = new ChatMessage(senderId, receiverId, msg, time, false);
            if (msgId != null) {
                chatRef.child(msgId).setValue(chatMessage);

                // Update RecentChats for sender (marked read = true for sender)
                ChatMessage senderChatMessage = new ChatMessage(senderId, receiverId, msg, time, true);
                DatabaseReference senderRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(senderId).child(receiverId);
                senderRecentRef.setValue(senderChatMessage);

                // Update RecentChats for receiver (marked read = false for receiver until seen!)
                ChatMessage receiverChatMessage = new ChatMessage(senderId, receiverId, msg, time, false);
                DatabaseReference receiverRecentRef = FirebaseDatabase.getInstance().getReference("RecentChats")
                        .child(receiverId).child(senderId);
                receiverRecentRef.setValue(receiverChatMessage);

                // Trigger in-app and system status bar notification for receiver
                NotificationHelper.sendMessageNotification(receiverId, msg);

                binding.etMessage.setText("");
            }
        }
    }
}

package com.roomchatapps.Pmishra.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.UserDetailActivity;
import com.roomchatapps.Pmishra.databinding.ItemChatMessageReceivedBinding;
import com.roomchatapps.Pmishra.databinding.ItemChatMessageRoomBinding;
import com.roomchatapps.Pmishra.databinding.ItemChatMessageSentBinding;
import com.roomchatapps.Pmishra.models.ChatMessage;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_ROOM = 3;

    private final List<ChatMessage> chatMessages;
    private final String currentUserId;
    private final boolean isRoomChat;
    private int lastAnimatedPosition = -1;

    public ChatAdapter() {
        this(new ArrayList<>(), true);
    }

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this(chatMessages, false);
    }

    public ChatAdapter(List<ChatMessage> chatMessages, boolean isRoomChat) {
        this.chatMessages = chatMessages != null ? chatMessages : new ArrayList<>();
        this.isRoomChat = isRoomChat;
        String uid = FirebaseAuth.getInstance().getUid();
        this.currentUserId = uid != null ? uid : "";
    }

    private final Handler expireHandler = new Handler(Looper.getMainLooper());
    public static final long GIFT_MESSAGE_EXPIRE_MS = 10 * 1000L;       // 10 seconds for Gift messages
    public static final long USER_MESSAGE_EXPIRE_MS = 15 * 60 * 1000L; // 15 minutes for User messages
    public static final long ROOM_MESSAGE_EXPIRE_MS = USER_MESSAGE_EXPIRE_MS;

    private RecyclerView attachedRecyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.attachedRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.attachedRecyclerView = null;
    }

    public void addAutoExpiringMessage(ChatMessage chatMsg, long expireMs) {
        if (chatMsg == null) return;
        chatMessages.add(chatMsg);
        int position = chatMessages.size() - 1;
        notifyItemInserted(position);

        long delay = expireMs > 0 ? expireMs : ROOM_MESSAGE_EXPIRE_MS;

        expireHandler.postDelayed(() -> {
            int index = chatMessages.indexOf(chatMsg);
            if (index != -1) {
                if (attachedRecyclerView != null) {
                    RecyclerView.ViewHolder holder = attachedRecyclerView.findViewHolderForAdapterPosition(index);
                    if (holder != null && holder.itemView != null) {
                        View v = holder.itemView;
                        v.animate()
                                .alpha(0f)
                                .translationX(-120f)
                                .scaleY(0.7f)
                                .setDuration(380L)
                                .withEndAction(() -> {
                                    v.setAlpha(1.0f);
                                    v.setTranslationX(0f);
                                    v.setScaleY(1.0f);
                                    int currentIndex = chatMessages.indexOf(chatMsg);
                                    if (currentIndex != -1) {
                                        chatMessages.remove(currentIndex);
                                        notifyItemRemoved(currentIndex);
                                    }
                                })
                                .start();
                        return;
                    }
                }
                chatMessages.remove(index);
                notifyItemRemoved(index);
            }
        }, delay);
    }

    public void clearAllMessages() {
        if (expireHandler != null) {
            expireHandler.removeCallbacksAndMessages(null);
        }
        int size = chatMessages.size();
        if (size > 0) {
            chatMessages.clear();
            notifyDataSetChanged();
        }
    }

    public void addMessages(List<ZegoBroadcastMessageInfo> zegoMessages) {
        if (zegoMessages == null || zegoMessages.isEmpty()) return;
        for (ZegoBroadcastMessageInfo msg : zegoMessages) {
            if (msg != null) {
                String sender = msg.fromUser != null ? msg.fromUser.userName : "User";
                String text = msg.message != null ? msg.message : "";
                ChatMessage chatMsg = new ChatMessage();
                chatMsg.setSenderId(msg.fromUser != null ? msg.fromUser.userID : "");
                chatMsg.setMessage(sender + " : " + text);
                chatMsg.setTimestamp(msg.sendTime > 0 ? msg.sendTime : System.currentTimeMillis());
                addAutoExpiringMessage(chatMsg, ROOM_MESSAGE_EXPIRE_MS);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isRoomChat) {
            return TYPE_ROOM;
        }
        if (position < 0 || position >= chatMessages.size()) return TYPE_RECEIVED;
        ChatMessage msg = chatMessages.get(position);
        if (msg != null && msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ROOM) {
            ItemChatMessageRoomBinding binding = ItemChatMessageRoomBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new RoomMessageViewHolder(binding);
        } else if (viewType == TYPE_SENT) {
            ItemChatMessageSentBinding binding = ItemChatMessageSentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SentMessageViewHolder(binding);
        } else {
            ItemChatMessageReceivedBinding binding = ItemChatMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int adapterPos = holder.getBindingAdapterPosition();
        final int targetPos = (adapterPos != RecyclerView.NO_POSITION) ? adapterPos : position;

        if (targetPos < 0 || targetPos >= chatMessages.size()) return;
        ChatMessage message = chatMessages.get(targetPos);
        if (message == null) return;

        // Animate newly added messages
        if (targetPos > lastAnimatedPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(20f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(240)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            lastAnimatedPosition = targetPos;
        }

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).setData(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).setData(message);
        } else if (holder instanceof RoomMessageViewHolder) {
            ((RoomMessageViewHolder) holder).setData(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages != null ? chatMessages.size() : 0;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageSentBinding binding;

        SentMessageViewHolder(ItemChatMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage message) {
            if (message == null) return;
            binding.tvMessage.setText(message.getMessage() != null ? message.getMessage() : "");
            binding.tvTime.setText(formatDate(message.getTimestamp()));

            if (binding.ivReadStatus != null) {
                if (message.isRead()) {
                    binding.ivReadStatus.setColorFilter(Color.parseColor("#00FFC6"));
                } else {
                    binding.ivReadStatus.setColorFilter(Color.parseColor("#D0FFFFFF"));
                }
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageReceivedBinding binding;

        ReceivedMessageViewHolder(ItemChatMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage message) {
            if (message == null) return;
            Context ctx = itemView.getContext();
            binding.tvMessage.setText(message.getMessage() != null ? message.getMessage() : "");
            binding.tvTime.setText(formatDate(message.getTimestamp()));

            if (binding.ivAvatar != null) {
                binding.ivAvatar.setVisibility(View.VISIBLE);
                String directAvatar = message.getSenderAvatar();
                if (directAvatar != null && !directAvatar.trim().isEmpty()) {
                    Glide.with(ctx)
                            .load(directAvatar)
                            .placeholder(R.drawable.logo_placeholder)
                            .error(R.drawable.logo_placeholder)
                            .into(binding.ivAvatar);
                } else {
                    binding.ivAvatar.setImageResource(R.drawable.logo_placeholder);
                }
            }

            FrameUtils.clearFrame(binding.ivFrame, binding.svgaFrame);

            String senderId = message.getSenderId();
            if (senderId != null && !senderId.trim().isEmpty()) {
                View.OnClickListener openProfile = v -> {
                    if (ctx != null) {
                        Intent intent = new Intent(ctx, UserDetailActivity.class);
                        intent.putExtra("uid", senderId);
                        ctx.startActivity(intent);
                    }
                };
                if (binding.avatarContainer != null) binding.avatarContainer.setOnClickListener(openProfile);
                if (binding.ivAvatar != null) binding.ivAvatar.setOnClickListener(openProfile);

                UserProfileCache.getUserProfile(senderId, profile -> {
                    if (ctx == null) return;
                    if (ctx instanceof Activity) {
                        Activity act = (Activity) ctx;
                        if (act.isFinishing() || act.isDestroyed()) return;
                    }

                    if (profile != null) {
                        if (binding.ivAvatar != null && profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                            Glide.with(ctx)
                                    .load(profile.avatarUrl)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .error(R.drawable.logo_placeholder)
                                    .into(binding.ivAvatar);
                        }
                        String equipped = (profile.equippedFrame != null) ? profile.equippedFrame : "";
                        FrameUtils.displayFrame(ctx, equipped, binding.ivFrame, binding.svgaFrame);
                    }
                });
            }
        }
    }

    static class RoomMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageRoomBinding binding;

        RoomMessageViewHolder(ItemChatMessageRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage message) {
            if (message == null) return;
            Context ctx = itemView.getContext();
            String rawMessage = message.getMessage() != null ? message.getMessage() : "";
            String senderId = message.getSenderId();
            boolean isSystemMsg = "SYSTEM".equalsIgnoreCase(senderId)
                    || rawMessage.contains("joined the room")
                    || rawMessage.contains("left the room")
                    || rawMessage.contains("Enter the room");
            boolean isGiftMsg = rawMessage.contains(" sent ") && rawMessage.contains(" 🎁");

            // 1. Choose background pill style based on message type
            if (binding.bubbleLayout != null) {
                if (isGiftMsg) {
                    binding.bubbleLayout.setBackgroundResource(R.drawable.bg_room_chat_pill_gift);
                } else if (isSystemMsg) {
                    binding.bubbleLayout.setBackgroundResource(R.drawable.bg_room_chat_pill_system);
                } else {
                    binding.bubbleLayout.setBackgroundResource(R.drawable.bg_room_chat_pill);
                }
            }

            // 2. Format message text dynamically with SpannableStringBuilder
            SpannableStringBuilder builder = new SpannableStringBuilder();

            if (isGiftMsg) {
                int sentIndex = rawMessage.indexOf(" sent ");
                if (sentIndex != -1) {
                    String senderName = rawMessage.substring(0, sentIndex);
                    String rest = rawMessage.substring(sentIndex);

                    int start = builder.length();
                    builder.append(senderName);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD700")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    start = builder.length();
                    builder.append(rest);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFE082")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    builder.append(rawMessage);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD700")), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (rawMessage.contains("joined the room") || rawMessage.contains("Enter the room")) {
                int joinIndex = rawMessage.contains("joined the room") ? rawMessage.indexOf("joined the room") : rawMessage.indexOf("Enter the room");
                if (joinIndex > 0) {
                    String userName = rawMessage.substring(0, joinIndex).trim();
                    String action = rawMessage.substring(joinIndex);

                    int start = builder.length();
                    builder.append(userName);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#80DEEA")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    builder.append(" ");
                    start = builder.length();
                    builder.append(action);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#80CBC4")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    builder.append(rawMessage);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#80CBC4")), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (rawMessage.contains("left the room")) {
                int leftIndex = rawMessage.indexOf("left the room");
                if (leftIndex > 0) {
                    String userName = rawMessage.substring(0, leftIndex).trim();
                    String action = rawMessage.substring(leftIndex);

                    int start = builder.length();
                    builder.append(userName);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#E0E0E0")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    builder.append(" ");
                    start = builder.length();
                    builder.append(action);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCDD2")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    builder.append(rawMessage);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCDD2")), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (rawMessage.contains(" : ")) {
                int colonIndex = rawMessage.indexOf(" : ");
                String senderName = rawMessage.substring(0, colonIndex);
                String msgText = rawMessage.substring(colonIndex + 3);

                int start = builder.length();
                builder.append(senderName);
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD54F")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = builder.length();
                builder.append(" : ");
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#B0BEC5")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = builder.length();
                builder.append(msgText);
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.append(rawMessage);
            }

            binding.tvMessage.setText(builder);

            // Configure Gift Icon if gift message
            if (binding.ivGiftIcon != null) {
                int giftIcon = message.getGiftIconRes();
                if (giftIcon != 0) {
                    binding.ivGiftIcon.setVisibility(View.VISIBLE);
                    binding.ivGiftIcon.setImageResource(giftIcon);
                } else if (isGiftMsg) {
                    binding.ivGiftIcon.setVisibility(View.VISIBLE);
                    binding.ivGiftIcon.setImageResource(R.drawable.gift_icon);
                } else {
                    binding.ivGiftIcon.setVisibility(View.GONE);
                }
            }

            // 3. Configure Avatar and System Icon
            if ("SYSTEM".equalsIgnoreCase(senderId)) {
                if (binding.avatarContainer != null) binding.avatarContainer.setVisibility(View.GONE);
                if (binding.ivSystemIcon != null) binding.ivSystemIcon.setVisibility(View.VISIBLE);
                if (binding.tvLevelBadge != null) binding.tvLevelBadge.setVisibility(View.GONE);
            } else {
                if (binding.ivSystemIcon != null) binding.ivSystemIcon.setVisibility(View.GONE);
                if (binding.avatarContainer != null) binding.avatarContainer.setVisibility(View.VISIBLE);

                if (binding.ivAvatar != null) {
                    String directAvatar = message.getSenderAvatar();
                    if (directAvatar == null || directAvatar.trim().isEmpty()) {
                        directAvatar = SessionManager.getInstance(ctx).getAvatar();
                    }

                    if (directAvatar != null && !directAvatar.trim().isEmpty()) {
                        Glide.with(ctx)
                                .load(directAvatar)
                                .placeholder(R.drawable.logo_placeholder)
                                .error(R.drawable.logo_placeholder)
                                .into(binding.ivAvatar);
                    } else {
                        binding.ivAvatar.setImageResource(R.drawable.logo_placeholder);
                    }
                }

                FrameUtils.clearFrame(binding.ivFrame, binding.svgaFrame);

                if (senderId != null && !senderId.trim().isEmpty()) {
                    View.OnClickListener openProfile = v -> {
                        if (ctx != null) {
                            Intent intent = new Intent(ctx, UserDetailActivity.class);
                            intent.putExtra("uid", senderId);
                            ctx.startActivity(intent);
                        }
                    };
                    if (binding.avatarContainer != null) binding.avatarContainer.setOnClickListener(openProfile);
                    if (binding.ivAvatar != null) binding.ivAvatar.setOnClickListener(openProfile);

                    UserProfileCache.getUserProfile(senderId, profile -> {
                        if (ctx == null) return;
                        if (ctx instanceof Activity) {
                            Activity act = (Activity) ctx;
                            if (act.isFinishing() || act.isDestroyed()) return;
                        }

                        if (profile != null) {
                            if (binding.ivAvatar != null && profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                                Glide.with(ctx)
                                        .load(profile.avatarUrl)
                                        .placeholder(R.drawable.logo_placeholder)
                                        .error(R.drawable.logo_placeholder)
                                        .into(binding.ivAvatar);
                            }
                            String equipped = (profile.equippedFrame != null) ? profile.equippedFrame : "";
                            FrameUtils.displayFrame(ctx, equipped, binding.ivFrame, binding.svgaFrame);
                        }
                    });
                }
            }
        }
    }

    private static String formatDate(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
    }
}

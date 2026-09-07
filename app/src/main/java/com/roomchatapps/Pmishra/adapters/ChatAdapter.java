package com.roomchatapps.Pmishra.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.databinding.ItemChatMessageReceivedBinding;
import com.roomchatapps.Pmishra.databinding.ItemChatMessageSentBinding;
import com.roomchatapps.Pmishra.models.ChatMessage;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatMessage> chatMessages;
    private final String currentUserId;
    private int lastAnimatedPosition = -1;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        String uid = FirebaseAuth.getInstance().getUid();
        this.currentUserId = uid != null ? uid : "";
    }

    @Override
    public int getItemViewType(int position) {
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
        if (viewType == TYPE_SENT) {
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

        // Animate ONLY newly added messages (Slide-in + Scale 0.97 -> 1.0 + Fade-in)
        if (targetPos > lastAnimatedPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(25f);
            holder.itemView.setScaleX(0.97f);
            holder.itemView.setScaleY(0.97f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(280)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            lastAnimatedPosition = targetPos;
        }

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).setData(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).setData(message);
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

            String senderId = message.getSenderId();
            if (senderId != null && !senderId.trim().isEmpty()) {
                View.OnClickListener openProfile = v -> {
                    Context ctx = itemView.getContext();
                    if (ctx != null) {
                        android.content.Intent intent = new android.content.Intent(ctx, com.roomchatapps.Pmishra.UserDetailActivity.class);
                        intent.putExtra("uid", senderId);
                        ctx.startActivity(intent);
                    }
                };
                if (binding.avatarContainer != null) binding.avatarContainer.setOnClickListener(openProfile);
                if (binding.ivAvatar != null) binding.ivAvatar.setOnClickListener(openProfile);

                UserProfileCache.getUserProfile(senderId, profile -> {
                    Context ctx = itemView.getContext();
                    if (ctx == null) return;
                    if (ctx instanceof Activity) {
                        Activity act = (Activity) ctx;
                        if (act.isFinishing() || act.isDestroyed()) return;
                    }

                    if (profile != null) {
                        if (profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                            Glide.with(ctx)
                                    .load(profile.avatarUrl)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .error(R.drawable.logo_placeholder)
                                    .into(binding.ivAvatar);
                        }

                        int frameRes = FrameUtils.getFrameDrawableRes(ctx, profile.equippedFrame);
                        if (frameRes != 0) {
                            binding.ivFrame.setImageResource(frameRes);
                            binding.ivFrame.setVisibility(View.VISIBLE);
                        } else {
                            binding.ivFrame.setVisibility(View.GONE);
                        }
                    }
                });
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
            binding.tvMessage.setText(message.getMessage() != null ? message.getMessage() : "");
            binding.tvTime.setText(formatDate(message.getTimestamp()));

            String senderId = message.getSenderId();
            if (senderId != null && !senderId.trim().isEmpty()) {
                View.OnClickListener openProfile = v -> {
                    Context ctx = itemView.getContext();
                    if (ctx != null) {
                        android.content.Intent intent = new android.content.Intent(ctx, com.roomchatapps.Pmishra.UserDetailActivity.class);
                        intent.putExtra("uid", senderId);
                        ctx.startActivity(intent);
                    }
                };
                if (binding.avatarContainer != null) binding.avatarContainer.setOnClickListener(openProfile);
                if (binding.ivAvatar != null) binding.ivAvatar.setOnClickListener(openProfile);

                UserProfileCache.getUserProfile(senderId, profile -> {
                    Context ctx = itemView.getContext();
                    if (ctx == null) return;
                    if (ctx instanceof Activity) {
                        Activity act = (Activity) ctx;
                        if (act.isFinishing() || act.isDestroyed()) return;
                    }

                    if (profile != null) {
                        if (profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                            Glide.with(ctx)
                                    .load(profile.avatarUrl)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .error(R.drawable.logo_placeholder)
                                    .into(binding.ivAvatar);
                        }

                        int frameRes = FrameUtils.getFrameDrawableRes(ctx, profile.equippedFrame);
                        if (frameRes != 0) {
                            binding.ivFrame.setImageResource(frameRes);
                            binding.ivFrame.setVisibility(View.VISIBLE);
                        } else {
                            binding.ivFrame.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    private static String formatDate(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
    }
}

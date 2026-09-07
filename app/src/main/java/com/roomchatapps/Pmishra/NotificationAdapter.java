package com.roomchatapps.Pmishra;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.databinding.ItemNotificationBinding;
import com.roomchatapps.Pmishra.models.NotificationModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification, int position);
        void onNotificationDismiss(NotificationModel notification, int position);
    }

    private final List<NotificationModel> notificationList;
    private final OnNotificationClickListener listener;
    private int lastAnimatedPosition = -1;

    public NotificationAdapter(List<NotificationModel> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel item = notificationList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvNotifTitle.setText(item.getTitle() != null ? item.getTitle() : "Notification");
        holder.binding.tvNotifMessage.setText(item.getMessage() != null ? item.getMessage() : "");
        holder.binding.tvNotifTime.setText(formatTimeAgo(item.getTimestamp()));

        // Load sender avatar or fallback
        if (item.getSenderAvatar() != null && !item.getSenderAvatar().isEmpty()) {
            Glide.with(context)
                    .load(item.getSenderAvatar())
                    .placeholder(R.drawable.ic_person)
                    .into(holder.binding.ivSenderAvatar);
        } else {
            holder.binding.ivSenderAvatar.setImageResource(R.drawable.ic_person);
        }

        // Configure type badge icon
        String type = item.getType() != null ? item.getType().toUpperCase() : "SYSTEM";
        switch (type) {
            case "GIFT":
                holder.binding.ivTypeBadge.setImageResource(R.drawable.game_mic_charm_pk_diamond_ic);
                break;
            case "FOLLOW":
                holder.binding.ivTypeBadge.setImageResource(R.drawable.common_gender_male_blue_16_ic);
                break;
            case "ROOM_INVITE":
                holder.binding.ivTypeBadge.setImageResource(R.drawable.app_create_room_ic);
                break;
            case "COMMENT":
                holder.binding.ivTypeBadge.setImageResource(R.drawable.baseline_comment_24);
                break;
            default:
                holder.binding.ivTypeBadge.setImageResource(R.drawable.app_alert_ic);
                break;
        }

        // Unread badge logic & pulsing glow animation
        if (!item.isRead()) {
            holder.binding.vUnreadDot.setVisibility(View.VISIBLE);
            holder.binding.cvItemContainer.setCardBackgroundColor(0x2240E0D0); // Subdued cyan glow background
            startPulsingAnimation(holder.binding.vUnreadDot);
        } else {
            holder.binding.vUnreadDot.setVisibility(View.GONE);
            holder.binding.cvItemContainer.setCardBackgroundColor(0xFF141E33);
            holder.binding.vUnreadDot.clearAnimation();
        }

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(item, pos);
                }
            }
        });

        // Entrance animation
        setEntranceAnimation(holder.itemView, position);
    }

    private void startPulsingAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.35f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.35f, 1.0f);
        scaleX.setDuration(1200);
        scaleY.setDuration(1200);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }

    private void setEntranceAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.setTranslationX(120f);
            viewToAnimate.setAlpha(0f);
            viewToAnimate.setScaleX(0.95f);
            viewToAnimate.setScaleY(0.95f);

            viewToAnimate.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(380)
                    .setStartDelay(Math.min(position * 40L, 300L))
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();

            lastAnimatedPosition = position;
        }
    }

    private String formatTimeAgo(long timestamp) {
        if (timestamp <= 0) return "Just now";
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60_000) return "Just now";
        if (diff < 3600_000) return (diff / 60_000) + "m ago";
        if (diff < 86400_000) return (diff / 3600_000) + "h ago";
        return (diff / 86400_000) + "d ago";
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;

        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

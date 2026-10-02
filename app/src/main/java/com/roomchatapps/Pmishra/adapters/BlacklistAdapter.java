package com.roomchatapps.Pmishra.adapters;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import java.util.List;

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.ViewHolder> {

    public interface OnUnblacklistClickListener {
        void onUnblacklist(BlacklistItem item);
    }

    public static class BlacklistItem {
        private final String userId;
        private final String userName;
        private final String userAvatar;
        private final long expiryTimestamp;

        public BlacklistItem(String userId, String userName, String userAvatar, long expiryTimestamp) {
            this.userId = userId;
            this.userName = userName;
            this.userAvatar = userAvatar;
            this.expiryTimestamp = expiryTimestamp;
        }

        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserAvatar() { return userAvatar; }
        public long getExpiryTimestamp() { return expiryTimestamp; }

        public String getFormattedDuration() {
            if (expiryTimestamp <= 0) {
                return "Status: Permanently Banned";
            }
            long remainingMs = expiryTimestamp - System.currentTimeMillis();
            if (remainingMs <= 0) {
                return "Status: Expired";
            }
            long hours = remainingMs / (3600 * 1000);
            long minutes = (remainingMs % (3600 * 1000)) / (60 * 1000);
            if (hours > 0) {
                return "Status: Banned (" + hours + "h " + minutes + "m remaining)";
            } else {
                return "Status: Banned (" + Math.max(1, minutes) + "m remaining)";
            }
        }
    }

    private List<BlacklistItem> memberList;
    private final OnUnblacklistClickListener listener;
    private long lastClickTime = 0;

    public BlacklistAdapter(List<BlacklistItem> memberList, OnUnblacklistClickListener listener) {
        this.memberList = memberList;
        this.listener = listener;
    }

    public void updateList(List<BlacklistItem> newList) {
        this.memberList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blacklist_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlacklistItem item = memberList.get(position);

        String name = (item.getUserName() != null && !item.getUserName().trim().isEmpty())
                ? item.getUserName() : "Member";
        holder.tvBlacklistName.setText(name);

        String idText = "ID: " + (item.getUserId() != null ? item.getUserId() : "");
        holder.tvBlacklistUserId.setText(idText);

        holder.tvBlacklistDuration.setText(item.getFormattedDuration());

        Glide.with(holder.itemView.getContext())
                .load(item.getUserAvatar())
                .placeholder(R.drawable.logo_placeholder)
                .into(holder.ivBlacklistProfile);

        holder.btnRemoveBlacklist.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
            lastClickTime = SystemClock.elapsedRealtime();
            if (listener != null) {
                listener.onUnblacklist(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList != null ? memberList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBlacklistProfile;
        TextView tvBlacklistName, tvBlacklistUserId, tvBlacklistDuration;
        AppCompatButton btnRemoveBlacklist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBlacklistProfile = itemView.findViewById(R.id.ivBlacklistProfile);
            tvBlacklistName = itemView.findViewById(R.id.tvBlacklistName);
            tvBlacklistUserId = itemView.findViewById(R.id.tvBlacklistUserId);
            tvBlacklistDuration = itemView.findViewById(R.id.tvBlacklistDuration);
            btnRemoveBlacklist = itemView.findViewById(R.id.btnRemoveBlacklist);
        }
    }
}
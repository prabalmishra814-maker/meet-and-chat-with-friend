package com.roomchatapps.Pmishra.adapters;

// ROOM ADMIN FIX
import android.graphics.Color;
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

public class AdminMemberAdapter extends RecyclerView.Adapter<AdminMemberAdapter.ViewHolder> {

    public interface OnRoleActionListener {
        void onMakeAdmin(AdminMemberItem item);
        void onRemoveAdmin(AdminMemberItem item);
    }

    public static class AdminMemberItem {
        private final String userId;
        private final String userName;
        private final String userAvatar;
        private String role; // "host", "admin", "member"

        public AdminMemberItem(String userId, String userName, String userAvatar, String role) {
            this.userId = userId;
            this.userName = userName;
            this.userAvatar = userAvatar;
            this.role = role != null ? role.toLowerCase() : "member";
        }

        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserAvatar() { return userAvatar; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role != null ? role.toLowerCase() : "member"; }
        public boolean isHost() { return "host".equalsIgnoreCase(role); }
        public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
    }

    private List<AdminMemberItem> memberList;
    private final OnRoleActionListener listener;
    private long lastClickTime = 0;

    public AdminMemberAdapter(List<AdminMemberItem> memberList, OnRoleActionListener listener) {
        this.memberList = memberList;
        this.listener = listener;
    }

    public void updateList(List<AdminMemberItem> newList) {
        this.memberList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminMemberItem item = memberList.get(position);

        String displayName = (item.getUserName() != null && !item.getUserName().trim().isEmpty())
                ? item.getUserName() : "Member";
        holder.tvAdminName.setText(displayName);
        String idText = "ID: " + (item.getUserId() != null ? item.getUserId() : "");
        holder.tvAdminUserId.setText(idText);

        Glide.with(holder.itemView.getContext())
                .load(item.getUserAvatar())
                .placeholder(R.drawable.logo_placeholder)
                .into(holder.ivAdminProfile);

        if (item.isHost()) {
            // HOST ROLE (Host Protection: cannot be demoted or promoted)
            holder.tvAdminRoleBadge.setText("HOST");
            holder.tvAdminRoleBadge.setTextColor(Color.parseColor("#FFD700")); // Gold
            holder.btnAdminAction.setVisibility(View.GONE);
        } else if (item.isAdmin()) {
            // ADMINISTRATOR ROLE
            holder.tvAdminRoleBadge.setText("ADMIN");
            holder.tvAdminRoleBadge.setTextColor(Color.parseColor("#00E5FF")); // Cyan
            holder.btnAdminAction.setVisibility(View.VISIBLE);
            holder.btnAdminAction.setText("Remove Admin");
            holder.btnAdminAction.setBackgroundResource(R.drawable.bg_cancel_button);

            holder.btnAdminAction.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return; // Multiple click protection
                lastClickTime = SystemClock.elapsedRealtime();
                if (listener != null) listener.onRemoveAdmin(item);
            });
        } else {
            // NORMAL MEMBER ROLE
            holder.tvAdminRoleBadge.setText("MEMBER");
            holder.tvAdminRoleBadge.setTextColor(Color.parseColor("#A0A0A0")); // Muted Silver
            holder.btnAdminAction.setVisibility(View.VISIBLE);
            holder.btnAdminAction.setText("Make Admin");
            holder.btnAdminAction.setBackgroundResource(R.drawable.bg_send_button_glow);

            holder.btnAdminAction.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return; // Multiple click protection
                lastClickTime = SystemClock.elapsedRealtime();
                if (listener != null) listener.onMakeAdmin(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return memberList != null ? memberList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAdminProfile;
        TextView tvAdminName, tvAdminUserId, tvAdminRoleBadge;
        AppCompatButton btnAdminAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAdminProfile = itemView.findViewById(R.id.ivAdminProfile);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvAdminUserId = itemView.findViewById(R.id.tvAdminUserId);
            tvAdminRoleBadge = itemView.findViewById(R.id.tvAdminRoleBadge);
            btnAdminAction = itemView.findViewById(R.id.btnAdminAction);
        }
    }
}

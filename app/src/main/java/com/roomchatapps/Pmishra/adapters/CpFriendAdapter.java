package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CpFriendAdapter extends RecyclerView.Adapter<CpFriendAdapter.ViewHolder> {

    public interface OnInviteClickListener {
        void onInviteClick(User user, int position);
    }

    private final Context context;
    private final List<User> originalList;
    private final List<User> displayList;
    private final Set<String> invitedUserIds = new HashSet<>();
    private OnInviteClickListener listener;

    public CpFriendAdapter(Context context, List<User> userList) {
        this.context = context;
        this.originalList = userList != null ? userList : new ArrayList<>();
        this.displayList = new ArrayList<>(this.originalList);
    }

    public void setOnInviteClickListener(OnInviteClickListener listener) {
        this.listener = listener;
    }

    private String currentQuery = "";

    public void setUsers(List<User> userList) {
        this.originalList.clear();
        if (userList != null) {
            this.originalList.addAll(userList);
        }
        filter(currentQuery);
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query : "";
        displayList.clear();
        if (TextUtils.isEmpty(query) || query.trim().isEmpty()) {
            displayList.addAll(originalList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            String[] tokens = lowerQuery.split("\\s+");

            for (User user : originalList) {
                String profileId = user.getProfileId() != null ? user.getProfileId().toLowerCase() : "";
                String userId = user.getUserId() != null ? user.getUserId().toLowerCase() : "";
                String nameStr = user.getUserName() != null ? user.getUserName().toLowerCase() : "";
                
                // Remove special emojis for clean matching if user types simple letters
                String cleanName = nameStr.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();

                boolean directMatch = profileId.contains(lowerQuery) || 
                                      userId.contains(lowerQuery) || 
                                      nameStr.contains(lowerQuery) || 
                                      cleanName.contains(lowerQuery);

                if (directMatch) {
                    displayList.add(user);
                } else {
                    // Token-based matching (matches if all words typed are present in ID or Name)
                    boolean allTokensMatch = true;
                    for (String token : tokens) {
                        if (!token.isEmpty()) {
                            boolean tokenInId = profileId.contains(token) || userId.contains(token);
                            boolean tokenInName = nameStr.contains(token) || cleanName.contains(token);
                            if (!tokenInId && !tokenInName) {
                                allTokensMatch = false;
                                break;
                            }
                        }
                    }
                    if (allTokensMatch && tokens.length > 0) {
                        displayList.add(user);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void markAsInvited(String userId) {
        if (userId != null) {
            invitedUserIds.add(userId);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cp_friend_candidate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = displayList.get(position);

        String name = !TextUtils.isEmpty(user.getUserName()) ? user.getUserName() : "User";
        holder.tvFriendName.setText(name);

        String displayId = user.getProfileId();
        if (TextUtils.isEmpty(displayId)) {
            displayId = user.getUserId();
        }
        if (TextUtils.isEmpty(displayId)) {
            displayId = "100098180";
        }
        holder.tvFriendId.setText("ID:" + displayId);

        String avatarUrl = user.getUserIcon();
        if (!TextUtils.isEmpty(avatarUrl)) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.img_20260904_135725)
                    .error(R.drawable.img_20260904_135725)
                    .into(holder.ivFriendAvatar);
        } else {
            holder.ivFriendAvatar.setImageResource(R.drawable.img_20260904_135725);
        }

        boolean isInvited = invitedUserIds.contains(user.getUserId()) || invitedUserIds.contains(user.getProfileId());
        boolean isSearching = !TextUtils.isEmpty(currentQuery) && !currentQuery.trim().isEmpty();

        if (isInvited) {
            holder.btnInvite.setText("Invited");
            holder.btnInvite.setBackgroundResource(R.drawable.bg_search_input_pill);
            holder.btnInvite.setTextColor(Color.parseColor("#888888"));
            holder.btnInvite.setEnabled(false);
        } else {
            if (isSearching) {
                holder.btnInvite.setText("Protect");
            } else {
                holder.btnInvite.setText("Invite");
            }
            holder.btnInvite.setBackgroundResource(R.drawable.bg_btn_invite_gradient);
            holder.btnInvite.setTextColor(Color.parseColor("#111111"));
            holder.btnInvite.setEnabled(true);
            holder.btnInvite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInviteClick(user, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivFriendAvatar;
        TextView tvFriendName;
        TextView tvFriendId;
        TextView btnInvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvFriendId = itemView.findViewById(R.id.tvFriendId);
            btnInvite = itemView.findViewById(R.id.btnInvite);
        }
    }
}

package com.roomchatapps.Pmishra.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.UserDetailActivity;
import com.roomchatapps.Pmishra.models.LeaderboardModel;

import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardModel> list;

    public LeaderboardAdapter(List<LeaderboardModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardModel item = list.get(position);

        holder.tvRank.setText(String.valueOf(item.getRank()));
        holder.tvUserName.setText(item.getName() != null ? item.getName() : "User");
        
        String sub = item.getProfileId() != null && !item.getProfileId().isEmpty() ? "ID: " + item.getProfileId() : "User";
        holder.tvUserSub.setText(sub);

        holder.tvSpentCoins.setText(formatCoins(item.getSpentCoins()));

        // Highlight top list ranks (Ranks 4 & 5) with glowing purple card background
        if (item.getRank() == 4 || item.getRank() == 5) {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_leaderboard_item_rank4);
        } else {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_leaderboard_item_normal);
        }

        if (item.getAvatar() != null && !item.getAvatar().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getAvatar())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }

        holder.itemView.setOnClickListener(v -> {
            if (item.getUid() != null) {
                Intent intent = new Intent(holder.itemView.getContext(), UserDetailActivity.class);
                intent.putExtra("uid", item.getUid());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static String formatCoins(long coins) {
        if (coins < 1000) {
            return String.valueOf(coins);
        } else if (coins < 1000000) {
            return String.format(Locale.US, "%.1fK", coins / 1000.0);
        } else {
            return String.format(Locale.US, "%.1fM", coins / 1000000.0);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardContainer;
        TextView tvRank, tvUserName, tvUserSub, tvSpentCoins;
        ShapeableImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserSub = itemView.findViewById(R.id.tvUserSub);
            tvSpentCoins = itemView.findViewById(R.id.tvSpentCoins);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}

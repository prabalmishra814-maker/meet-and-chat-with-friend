package com.roomchatapps.Pmishra.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.RoomContributionModel;
import com.roomchatapps.Pmishra.utils.CoinUtils;

import java.util.ArrayList;
import java.util.List;

public class RoomContributionAdapter extends RecyclerView.Adapter<RoomContributionAdapter.ViewHolder> {

    private final List<RoomContributionModel> list = new ArrayList<>();

    public RoomContributionAdapter(List<RoomContributionModel> items) {
        if (items != null) {
            this.list.addAll(items);
        }
    }

    public void updateList(List<RoomContributionModel> newList) {
        this.list.clear();
        if (newList != null) {
            this.list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_contribution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomContributionModel item = list.get(position);
        int rank = position + 1;

        holder.tvRank.setText(String.valueOf(rank));
        if (rank == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Gold
        } else if (rank == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Silver
        } else if (rank == 3) {
            holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#A0FFFFFF"));
        }

        String name = item.getUserName();
        if (name == null || name.trim().isEmpty()) {
            name = "User " + (item.getUserId() != null ? item.getUserId() : "");
        }
        holder.tvName.setText(name);

        String avatar = item.getUserAvatar();
        if (avatar != null && !avatar.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatar)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.logo_placeholder);
        }

        // ROOM COIN FORMAT: Use CoinUtils compact notation
        holder.tvAmount.setText(CoinUtils.formatCompactCoins(item.getAmount()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvContributorRank);
            ivAvatar = itemView.findViewById(R.id.ivContributorAvatar);
            tvName = itemView.findViewById(R.id.tvContributorName);
            tvAmount = itemView.findViewById(R.id.tvContributorAmount);
        }
    }
}

package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
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
import com.roomchatapps.Pmishra.models.GuardRankModel;

import java.util.List;

public class GuardRankAdapter extends RecyclerView.Adapter<GuardRankAdapter.ViewHolder> {

    private final Context context;
    private final List<GuardRankModel> rankList;

    public GuardRankAdapter(Context context, List<GuardRankModel> rankList) {
        this.context = context;
        this.rankList = rankList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guard_rank_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuardRankModel item = rankList.get(position);

        holder.tvRank.setText(String.valueOf(item.getRank()));
        holder.tvUserName.setText(!TextUtils.isEmpty(item.getUserName()) ? item.getUserName() : "User");
        holder.tvGuardScore.setText(!TextUtils.isEmpty(item.getGuardScore()) ? item.getGuardScore() : "0");

        String avatarUrl = item.getAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl)) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.img_20260904_135725)
                    .error(R.drawable.img_20260904_135725)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.img_20260904_135725);
        }
    }

    @Override
    public int getItemCount() {
        return rankList != null ? rankList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        ShapeableImageView ivAvatar;
        TextView tvUserName;
        TextView tvGuardScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvGuardScore = itemView.findViewById(R.id.tvGuardScore);
        }
    }
}

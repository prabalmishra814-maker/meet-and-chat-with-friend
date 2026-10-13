package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.FamilyRankModel;

import java.util.ArrayList;
import java.util.List;

public class FamilyRankAdapter extends RecyclerView.Adapter<FamilyRankAdapter.ViewHolder> {

    public interface OnFamilyClickListener {
        void onFamilyClick(FamilyRankModel family, int position);
    }

    private final Context context;
    private final List<FamilyRankModel> familyList = new ArrayList<>();
    private OnFamilyClickListener listener;

    public FamilyRankAdapter(Context context, List<FamilyRankModel> familyList) {
        this.context = context;
        if (familyList != null) {
            this.familyList.addAll(familyList);
        }
    }

    public void setOnFamilyClickListener(OnFamilyClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<FamilyRankModel> newFamilies) {
        this.familyList.clear();
        if (newFamilies != null) {
            this.familyList.addAll(newFamilies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_family_rank_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FamilyRankModel item = familyList.get(position);

        int rank = item.getRank() > 0 ? item.getRank() : (position + 1);

        // 1. Set Card Metallic Background & Shield Badge based on Rank
        if (rank == 1) {
            holder.cardFamilyRankRoot.setBackgroundResource(R.drawable.bg_family_gold_card);
            holder.ivRankShield.setImageResource(R.drawable.ic_family_shield_gold);
            holder.ivRankShield.setVisibility(View.VISIBLE);
            holder.tvRankNumber.setText("1");
            holder.tvRankNumber.setTextColor(android.graphics.Color.parseColor("#3D2600"));
        } else if (rank == 2) {
            holder.cardFamilyRankRoot.setBackgroundResource(R.drawable.bg_family_silver_card);
            holder.ivRankShield.setImageResource(R.drawable.ic_family_shield_silver);
            holder.ivRankShield.setVisibility(View.VISIBLE);
            holder.tvRankNumber.setText("2");
            holder.tvRankNumber.setTextColor(android.graphics.Color.parseColor("#1F2937"));
        } else if (rank == 3) {
            holder.cardFamilyRankRoot.setBackgroundResource(R.drawable.bg_family_bronze_card);
            holder.ivRankShield.setImageResource(R.drawable.ic_family_shield_bronze);
            holder.ivRankShield.setVisibility(View.VISIBLE);
            holder.tvRankNumber.setText("3");
            holder.tvRankNumber.setTextColor(android.graphics.Color.parseColor("#2E1C00"));
        } else {
            holder.cardFamilyRankRoot.setBackgroundResource(R.drawable.bg_family_normal_card);
            holder.ivRankShield.setVisibility(View.GONE);
            holder.tvRankNumber.setText(String.format(java.util.Locale.getDefault(), "%02d", rank));
            holder.tvRankNumber.setTextColor(android.graphics.Color.WHITE);
        }

        // 2. Family Name & Logo & Name Badge
        String name = !TextUtils.isEmpty(item.getFamilyName()) ? item.getFamilyName() : "Family";
        holder.tvFamilyName.setText(name);
        if (holder.ivFamilyNameBadge != null) {
            holder.ivFamilyNameBadge.setImageResource(R.drawable.family_name_badges);
            holder.ivFamilyNameBadge.setVisibility(View.VISIBLE);
        }

        String logoUrl = item.getFamilyLogo();
        if (!TextUtils.isEmpty(logoUrl)) {
            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .into(holder.ivFamilyLogo);
        } else {
            holder.ivFamilyLogo.setImageResource(R.drawable.logo_placeholder);
        }

        // 3. Crown Score
        holder.tvCrownScore.setText(String.valueOf(item.getScore()));

        // 4. Top Member Avatars Overlapping Row
        List<String> topAvatars = item.getTopMemberAvatars();
        bindMemberAvatar(holder.ivMemberAvatar1, topAvatars, 0);
        bindMemberAvatar(holder.ivMemberAvatar2, topAvatars, 1);
        bindMemberAvatar(holder.ivMemberAvatar3, topAvatars, 2);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFamilyClick(item, position);
            }
        });
    }

    private void bindMemberAvatar(ShapeableImageView avatarView, List<String> avatars, int index) {
        if (avatarView == null) return;
        if (avatars != null && index < avatars.size() && !TextUtils.isEmpty(avatars.get(index))) {
            avatarView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(avatars.get(index))
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .circleCrop()
                    .into(avatarView);
        } else {
            avatarView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return familyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardFamilyRankRoot;
        ImageView ivRankShield;
        TextView tvRankNumber;
        ShapeableImageView ivFamilyLogo;
        TextView tvFamilyName;
        ImageView ivFamilyNameBadge;
        ShapeableImageView ivMemberAvatar1, ivMemberAvatar2, ivMemberAvatar3;
        TextView tvCrownScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFamilyRankRoot = itemView.findViewById(R.id.cardFamilyRankRoot);
            ivRankShield = itemView.findViewById(R.id.ivRankShield);
            tvRankNumber = itemView.findViewById(R.id.tvRankNumber);
            ivFamilyLogo = itemView.findViewById(R.id.ivFamilyLogo);
            tvFamilyName = itemView.findViewById(R.id.tvFamilyName);
            ivFamilyNameBadge = itemView.findViewById(R.id.ivFamilyNameBadge);
            ivMemberAvatar1 = itemView.findViewById(R.id.ivMemberAvatar1);
            ivMemberAvatar2 = itemView.findViewById(R.id.ivMemberAvatar2);
            ivMemberAvatar3 = itemView.findViewById(R.id.ivMemberAvatar3);
            tvCrownScore = itemView.findViewById(R.id.tvCrownScore);
        }
    }
}

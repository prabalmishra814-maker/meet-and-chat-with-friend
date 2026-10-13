package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.OtherCollectionActivity;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.SupporterModel;

import java.util.List;

public class SupporterAdapter extends RecyclerView.Adapter<SupporterAdapter.ViewHolder> {

    public interface OnSupporterClickListener {
        void onSupporterClick(SupporterModel supporter, int position);
    }

    private final Context context;
    private final List<SupporterModel> supporterList;
    private OnSupporterClickListener listener;

    public SupporterAdapter(Context context, List<SupporterModel> supporterList) {
        this.context = context;
        this.supporterList = supporterList;
    }

    public void setOnSupporterClickListener(OnSupporterClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_supporter_avatar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupporterModel item = supporterList.get(position);

        String name = !TextUtils.isEmpty(item.getUserName()) ? item.getUserName() : "Supporter";
        holder.tvSupporterName.setText(name);

        int rank = item.getRank() > 0 ? item.getRank() : (position + 1);
        holder.tvSupporterRankBadge.setText(String.valueOf(rank));

        if (rank == 1) {
            holder.tvSupporterRankBadge.setBackgroundResource(R.drawable.bg_level_badge_gold);
        } else if (rank == 2) {
            holder.tvSupporterRankBadge.setBackgroundResource(R.drawable.bg_level_badge_cyan);
        } else if (rank == 3) {
            holder.tvSupporterRankBadge.setBackgroundResource(R.drawable.bg_level_badge_pink);
        } else {
            holder.tvSupporterRankBadge.setBackgroundResource(R.drawable.bg_level_badge_purple);
        }

        String avatarUrl = item.getAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl)) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .circleCrop()
                    .into(holder.ivSupporterAvatar);
        } else {
            holder.ivSupporterAvatar.setImageResource(R.drawable.logo_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSupporterClick(item, position);
            } else if (!TextUtils.isEmpty(item.getUserId())) {
                Intent intent = new Intent(context, OtherCollectionActivity.class);
                intent.putExtra("selectedUserId", item.getUserId());
                intent.putExtra("selectedUserName", item.getUserName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return supporterList != null ? supporterList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivSupporterAvatar;
        TextView tvSupporterRankBadge;
        TextView tvSupporterName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSupporterAvatar = itemView.findViewById(R.id.ivSupporterAvatar);
            tvSupporterRankBadge = itemView.findViewById(R.id.tvSupporterRankBadge);
            tvSupporterName = itemView.findViewById(R.id.tvSupporterName);
        }
    }
}

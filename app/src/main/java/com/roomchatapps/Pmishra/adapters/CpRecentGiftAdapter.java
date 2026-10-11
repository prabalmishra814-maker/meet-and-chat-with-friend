package com.roomchatapps.Pmishra.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CpRecentGiftModel;

import java.util.List;

public class CpRecentGiftAdapter extends RecyclerView.Adapter<CpRecentGiftAdapter.ViewHolder> {

    private List<CpRecentGiftModel> giftList;

    public CpRecentGiftAdapter(List<CpRecentGiftModel> giftList) {
        this.giftList = giftList;
    }

    public void updateList(List<CpRecentGiftModel> newList) {
        this.giftList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cp_recent_gift, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CpRecentGiftModel gift = giftList.get(position);

        if (holder.tvGiftName != null) {
            holder.tvGiftName.setText(!TextUtils.isEmpty(gift.getGiftName()) ? gift.getGiftName() : "Gift");
        }

        if (holder.tvSenderInfo != null) {
            String sender = gift.getSenderName();
            if (TextUtils.isEmpty(sender)) {
                sender = "Partner";
            }
            holder.tvSenderInfo.setText("From: " + sender);
        }

        if (holder.tvGiftPoints != null) {
            long cost = gift.getGiftCost();
            holder.tvGiftPoints.setText("💖 +" + Math.max(1, cost));
        }

        if (holder.ivGiftIcon != null) {
            int iconRes = gift.getIconRes();
            if (iconRes != 0) {
                holder.ivGiftIcon.setImageResource(iconRes);
            } else {
                holder.ivGiftIcon.setImageResource(R.drawable.gift_icon);
            }
        }
    }

    @Override
    public int getItemCount() {
        return giftList != null ? giftList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGiftIcon;
        TextView tvGiftName;
        TextView tvSenderInfo;
        TextView tvGiftPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGiftIcon = itemView.findViewById(R.id.ivGiftIcon);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvSenderInfo = itemView.findViewById(R.id.tvSenderInfo);
            tvGiftPoints = itemView.findViewById(R.id.tvGiftPoints);
        }
    }
}

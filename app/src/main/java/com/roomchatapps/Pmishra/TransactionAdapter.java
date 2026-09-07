package com.roomchatapps.Pmishra;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.models.TransactionModel;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<TransactionModel> transactionList;
    private int lastAnimatedPosition = -1;

    public TransactionAdapter(List<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionModel item = transactionList.get(position);

        holder.tvTxTitle.setText(item.getTitle() != null ? item.getTitle() : "Transaction");
        holder.tvTxDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.tvTxTime.setText(formatTimeAgo(item.getTimestamp()));

        String type = item.getType() != null ? item.getType().toUpperCase() : "TOPUP";

        switch (type) {
            case "TOPUP":
                holder.ivTxIcon.setImageResource(R.drawable.profile_entrance_wallet_img);
                holder.tvTxAmount.setText("+" + item.getCoinAmount() + " Coins");
                holder.tvTxAmount.setTextColor(Color.parseColor("#00FF7F")); // Bright green
                break;
            case "GIFT_SENT":
                holder.ivTxIcon.setImageResource(R.drawable.room_gift_ic);
                holder.tvTxAmount.setText(item.getCoinAmount() + " Coins");
                holder.tvTxAmount.setTextColor(Color.parseColor("#FF4500")); // Coral red
                break;
            case "GIFT_RECEIVED":
                holder.ivTxIcon.setImageResource(R.drawable.game_mic_charm_pk_diamond_ic);
                holder.tvTxAmount.setText("+" + item.getDiamondAmount() + " Diamonds");
                holder.tvTxAmount.setTextColor(Color.parseColor("#FFD700")); // Gold
                break;
            default:
                holder.ivTxIcon.setImageResource(R.drawable.profile_entrance_wallet_img);
                holder.tvTxAmount.setText("+" + item.getCoinAmount() + " Coins");
                holder.tvTxAmount.setTextColor(Color.parseColor("#40E0D0")); // Cyan
                break;
        }

        setEntranceAnimation(holder.itemView, position);
    }

    private void setEntranceAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.setTranslationX(100f);
            viewToAnimate.setAlpha(0f);

            viewToAnimate.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(350)
                    .setStartDelay(Math.min(position * 40L, 250L))
                    .setInterpolator(new DecelerateInterpolator(1.4f))
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
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivTxIcon;
        final TextView tvTxTitle;
        final TextView tvTxDescription;
        final TextView tvTxTime;
        final TextView tvTxAmount;

        ViewHolder(View itemView) {
            super(itemView);
            ivTxIcon = itemView.findViewById(R.id.ivTxIcon);
            tvTxTitle = itemView.findViewById(R.id.tvTxTitle);
            tvTxDescription = itemView.findViewById(R.id.tvTxDescription);
            tvTxTime = itemView.findViewById(R.id.tvTxTime);
            tvTxAmount = itemView.findViewById(R.id.tvTxAmount);
        }
    }
}

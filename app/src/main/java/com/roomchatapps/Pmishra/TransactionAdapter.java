package com.roomchatapps.Pmishra;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.models.TransactionModel;

import java.text.NumberFormat;
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

    public void resetAnimationState() {
        lastAnimatedPosition = -1;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= transactionList.size()) return;
        TransactionModel item = transactionList.get(position);
        if (item == null) return;

        holder.tvTxTitle.setText(item.getTitle() != null ? item.getTitle() : "Transaction");
        holder.tvTxDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.tvTxTime.setText(formatTimeAgo(item.getTimestamp()));

        String type = item.getType() != null ? item.getType().toUpperCase() : "TOPUP";
        NumberFormat formatter = NumberFormat.getInstance();

        switch (type) {
            case "TOPUP":
            case "WELCOME_BONUS":
            case "REFERRAL":
                holder.tvTxAmount.setText("+" + formatter.format(item.getCoinAmount()) + " Coins");
                holder.tvTxAmount.setTextColor(Color.parseColor("#00FF7F")); // Bright green
                break;

            case "GIFT_SENT":
                long cost = Math.abs(item.getCoinAmount());
                holder.tvTxAmount.setText("-" + formatter.format(cost) + " Coins");
                holder.tvTxAmount.setTextColor(Color.parseColor("#FF4D4D")); // Red-Orange
                break;

            case "GIFT_RECEIVED":
                long recVal = item.getDiamondAmount() > 0 ? item.getDiamondAmount() : Math.abs(item.getCoinAmount());
                if (recVal > 0) {
                    String unit = item.getDiamondAmount() > 0 ? " Diamonds" : " Coins";
                    holder.tvTxAmount.setText("+" + formatter.format(recVal) + unit);
                    holder.tvTxAmount.setTextColor(Color.parseColor("#00FF7F")); // Bright green
                } else {
                    holder.tvTxAmount.setText("+0 Coins");
                    holder.tvTxAmount.setTextColor(Color.parseColor("#00FF7F"));
                }
                break;

            case "STORE_BUY":
            case "THEME_BUY":
                long storeCost = Math.abs(item.getCoinAmount());
                holder.tvTxAmount.setText("-" + formatter.format(storeCost) + " Coins");
                holder.tvTxAmount.setTextColor(Color.parseColor("#FF4D4D")); // Red-Orange
                break;

            case "GAME_SPIN":
                if (item.getCoinAmount() >= 0) {
                    holder.tvTxAmount.setText("+" + formatter.format(item.getCoinAmount()) + " Coins");
                    holder.tvTxAmount.setTextColor(Color.parseColor("#00FF7F"));
                } else {
                    holder.tvTxAmount.setText("-" + formatter.format(Math.abs(item.getCoinAmount())) + " Coins");
                    holder.tvTxAmount.setTextColor(Color.parseColor("#FF4D4D"));
                }
                break;

            default:
                if (item.getCoinAmount() >= 0) {
                    holder.tvTxAmount.setText("+" + formatter.format(item.getCoinAmount()) + " Coins");
                    holder.tvTxAmount.setTextColor(Color.parseColor("#00FF7F"));
                } else {
                    holder.tvTxAmount.setText("-" + formatter.format(Math.abs(item.getCoinAmount())) + " Coins");
                    holder.tvTxAmount.setTextColor(Color.parseColor("#FF4D4D"));
                }
                break;
        }

        setEntranceAnimation(holder.itemView, position);
    }

    private void setEntranceAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition && position < 15) {
            viewToAnimate.setTranslationX(60f);
            viewToAnimate.setAlpha(0f);

            viewToAnimate.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(250)
                    .setStartDelay(Math.min(position * 30L, 150L))
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
        final TextView tvTxTitle;
        final TextView tvTxDescription;
        final TextView tvTxTime;
        final TextView tvTxAmount;

        ViewHolder(View itemView) {
            super(itemView);
            tvTxTitle = itemView.findViewById(R.id.tvTxTitle);
            tvTxDescription = itemView.findViewById(R.id.tvTxDescription);
            tvTxTime = itemView.findViewById(R.id.tvTxTime);
            tvTxAmount = itemView.findViewById(R.id.tvTxAmount);
        }
    }
}

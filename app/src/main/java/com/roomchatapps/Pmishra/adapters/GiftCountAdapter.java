package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.GiftCountModel;

import java.util.List;

public class GiftCountAdapter extends RecyclerView.Adapter<GiftCountAdapter.ViewHolder> {

    private final List<GiftCountModel> giftList;

    public GiftCountAdapter(List<GiftCountModel> giftList) {
        this.giftList = giftList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_count, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int adapterPos = holder.getBindingAdapterPosition();
        int currentPos = (adapterPos != RecyclerView.NO_POSITION) ? adapterPos : position;
        GiftCountModel item = giftList.get(currentPos);

        holder.tvGiftName.setText(item.getGiftName());
        if (item.getCostCoins() == 0) {
            holder.tvGiftPrice.setText("FREE");
        } else {
            holder.tvGiftPrice.setText(item.getCostCoins() + " 🪙");
        }

        if (item.getIconRes() != 0) {
            holder.ivGiftIcon.setImageResource(item.getIconRes());
        }

        holder.tvReceivedCount.setText("📥 " + item.getReceivedCount());
        holder.tvSentCount.setText("📤 " + item.getSentCount());
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivGiftIcon;
        final TextView tvGiftName;
        final TextView tvGiftPrice;
        final TextView tvReceivedCount;
        final TextView tvSentCount;

        ViewHolder(View itemView) {
            super(itemView);
            ivGiftIcon = itemView.findViewById(R.id.ivGiftIcon);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvGiftPrice = itemView.findViewById(R.id.tvGiftPrice);
            tvReceivedCount = itemView.findViewById(R.id.tvReceivedCount);
            tvSentCount = itemView.findViewById(R.id.tvSentCount);
        }
    }
}

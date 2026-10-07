package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CollectionItemModel;

import java.util.List;

public class GiftGridAdapter extends RecyclerView.Adapter<GiftGridAdapter.ViewHolder> {

    private final List<CollectionItemModel> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CollectionItemModel item);
    }

    public GiftGridAdapter(List<CollectionItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_gift_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionItemModel item = items.get(position);

        if (item.getGiftStoreItem() != null) {
            holder.ivGiftImage.setImageResource(item.getGiftStoreItem().iconRes);
        } else {
            int res = item.getGiftIconRes();
            if (res == 0) res = R.drawable.gift_icon;
            holder.ivGiftImage.setImageResource(res);
        }

        holder.tvGiftPrice.setText(String.valueOf(item.getItemPrice()));
        holder.tvGiftQuantity.setText("x" + item.getReceivedCount());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGiftImage;
        TextView tvGiftPrice, tvGiftQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGiftImage = itemView.findViewById(R.id.ivGiftImage);
            tvGiftPrice = itemView.findViewById(R.id.tvGiftPrice);
            tvGiftQuantity = itemView.findViewById(R.id.tvGiftQuantity);
        }
    }
}

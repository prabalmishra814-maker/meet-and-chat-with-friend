package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.FrameUtils;

import java.util.List;

public class HorizontalCollectionAdapter extends RecyclerView.Adapter<HorizontalCollectionAdapter.ViewHolder> {

    private final List<CollectionItemModel> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CollectionItemModel item);
    }

    public HorizontalCollectionAdapter(List<CollectionItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionItemModel item = items.get(position);

        if (item.getItemType() == CollectionItemModel.ItemType.PHOTO) {
            if (item.isAddButton()) {
                holder.ivImage.setImageResource(R.drawable.add_button);
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText("+ Add");
            } else if (item.getPhotoUrl() != null && !item.getPhotoUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getPhotoUrl())
                        .placeholder(R.drawable.logo_placeholder)
                        .error(R.drawable.logo_placeholder)
                        .into(holder.ivImage);

                if (item.isCurrentAvatar()) {
                    holder.tvBadge.setVisibility(View.VISIBLE);
                    holder.tvBadge.setText("Active");
                } else {
                    holder.tvBadge.setVisibility(View.GONE);
                }
            } else {
                holder.ivImage.setImageResource(R.drawable.logo_placeholder);
                holder.tvBadge.setVisibility(View.GONE);
            }
        } else {
            int resId = 0;
            StoreItemModel storeItem = item.getStoreItem();

            if (storeItem != null) {
                if (storeItem.getIconResName() != null && !storeItem.getIconResName().trim().isEmpty()) {
                    resId = holder.itemView.getResources().getIdentifier(storeItem.getIconResName(), "drawable", holder.itemView.getContext().getPackageName());
                }
                if (resId == 0) {
                    resId = FrameUtils.getFrameDrawableRes(holder.itemView.getContext(), storeItem.getId());
                }
            } else if (item.getGiftStoreItem() != null) {
                resId = item.getGiftStoreItem().iconRes;
            } else {
                resId = item.getGiftIconRes();
            }

            if (resId == 0) resId = R.drawable.logo_placeholder;
            holder.ivImage.setImageResource(resId);

            if (item.isEquipped()) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText("Equipped");
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivHorizontalImage);
            tvBadge = itemView.findViewById(R.id.tvHorizontalBadge);
        }
    }
}

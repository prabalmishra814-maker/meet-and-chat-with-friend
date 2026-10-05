package com.roomchatapps.Pmishra;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.opensource.svgaplayer.SVGAImageView;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.CoinUtils;
import com.roomchatapps.Pmishra.utils.FrameUtils;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    public interface OnStoreItemClickListener {
        void onItemAction(StoreItemModel item, int position);
        void onItemClick(StoreItemModel item, int position);
        void onItemSend(StoreItemModel item, int position);
    }

    private final List<StoreItemModel> itemList;
    private final OnStoreItemClickListener listener;
    private int lastAnimatedPosition = -1;

    public StoreAdapter(List<StoreItemModel> itemList, OnStoreItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreItemModel item = itemList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvItemName.setText(item.getName() != null ? item.getName() : "Item");
        if (holder.tvItemDescription != null) {
            holder.tvItemDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        }
        if (holder.tvBadge != null) {
            holder.tvBadge.setText(item.getBadgeText() != null ? item.getBadgeText() : "FEATURED");
        }

        // Format Price & Duration e.g. 20000000 /7 days
        if (holder.tvItemPriceDuration != null) {
            int validity = item.getValidityDays() > 0 ? item.getValidityDays() : 7;
            holder.tvItemPriceDuration.setText(CoinUtils.formatCoins(item.getPriceCoins()) + " /" + validity + " days");
        }

        boolean isEntrance = "ENTRANCE".equalsIgnoreCase(item.getCategory());

        // Display static matching picture thumbnail in Store grid
        if (holder.svgaItemIcon != null) holder.svgaItemIcon.setVisibility(View.GONE);
        if (holder.ivItemIcon != null) {
            holder.ivItemIcon.setVisibility(View.VISIBLE);
            int resId = resolveDrawableRes(context, item.getIconResName());
            if (resId == 0) {
                resId = FrameUtils.getFrameDrawableRes(context, item.getId());
            }
            if (resId == 0) {
                resId = isEntrance ? R.drawable.store : R.drawable.ic_crown_gold_frame;
            }
            holder.ivItemIcon.setImageResource(resId);
        }

        // Configure main action button state (Buy / Equip / Unequip)
        if (holder.btnAction != null) {
            if (item.isEquipped()) {
                holder.btnAction.setText("Unequip");
                holder.btnAction.setBackgroundColor(Color.parseColor("#FF6B6B")); // Coral Red
                holder.btnAction.setTextColor(Color.parseColor("#FFFFFF"));
            } else if (item.isOwned()) {
                holder.btnAction.setText("Equip");
                holder.btnAction.setBackgroundColor(Color.parseColor("#40E0D0")); // Cyan
                holder.btnAction.setTextColor(Color.parseColor("#050E1E"));
            } else {
                holder.btnAction.setText("Buy");
                holder.btnAction.setBackgroundColor(Color.parseColor("#26E699")); // Emerald Green Gradient style
                holder.btnAction.setTextColor(Color.parseColor("#050E1E"));
            }

            holder.btnAction.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                if (listener != null) {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemAction(item, pos);
                    }
                }
            });
        }

        // Configure Send button
        if (holder.btnSend != null) {
            holder.btnSend.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                if (listener != null) {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemSend(item, pos);
                    }
                }
            });
        }

        // Configure Play Preview button & Item View click
        View.OnClickListener previewClickListener = v -> {
            AnimationHelper.bounceAnimation(v);
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(item, pos);
                }
            }
        };

        if (holder.ivPlayPreview != null) {
            holder.ivPlayPreview.setOnClickListener(previewClickListener);
        }
        holder.itemView.setOnClickListener(previewClickListener);

        setEntranceAnimation(holder.itemView, position);
    }

    private int resolveDrawableRes(Context context, String resName) {
        if (resName == null || resName.isEmpty()) return 0;
        try {
            return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    private void setEntranceAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.setTranslationY(80f);
            viewToAnimate.setAlpha(0f);
            viewToAnimate.setScaleX(0.9f);
            viewToAnimate.setScaleY(0.9f);

            viewToAnimate.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(400)
                    .setStartDelay(Math.min(position * 50L, 300L))
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();

            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivItemIcon;
        final SVGAImageView svgaItemIcon;
        final ImageView ivPlayPreview;
        final TextView tvItemName;
        final TextView tvItemDescription;
        final TextView tvItemPriceDuration;
        final TextView tvBadge;
        final MaterialButton btnAction;
        final MaterialButton btnSend;

        ViewHolder(View itemView) {
            super(itemView);
            ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
            svgaItemIcon = itemView.findViewById(R.id.svgaItemIcon);
            ivPlayPreview = itemView.findViewById(R.id.ivPlayPreview);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemPriceDuration = itemView.findViewById(R.id.tvItemPriceDuration);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnSend = itemView.findViewById(R.id.btnSend);
        }
    }
}

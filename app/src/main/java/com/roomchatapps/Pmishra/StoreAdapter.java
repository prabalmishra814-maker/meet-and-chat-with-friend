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

    private final android.os.Handler timerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            timerHandler.postDelayed(this, 1000L);
        }
    };

    public StoreAdapter(List<StoreItemModel> itemList, OnStoreItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000L);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        timerHandler.removeCallbacks(timerRunnable);
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

        // Format Price & Reverse Live Countdown Timer
        long now = System.currentTimeMillis();
        long exp = item.getExpiryTimestamp();

        if (item.isOwned() && exp > now) {
            long diff = exp - now;
            long days = diff / (86400000L);
            long hours = (diff / 3600000L) % 24;
            long mins = (diff / 60000L) % 60;
            long secs = (diff / 1000L) % 60;

            String timerStr;
            if (days > 0) {
                timerStr = String.format(java.util.Locale.US, "⏱️ %dd %02dh %02dm %02ds", days, hours, mins, secs);
            } else {
                timerStr = String.format(java.util.Locale.US, "⏱️ %02dh %02dm %02ds", hours, mins, secs);
            }
            if (holder.tvItemPriceDuration != null) {
                holder.tvItemPriceDuration.setText(timerStr);
                holder.tvItemPriceDuration.setTextColor(Color.parseColor("#40E0D0")); // Cyan accent for ticking timer
            }
        } else if (item.isOwned() && exp <= now && exp > 0) {
            // Expired right now -> Reset state
            item.setOwned(false);
            item.setEquipped(false);
            int validity = item.getValidityDays() > 0 ? item.getValidityDays() : 7;
            if (holder.tvItemPriceDuration != null) {
                holder.tvItemPriceDuration.setText(CoinUtils.formatCoins(item.getPriceCoins()) + " /" + validity + " days");
                holder.tvItemPriceDuration.setTextColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            int validity = item.getValidityDays() > 0 ? item.getValidityDays() : 7;
            if (holder.tvItemPriceDuration != null) {
                holder.tvItemPriceDuration.setText(CoinUtils.formatCoins(item.getPriceCoins()) + " /" + validity + " days");
                holder.tvItemPriceDuration.setTextColor(Color.parseColor("#FFFFFF"));
            }
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

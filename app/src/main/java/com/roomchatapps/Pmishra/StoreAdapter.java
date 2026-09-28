package com.roomchatapps.Pmishra;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.models.StoreItemModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    public interface OnStoreItemClickListener {
        void onItemAction(StoreItemModel item, int position);
        void onItemClick(StoreItemModel item, int position);
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
        holder.tvItemDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.tvBadge.setText(item.getBadgeText() != null ? item.getBadgeText() : "FEATURED");

        boolean isEntrance = "ENTRANCE".equalsIgnoreCase(item.getCategory());

        // For ENTRANCE items: Show static icon in Store grid; play SVGA on click preview!
        if (isEntrance) {
            if (holder.svgaItemIcon != null) holder.svgaItemIcon.setVisibility(View.GONE);
            holder.ivItemIcon.setVisibility(View.VISIBLE);
            int resId = resolveDrawableRes(context, item.getIconResName());
            if (resId == 0) resId = R.drawable.store;
            holder.ivItemIcon.setImageResource(resId);
        } else if (item.getSvgaPath() != null && !item.getSvgaPath().isEmpty()) {
            holder.ivItemIcon.setVisibility(View.GONE);
            if (holder.svgaItemIcon != null) {
                holder.svgaItemIcon.setVisibility(View.VISIBLE);
                holder.svgaItemIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                SVGAParser parser = new SVGAParser(context);
                parser.decodeFromAssets(item.getSvgaPath(), new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (holder.svgaItemIcon != null) {
                                holder.svgaItemIcon.setVideoItem(videoItem);
                                holder.svgaItemIcon.setLoops(0);
                                holder.svgaItemIcon.stepToFrame(0, true);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (holder.svgaItemIcon != null) holder.svgaItemIcon.setVisibility(View.GONE);
                            holder.ivItemIcon.setVisibility(View.VISIBLE);
                            int resId = resolveDrawableRes(context, item.getIconResName());
                            if (resId == 0) resId = R.drawable.family_owner_frame;
                            holder.ivItemIcon.setImageResource(resId);
                        });
                    }
                }, null);
            }
        } else {
            if (holder.svgaItemIcon != null) holder.svgaItemIcon.setVisibility(View.GONE);
            holder.ivItemIcon.setVisibility(View.VISIBLE);
            int resId = resolveDrawableRes(context, item.getIconResName());
            if (resId == 0) resId = R.drawable.family_owner_frame;
            holder.ivItemIcon.setImageResource(resId);
            AnimationHelper.pulseGlowAnimation(holder.ivItemIcon);
        }

        // Configure button state & appearance
        if (item.isEquipped()) {
            holder.btnAction.setText("Unequip");
            holder.btnAction.setBackgroundColor(Color.parseColor("#FF6B6B")); // Coral Red
            holder.btnAction.setTextColor(Color.parseColor("#FFFFFF"));
        } else if (item.isOwned()) {
            holder.btnAction.setText("Equip");
            holder.btnAction.setBackgroundColor(Color.parseColor("#40E0D0")); // Cyan
            holder.btnAction.setTextColor(Color.parseColor("#050E1E"));
        } else {
            holder.btnAction.setText(com.roomchatapps.Pmishra.utils.CoinUtils.formatCoins(item.getPriceCoins()) + " 🪙");
            holder.btnAction.setBackgroundColor(Color.parseColor("#FFD700")); // Gold
            holder.btnAction.setTextColor(Color.parseColor("#050E1E"));
        }

        holder.itemView.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(v);
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(item, pos);
                }
            }
        });

        holder.btnAction.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(v);
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemAction(item, pos);
                }
            }
        });

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
        final TextView tvItemName;
        final TextView tvItemDescription;
        final TextView tvBadge;
        final MaterialButton btnAction;

        ViewHolder(View itemView) {
            super(itemView);
            ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
            svgaItemIcon = itemView.findViewById(R.id.svgaItemIcon);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}

package com.roomchatapps.Pmishra.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.utils.CoinUtils;

import java.util.ArrayList;
import java.util.List;

public class GiftStoreAdapter extends RecyclerView.Adapter<GiftStoreAdapter.GiftViewHolder> {

    public static class GiftStoreItem {
        public String name;
        public String svgaPath;
        public int iconRes;
        public long cost;
        public String category;

        public GiftStoreItem(String name, String svgaPath, int iconRes, long cost, String category) {
            this.name = name;
            this.svgaPath = svgaPath;
            this.iconRes = iconRes;
            this.cost = cost;
            this.category = category != null ? category : "Gift";
        }

        public GiftStoreItem(String name, String svgaPath, int iconRes, long cost) {
            this(name, svgaPath, iconRes, cost, "Gift");
        }
    }

    private final List<GiftStoreItem> items;
    private int selectedIndex = 0;

    public interface OnGiftSelectedListener {
        void onGiftSelected(GiftStoreItem item, int position, boolean isReSelected);
    }

    private OnGiftSelectedListener listener;

    public void setOnGiftSelectedListener(OnGiftSelectedListener listener) {
        this.listener = listener;
    }

    public GiftStoreAdapter(List<GiftStoreItem> items) {
        this.items = new ArrayList<>();
        if (items != null) {
            this.items.addAll(items);
        }
    }

    public void updateItems(List<GiftStoreItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.selectedIndex = 0;
        notifyDataSetChanged();
    }

    public GiftStoreItem getSelectedGift() {
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            return items.get(selectedIndex);
        }
        return null;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_store_card, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftStoreItem item = items.get(position);
        boolean isSelected = (position == selectedIndex);

        holder.tvGiftName.setText(item.name);
        holder.tvGiftCost.setText(com.roomchatapps.Pmishra.utils.CoinUtils.getCoinSpannable(holder.itemView.getContext(), item.cost));
        holder.imgGiftStatic.setImageResource(item.iconRes);
        holder.imgGiftStatic.setVisibility(View.VISIBLE);

        if (isSelected) {
            holder.cardGiftItem.setStrokeColor(Color.parseColor("#FF007A"));
            holder.cardGiftItem.setStrokeWidth(dp2px(holder.itemView, 2));
            holder.cardGiftItem.setCardBackgroundColor(Color.parseColor("#33FF007A"));
            startContinuousPulsing(holder.imgGiftStatic);
        } else {
            holder.cardGiftItem.setStrokeColor(Color.parseColor("#25FFFFFF"));
            holder.cardGiftItem.setStrokeWidth(dp2px(holder.itemView, 1));
            holder.cardGiftItem.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"));
            stopPulsing(holder.imgGiftStatic);
        }

        holder.cardGiftItem.setOnClickListener(v -> {
            int previousSelected = selectedIndex;
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                pos = holder.getAdapterPosition();
            }

            boolean isReSelected = (pos == previousSelected);

            selectedIndex = pos;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedIndex);

            if (listener != null && pos >= 0 && pos < items.size()) {
                listener.onGiftSelected(items.get(pos), pos, isReSelected);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull GiftViewHolder holder) {
        super.onViewRecycled(holder);
        stopPulsing(holder.imgGiftStatic);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void startContinuousPulsing(View view) {
        if (view == null) return;
        stopPulsing(view);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.25f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.25f);

        scaleX.setDuration(500);
        scaleY.setDuration(500);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        view.setTag(R.id.cardGiftItem, set);
        set.start();
    }

    private void stopPulsing(View view) {
        if (view == null) return;
        Object tag = view.getTag(R.id.cardGiftItem);
        if (tag instanceof AnimatorSet) {
            ((AnimatorSet) tag).cancel();
            view.setTag(R.id.cardGiftItem, null);
        }
        view.animate().cancel();
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
    }

    private int dp2px(View view, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics());
    }

    static class GiftViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardGiftItem;
        ImageView imgGiftStatic;
        TextView tvGiftName, tvGiftCost;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            cardGiftItem = itemView.findViewById(R.id.cardGiftItem);
            imgGiftStatic = itemView.findViewById(R.id.imgGiftStatic);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvGiftCost = itemView.findViewById(R.id.tvGiftCost);
        }
    }
}

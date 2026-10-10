package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.RingModel;

import java.util.List;

public class RingAdapter extends RecyclerView.Adapter<RingAdapter.ViewHolder> {

    public interface OnRingClickListener {
        void onRingClick(RingModel ring, int position);
        void onPlayPreviewClick(RingModel ring, int position);
    }

    private List<RingModel> ringList;
    private OnRingClickListener listener;
    private int selectedPosition = 0;

    public RingAdapter(List<RingModel> ringList, OnRingClickListener listener) {
        this.ringList = ringList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ring_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RingModel item = ringList.get(position);
        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvPrice.setText(String.valueOf(item.getPriceCoins()));

        if (position == selectedPosition) {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_ring_card_selected);
        } else {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_ring_card_normal);
        }

        holder.cardContainer.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onRingClick(item, selectedPosition);
            }
        });

        holder.btnPlayPreview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayPreviewClick(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return ringList != null ? ringList.size() : 0;
    }

    public RingModel getSelectedRing() {
        if (ringList != null && selectedPosition >= 0 && selectedPosition < ringList.size()) {
            return ringList.get(selectedPosition);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardContainer;
        ImageView ivIcon;
        ImageView btnPlayPreview;
        TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.cardRingContainer);
            ivIcon = itemView.findViewById(R.id.ivRingIcon);
            btnPlayPreview = itemView.findViewById(R.id.btnPlayPreview);
            tvPrice = itemView.findViewById(R.id.tvRingPrice);
        }
    }
}

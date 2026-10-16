package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CpLevelRewardModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CpRewardAdapter extends RecyclerView.Adapter<CpRewardAdapter.ViewHolder> {

    public interface OnRewardClickListener {
        void onRewardClick(CpLevelRewardModel reward);
    }

    private final Context context;
    private final List<CpLevelRewardModel> rewardList = new ArrayList<>();
    private final OnRewardClickListener listener;

    public CpRewardAdapter(Context context, List<CpLevelRewardModel> rewards, OnRewardClickListener listener) {
        this.context = context;
        if (rewards != null) {
            this.rewardList.addAll(rewards);
        }
        this.listener = listener;
    }

    public void updateList(List<CpLevelRewardModel> newRewards) {
        this.rewardList.clear();
        if (newRewards != null) {
            this.rewardList.addAll(newRewards);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cp_reward_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CpLevelRewardModel item = rewardList.get(position);

        holder.tvRewardTitle.setText(item.getTitle());

        if (item.isEquipped()) {
            holder.tvRewardBadge.setText("EQUIPPED ✨");
            holder.tvRewardBadge.setBackgroundResource(R.drawable.chip_room_bg);
            holder.cardReward.setStrokeColor(Color.parseColor("#40E0D0"));
        } else if (item.isUnlocked()) {
            holder.tvRewardBadge.setText("UNLOCKED ✨");
            holder.tvRewardBadge.setBackgroundResource(R.drawable.bg_level_badge_cyan);
            holder.cardReward.setStrokeColor(Color.parseColor("#50FF69B4"));
        } else {
            holder.tvRewardBadge.setText("LV." + item.getUnlockLevel());
            holder.tvRewardBadge.setBackgroundColor(Color.parseColor("#80000000"));
            holder.cardReward.setStrokeColor(Color.parseColor("#30FFFFFF"));
        }

        // Display SVGA preview on card
        if (item.getSvgaPath() != null && !item.getSvgaPath().isEmpty()) {
            holder.svgaRewardPreview.setVisibility(View.VISIBLE);
            holder.ivRewardIcon.setVisibility(View.GONE);
            holder.svgaRewardPreview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            SVGAParser parser = new SVGAParser(context);
            parser.decodeFromAssets(item.getSvgaPath(), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    if (holder.itemView.getContext() != null && holder.svgaRewardPreview != null) {
                        holder.svgaRewardPreview.setVideoItem(videoItem);
                        holder.svgaRewardPreview.setLoops(0);
                        holder.svgaRewardPreview.stepToFrame(0, true);
                    }
                }

                @Override
                public void onError() {
                    if (holder.svgaRewardPreview != null) {
                        holder.svgaRewardPreview.setVisibility(View.GONE);
                    }
                    if (holder.ivRewardIcon != null) {
                        holder.ivRewardIcon.setVisibility(View.VISIBLE);
                        if (item.getIconRes() != 0) {
                            holder.ivRewardIcon.setImageResource(item.getIconRes());
                        }
                    }
                }
            }, null);
        } else {
            holder.svgaRewardPreview.setVisibility(View.GONE);
            holder.ivRewardIcon.setVisibility(View.VISIBLE);
            if (item.getIconRes() != 0) {
                holder.ivRewardIcon.setImageResource(item.getIconRes());
            }
        }

        holder.cardReward.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardReward;
        TextView tvRewardBadge;
        FrameLayout flPreviewContainer;
        ImageView ivRewardIcon;
        SVGAImageView svgaRewardPreview;
        TextView tvRewardTitle;
        TextView tvRewardSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardReward = itemView.findViewById(R.id.cardReward);
            tvRewardBadge = itemView.findViewById(R.id.tvRewardBadge);
            flPreviewContainer = itemView.findViewById(R.id.flPreviewContainer);
            ivRewardIcon = itemView.findViewById(R.id.ivRewardIcon);
            svgaRewardPreview = itemView.findViewById(R.id.svgaRewardPreview);
            tvRewardTitle = itemView.findViewById(R.id.tvRewardTitle);
            tvRewardSubtitle = itemView.findViewById(R.id.tvRewardSubtitle);
        }
    }
}

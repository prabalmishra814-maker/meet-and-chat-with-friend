package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.AnimationHelper;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.DayRewardConfig;
import com.roomchatapps.Pmishra.models.UserCheckInState;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// DAILY CHECK-IN UI REDESIGN
public class DailyCheckInAdapter extends RecyclerView.Adapter<DailyCheckInAdapter.ViewHolder> {

    public interface OnClaimClickListener {
        void onClaimClick(DayRewardConfig reward, int position);
    }

    private final Context context;
    private final List<DayRewardConfig> rewards;
    private UserCheckInState checkInState;
    private final OnClaimClickListener listener;

    public DailyCheckInAdapter(Context context, List<DayRewardConfig> rewards, UserCheckInState checkInState, OnClaimClickListener listener) {
        this.context = context;
        this.rewards = rewards;
        this.checkInState = checkInState;
        this.listener = listener;
    }

    public void updateState(UserCheckInState newState) {
        this.checkInState = newState;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_checkin_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayRewardConfig item = rewards.get(position);
        if (item == null) return;

        holder.tvDayBadge.setText("Day " + item.getDayNumber());
        holder.tvRewardName.setText(item.getRewardName());

        // Subtitle badge for special rewards
        if ("ENTRANCE".equalsIgnoreCase(item.getRewardType())) {
            holder.tvRewardSubtitle.setVisibility(View.VISIBLE);
            holder.tvRewardSubtitle.setText("Entry Effect");
        } else if ("FRAME".equalsIgnoreCase(item.getRewardType())) {
            holder.tvRewardSubtitle.setVisibility(View.VISIBLE);
            holder.tvRewardSubtitle.setText("Avatar Frame");
        } else if ("ENERGY".equalsIgnoreCase(item.getRewardType())) {
            holder.tvRewardSubtitle.setVisibility(View.VISIBLE);
            holder.tvRewardSubtitle.setText("Energy Balance");
        } else {
            holder.tvRewardSubtitle.setVisibility(View.GONE);
        }

        // Setup Timeline vertical lines
        holder.vLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.vLineBottom.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);

        // Load Icon
        int iconResId = 0;
        if (item.getIconResName() != null && !item.getIconResName().isEmpty()) {
            iconResId = context.getResources().getIdentifier(item.getIconResName(), "drawable", context.getPackageName());
        }
        if (iconResId == 0) {
            if ("COIN".equalsIgnoreCase(item.getRewardType())) {
                iconResId = R.drawable.coin;
            } else if ("ENERGY".equalsIgnoreCase(item.getRewardType())) {
                iconResId = R.drawable.energy;
            } else {
                iconResId = R.drawable.ic_crown_gold_frame;
            }
        }
        holder.ivRewardIcon.setImageResource(iconResId);

        // Load SVGA preview if present
        if (item.getSvgaPath() != null && !item.getSvgaPath().isEmpty()) {
            holder.svgaRewardPreview.setVisibility(View.VISIBLE);
            holder.svgaRewardPreview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            SVGAParser parser = new SVGAParser(context);
            parser.decodeFromAssets(item.getSvgaPath(), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    if (holder.itemView.getContext() != null) {
                        holder.svgaRewardPreview.setVideoItem(videoItem);
                        holder.svgaRewardPreview.setLoops(0);
                        holder.svgaRewardPreview.stepToFrame(0, true);
                    }
                }

                @Override
                public void onError() {
                    holder.svgaRewardPreview.setVisibility(View.GONE);
                }
            }, null);
        } else {
            holder.svgaRewardPreview.setVisibility(View.GONE);
        }

        // Determine day state
        int dayNum = item.getDayNumber();
        boolean isClaimed = checkInState != null && checkInState.isDayClaimed(dayNum);
        boolean isCurrentAvailable = checkInState != null && !checkInState.isTodayClaimed() && checkInState.getCurrentDay() == dayNum;

        if (isClaimed) {
            holder.tvDayBadge.setTextColor(Color.parseColor("#B2DFDB"));
            holder.vNodeCircle.setBackgroundResource(R.drawable.bg_timeline_node_claimed);
            holder.ivArrow.setVisibility(View.GONE);
            holder.llRewardCard.setBackgroundResource(R.drawable.bg_timeline_card_claimed);
            holder.tvRewardName.setTextColor(Color.parseColor("#B2DFDB"));
            holder.tvRewardSubtitle.setTextColor(Color.parseColor("#80B2DFDB"));

            holder.btnClaim.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("CLAIMED ✓");
            holder.tvStatus.setTextColor(Color.parseColor("#00F5D4"));
        } else if (isCurrentAvailable) {
            holder.tvDayBadge.setTextColor(Color.parseColor("#FFD700"));
            holder.vNodeCircle.setBackgroundResource(R.drawable.bg_timeline_node_current);
            holder.ivArrow.setVisibility(View.VISIBLE);
            holder.llRewardCard.setBackgroundResource(R.drawable.bg_timeline_card_current);
            holder.tvRewardName.setTextColor(Color.parseColor("#FFFFFF"));
            holder.tvRewardSubtitle.setTextColor(Color.parseColor("#FFE082"));

            holder.btnClaim.setVisibility(View.VISIBLE);
            holder.btnClaim.setText("CLAIM");
            holder.btnClaim.setTextColor(Color.parseColor("#FFFFFF"));
            holder.tvStatus.setVisibility(View.GONE);

            AnimationHelper.pulseGlowAnimation(holder.btnClaim);

            holder.btnClaim.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(v);
                if (listener != null) {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onClaimClick(item, pos);
                    }
                }
            });
        } else {
            holder.tvDayBadge.setTextColor(Color.parseColor("#80FFFFFF"));
            holder.vNodeCircle.setBackgroundResource(R.drawable.bg_timeline_node_locked);
            holder.ivArrow.setVisibility(View.GONE);
            holder.llRewardCard.setBackgroundResource(R.drawable.bg_timeline_card_locked);
            holder.tvRewardName.setTextColor(Color.parseColor("#80FFFFFF"));
            holder.tvRewardSubtitle.setTextColor(Color.parseColor("#50FFFFFF"));

            holder.btnClaim.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("LOCKED");
            holder.tvStatus.setTextColor(Color.parseColor("#80FFFFFF"));
        }
    }

    @Override
    public int getItemCount() {
        return rewards != null ? rewards.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vLineTop, vLineBottom, vNodeCircle, llRewardCard;
        TextView tvDayBadge, tvRewardName, tvRewardSubtitle, tvStatus;
        ImageView ivGiftBox, ivArrow, ivRewardIcon;
        SVGAImageView svgaRewardPreview;
        MaterialButton btnClaim;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vLineTop = itemView.findViewById(R.id.vLineTop);
            vLineBottom = itemView.findViewById(R.id.vLineBottom);
            vNodeCircle = itemView.findViewById(R.id.vNodeCircle);
            llRewardCard = itemView.findViewById(R.id.llRewardCard);
            tvDayBadge = itemView.findViewById(R.id.tvDayBadge);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvRewardSubtitle = itemView.findViewById(R.id.tvRewardSubtitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivGiftBox = itemView.findViewById(R.id.ivGiftBox);
            ivArrow = itemView.findViewById(R.id.ivArrow);
            ivRewardIcon = itemView.findViewById(R.id.ivRewardIcon);
            svgaRewardPreview = itemView.findViewById(R.id.svgaRewardPreview);
            btnClaim = itemView.findViewById(R.id.btnClaim);
        }
    }
}

package com.roomchatapps.Pmishra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.models.ReferralModel;

import java.util.List;

public class ReferralAdapter extends RecyclerView.Adapter<ReferralAdapter.ViewHolder> {

    private final List<ReferralModel> referralList;
    private int lastAnimatedPosition = -1;

    public ReferralAdapter(List<ReferralModel> referralList) {
        this.referralList = referralList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_referral, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReferralModel item = referralList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvUsername.setText(item.getReferredName() != null ? item.getReferredName() : "Referred User");
        holder.tvJoinedTime.setText(formatTimeAgo(item.getTimestamp()));
        holder.tvRewardBadge.setText("+" + item.getRewardCoins() + " 🪙");

        if (item.getReferredAvatar() != null && !item.getReferredAvatar().isEmpty()) {
            Glide.with(context)
                    .load(item.getReferredAvatar())
                    .placeholder(R.drawable.ic_person)
                    .into(holder.ivUserAvatar);
        } else {
            holder.ivUserAvatar.setImageResource(R.drawable.ic_person);
        }

        setEntranceAnimation(holder.itemView, position);
    }

    private void setEntranceAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.setTranslationX(100f);
            viewToAnimate.setAlpha(0f);

            viewToAnimate.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(350)
                    .setStartDelay(Math.min(position * 40L, 250L))
                    .setInterpolator(new DecelerateInterpolator(1.4f))
                    .start();

            lastAnimatedPosition = position;
        }
    }

    private String formatTimeAgo(long timestamp) {
        if (timestamp <= 0) return "Just now";
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60_000) return "Just now";
        if (diff < 3600_000) return (diff / 60_000) + "m ago";
        if (diff < 86400_000) return (diff / 3600_000) + "h ago";
        return (diff / 86400_000) + "d ago";
    }

    @Override
    public int getItemCount() {
        return referralList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivUserAvatar;
        final TextView tvUsername;
        final TextView tvJoinedTime;
        final TextView tvRewardBadge;

        ViewHolder(View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvJoinedTime = itemView.findViewById(R.id.tvJoinedTime);
            tvRewardBadge = itemView.findViewById(R.id.tvRewardBadge);
        }
    }
}

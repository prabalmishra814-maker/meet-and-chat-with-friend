package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.AnimationHelper;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.SeatAnimationManager;
import com.roomchatapps.Pmishra.models.User;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import java.util.List;

public class SpeakerAdapter extends RecyclerView.Adapter<SpeakerAdapter.ViewHolder> {

    private final List<User> speakerList;
    private int lastAnimatedPosition = -1;

    public SpeakerAdapter(List<User> speakerList) {
        this.speakerList = speakerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_speaker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) {
            pos = holder.getAbsoluteAdapterPosition();
        }
        if (pos == RecyclerView.NO_POSITION) {
            pos = position;
        }

        final int targetPos = pos;
        User user = speakerList.get(targetPos);

        // Entrance animation
        if (targetPos > lastAnimatedPosition) {
            SeatAnimationManager.animateSeatEntrance(holder.itemView, user.isHost());
            lastAnimatedPosition = targetPos;
        }

        holder.tvSpeakerName.setText(user.getUserName());

        String userId = user.getUserId();
        if (userId != null && !userId.trim().isEmpty()) {
            UserProfileCache.getUserProfile(userId, profile -> {
                if (profile != null) {
                    if (profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(profile.avatarUrl)
                                .placeholder(R.drawable.logo_placeholder)
                                .into(holder.ivSpeakerProfile);
                    } else {
                        Glide.with(holder.itemView.getContext())
                                .load(user.getUserIcon())
                                .placeholder(R.drawable.logo_placeholder)
                                .into(holder.ivSpeakerProfile);
                    }

                    int frameRes = FrameUtils.getFrameDrawableRes(holder.itemView.getContext(), profile.equippedFrame);
                    if (holder.ivSpeakerFrame != null) {
                        holder.ivSpeakerFrame.setImageResource(frameRes);
                        holder.ivSpeakerFrame.setVisibility(View.VISIBLE);
                        AnimationHelper.pulseGlowAnimation(holder.ivSpeakerFrame);
                    }
                }
            });
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(user.getUserIcon())
                    .placeholder(R.drawable.logo_placeholder)
                    .into(holder.ivSpeakerProfile);
        }

        // Mic Status
        boolean wasMicOn = holder.ivMicStatus.getTag() != null && (boolean) holder.ivMicStatus.getTag();
        if (user.isMicOn()) {
            holder.ivMicStatus.setImageResource(R.drawable.ic_launcher_foreground); 
            holder.ivMicStatus.setVisibility(View.VISIBLE);
            if (!wasMicOn) {
                SeatAnimationManager.animateMicStateChange(holder.ivMicStatus, true);
            }
            holder.ivMicStatus.setTag(true);
        } else {
            holder.ivMicStatus.setImageResource(R.drawable.ic_launcher_background); 
            holder.ivMicStatus.setVisibility(View.VISIBLE);
            if (wasMicOn) {
                SeatAnimationManager.animateMicStateChange(holder.ivMicStatus, false);
            }
            holder.ivMicStatus.setTag(false);
        }

        // Speaking Detection Animation (Highlight stroke & Pulsing ring)
        if (user.isSpeaking() && user.isMicOn()) {
            holder.ivSpeakerProfile.setStrokeColor(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.accent_primary));
            holder.ivSpeakerProfile.setStrokeWidth(4f);
            if (holder.speakingIndicator != null) {
                SeatAnimationManager.startPulsingRing(holder.speakingIndicator);
            }
        } else {
            holder.ivSpeakerProfile.setStrokeColor(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.glass_white));
            holder.ivSpeakerProfile.setStrokeWidth(2f);
            if (holder.speakingIndicator != null) {
                SeatAnimationManager.stopPulsingRing(holder.speakingIndicator);
            }
        }
        
        // Host Badge
        if (holder.hostBadge != null) {
            holder.hostBadge.setVisibility(user.isHost() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return speakerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.imageview.ShapeableImageView ivSpeakerProfile;
        ImageView ivSpeakerFrame;
        ImageView ivMicStatus;
        TextView tvSpeakerName;
        View speakingIndicator;
        View hostBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSpeakerProfile = itemView.findViewById(R.id.ivSpeakerProfile);
            ivSpeakerFrame = itemView.findViewById(R.id.ivSpeakerFrame);
            ivMicStatus = itemView.findViewById(R.id.ivMicStatus);
            tvSpeakerName = itemView.findViewById(R.id.tvSpeakerName);
            speakingIndicator = itemView.findViewById(R.id.speakingIndicator);
            hostBadge = itemView.findViewById(R.id.hostBadge);
        }
    }
}

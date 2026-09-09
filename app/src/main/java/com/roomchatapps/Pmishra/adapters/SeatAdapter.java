package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.SeatAnimationManager;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.zego.SeatModel;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.ViewHolder> {

    public static final int TYPE_HOST = 0;
    public static final int TYPE_REGULAR = 1;

    public interface OnSeatClickListener {
        void onSeatClick(SeatModel model);
    }

    private final List<SeatModel> seats = new ArrayList<>();
    private final OnSeatClickListener listener;

    public SeatAdapter(OnSeatClickListener listener) {
        this.listener = listener;
    }

    public void setSeats(List<SeatModel> newSeats) {
        this.seats.clear();
        if (newSeats != null) {
            this.seats.addAll(newSeats);
        }
        notifyDataSetChanged();
    }

    public void setSpeaking(String userID, boolean isSpeaking) {
        if (userID == null) return;
        for (int i = 0; i < seats.size(); i++) {
            SeatModel model = seats.get(i);
            if (userID.equals(model.userID)) {
                if (model.isSpeaking != isSpeaking) {
                    model.isSpeaking = isSpeaking;
                    notifyItemChanged(i);
                }
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_HOST : TYPE_REGULAR;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == TYPE_HOST) ? R.layout.item_host_seat : R.layout.item_seat;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SeatModel model = seats.get(position);
        Context context = holder.itemView.getContext();

        // Host badge
        if (holder.hostBadge != null) {
            holder.hostBadge.setVisibility(model.isHost() ? View.VISIBLE : View.GONE);
        }

        if (model.isClosed) {
            // Locked / Closed Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.VISIBLE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.GONE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.GONE);
            if (holder.ivSeatFrame != null) holder.ivSeatFrame.setVisibility(View.GONE);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.VISIBLE);
            if (holder.ivMicStatus != null) holder.ivMicStatus.setVisibility(View.GONE);
            if (holder.tvSeatName != null) holder.tvSeatName.setText("Locked");
            SeatAnimationManager.stopPulsingRing(holder.speakingIndicator);

        } else if (model.isEmpty()) {
            // Empty Open Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.VISIBLE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.VISIBLE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.GONE);
            if (holder.ivSeatFrame != null) holder.ivSeatFrame.setVisibility(View.GONE);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.GONE);
            if (holder.ivMicStatus != null) holder.ivMicStatus.setVisibility(View.GONE);
            
            if (holder.tvSeatName != null) {
                if (position == 0) {
                    holder.tvSeatName.setText("Host Seat");
                } else {
                    holder.tvSeatName.setText(String.valueOf(model.index));
                }
            }
            SeatAnimationManager.stopPulsingRing(holder.speakingIndicator);

        } else {
            // Occupied Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.GONE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.GONE);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.GONE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.VISIBLE);

            // User Name
            String displayName = model.userName != null && !model.userName.isEmpty() ? model.userName : "User";
            if (holder.tvSeatName != null) {
                if (position == 0) {
                    holder.tvSeatName.setText("❤️ " + displayName + " ❤️");
                } else {
                    holder.tvSeatName.setText(displayName);
                }
            }

            // Mic Status
            if (holder.ivMicStatus != null) {
                holder.ivMicStatus.setVisibility(View.VISIBLE);
                if (model.isMuted) {
                    holder.ivMicStatus.setImageResource(R.drawable.ic_mic_off);
                    holder.ivMicStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light));
                } else if (model.isMicOn) {
                    holder.ivMicStatus.setImageResource(R.drawable.ic_mic_on);
                    holder.ivMicStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_light));
                } else {
                    holder.ivMicStatus.setImageResource(R.drawable.ic_mic_off);
                    holder.ivMicStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
                }
            }

            // Load Avatar & Frame
            if (holder.ivSeatAvatar != null) {
                if (model.userAvatar != null && !model.userAvatar.trim().isEmpty()) {
                    Glide.with(context)
                            .load(model.userAvatar)
                            .placeholder(R.drawable.logo_placeholder)
                            .into(holder.ivSeatAvatar);
                } else {
                    UserProfileCache.getUserProfile(model.userID, profile -> {
                        if (profile != null) {
                            if (profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                                Glide.with(context)
                                        .load(profile.avatarUrl)
                                        .placeholder(R.drawable.logo_placeholder)
                                        .into(holder.ivSeatAvatar);
                            } else {
                                Glide.with(context)
                                        .load(R.drawable.logo_placeholder)
                                        .into(holder.ivSeatAvatar);
                            }

                            if (holder.ivSeatFrame != null) {
                                int frameRes = FrameUtils.getFrameDrawableRes(context, profile.equippedFrame);
                                if (frameRes != 0) {
                                    holder.ivSeatFrame.setImageResource(frameRes);
                                    holder.ivSeatFrame.setVisibility(View.VISIBLE);
                                } else {
                                    holder.ivSeatFrame.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.logo_placeholder)
                                    .into(holder.ivSeatAvatar);
                            if (holder.ivSeatFrame != null) {
                                holder.ivSeatFrame.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            // Speaking Pulsing Ring Animation
            if (model.isSpeaking && model.isMicOn && !model.isMuted) {
                SeatAnimationManager.startPulsingRing(holder.speakingIndicator);
                if (holder.ivSeatAvatar != null) {
                    holder.ivSeatAvatar.setStrokeColor(ContextCompat.getColorStateList(context, R.color.accent_primary));
                    holder.ivSeatAvatar.setStrokeWidth(3f);
                }
            } else {
                SeatAnimationManager.stopPulsingRing(holder.speakingIndicator);
                if (holder.ivSeatAvatar != null) {
                    holder.ivSeatAvatar.setStrokeColor(ContextCompat.getColorStateList(context, position == 0 ? android.R.color.holo_orange_light : R.color.glass_white));
                    holder.ivSeatAvatar.setStrokeWidth(position == 0 ? 2.5f : 1.5f);
                }
            }
        }

        // On Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSeatClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return seats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vEmptySeatBg;
        ImageView ivAddIcon;
        ShapeableImageView ivSeatAvatar;
        ImageView ivSeatFrame;
        ImageView ivSeatLocked;
        ImageView ivMicStatus;
        TextView tvSeatName;
        View speakingIndicator;
        View hostBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vEmptySeatBg = itemView.findViewById(R.id.vEmptySeatBg);
            ivAddIcon = itemView.findViewById(R.id.ivAddIcon);
            ivSeatAvatar = itemView.findViewById(R.id.ivSeatAvatar);
            ivSeatFrame = itemView.findViewById(R.id.ivSeatFrame);
            ivSeatLocked = itemView.findViewById(R.id.ivSeatLocked);
            ivMicStatus = itemView.findViewById(R.id.ivMicStatus);
            tvSeatName = itemView.findViewById(R.id.tvSeatName);
            speakingIndicator = itemView.findViewById(R.id.speakingIndicator);
            hostBadge = itemView.findViewById(R.id.hostBadge);
        }
    }
}

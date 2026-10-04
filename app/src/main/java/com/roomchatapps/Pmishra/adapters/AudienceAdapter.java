package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.opensource.svgaplayer.SVGAImageView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.RoomChatActivity;
import com.roomchatapps.Pmishra.SeatAnimationManager;
import com.roomchatapps.Pmishra.models.User;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.zego.SeatManager;
import java.util.List;

public class AudienceAdapter extends RecyclerView.Adapter<AudienceAdapter.ViewHolder> {

    public interface OnInviteClickListener {
        void onInviteClick(User user);
    }

    private List<User> audienceList;
    private boolean isHostViewer = false;
    private OnInviteClickListener inviteClickListener;
    private int lastAnimatedPosition = -1;

    public AudienceAdapter(List<User> audienceList) {
        this.audienceList = audienceList;
    }

    public AudienceAdapter(List<User> audienceList, boolean isHostViewer, OnInviteClickListener inviteClickListener) {
        this.audienceList = audienceList;
        this.isHostViewer = isHostViewer;
        this.inviteClickListener = inviteClickListener;
    }

    public void updateList(List<User> newList) {
        this.audienceList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audience, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = audienceList.get(position);

        // Entrance animation
        if (position > lastAnimatedPosition) {
            SeatAnimationManager.animateSeatEntrance(holder.itemView, false);
            lastAnimatedPosition = position;
        }

        holder.tvAudienceName.setText(user.getUserName() != null ? user.getUserName() : "Member");

        boolean isSeated = user.getUserId() != null && SeatManager.getInstance().findUserSeatIndex(user.getUserId()) != -1;
        if (isSeated) {
            int seatIdx = SeatManager.getInstance().findUserSeatIndex(user.getUserId());
            if (seatIdx == 0 || user.isHost()) {
                holder.tvAudienceStatus.setText("🔴 Live (Host)");
            } else {
                holder.tvAudienceStatus.setText("🎙️ Live on Seat " + (seatIdx + 1));
            }
            if (holder.btnInviteUser != null) {
                holder.btnInviteUser.setVisibility(View.GONE);
            }
        } else {
            if (user.isHost()) {
                holder.tvAudienceStatus.setText("👑 Room Host");
            } else {
                holder.tvAudienceStatus.setText("🎧 Joined Room");
            }

            String currentUserId = null;
            if (holder.itemView.getContext() instanceof RoomChatActivity) {
                currentUserId = ((RoomChatActivity) holder.itemView.getContext()).getUserID();
            }

            boolean isSelf = (currentUserId != null && user.getUserId() != null && currentUserId.equals(user.getUserId()));

            if (isHostViewer && inviteClickListener != null && !isSelf) {
                if (holder.btnInviteUser != null) {
                    holder.btnInviteUser.setVisibility(View.VISIBLE);
                    holder.btnInviteUser.setOnClickListener(v -> {
                        int adapterPos = holder.getBindingAdapterPosition();
                        if (adapterPos != RecyclerView.NO_POSITION && adapterPos < audienceList.size()) {
                            inviteClickListener.onInviteClick(audienceList.get(adapterPos));
                        }
                    });
                }
            } else {
                if (holder.btnInviteUser != null) {
                    holder.btnInviteUser.setVisibility(View.GONE);
                }
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(user.getUserIcon())
                .placeholder(R.drawable.logo_placeholder)
                .into(holder.ivAudienceProfile);

        String userId = user.getUserId();
        if (userId != null && !userId.trim().isEmpty()) {
            View.OnClickListener openProfile = v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.roomchatapps.Pmishra.UserDetailActivity.class);
                intent.putExtra("uid", userId);
                v.getContext().startActivity(intent);
            };
            if (holder.ivAudienceProfile != null) holder.ivAudienceProfile.setOnClickListener(openProfile);
            if (holder.tvAudienceName != null) holder.tvAudienceName.setOnClickListener(openProfile);

            UserProfileCache.getUserProfile(userId, profile -> {
                if (profile != null) {
                    FrameUtils.displayFrame(holder.itemView.getContext(), profile.equippedFrame, holder.ivAudienceFrame, holder.svgaAudienceFrame);
                } else {
                    FrameUtils.clearFrame(holder.ivAudienceFrame, holder.svgaAudienceFrame);
                }
            });
        } else {
            FrameUtils.clearFrame(holder.ivAudienceFrame, holder.svgaAudienceFrame);
        }
    }

    @Override
    public int getItemCount() {
        return audienceList != null ? audienceList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAudienceProfile;
        ImageView ivAudienceFrame;
        SVGAImageView svgaAudienceFrame;
        TextView tvAudienceName, tvAudienceStatus;
        MaterialButton btnInviteUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAudienceProfile = itemView.findViewById(R.id.ivAudienceProfile);
            ivAudienceFrame = itemView.findViewById(R.id.ivAudienceFrame);
            svgaAudienceFrame = itemView.findViewById(R.id.svgaAudienceFrame);
            tvAudienceName = itemView.findViewById(R.id.tvAudienceName);
            tvAudienceStatus = itemView.findViewById(R.id.tvAudienceStatus);
            btnInviteUser = itemView.findViewById(R.id.btnInviteUser);
        }
    }
}

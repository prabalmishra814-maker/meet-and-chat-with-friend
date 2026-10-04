package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.util.TypedValue;
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
import com.opensource.svgaplayer.SVGAImageView;
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
    private int availableHeight = 0;
    private int availableWidth = 0;

    public SeatAdapter(OnSeatClickListener listener) {
        this.listener = listener;
    }

    public void setAvailableHeight(int heightPx) {
        setAvailableDimensions(this.availableWidth, heightPx);
    }

    public void setAvailableDimensions(int widthPx, int heightPx) {
        if (this.availableWidth != widthPx || this.availableHeight != heightPx) {
            this.availableWidth = widthPx;
            this.availableHeight = heightPx;
            notifyDataSetChanged();
        }
    }

    public int getAvailableHeight() {
        return availableHeight;
    }

    public int getAvailableWidth() {
        return availableWidth;
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
        float density = context.getResources().getDisplayMetrics().density;
        boolean isHost = (position == 0);
        int totalSeats = getItemCount();
        int totalRows = (totalSeats <= 9) ? 3 : (totalSeats <= 17 ? 5 : 6);
        int rowIndex = (position == 0) ? 0 : 1 + (position - 1) / 4;

        int parentWidth = 0;
        if (holder.itemView.getParent() instanceof View) {
            View parentView = (View) holder.itemView.getParent();
            parentWidth = parentView.getWidth() - parentView.getPaddingLeft() - parentView.getPaddingRight();
        }
        int widthToUse = (parentWidth > 0) ? parentWidth : availableWidth;

        // Apply dynamic responsive sizing spec
        SeatLayoutSpec spec = SeatLayoutSpec.create(isHost, totalSeats, rowIndex, totalRows, widthToUse, availableHeight, density);
        holder.applySpec(spec);

        // Host badge
        if (holder.hostBadge != null) {
            holder.hostBadge.setVisibility(model.isHost() ? View.VISIBLE : View.GONE);
        }

        if (model.isClosed) {
            // Locked / Closed Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.VISIBLE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.GONE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.GONE);
            FrameUtils.clearFrame(holder.ivSeatFrame, holder.svgaSeatFrame);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.VISIBLE);
            if (holder.tvSeatName != null) holder.tvSeatName.setText("Locked");
            SeatAnimationManager.stopPulsingRing(holder.speakingIndicator);

            if (holder.ivMicStatus != null) {
                if (model.isMuted) {
                    holder.ivMicStatus.setVisibility(View.VISIBLE);
                    holder.ivMicStatus.setImageResource(R.drawable.ic_mic_off);
                    holder.ivMicStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light));
                } else {
                    holder.ivMicStatus.setVisibility(View.GONE);
                }
            }

        } else if (model.isEmpty()) {
            // Empty Open Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.VISIBLE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.VISIBLE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.GONE);
            FrameUtils.clearFrame(holder.ivSeatFrame, holder.svgaSeatFrame);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.GONE);

            // SEAT LAYOUT FIX
            if (holder.tvSeatName != null) {
                if (position == 0) {
                    holder.tvSeatName.setText("Host Seat");
                } else {
                    holder.tvSeatName.setText(String.valueOf(model.index + 1));
                }
            }
            SeatAnimationManager.stopPulsingRing(holder.speakingIndicator);

            if (holder.ivMicStatus != null) {
                if (model.isMuted) {
                    holder.ivMicStatus.setVisibility(View.VISIBLE);
                    holder.ivMicStatus.setImageResource(R.drawable.ic_mic_off);
                    holder.ivMicStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light));
                } else {
                    holder.ivMicStatus.setVisibility(View.GONE);
                }
            }

        } else {
            // Occupied Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.GONE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.GONE);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.GONE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.VISIBLE);

            // User Name
            String displayName = model.userName != null && !model.userName.isEmpty() ? model.userName : "User";
            if (holder.tvSeatName != null) {
                holder.tvSeatName.setText(displayName);
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

            // 1. Avatar
            if (holder.ivSeatAvatar != null) {
                if (model.userAvatar != null && !model.userAvatar.trim().isEmpty()) {
                    Glide.with(context)
                            .load(model.userAvatar)
                            .placeholder(R.drawable.logo_placeholder)
                            .into(holder.ivSeatAvatar);
                } else {
                    UserProfileCache.getUserProfile(model.userID, profile -> {
                        if (model.isEmpty()) return;
                        if (profile != null && profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                            Glide.with(context)
                                    .load(profile.avatarUrl)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .into(holder.ivSeatAvatar);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.logo_placeholder)
                                    .into(holder.ivSeatAvatar);
                        }
                    });
                }
            }

            // 2. Frame
            if (model.isEmpty()) {
                FrameUtils.clearFrame(holder.ivSeatFrame, holder.svgaSeatFrame);
            } else if (model.equippedFrame != null && !model.equippedFrame.trim().isEmpty()) {
                FrameUtils.displayFrame(context, model.equippedFrame, holder.ivSeatFrame, holder.svgaSeatFrame);
            } else {
                UserProfileCache.getUserProfile(model.userID, profile -> {
                    if (model.isEmpty()) {
                        FrameUtils.clearFrame(holder.ivSeatFrame, holder.svgaSeatFrame);
                        return;
                    }
                    if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                        model.equippedFrame = profile.equippedFrame;
                        FrameUtils.displayFrame(context, profile.equippedFrame, holder.ivSeatFrame, holder.svgaSeatFrame);
                    } else {
                        FrameUtils.displayFrame(context, null, holder.ivSeatFrame, holder.svgaSeatFrame);
                    }
                });
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
                    holder.ivSeatAvatar.setStrokeWidth(position == 0 ? 2f : 1.2f);
                }
            }
        }

        // Clear click listener on broad parent item view
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);

        // Attach click listener ONLY to the actual seat view container (avatarContainer)
        if (holder.avatarContainer != null) {
            holder.avatarContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSeatClick(model);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return seats.size();
    }

    public static class SeatLayoutSpec {
        public int containerSizePx;
        public int circleSizePx;
        public int addIconSizePx;
        public int lockIconSizePx;
        public int micIconSizePx;
        public float textSizeSp;
        public int topPaddingPx;
        public int bottomPaddingPx;
        public int textMarginTopPx;

        public static SeatLayoutSpec create(
                boolean isHost,
                int totalSeats,
                int rowIndex,
                int totalRows,
                int availableWidthPx,
                int availableHeightPx,
                float density
        ) {
            SeatLayoutSpec spec = new SeatLayoutSpec();

            // 1. Calculate exact row height allocated in pixels
            int rowHeightPx;
            if (availableHeightPx > 0) {
                rowHeightPx = (availableHeightPx * (rowIndex + 1) / totalRows) - (availableHeightPx * rowIndex / totalRows);
            } else {
                rowHeightPx = Math.round((totalRows <= 3 ? 120f : (totalRows <= 5 ? 76f : 62f)) * density);
            }
            float rowHeightDp = rowHeightPx / density;

            // 2. Calculate column width in dp
            float availableWidthDp;
            if (availableWidthPx > 0) {
                availableWidthDp = availableWidthPx / density;
            } else {
                availableWidthDp = 336f; // Fallback default (360dp screen - 24dp padding)
            }
            float colWidthDp = availableWidthDp / 4f;

            // 3. Determine text size and text margin
            if (totalSeats <= 9) {
                spec.textSizeSp = isHost ? 12.5f : 11.0f;
                spec.textMarginTopPx = Math.round((isHost ? 3.0f : 2.5f) * density);
            } else if (totalSeats <= 17) {
                spec.textSizeSp = isHost ? 11.0f : 10.0f;
                spec.textMarginTopPx = Math.round((isHost ? 2.5f : 2.0f) * density);
            } else {
                spec.textSizeSp = isHost ? 10.0f : 9.0f;
                spec.textMarginTopPx = Math.round((isHost ? 2.0f : 1.5f) * density);
            }

            float estimatedTextHeightDp = spec.textSizeSp * 1.25f;
            float textMarginTopDp = spec.textMarginTopPx / density;

            // 4. Calculate max container size allowed by height and width
            float minVerticalPaddingDp = 2.0f;
            float maxContainerHeightDp = Math.max(20f, rowHeightDp - (estimatedTextHeightDp + textMarginTopDp + minVerticalPaddingDp));
            float maxCircleFromHeightDp = maxContainerHeightDp / 1.25f;

            float maxContainerWidthDp = isHost ? (colWidthDp * 1.4f) : Math.max(20f, colWidthDp - 4f);
            float maxCircleFromWidthDp = maxContainerWidthDp / 1.25f;

            float targetCircleDp = Math.min(maxCircleFromWidthDp, maxCircleFromHeightDp);

            // 5. Apply mode-specific min/max bounds
            float minCircleDp, maxCircleDp;
            if (totalSeats <= 9) {
                minCircleDp = isHost ? 48f : 42f;
                maxCircleDp = isHost ? 68f : 58f;
            } else if (totalSeats <= 17) {
                minCircleDp = isHost ? 36f : 32f;
                maxCircleDp = isHost ? 56f : 48f;
            } else {
                minCircleDp = isHost ? 30f : 26f;
                maxCircleDp = isHost ? 48f : 42f;
            }

            float finalCircleDp = Math.max(minCircleDp, Math.min(targetCircleDp, maxCircleDp));
            float frameMultiplier = isHost ? 1.28f : 1.25f;

            spec.circleSizePx = Math.round(finalCircleDp * density);
            spec.containerSizePx = Math.round(finalCircleDp * frameMultiplier * density);

            // Derive icons sizes relative to circle size
            spec.addIconSizePx = Math.max(12, Math.round(spec.circleSizePx * 0.48f));
            spec.lockIconSizePx = Math.max(12, Math.round(spec.circleSizePx * 0.42f));
            spec.micIconSizePx = Math.max(12, Math.round(spec.circleSizePx * 0.38f));

            // Total internal content height in pixels
            int estimatedTextHeightPx = Math.round(estimatedTextHeightDp * density);
            int contentHeightPx = spec.containerSizePx + spec.textMarginTopPx + estimatedTextHeightPx;

            // Calculate remaining vertical space to absorb in item padding
            int remainingSpacePx = Math.max(0, rowHeightPx - contentHeightPx);
            spec.topPaddingPx = remainingSpacePx / 2;
            spec.bottomPaddingPx = remainingSpacePx - spec.topPaddingPx;

            return spec;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View avatarContainer;
        View vEmptySeatBg;
        ImageView ivAddIcon;
        ShapeableImageView ivSeatAvatar;
        ImageView ivSeatFrame;
        SVGAImageView svgaSeatFrame;
        ImageView ivSeatLocked;
        ImageView ivMicStatus;
        TextView tvSeatName;
        View speakingIndicator;
        View hostBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            vEmptySeatBg = itemView.findViewById(R.id.vEmptySeatBg);
            ivAddIcon = itemView.findViewById(R.id.ivAddIcon);
            ivSeatAvatar = itemView.findViewById(R.id.ivSeatAvatar);
            ivSeatFrame = itemView.findViewById(R.id.ivSeatFrame);
            svgaSeatFrame = itemView.findViewById(R.id.svgaSeatFrame);
            ivSeatLocked = itemView.findViewById(R.id.ivSeatLocked);
            ivMicStatus = itemView.findViewById(R.id.ivMicStatus);
            tvSeatName = itemView.findViewById(R.id.tvSeatName);
            speakingIndicator = itemView.findViewById(R.id.speakingIndicator);
            hostBadge = itemView.findViewById(R.id.hostBadge);
        }

        public void applySpec(SeatLayoutSpec spec) {
            if (spec == null || itemView == null) return;

            itemView.setPadding(0, spec.topPaddingPx, 0, spec.bottomPaddingPx);

            if (avatarContainer != null) {
                ViewGroup.LayoutParams params = avatarContainer.getLayoutParams();
                if (params != null) {
                    params.width = spec.containerSizePx;
                    params.height = spec.containerSizePx;
                    avatarContainer.setLayoutParams(params);
                }
            }

            updateViewSize(vEmptySeatBg, spec.circleSizePx, spec.circleSizePx);
            updateViewSize(ivSeatAvatar, spec.circleSizePx, spec.circleSizePx);
            updateViewSize(speakingIndicator, spec.circleSizePx, spec.circleSizePx);

            updateViewSize(ivAddIcon, spec.addIconSizePx, spec.addIconSizePx);
            updateViewSize(ivSeatLocked, spec.lockIconSizePx, spec.lockIconSizePx);
            updateViewSize(ivMicStatus, spec.micIconSizePx, spec.micIconSizePx);

            if (tvSeatName != null) {
                tvSeatName.setTextSize(TypedValue.COMPLEX_UNIT_SP, spec.textSizeSp);
                ViewGroup.LayoutParams nameParams = tvSeatName.getLayoutParams();
                if (nameParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) nameParams).topMargin = spec.textMarginTopPx;
                    tvSeatName.setLayoutParams(nameParams);
                }
            }
        }

        private void updateViewSize(View view, int width, int height) {
            if (view != null) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params != null) {
                    if (params.width != width || params.height != height) {
                        params.width = width;
                        params.height = height;
                        view.setLayoutParams(params);
                    }
                }
            }
        }
    }
}

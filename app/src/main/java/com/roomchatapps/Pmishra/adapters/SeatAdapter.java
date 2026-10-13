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
import android.os.Handler;
import android.os.Looper;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.SeatAnimationManager;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.zego.SeatManager;
import com.roomchatapps.Pmishra.zego.SeatModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.ViewHolder> {

    public static final int TYPE_HOST = 0;
    public static final int TYPE_REGULAR = 1;

    public interface OnSeatClickListener {
        void onSeatClick(SeatModel model);
    }

    private final List<SeatModel> seats = new ArrayList<>();
    private final Map<String, String> activeSeatEmojiMap = new ConcurrentHashMap<>();
    private final Map<String, Queue<String>> userEmojiQueueMap = new ConcurrentHashMap<>();
    private final Set<String> playingUserSet = ConcurrentHashMap.newKeySet();
    private final Handler emojiHandler = new Handler(Looper.getMainLooper());
    private final OnSeatClickListener listener;
    private int availableHeight = 0;
    private int availableWidth = 0;
    private RecyclerView attachedRecyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.attachedRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.attachedRecyclerView = null;
    }

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
        int adapterPos = holder.getBindingAdapterPosition();
        final int realPos = (adapterPos != RecyclerView.NO_POSITION) ? adapterPos : position;
        if (realPos < 0 || realPos >= seats.size()) return;

        SeatModel model = seats.get(realPos);
        Context context = holder.itemView.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        boolean isHost = (realPos == 0);
        int totalSeats = getItemCount();
        int totalRows = (totalSeats <= 9) ? 3 : (totalSeats <= 17 ? 5 : 6);
        int rowIndex = (realPos == 0) ? 0 : 1 + (realPos - 1) / 4;

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

        if (!model.isEmpty()) {
            // Occupied Seat (User sitting on seat, whether locked or open)
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.GONE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.GONE);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(model.isClosed ? View.VISIBLE : View.GONE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.VISIBLE);

            // User Name
            String displayName = model.userName != null && !model.userName.isEmpty() ? model.userName : "User";
            String userRole = SeatManager.getInstance().getUserRole(model.userID);

            if ("host".equalsIgnoreCase(userRole) || model.isHost()) {
                displayName = displayName + "\n(Host)";
            } else if ("admin".equalsIgnoreCase(userRole)) {
                displayName = displayName + "\n(Admin)";
            }

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
                            .circleCrop()
                            .into(holder.ivSeatAvatar);
                } else {
                    UserProfileCache.getUserProfile(model.userID, profile -> {
                        if (model.isEmpty()) return;
                        if (profile != null && profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                            Glide.with(context)
                                    .load(profile.avatarUrl)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .circleCrop()
                                    .into(holder.ivSeatAvatar);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.logo_placeholder)
                                    .circleCrop()
                                    .into(holder.ivSeatAvatar);
                        }
                    });
                }
            }

            // 2. Frame
            if (model.equippedFrame != null && !model.equippedFrame.trim().isEmpty()) {
                FrameUtils.displayFrame(context, model.equippedFrame, holder.ivSeatFrame, holder.svgaSeatFrame);
            } else {
                UserProfileCache.getUserProfile(model.userID, profile -> {
                    if (model.isEmpty()) return;
                    String equipped = (profile != null && profile.equippedFrame != null) ? profile.equippedFrame : "";
                    FrameUtils.displayFrame(context, equipped, holder.ivSeatFrame, holder.svgaSeatFrame);
                });
            }

            // 3. Animated Emoji Overlay (Plays 1 loop for natural duration)
            if (holder.svgaSeatEmoji != null) {
                String activeEmojiId = activeSeatEmojiMap.get(model.userID);
                if (activeEmojiId != null && !activeEmojiId.trim().isEmpty()) {
                    holder.svgaSeatEmoji.setVisibility(View.VISIBLE);
                    holder.svgaSeatEmoji.setLoops(1);
                    String assetPath = "emoji/" + activeEmojiId + ".svga";
                    SVGAParser parser = new SVGAParser(context.getApplicationContext());
                    parser.decodeFromAssets(assetPath, new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NonNull SVGAVideoEntity videoItem) {
                            if (holder.svgaSeatEmoji != null) {
                                SVGADrawable drawable = new SVGADrawable(videoItem);
                                holder.svgaSeatEmoji.setImageDrawable(drawable);
                                holder.svgaSeatEmoji.setLoops(1);
                                holder.svgaSeatEmoji.startAnimation();
                            }
                        }

                        @Override
                        public void onError() {
                            if (holder.svgaSeatEmoji != null) {
                                holder.svgaSeatEmoji.setVisibility(View.GONE);
                            }
                        }
                    }, null);
                } else {
                    holder.svgaSeatEmoji.setVisibility(View.GONE);
                    holder.svgaSeatEmoji.clear();
                }
            }

        } else if (model.isClosed) {
            // Locked Empty Seat
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

        } else {
            // Open Empty Seat
            if (holder.vEmptySeatBg != null) holder.vEmptySeatBg.setVisibility(View.VISIBLE);
            if (holder.ivAddIcon != null) holder.ivAddIcon.setVisibility(View.VISIBLE);
            if (holder.ivSeatAvatar != null) holder.ivSeatAvatar.setVisibility(View.GONE);
            FrameUtils.clearFrame(holder.ivSeatFrame, holder.svgaSeatFrame);
            if (holder.ivSeatLocked != null) holder.ivSeatLocked.setVisibility(View.GONE);

            if (holder.tvSeatName != null) {
                if (realPos == 0) {
                    holder.tvSeatName.setText("Host Seat");
                } else {
                    holder.tvSeatName.setText(String.valueOf(model.index));
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
                holder.ivSeatAvatar.setStrokeColor(ContextCompat.getColorStateList(context, realPos == 0 ? android.R.color.holo_orange_light : R.color.glass_white));
                holder.ivSeatAvatar.setStrokeWidth(realPos == 0 ? 2f : 1.2f);
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
            float frameMultiplier = 1.50f; // 1.50x ratio ensures frame surrounds avatar border perfectly

            spec.circleSizePx = Math.round(finalCircleDp * density);
            spec.containerSizePx = Math.round(finalCircleDp * frameMultiplier * density);

            // Derive icons sizes relative to circle size (increased icon size inside circle)
            spec.addIconSizePx = Math.max(14, Math.round(spec.circleSizePx * 0.62f));
            spec.lockIconSizePx = Math.max(14, Math.round(spec.circleSizePx * 0.52f));
            spec.micIconSizePx = Math.max(12, Math.round(spec.circleSizePx * 0.40f));

            // Total internal content height in pixels
            int estimatedTextHeightPx = Math.round(estimatedTextHeightDp * density);
            int contentHeightPx = spec.containerSizePx + spec.textMarginTopPx + estimatedTextHeightPx;

            // Calculate remaining vertical space and cap row padding to keep row gaps tight & compact
            int remainingSpacePx = Math.max(0, rowHeightPx - contentHeightPx);
            float maxPaddingDp = (totalSeats <= 9) ? 8.0f : ((totalSeats <= 17) ? 4.0f : 2.5f);
            int maxPaddingPx = Math.round(maxPaddingDp * density);
            int actualPaddingPx = Math.min(maxPaddingPx, remainingSpacePx);

            spec.topPaddingPx = actualPaddingPx / 2;
            spec.bottomPaddingPx = actualPaddingPx - spec.topPaddingPx;

            return spec;
        }
    }

    public void playEmojiOnSeat(Context context, String userID, String emojiId) {
        if (userID == null || emojiId == null) return;

        Queue<String> queue = userEmojiQueueMap.computeIfAbsent(userID, k -> new LinkedList<>());
        queue.add(emojiId);

        if (!playingUserSet.contains(userID)) {
            processNextEmojiForUser(context, userID);
        }
    }

    public void playEmojiOnSeat(String userID, String emojiId) {
        playEmojiOnSeat(null, userID, emojiId);
    }

    private void processNextEmojiForUser(Context ctxParam, String userID) {
        Queue<String> queue = userEmojiQueueMap.get(userID);
        if (queue == null || queue.isEmpty()) {
            playingUserSet.remove(userID);
            activeSeatEmojiMap.remove(userID);
            refreshUserSeatItem(userID);
            return;
        }

        playingUserSet.add(userID);
        String nextEmojiId = queue.poll();
        if (nextEmojiId != null) {
            activeSeatEmojiMap.put(userID, nextEmojiId);
            refreshUserSeatItem(userID);

            Context context = (ctxParam != null) ? ctxParam : (attachedRecyclerView != null ? attachedRecyclerView.getContext() : null);

            if (context != null) {
                String assetPath = "emoji/" + nextEmojiId + ".svga";
                SVGAParser parser = new SVGAParser(context.getApplicationContext());
                parser.decodeFromAssets(assetPath, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NonNull SVGAVideoEntity videoItem) {
                        int frames = videoItem.getFrames();
                        int fps = videoItem.getFPS() > 0 ? videoItem.getFPS() : 20;
                        long exactNaturalDurationMs = (long) (((double) frames / fps) * 1000L);

                        emojiHandler.postDelayed(() -> processNextEmojiForUser(context, userID), Math.max(500L, exactNaturalDurationMs + 50L));
                    }

                    @Override
                    public void onError() {
                        emojiHandler.postDelayed(() -> processNextEmojiForUser(context, userID), 2000L);
                    }
                }, null);
            } else {
                emojiHandler.postDelayed(() -> processNextEmojiForUser(null, userID), 2000L);
            }
        } else {
            playingUserSet.remove(userID);
            activeSeatEmojiMap.remove(userID);
            refreshUserSeatItem(userID);
        }
    }

    private void refreshUserSeatItem(String userID) {
        int targetPos = -1;
        for (int i = 0; i < seats.size(); i++) {
            if (userID.equals(seats.get(i).userID)) {
                targetPos = i;
                break;
            }
        }
        if (targetPos != -1) {
            notifyItemChanged(targetPos);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View avatarContainer;
        View vEmptySeatBg;
        ImageView ivAddIcon;
        ShapeableImageView ivSeatAvatar;
        ImageView ivSeatFrame;
        SVGAImageView svgaSeatFrame;
        SVGAImageView svgaSeatEmoji;
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
            svgaSeatEmoji = itemView.findViewById(R.id.svgaSeatEmoji);
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

            updateViewSize(ivSeatFrame, spec.containerSizePx, spec.containerSizePx);
            updateViewSize(svgaSeatFrame, spec.containerSizePx, spec.containerSizePx);
            updateViewSize(svgaSeatEmoji, spec.containerSizePx, spec.containerSizePx);

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

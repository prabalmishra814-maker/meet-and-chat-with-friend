package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.OtherCollectionActivity;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.IntimacyFriendModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IntimacyFriendAdapter extends RecyclerView.Adapter<IntimacyFriendAdapter.ViewHolder> {

    public interface OnFriendClickListener {
        void onFriendClick(IntimacyFriendModel friend, int position);
    }

    private final Context context;
    private final List<IntimacyFriendModel> friendList;
    private OnFriendClickListener listener;

    public IntimacyFriendAdapter(Context context, List<IntimacyFriendModel> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_intimacy_friend_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IntimacyFriendModel item = friendList.get(position);

        String name = !TextUtils.isEmpty(item.getUserName()) ? item.getUserName() : "Friend";
        holder.tvFriendName.setText(name);

        if (item.isBestFriend()) {
            holder.cardFriendRoot.setBackgroundResource(R.drawable.bg_best_friend_card);
            holder.tvFriendBadge.setText("Best Friend");
            holder.tvFriendBadge.setTextColor(0xFF5D4037);
        } else {
            if (position % 2 == 0) {
                holder.cardFriendRoot.setBackgroundResource(R.drawable.bg_intimacy_friend_card_purple);
            } else {
                holder.cardFriendRoot.setBackgroundResource(R.drawable.bg_intimacy_friend_card_pink);
            }
            holder.tvFriendBadge.setText("Lv." + Math.max(1, item.getLevel()));
            holder.tvFriendBadge.setTextColor(0xFFFFFFFF);
        }

        String avatarUrl = item.getAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl)) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .circleCrop()
                    .into(holder.ivFriendAvatar);
        } else {
            holder.ivFriendAvatar.setImageResource(R.drawable.logo_placeholder);
        }

        long days = Math.max(1, item.getDurationDays());
        long startMs = item.getStartDateTimestamp() > 0 ? item.getStartDateTimestamp() : System.currentTimeMillis();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startMs));
        holder.tvFriendDuration.setText(dateStr + "-now\n" + days + " days");

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onFriendClick(item, position);
            } else if (!TextUtils.isEmpty(item.getUserId())) {
                Intent intent = new Intent(context, OtherCollectionActivity.class);
                intent.putExtra("selectedUserId", item.getUserId());
                intent.putExtra("selectedUserName", item.getUserName());
                context.startActivity(intent);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return friendList != null ? friendList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardFriendRoot;
        TextView tvFriendBadge;
        ShapeableImageView ivFriendAvatar;
        TextView tvFriendName;
        TextView tvFriendDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFriendRoot = itemView.findViewById(R.id.cardFriendRoot);
            tvFriendBadge = itemView.findViewById(R.id.tvFriendBadge);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvFriendDuration = itemView.findViewById(R.id.tvFriendDuration);
        }
    }
}

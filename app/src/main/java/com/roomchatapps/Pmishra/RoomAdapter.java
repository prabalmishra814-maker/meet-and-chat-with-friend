package com.roomchatapps.Pmishra;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Adapter for displaying rooms in a RecyclerView.
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private final Context context;
    private final List<RoomModel> roomList;
    private int lastAnimatedPosition = -1;

    public RoomAdapter(Context context, List<RoomModel> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        int adapterPos = holder.getBindingAdapterPosition();
        int currentPos = (adapterPos != RecyclerView.NO_POSITION) ? adapterPos : position;
        RoomModel room = roomList.get(currentPos);

        // Entrance animation (Only once per item)
        if (currentPos > lastAnimatedPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(40f);
            holder.itemView.setScaleX(0.95f);
            holder.itemView.setScaleY(0.95f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(380)
                    .setStartDelay(Math.min(currentPos * 50L, 250L))
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
            lastAnimatedPosition = currentPos;
        }

        // Set room name
        holder.tvRoomName.setText(room.getRoom_name());

        // Load room image using Glide
        Glide.with(context)
                .load(room.getImg())
                .placeholder(R.drawable.logo_placeholder)
                .error(R.drawable.logo_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.ivRoomImage);

        // Load admin profile picture onto ivBadge
        if (holder.ivBadge != null) {
            String hostUid = room.getUid();
            if (hostUid != null && !hostUid.trim().isEmpty()) {
                com.roomchatapps.Pmishra.utils.UserProfileCache.getUserProfile(hostUid, profile -> {
                    if (profile != null && profile.avatarUrl != null && !profile.avatarUrl.trim().isEmpty()) {
                        Glide.with(context)
                                .load(profile.avatarUrl)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(holder.ivBadge);
                    } else {
                        holder.ivBadge.setImageResource(R.drawable.ic_person);
                    }
                });
            } else {
                holder.ivBadge.setImageResource(R.drawable.ic_person);
            }
        }

        // Set click listener to open RoomChatActivity
        holder.itemView.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(v);
            
            Intent intent = new Intent(context, RoomChatActivity.class);
            intent.putExtra("roomID", room.getRoomId());
            intent.putExtra("room_name", room.getRoom_name());
            intent.putExtra("uid", room.getUid());
            intent.putExtra("img", room.getImg());

            // Determine if current user is the host and get their name
            String currentUserId = "";
            String currentUserName = "User";

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

            if (firebaseUser != null) {
                currentUserId = firebaseUser.getUid();
                currentUserName = firebaseUser.getDisplayName();
            } else if (account != null) {
                currentUserId = account.getId();
                currentUserName = account.getDisplayName();
            }

            boolean isHost = room.getUid() != null && room.getUid().equals(currentUserId);
            
            intent.putExtra("username", currentUserName);
            intent.putExtra("userID", currentUserId);
            intent.putExtra("host", isHost); 

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRoomImage;
        ImageView ivBadge;
        TextView tvRoomName;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            ivBadge = itemView.findViewById(R.id.ivBadge);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
        }
    }
}

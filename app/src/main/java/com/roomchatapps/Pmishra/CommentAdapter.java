package com.roomchatapps.Pmishra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.opensource.svgaplayer.SVGAImageView;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private int lastAnimatedPosition = -1;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Entrance animation
        if (position > lastAnimatedPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationX(-20f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setStartDelay(Math.min(position * 50L, 300L))
                    .start();
            lastAnimatedPosition = position;
        }

        holder.tvCommentText.setText(comment.getText());
        holder.tvCommentTime.setText(comment.getTimestamp());

        if (comment.getUid() != null && !comment.getUid().trim().isEmpty()) {
            UserProfileCache.getUserProfile(comment.getUid(), profile -> {
                if (profile != null) {
                    holder.tvCommentUsername.setText(profile.name != null ? profile.name : "User");
                    Glide.with(context)
                            .load(profile.avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(holder.ivCommentUser);
                    FrameUtils.displayFrame(context, profile.equippedFrame, holder.ivCommentFrame, holder.svgaCommentFrame);
                } else {
                    FrameUtils.clearFrame(holder.ivCommentFrame, holder.svgaCommentFrame);
                }
            });
        } else {
            FrameUtils.clearFrame(holder.ivCommentFrame, holder.svgaCommentFrame);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivCommentUser;
        ImageView ivCommentFrame;
        SVGAImageView svgaCommentFrame;
        TextView tvCommentUsername, tvCommentText, tvCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCommentUser = itemView.findViewById(R.id.ivCommentUser);
            ivCommentFrame = itemView.findViewById(R.id.ivCommentFrame);
            svgaCommentFrame = itemView.findViewById(R.id.svgaCommentFrame);
            tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
        }
    }
}

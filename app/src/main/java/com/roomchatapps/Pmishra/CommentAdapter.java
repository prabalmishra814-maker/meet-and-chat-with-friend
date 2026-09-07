package com.roomchatapps.Pmishra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        FirebaseDatabase.getInstance().getReference("users").child(comment.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String profile = snapshot.child("avtar").getValue(String.class);
                            holder.tvCommentUsername.setText(name);
                            Glide.with(context).load(profile).placeholder(R.drawable.ic_person).into(holder.ivCommentUser);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivCommentUser;
        TextView tvCommentUsername, tvCommentText, tvCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCommentUser = itemView.findViewById(R.id.ivCommentUser);
            tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
        }
    }
}

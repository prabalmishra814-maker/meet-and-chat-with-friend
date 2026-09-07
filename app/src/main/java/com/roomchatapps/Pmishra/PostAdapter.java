package com.roomchatapps.Pmishra;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private String currentUid;
    private int lastAnimatedPosition = -1;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.currentUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Entrance Animation (Only once per item position)
        if (position > lastAnimatedPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(30f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(450)
                    .setStartDelay(Math.min(position * 80L, 400L))
                    .start();
            lastAnimatedPosition = position;
        }

        holder.tvPostContent.setText(post.getTitle());
        holder.tvPostTime.setText(post.getDatetime());

        // Handle Likes
        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        holder.tvLikeCount.setText(String.valueOf(likeCount));

        boolean isLiked = post.getLikes() != null && post.getLikes().containsKey(currentUid);
        if (isLiked) {
            holder.ivLikeIcon.setImageResource(R.drawable.call_heart_ic);
            holder.ivLikeIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light));
        } else {
            holder.ivLikeIcon.setImageResource(R.drawable.call_heart_ic);
            holder.ivLikeIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
        }

        holder.btnLike.setOnClickListener(v -> {
            if (post.getPostId() == null) return;
            
            // Reaction animation
            ReactionAnimator.animateLikeButtonClick(holder.btnLike, holder.ivLikeIcon);
            if (holder.itemView.getRootView() instanceof ViewGroup) {
                ReactionAnimator.spawnFloatingReaction((ViewGroup) holder.itemView.getRootView(), holder.ivLikeIcon, R.drawable.call_heart_ic);
            }
            
            FirebaseDatabase.getInstance().getReference("Posts")
                    .child(post.getPostId())
                    .child("likes")
                    .child(currentUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Already liked, so unlike it
                                snapshot.getRef().removeValue();
                            } else {
                                // Not liked, so like it
                                snapshot.getRef().setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });

        // Handle Comments
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
        View.OnClickListener openComments = v -> {
            android.content.Intent intent = new android.content.Intent(context, CommentsActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        };
        
        holder.btnComment.setOnClickListener(openComments);
        holder.commentPreviewLayout.setOnClickListener(openComments);

        // Load Top Comment Preview
        FirebaseDatabase.getInstance().getReference("Comments").child(post.getPostId())
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            holder.commentPreviewLayout.setVisibility(View.VISIBLE);
                            for (DataSnapshot data : snapshot.getChildren()) {
                                Comment comment = data.getValue(Comment.class);
                                if (comment != null) {
                                    // Fetch username for the comment
                                    FirebaseDatabase.getInstance().getReference("users").child(comment.getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                    if (userSnapshot.exists()) {
                                                        String name = userSnapshot.child("name").getValue(String.class);
                                                        holder.tvTopComment.setText(name + ": " + comment.getText());
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
                                }
                            }
                        } else {
                            holder.commentPreviewLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Load Post Image
        if (post.getPoster() != null && !post.getPoster().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getPoster()).into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Fetch User Info
        FirebaseDatabase.getInstance().getReference("users").child(post.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String profile = snapshot.child("avtar").getValue(String.class);

                            holder.tvUsername.setText(name);
                            Glide.with(context)
                                    .load(profile)
                                    .placeholder(R.drawable.ic_person)
                                    .into(holder.ivUserProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        holder.ivUserProfile.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, UserDetailActivity.class);
            intent.putExtra("uid", post.getUid());
            context.startActivity(intent);
        });

        holder.tvUsername.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, UserDetailActivity.class);
            intent.putExtra("uid", post.getUid());
            context.startActivity(intent);
        });

        // Handle Follow System
        if (post.getUid().equals(currentUid)) {
            holder.btnFollow.setVisibility(View.GONE);
        } else {
            holder.btnFollow.setVisibility(View.VISIBLE);
            
            // Fetch User Stats (Followers count)
            FirebaseDatabase.getInstance().getReference("Follow")
                    .child(post.getUid()).child("followers")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            holder.tvUserFollowers.setText(snapshot.getChildrenCount() + " followers");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

            DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                    .child(currentUid).child("following");
            
            followRef.child(post.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.btnFollow.setText("Following");
                        holder.btnFollow.setAlpha(0.6f);
                    } else {
                        holder.btnFollow.setText("Follow");
                        holder.btnFollow.setAlpha(1.0f);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            holder.btnFollow.setOnClickListener(v -> {
                AnimationHelper.animateFollowButton(holder.btnFollow, () -> {
                    DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                            .child(currentUid).child("following").child(post.getUid());
                    DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                            .child(post.getUid()).child("followers").child(currentUid);

                    followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                followingRef.removeValue();
                                followersRef.removeValue();
                            } else {
                                followingRef.setValue(true);
                                followersRef.setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile, ivPostImage, ivLikeIcon;
        TextView tvUsername, tvPostTime, tvPostContent, tvLikeCount, tvCommentCount, tvTopComment, tvUserFollowers;
        View btnLike, btnComment, btnMessage, commentPreviewLayout;
        android.widget.Button btnFollow;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivLikeIcon = itemView.findViewById(R.id.ivLikeIcon);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnMessage = itemView.findViewById(R.id.btnMessage);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvTopComment = itemView.findViewById(R.id.tvTopComment);
            commentPreviewLayout = itemView.findViewById(R.id.commentPreviewLayout);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            tvUserFollowers = itemView.findViewById(R.id.tvUserFollowers);
        }
    }
}

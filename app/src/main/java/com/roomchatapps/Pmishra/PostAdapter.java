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
import com.opensource.svgaplayer.SVGAImageView;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

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

        // Fetch User Info & Frame
        if (post.getUid() != null && !post.getUid().trim().isEmpty()) {
            UserProfileCache.getUserProfile(post.getUid(), profile -> {
                if (profile != null) {
                    holder.tvUsername.setText(profile.name != null ? profile.name : "User");
                    Glide.with(context)
                            .load(profile.avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(holder.ivUserProfile);
                    FrameUtils.displayFrame(context, profile.equippedFrame, holder.ivUserFrame, holder.svgaUserFrame);
                } else {
                    FrameUtils.clearFrame(holder.ivUserFrame, holder.svgaUserFrame);
                }
            });
        } else {
            FrameUtils.clearFrame(holder.ivUserFrame, holder.svgaUserFrame);
        }

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

        if (holder.btnGift != null) {
            holder.btnGift.setOnClickListener(v -> showPostGiftDialog(post, holder));
        }
    }

    private void showPostGiftDialog(Post post, PostViewHolder holder) {
        if (post == null || post.getUid() == null) return;
        if (currentUid != null && currentUid.equals(post.getUid())) {
            android.widget.Toast.makeText(context, "You cannot send a gift to yourself!", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_gift_store, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        RecyclerView rvGiftRecipients = dialogView.findViewById(R.id.rvGiftRecipients);
        RecyclerView rvGifts = dialogView.findViewById(R.id.rvGifts);
        View btnSendAction = dialogView.findViewById(R.id.btnSendGiftAction);
        TextView tvSendActionText = dialogView.findViewById(R.id.tvSendActionText);
        com.google.android.material.tabs.TabLayout tabCategory = dialogView.findViewById(R.id.tabCategoryGifts);
        TextView tvGiftDialogCoins = dialogView.findViewById(R.id.tvGiftDialogCoins);
        View llCoinBalance = dialogView.findViewById(R.id.llCoinBalance);

        if (tvGiftDialogCoins != null) {
            com.roomchatapps.Pmishra.utils.WalletManager.getUserCoins(currentUid, balance -> {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> tvGiftDialogCoins.setText(String.valueOf(balance)));
                }
            });
        }

        if (llCoinBalance != null) {
            llCoinBalance.setOnClickListener(v -> {
                dialog.dismiss();
                context.startActivity(new android.content.Intent(context, CoinRechargeActivity.class));
            });
        }

        if (rvGiftRecipients != null) {
            List<com.roomchatapps.Pmishra.models.GiftRecipientModel> recipientList = new java.util.ArrayList<>();
            UserProfileCache.getUserProfile(post.getUid(), profile -> {
                String authorName = (profile != null && profile.name != null) ? profile.name : "Post Author";
                String authorAvatar = (profile != null && profile.avatarUrl != null) ? profile.avatarUrl : "";
                String authorFrame = (profile != null) ? profile.equippedFrame : "";
                recipientList.add(new com.roomchatapps.Pmishra.models.GiftRecipientModel(post.getUid(), authorName, authorAvatar, authorFrame, false, true));

                com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter recipientAdapter = new com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter(recipientList);
                rvGiftRecipients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
                rvGiftRecipients.setAdapter(recipientAdapter);
            });
        }

        List<com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem> giftList = new java.util.ArrayList<>();
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Golden Tea", "gift/golden_tea.svga", R.drawable.gift_golden_tea, 10L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Doraemon Gift", "gift/doraemon_gift.svga", R.drawable.gift_doraemon, 100L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Birthday Cake", "gift/birthday_cake.svga", R.drawable.gift_baklava, 500L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Umbrella", "gift/umbrella.svga", R.drawable.gift_umbrella, 1000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Love Pure", "gift/love_pure.svga", R.drawable.gift_love, 2000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Love Gift Box", "gift/love_gift_box.svga", R.drawable.gift_love_gift_box, 5000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Makeup Box", "gift/makeup_box.svga", R.drawable.gift_makeup_box, 10000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Pearls Necklace", "gift/pearls_necklace.svga", R.drawable.gift_pearls_necklace, 20000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Love Proposal", "gift/love_proposal.svga", R.drawable.gift_love_proposal, 50000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Fantasy Castle", "gift/fantasy_castle.svga", R.drawable.gift_fantasy_castle, 100000L, "Gift"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Popcorn", "gift/popcorn.svga", R.drawable.gift_popcorn, 500L, "Lucky"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Gold Bar", "gift/gold_bar.svga", R.drawable.gift_gold_bar, 10000L, "Lucky"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Crystal Rose", "gift/crystal_rose.svga", R.drawable.gift_crystal_rose, 50000L, "Lucky"));
        giftList.add(new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem("Diamond Ring", "gift/diamond_ring_gift.svga", R.drawable.gift_golden_rings, 1500000L, "Relationship"));

        final int[] selectedQuantity = new int[]{1};
        TextView chipQty1 = dialogView.findViewById(R.id.chipQty1);
        TextView chipQty7 = dialogView.findViewById(R.id.chipQty7);
        TextView chipQty77 = dialogView.findViewById(R.id.chipQty77);
        TextView chipQty777 = dialogView.findViewById(R.id.chipQty777);

        View.OnClickListener qtyClickListener = v -> {
            if (chipQty1 != null) chipQty1.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty7 != null) chipQty7.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty77 != null) chipQty77.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty777 != null) chipQty777.setBackgroundResource(R.drawable.bg_room_chat_pill);

            v.setBackgroundResource(R.drawable.bg_send_button_glow);
            int qty = 1;
            if (v.getId() == R.id.chipQty7) qty = 7;
            else if (v.getId() == R.id.chipQty77) qty = 77;
            else if (v.getId() == R.id.chipQty777) qty = 777;

            selectedQuantity[0] = qty;
            if (tvSendActionText != null) {
                tvSendActionText.setText(qty > 1 ? "Send (x" + qty + ")" : "Send");
            }
        };

        if (chipQty1 != null) chipQty1.setOnClickListener(qtyClickListener);
        if (chipQty7 != null) chipQty7.setOnClickListener(qtyClickListener);
        if (chipQty77 != null) chipQty77.setOnClickListener(qtyClickListener);
        if (chipQty777 != null) chipQty777.setOnClickListener(qtyClickListener);

        if (rvGifts != null) {
            rvGifts.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(context, 4));
            com.roomchatapps.Pmishra.adapters.GiftStoreAdapter adapter = new com.roomchatapps.Pmishra.adapters.GiftStoreAdapter(giftList);
            rvGifts.setAdapter(adapter);

            if (tabCategory != null) {
                String[] categories = new String[]{"Gift", "Lucky", "Relationship"};
                tabCategory.removeAllTabs();
                for (String cat : categories) {
                    tabCategory.addTab(tabCategory.newTab().setText(cat));
                }
                tabCategory.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                        String selected = tab.getText() != null ? tab.getText().toString() : "Gift";
                        List<com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem> filtered = new java.util.ArrayList<>();
                        for (com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem item : giftList) {
                            if (selected.equalsIgnoreCase(item.category)) {
                                filtered.add(item);
                            }
                        }
                        adapter.updateItems(filtered.isEmpty() ? giftList : filtered);
                    }

                    @Override
                    public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
                });
            }

            if (btnSendAction != null) {
                btnSendAction.setOnClickListener(v -> {
                    com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem selectedItem = adapter.getSelectedGift();
                    if (selectedItem == null) {
                        android.widget.Toast.makeText(context, "Please select a gift first", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    btnSendAction.setEnabled(false);
                    long totalCost = selectedItem.cost * Math.max(1, selectedQuantity[0]);

                    com.roomchatapps.Pmishra.utils.WalletManager.spendCoinsForGift(currentUid, post.getUid(), totalCost, "Post Gift: " + selectedItem.name, new com.roomchatapps.Pmishra.utils.WalletManager.WalletCallback() {
                        @Override
                        public void onSuccess(String message, long newCoinBalance) {
                            btnSendAction.setEnabled(true);
                            dialog.dismiss();

                            if (holder != null && holder.btnGift != null) {
                                ReactionAnimator.animateLikeButtonClick(holder.btnGift, holder.btnGift);
                                if (holder.itemView.getRootView() instanceof ViewGroup) {
                                    ReactionAnimator.spawnFloatingReaction((ViewGroup) holder.itemView.getRootView(), holder.btnGift, R.drawable.room_gift_ic);
                                }
                            }

                            com.roomchatapps.Pmishra.utils.NotificationHelper.sendGiftNotification(post.getUid(), selectedItem.name);

                            android.widget.Toast.makeText(context, "✨ Sent " + selectedItem.name + " to post author!", android.widget.Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String error) {
                            btnSendAction.setEnabled(true);
                            android.widget.Toast.makeText(context, "Failed: " + error, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        }

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile, ivUserFrame, ivPostImage, ivLikeIcon, btnGift;
        SVGAImageView svgaUserFrame;
        TextView tvUsername, tvPostTime, tvPostContent, tvLikeCount, tvCommentCount, tvTopComment, tvUserFollowers;
        View btnLike, btnComment, btnMessage, commentPreviewLayout;
        android.widget.Button btnFollow;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            ivUserFrame = itemView.findViewById(R.id.ivUserFrame);
            svgaUserFrame = itemView.findViewById(R.id.svgaUserFrame);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivLikeIcon = itemView.findViewById(R.id.ivLikeIcon);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnMessage = itemView.findViewById(R.id.btnMessage);
            btnGift = itemView.findViewById(R.id.btnGift);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvTopComment = itemView.findViewById(R.id.tvTopComment);
            commentPreviewLayout = itemView.findViewById(R.id.commentPreviewLayout);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            tvUserFollowers = itemView.findViewById(R.id.tvUserFollowers);
        }
    }
}

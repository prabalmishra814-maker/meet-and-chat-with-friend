package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.FragmentExploreBinding;
import com.roomchatapps.Pmishra.databinding.ItemCategoryPillBinding;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private List<Post> postList;
    private final List<Post> allRawPosts = new ArrayList<>();
    private PostAdapter postAdapter;
    private DatabaseReference postsRef;
    private ValueEventListener postsListener;
    private boolean isFollowingTab = false;
    private List<String> followingList = new ArrayList<>();
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        // Entrance animations
        AnimationHelper.fadeIn(binding.tabsLayout, 600);
        AnimationHelper.slideUp(binding.filterLayout, 700);
        AnimationHelper.popIn(binding.fabPost);
        
        setupTabs();
        setupCategories();
        setupPosts();

        if (binding.btnSearch != null) {
            binding.btnSearch.setOnClickListener(v -> {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), SearchActivity.class);
                    startActivity(intent);
                }
            });
        }

        binding.fabPost.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), PostCreateActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupTabs() {
        binding.tabSquare.setOnClickListener(v -> {
            isFollowingTab = false;
            updateTabUI();
            setupPosts();
        });

        binding.tabFollow.setOnClickListener(v -> {
            isFollowingTab = true;
            updateTabUI();
            fetchFollowingListAndLoadPosts();
        });
    }

    private void updateTabUI() {
        if (isFollowingTab) {
            binding.activeIndicatorSquare.setVisibility(View.INVISIBLE);
            binding.activeIndicatorFollow.setVisibility(View.VISIBLE);
            binding.tvSquareTab.setAlpha(0.5f);
            binding.tvFollowTab.setAlpha(1.0f);
        } else {
            binding.activeIndicatorSquare.setVisibility(View.VISIBLE);
            binding.activeIndicatorFollow.setVisibility(View.INVISIBLE);
            binding.tvSquareTab.setAlpha(1.0f);
            binding.tvFollowTab.setAlpha(0.5f);
        }
    }

    private void fetchFollowingListAndLoadPosts() {
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUid).child("following")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded() || getContext() == null || binding == null) return;
                        followingList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            followingList.add(ds.getKey());
                        }
                        setupPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public static class CategoryItem {
        public String name;
        public int iconRes;

        public CategoryItem(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }

    private void setupCategories() {
        if (getContext() == null || binding == null) return;
        List<CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryItem("All", R.drawable.ic_search));
        categories.add(new CategoryItem("India", R.drawable.room_gift_ic));
        categories.add(new CategoryItem("Saudi", R.drawable.ic_become_vip));
        categories.add(new CategoryItem("Pakistan", R.drawable.room_gift_ic));
        categories.add(new CategoryItem("UAE", R.drawable.ic_become_vip));
        categories.add(new CategoryItem("Gaming", R.drawable.game_mic_charm_pk_diamond_ic));
        categories.add(new CategoryItem("Music", R.drawable.app_tab_message_selected_ic));

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(new CategoryAdapter(categories));
    }

    private void setupPosts() {
        if (getContext() == null || binding == null) return;

        if (postList == null) {
            postList = new ArrayList<>();
            postAdapter = new PostAdapter(getContext(), postList);
            binding.rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvPosts.setAdapter(postAdapter);
        }

        if (postsListener == null) {
            postsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded() || getContext() == null || binding == null) return;

                    allRawPosts.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Post post = data.getValue(Post.class);
                        if (post != null) {
                            if (post.getPostId() == null) {
                                post.setPostId(data.getKey());
                            }
                            allRawPosts.add(0, post); // Latest posts first
                        }
                    }
                    filterAndDisplayPosts();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            postsRef.addValueEventListener(postsListener);
        } else {
            filterAndDisplayPosts();
        }
    }

    private void filterAndDisplayPosts() {
        if (binding == null || postList == null) return;

        postList.clear();
        List<Post> matched = new ArrayList<>();

        for (Post post : allRawPosts) {
            if (post == null) continue;

            if (isFollowingTab) {
                if (followingList.contains(post.getUid())) {
                    matched.add(post);
                }
            } else {
                matched.add(post);
            }
        }

        if (!"All".equalsIgnoreCase(selectedCategory)) {
            List<Post> categoryMatched = new ArrayList<>();
            String cat = selectedCategory.toLowerCase();
            for (Post p : matched) {
                String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
                String user = p.getUsername() != null ? p.getUsername().toLowerCase() : "";
                if (title.contains(cat) || user.contains(cat)) {
                    categoryMatched.add(p);
                }
            }
            // If specific category posts found, display them; otherwise show all matched posts so feed is never blank
            if (!categoryMatched.isEmpty()) {
                postList.addAll(categoryMatched);
            } else {
                postList.addAll(matched);
            }
        } else {
            postList.addAll(matched);
        }

        if (postAdapter != null) {
            postAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postsRef != null && postsListener != null) {
            postsRef.removeEventListener(postsListener);
            postsListener = null;
        }
        binding = null;
    }

    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private final List<CategoryItem> items;
        private int selectedPos = 0;

        CategoryAdapter(List<CategoryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCategoryPillBinding b = ItemCategoryPillBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryItem item = items.get(position);

            // Item Entrance Animation
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationX(20f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setStartDelay(position * 50L)
                    .start();

            holder.binding.tvCategoryName.setText(item.name);
            holder.binding.ivCategoryIcon.setImageResource(item.iconRes);

            if (position == selectedPos) {
                holder.binding.cardCategory.setCardBackgroundColor(Color.parseColor("#40E0D0"));
                holder.binding.cardCategory.setStrokeColor(Color.parseColor("#40E0D0"));
                holder.binding.tvCategoryName.setTextColor(Color.parseColor("#050E1E"));
                holder.binding.ivCategoryIcon.setColorFilter(Color.parseColor("#050E1E"));
            } else {
                holder.binding.cardCategory.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"));
                holder.binding.cardCategory.setStrokeColor(Color.parseColor("#25FFFFFF"));
                holder.binding.tvCategoryName.setTextColor(Color.parseColor("#B3FFFFFF"));
                holder.binding.ivCategoryIcon.setColorFilter(Color.parseColor("#B3FFFFFF"));
            }

            holder.itemView.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(holder.itemView);
                int oldPos = selectedPos;
                selectedPos = holder.getBindingAdapterPosition();
                if (oldPos != -1) notifyItemChanged(oldPos);
                if (selectedPos != -1) notifyItemChanged(selectedPos);
                selectedCategory = item.name;
                filterAndDisplayPosts();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemCategoryPillBinding binding;
            ViewHolder(ItemCategoryPillBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

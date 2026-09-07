package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.databinding.LayoutCommentBottomSheetBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private String postId;
    private LayoutCommentBottomSheetBinding binding;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private DatabaseReference commentsRef;

    public static CommentBottomSheet newInstance(String postId) {
        CommentBottomSheet fragment = new CommentBottomSheet();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutCommentBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Content animation
        binding.getRoot().setAlpha(0f);
        binding.getRoot().animate().alpha(1f).setDuration(400).start();
        
        binding.rvComments.setAlpha(0f);
        binding.rvComments.setTranslationY(40f);
        binding.rvComments.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();

        commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        
        setupRecyclerView();
        loadComments();

        binding.btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(requireContext(), commentList);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvComments.setAdapter(commentAdapter);
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Comment comment = data.getValue(Comment.class);
                    if (comment != null) {
                        if (comment.getCommentId() == null) {
                            comment.setCommentId(data.getKey());
                        }
                        commentList.add(0, comment); // Add newest comments at the top
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendComment() {
        String text = binding.etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = commentsRef.push().getKey();
        String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        Comment comment = new Comment(commentId, postId, uid, text, timestamp);
        if (commentId != null) {
            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    binding.etComment.setText("");
                    // Update comment count in Post
                    FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long count = 0;
                            if (snapshot.exists()) {
                                count = (long) snapshot.getValue();
                            }
                            snapshot.getRef().setValue(count + 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

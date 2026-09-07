package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsActivity extends AppCompatActivity {

    private String postId;
    private RecyclerView rvComments;
    private EditText etComment;
    private ImageView btnSendComment, btnBack;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private DatabaseReference commentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // We might not want padding on the whole content for EdgeToEdge
                // But we should ensure bottom navigation is not covered
                return insets;
            });
        }

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            finish();
            return;
        }

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        btnBack = findViewById(R.id.btnBack);

        commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        
        btnBack.setOnClickListener(v -> finish());
        
        setupRecyclerView();
        loadComments();

        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
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
                        commentList.add(0, comment); // Newest at top
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendComment() {
        String text = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = commentsRef.push().getKey();
        String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        Comment comment = new Comment(commentId, postId, uid, text, timestamp);
        if (commentId != null) {
            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    etComment.setText("");
                    
                    // Fetch publisher UID to send notification
                    FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("publisher")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String publisher = snapshot.getValue(String.class);
                                        if (publisher != null) {
                                            com.roomchatapps.Pmishra.utils.NotificationHelper.sendCommentNotification(publisher, postId);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });

                    // Update comment count in Post
                    FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long count = 0;
                            if (snapshot.exists() && snapshot.getValue() != null) {
                                try {
                                    count = (long) snapshot.getValue();
                                } catch (Exception e) {
                                    // Handle cases where it might be stored as String or Integer
                                    count = Long.parseLong(String.valueOf(snapshot.getValue()));
                                }
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
}

package com.roomchatapps.Pmishra;

public class Comment {
    private String commentId, postId, uid, text, timestamp;

    public Comment() {
    }

    public Comment(String commentId, String postId, String uid, String text, String timestamp) {
        this.commentId = commentId;
        this.postId = postId;
        this.uid = uid;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

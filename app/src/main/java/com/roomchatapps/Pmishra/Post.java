package com.roomchatapps.Pmishra;

public class Post {
    private String title, poster, datetime, uid, postId;
    private String username, userProfile; // Derived from Users node usually, but keeping here for simplicity if needed
    private long commentCount;
    private java.util.Map<String, Boolean> likes = new java.util.HashMap<>();

    public Post() {
        // Required for Firebase
    }

    public Post(String title, String poster, String datetime, String uid, String postId) {
        this.title = title;
        this.poster = poster;
        this.datetime = datetime;
        this.uid = uid;
        this.postId = postId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getDatetime() { return datetime; }
    public void setDatetime(String datetime) { this.datetime = datetime; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserProfile() { return userProfile; }
    public void setUserProfile(String userProfile) { this.userProfile = userProfile; }

    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }

    public java.util.Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(java.util.Map<String, Boolean> likes) { this.likes = likes; }
}

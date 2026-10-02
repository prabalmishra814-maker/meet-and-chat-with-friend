package com.roomchatapps.Pmishra.adapters;

// ROOM ADMIN FIX
public class AdminMemberItem {
    private final String userId;
    private final String userName;
    private final String userAvatar;
    private String role; // "host", "admin", "member"

    public AdminMemberItem(String userId, String userName, String userAvatar, String role) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.role = role != null ? role.toLowerCase() : "member";
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role != null ? role.toLowerCase() : "member"; }
    public boolean isHost() { return "host".equalsIgnoreCase(role); }
    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
}

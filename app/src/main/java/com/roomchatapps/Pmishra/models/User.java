package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class User {
    private String userId;
    private String profileId;
    private String userName;
    private String userIcon;
    private boolean isHost;
    private boolean isSpeaker;
    private boolean isMicOn;
    
    @Exclude
    private boolean isSpeaking;

    public User() {}

    public User(String userId, String userName, String userIcon, boolean isHost) {
        this.userId = userId;
        this.userName = userName;
        this.userIcon = userIcon;
        this.isHost = isHost;
        this.isSpeaker = isHost;
        this.isMicOn = isHost;
        this.isSpeaking = false;
    }

    @PropertyName("uid")
    public String getUserId() { return userId; }
    @PropertyName("uid")
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("profileId")
    public String getProfileId() { return profileId; }
    @PropertyName("profileId")
    public void setProfileId(String profileId) { this.profileId = profileId; }

    @PropertyName("name")
    public String getUserName() { return userName; }
    @PropertyName("name")
    public void setUserName(String userName) { this.userName = userName; }

    @PropertyName("avtar")
    public String getUserIcon() { return userIcon; }
    @PropertyName("avtar")
    public void setUserIcon(String userIcon) { this.userIcon = userIcon; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }

    public boolean isSpeaker() { return isSpeaker; }
    public void setSpeaker(boolean speaker) { isSpeaker = speaker; }

    public boolean isMicOn() { return isMicOn; }
    public void setMicOn(boolean micOn) { isMicOn = micOn; }

    @Exclude
    public boolean isSpeaking() { return isSpeaking; }
    @Exclude
    public void setSpeaking(boolean speaking) { isSpeaking = speaking; }
}

package com.roomchatapps.Pmishra.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_ID = "profileId";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_LOGIN_TYPE = "loginType";

    private static SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void createLoginSession(String uid, String name, String email, String profileId, String avatar, String loginType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_NAME, name != null ? name : "User");
        editor.putString(KEY_EMAIL, email != null ? email : "");
        editor.putString(KEY_PROFILE_ID, profileId != null ? profileId : "");
        editor.putString(KEY_AVATAR, avatar != null ? avatar : "");
        editor.putString(KEY_LOGIN_TYPE, loginType != null ? loginType : "email");
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_UID, pref.getString(KEY_UID, null));
        user.put(KEY_NAME, pref.getString(KEY_NAME, "User"));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        user.put(KEY_PROFILE_ID, pref.getString(KEY_PROFILE_ID, ""));
        user.put(KEY_AVATAR, pref.getString(KEY_AVATAR, ""));
        user.put(KEY_LOGIN_TYPE, pref.getString(KEY_LOGIN_TYPE, "email"));
        return user;
    }

    public String getUid() {
        return pref.getString(KEY_UID, null);
    }

    public String getName() {
        return pref.getString(KEY_NAME, "User");
    }

    public String getUserName() {
        return getName();
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getProfileId() {
        return pref.getString(KEY_PROFILE_ID, "");
    }

    public String getAvatar() {
        return pref.getString(KEY_AVATAR, "");
    }

    public String getLoginType() {
        return pref.getString(KEY_LOGIN_TYPE, "email");
    }

    public void updateUserProfile(String name, String avatar) {
        if (name != null) editor.putString(KEY_NAME, name);
        if (avatar != null) editor.putString(KEY_AVATAR, avatar);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}

package com.roomchatapps.Pmishra.zego;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SeatManager {
    private static final String TAG = "SeatManager";
    public static final int TOTAL_SEATS = 16;
    private static SeatManager instance;

    private final List<SeatModel> seatList = new ArrayList<>();
    private final List<SeatListener> listeners = new ArrayList<>();
    private String currentRoomID = "";

    public interface SeatListener {
        void onSeatsUpdated(List<SeatModel> seats);
    }

    private SeatManager() {
        resetSeats();
    }

    public static synchronized SeatManager getInstance() {
        if (instance == null) {
            instance = new SeatManager();
        }
        return instance;
    }

    public void setCurrentRoomID(String roomID) {
        this.currentRoomID = roomID != null ? roomID : "";
    }

    private int totalSeats = 16;

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int count) {
        if (count != 8 && count != 16 && count != 24) return;
        this.totalSeats = count;

        if (seatList.size() < count) {
            while (seatList.size() < count) {
                seatList.add(new SeatModel(seatList.size()));
            }
        } else if (seatList.size() > count) {
            while (seatList.size() > count) {
                seatList.remove(seatList.size() - 1);
            }
        }

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public void resetSeats() {
        seatList.clear();
        for (int i = 0; i < totalSeats; i++) {
            seatList.add(new SeatModel(i));
        }
    }

    public List<SeatModel> getSeats() {
        return new ArrayList<>(seatList);
    }

    public int findUserSeatIndex(String userID) {
        if (userID == null || userID.trim().isEmpty()) return -1;
        for (int i = 0; i < seatList.size(); i++) {
            if (userID.equals(seatList.get(i).userID)) {
                return i;
            }
        }
        return -1;
    }

    private String hostUserID = "";

    public void setHostUserID(String hostUid) {
        this.hostUserID = hostUid != null ? hostUid.trim() : "";
    }

    public String getHostUserID() {
        return hostUserID;
    }

    public boolean takeSeat(int index, String userID, String userName) {
        return takeSeat(index, userID, userName, "", "");
    }

    public boolean takeSeat(int index, String userID, String userName, String avatar) {
        return takeSeat(index, userID, userName, avatar, "");
    }

    public boolean takeSeat(int index, String userID, String userName, String avatar, String equippedFrame) {
        if (index < 0 || index >= totalSeats) return false;

        SeatModel model = seatList.get(index);
        if (model.isClosed) return false;

        // Seat 0 Security: Strictly reserved for Room Host ONLY
        if (index == 0 && hostUserID != null && !hostUserID.isEmpty() && !hostUserID.equals(userID)) {
            return false;
        }

        // Prevent 2 users on 1 seat: Check if seat is already occupied by another user
        if (!model.isEmpty() && !userID.equals(model.userID)) {
            return false;
        }

        // Leave any existing seat first & preserve equipped frame/avatar if seat switching
        String previousFrame = "";
        String previousAvatar = "";
        int existingIndex = findUserSeatIndex(userID);
        if (existingIndex != -1 && existingIndex != index) {
            SeatModel oldSeat = seatList.get(existingIndex);
            previousFrame = oldSeat.equippedFrame;
            previousAvatar = oldSeat.userAvatar;
            oldSeat.clear();
        }

        model.userID = userID;
        model.userName = userName;

        // Resolve Avatar
        if (avatar != null && !avatar.trim().isEmpty()) {
            model.userAvatar = avatar;
        } else if (previousAvatar != null && !previousAvatar.trim().isEmpty()) {
            model.userAvatar = previousAvatar;
        } else {
            UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(userID);
            if (cached != null && cached.avatarUrl != null) {
                model.userAvatar = cached.avatarUrl;
            }
        }

        // Resolve Equipped Frame
        String finalFrame = equippedFrame;
        if (finalFrame == null || finalFrame.trim().isEmpty()) {
            finalFrame = previousFrame;
        }
        if (finalFrame == null || finalFrame.trim().isEmpty()) {
            UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(userID);
            if (cached != null && cached.equippedFrame != null) {
                finalFrame = cached.equippedFrame;
            }
        }

        model.equippedFrame = finalFrame != null ? finalFrame : "";
        model.isMicOn = true;
        model.isMuted = false;

        Runnable doSync = () -> {
            notifySeatsUpdated();
            syncSeatsToExtraInfo();
            syncSeatsToFirebase();
        };

        if (model.equippedFrame.isEmpty()) {
            UserProfileCache.getUserProfile(userID, profile -> {
                if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                    model.equippedFrame = profile.equippedFrame;
                }
                doSync.run();
            });
        } else {
            doSync.run();
        }

        return true;
    }

    public void leaveSeat(int index) {
        if (index < 0 || index >= totalSeats) return;

        seatList.get(index).clear();
        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public void muteSeat(int index, boolean isMuted) {
        if (index < 0 || index >= totalSeats) return;

        SeatModel model = seatList.get(index);
        model.isMuted = isMuted;
        if (isMuted) {
            model.isMicOn = false;
        }

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public void closeSeat(int index, boolean isClosed) {
        if (index < 0 || index >= totalSeats) return;

        SeatModel model = seatList.get(index);
        model.isClosed = isClosed;
        if (isClosed) {
            model.clear();
            model.isClosed = true;
        }

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public void kickUser(int index) {
        leaveSeat(index);
    }

    public void updateMicStatus(int index, boolean isMicOn) {
        if (index < 0 || index >= totalSeats) return;

        SeatModel model = seatList.get(index);
        if (model.isMuted && isMicOn) {
            // Cannot unmute if host muted
            return;
        }
        model.isMicOn = isMicOn;

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public void setSpeaking(String userID, boolean isSpeaking, float soundLevel) {
        int index = findUserSeatIndex(userID);
        if (index != -1) {
            SeatModel model = seatList.get(index);
            if (model.isSpeaking != isSpeaking || Math.abs(model.soundLevel - soundLevel) > 1f) {
                model.isSpeaking = isSpeaking;
                model.soundLevel = soundLevel;
                notifySeatsUpdated();
            }
        }
    }

    public void addListener(SeatListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SeatListener listener) {
        listeners.remove(listener);
    }

    private void notifySeatsUpdated() {
        List<SeatModel> snapshot = getSeats();
        for (SeatListener listener : listeners) {
            listener.onSeatsUpdated(snapshot);
        }
    }

    public void setSeatsFromExternal(List<SeatModel> externalSeats) {
        if (externalSeats == null || externalSeats.isEmpty()) return;
        for (SeatModel external : externalSeats) {
            if (external != null && external.index >= 0 && external.index < TOTAL_SEATS) {
                // Security enforcement: Seat 0 is strictly reserved for Room Host ONLY
                if (external.index == 0 && hostUserID != null && !hostUserID.isEmpty() && !external.userID.equals(hostUserID)) {
                    external.clear();
                }
                SeatModel local = seatList.get(external.index);
                String newUserId = external.userID != null ? external.userID : "";

                String newFrame = external.equippedFrame != null ? external.equippedFrame : "";
                if (newFrame.trim().isEmpty() && newUserId.equals(local.userID) && local.equippedFrame != null && !local.equippedFrame.trim().isEmpty()) {
                    newFrame = local.equippedFrame;
                }

                local.userID = newUserId;
                local.userName = external.userName != null ? external.userName : "";
                local.userAvatar = external.userAvatar != null ? external.userAvatar : "";
                local.equippedFrame = newFrame;
                local.isMicOn = external.isMicOn;
                local.isMuted = external.isMuted;
                local.isClosed = external.isClosed;

                if (!local.isEmpty() && (local.equippedFrame == null || local.equippedFrame.trim().isEmpty())) {
                    UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(local.userID);
                    if (cached != null && cached.equippedFrame != null && !cached.equippedFrame.trim().isEmpty()) {
                        local.equippedFrame = cached.equippedFrame;
                    } else {
                        final SeatModel targetLocal = local;
                        UserProfileCache.getUserProfile(local.userID, profile -> {
                            if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                                targetLocal.equippedFrame = profile.equippedFrame;
                                notifySeatsUpdated();
                            }
                        });
                    }
                }
            }
        }
        notifySeatsUpdated();
    }

    public void updateSeatsFromExtraInfo(List<ZegoRoomExtraInfo> extraInfoList) {
        if (extraInfoList == null) return;
        for (ZegoRoomExtraInfo info : extraInfoList) {
            if ("seats".equals(info.key)) {
                parseSeatsJson(info.value);
                break;
            }
        }
    }

    private void syncSeatsToExtraInfo() {
        try {
            JSONArray array = new JSONArray();
            for (SeatModel seat : seatList) {
                JSONObject obj = new JSONObject();
                obj.put("index", seat.index);
                obj.put("userID", seat.userID);
                obj.put("userName", seat.userName);
                obj.put("userAvatar", seat.userAvatar);
                obj.put("equippedFrame", seat.equippedFrame != null ? seat.equippedFrame : "");
                obj.put("isMicOn", seat.isMicOn);
                obj.put("isMuted", seat.isMuted);
                obj.put("isClosed", seat.isClosed);
                array.put(obj);
            }
            ZegoManager.getInstance().setRoomExtraInfo("seats", array.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error serializing seats JSON", e);
        }
    }

    public void syncSeatsToFirebase() {
        if (currentRoomID == null || currentRoomID.trim().isEmpty()) return;
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("room_seats").child(currentRoomID);
            ref.setValue(seatList);
        } catch (Exception e) {
            Log.e(TAG, "Error syncing seats to Firebase", e);
        }
    }

    private void parseSeatsJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length() && i < TOTAL_SEATS; i++) {
                JSONObject obj = array.getJSONObject(i);
                int index = obj.optInt("index", i);
                if (index < 0 || index >= TOTAL_SEATS) continue;

                SeatModel model = seatList.get(index);
                String parsedUserId = obj.optString("userID", "");
                // Security enforcement: Seat 0 is strictly reserved for Room Host ONLY
                if (index == 0 && hostUserID != null && !hostUserID.isEmpty() && !parsedUserId.equals(hostUserID)) {
                    model.clear();
                } else {
                    String parsedFrame = obj.optString("equippedFrame", "");
                    if (parsedFrame.trim().isEmpty() && parsedUserId.equals(model.userID) && model.equippedFrame != null && !model.equippedFrame.trim().isEmpty()) {
                        parsedFrame = model.equippedFrame;
                    }

                    model.userID = parsedUserId;
                    model.userName = obj.optString("userName", "");
                    model.userAvatar = obj.optString("userAvatar", "");
                    model.equippedFrame = parsedFrame;
                    model.isMicOn = obj.optBoolean("isMicOn", true);
                    model.isMuted = obj.optBoolean("isMuted", false);
                    model.isClosed = obj.optBoolean("isClosed", false);

                    if (!model.isEmpty() && (model.equippedFrame == null || model.equippedFrame.trim().isEmpty())) {
                        UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(model.userID);
                        if (cached != null && cached.equippedFrame != null && !cached.equippedFrame.trim().isEmpty()) {
                            model.equippedFrame = cached.equippedFrame;
                        } else {
                            final SeatModel targetModel = model;
                            UserProfileCache.getUserProfile(model.userID, profile -> {
                                if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                                    targetModel.equippedFrame = profile.equippedFrame;
                                    notifySeatsUpdated();
                                }
                            });
                        }
                    }
                }
            }
            notifySeatsUpdated();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing seats JSON", e);
        }
    }
}

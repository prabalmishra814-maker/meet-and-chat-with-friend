package com.roomchatapps.Pmishra.zego;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roomchatapps.Pmishra.utils.UserProfileCache;

import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatManager {
    private static final String TAG = "SeatManager";
    // SEAT SYSTEM FIX
    public static final int TOTAL_SEATS = 17;
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

    // SEAT SYSTEM FIX
    private int totalSeats = 17;

    public int getTotalSeats() {
        return totalSeats;
    }

    // SEAT SYSTEM FIX
    public synchronized void setTotalSeats(int count) {
        int validCount = count;
        if (validCount != 9 && validCount != 17 && validCount != 21) {
            if (validCount <= 12) {
                validCount = 9;
            } else if (validCount <= 19) {
                validCount = 17;
            } else {
                validCount = 21;
            }
        }
        this.totalSeats = validCount;

        if (seatList.size() < validCount) {
            while (seatList.size() < validCount) {
                seatList.add(new SeatModel(seatList.size()));
            }
        } else if (seatList.size() > validCount) {
            while (seatList.size() > validCount) {
                seatList.remove(seatList.size() - 1);
            }
        }

        sanitizeDuplicateUsers();
        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public synchronized void resetSeats() {
        seatList.clear();
        for (int i = 0; i < totalSeats; i++) {
            seatList.add(new SeatModel(i));
        }
    }

    public synchronized List<SeatModel> getSeats() {
        List<SeatModel> copy = new ArrayList<>(seatList.size());
        for (SeatModel model : seatList) {
            copy.add(new SeatModel(model));
        }
        return copy;
    }

    public synchronized int findUserSeatIndex(String userID) {
        if (userID == null || userID.trim().isEmpty()) return -1;
        String uid = userID.trim();
        for (int i = 0; i < seatList.size(); i++) {
            if (uid.equals(seatList.get(i).userID)) {
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

    /**
     * Sanitizes seatList so no user ID appears on more than one seat simultaneously.
     */
    private void sanitizeDuplicateUsers() {
        Set<String> seenUsers = new HashSet<>();
        for (SeatModel seat : seatList) {
            if (!seat.isEmpty()) {
                if (seenUsers.contains(seat.userID)) {
                    // Duplicate occupant detected - clear duplicate seat!
                    seat.clear();
                } else {
                    seenUsers.add(seat.userID);
                }
            }
        }
    }

    public boolean takeSeat(int index, String userID, String userName) {
        return takeSeat(index, userID, userName, "", "");
    }

    public boolean takeSeat(int index, String userID, String userName, String avatar) {
        return takeSeat(index, userID, userName, avatar, "", false);
    }

    public boolean takeSeat(int index, String userID, String userName, String avatar, String equippedFrame) {
        return takeSeat(index, userID, userName, avatar, equippedFrame, false);
    }

    public boolean takeSeat(int index, String userID, String userName, String avatar, String equippedFrame, boolean allowLockedSeat) {
        if (index < 0 || index >= totalSeats) return false;

        String uid = userID != null ? userID.trim() : "";
        if (uid.isEmpty()) return false;

        synchronized (this) {
            SeatModel model = seatList.get(index);
            if (model.isClosed && !allowLockedSeat) return false;

            // Seat 0 Security: Strictly reserved for Room Host ONLY
            if (index == 0 && hostUserID != null && !hostUserID.isEmpty() && !hostUserID.equals(uid)) {
                return false;
            }

            // Prevent 2 users on 1 seat: Check if seat is already occupied by another user
            if (!model.isEmpty() && !uid.equals(model.userID)) {
                return false;
            }

            if (model.isClosed && allowLockedSeat) {
                model.isClosed = false; // Unlock seat for invited user
            }

            // Leave any existing seat first & preserve equipped frame/avatar/mic state if seat switching
            String previousFrame = "";
            String previousAvatar = "";
            boolean hasPreviousSeat = false;
            boolean previousMicOn = true;
            for (int i = 0; i < totalSeats; i++) {
                SeatModel seat = seatList.get(i);
                if (uid.equals(seat.userID) && i != index) {
                    if (previousFrame.isEmpty()) previousFrame = seat.equippedFrame;
                    if (previousAvatar.isEmpty()) previousAvatar = seat.userAvatar;
                    previousMicOn = seat.isMicOn;
                    hasPreviousSeat = true;
                    seat.clear();
                }
            }

            model.userID = uid;
            model.userName = userName != null ? userName : "";

            // Resolve Avatar
            if (avatar != null && !avatar.trim().isEmpty()) {
                model.userAvatar = avatar;
            } else if (!previousAvatar.isEmpty()) {
                model.userAvatar = previousAvatar;
            } else {
                UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(uid);
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
                UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(uid);
                if (cached != null && cached.equippedFrame != null) {
                    finalFrame = cached.equippedFrame;
                }
            }

            model.equippedFrame = finalFrame != null ? finalFrame : "";
            if (hasPreviousSeat) {
                model.isMicOn = previousMicOn;
            } else {
                model.isMicOn = ZegoManager.getInstance().isMicEnabled();
            }
            model.isMuted = false;

            sanitizeDuplicateUsers();
        }

        Runnable doSync = () -> {
            notifySeatsUpdated();
            syncSeatsToExtraInfo();
            syncSeatsToFirebase();
        };

        final String targetUid = uid;
        final int targetIdx = index;

        SeatModel model;
        synchronized (this) {
            model = seatList.get(targetIdx);
        }

        if (model.equippedFrame.isEmpty()) {
            UserProfileCache.getUserProfile(targetUid, profile -> {
                if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                    synchronized (this) {
                        SeatModel currentModel = seatList.get(targetIdx);
                        if (targetUid.equals(currentModel.userID)) {
                            currentModel.equippedFrame = profile.equippedFrame;
                        }
                    }
                }
                doSync.run();
            });
        } else {
            doSync.run();
        }

        return true;
    }

    public synchronized void leaveSeat(int index) {
        if (index < 0 || index >= totalSeats) return;

        seatList.get(index).clear();
        sanitizeDuplicateUsers();
        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public synchronized void muteSeat(int index, boolean isMuted) {
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

    public synchronized void closeSeat(int index, boolean isClosed) {
        if (index < 0 || index >= totalSeats) return;

        SeatModel model = seatList.get(index);
        model.isClosed = isClosed;
        if (isClosed) {
            model.clear();
            model.isClosed = true;
        }

        sanitizeDuplicateUsers();
        notifySeatsUpdated();
        syncSeatsToExtraInfo();
        syncSeatsToFirebase();
    }

    public void kickUser(int index) {
        leaveSeat(index);
    }

    public synchronized void updateMicStatus(int index, boolean isMicOn) {
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

    public synchronized void setSpeaking(String userID, boolean isSpeaking, float soundLevel) {
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
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeListener(SeatListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifySeatsUpdated() {
        List<SeatModel> snapshot = getSeats();
        List<SeatListener> targets;
        synchronized (listeners) {
            targets = new ArrayList<>(listeners);
        }
        for (SeatListener listener : targets) {
            listener.onSeatsUpdated(snapshot);
        }
    }

    // SEAT LIMIT SYNC FIX
    public void setSeatsFromExternal(List<SeatModel> externalSeats) {
        if (externalSeats == null || externalSeats.isEmpty()) return;
        synchronized (this) {
            int externalCount = externalSeats.size();
            if (externalCount == 9 || externalCount == 17 || externalCount == 21) {
                if (this.totalSeats != externalCount) {
                    this.totalSeats = externalCount;
                    if (seatList.size() < externalCount) {
                        while (seatList.size() < externalCount) {
                            seatList.add(new SeatModel(seatList.size()));
                        }
                    } else if (seatList.size() > externalCount) {
                        while (seatList.size() > externalCount) {
                            seatList.remove(seatList.size() - 1);
                        }
                    }
                }
            }
            for (SeatModel external : externalSeats) {
                if (external != null && external.index >= 0 && external.index < totalSeats) {
                    // Security enforcement: Seat 0 is strictly reserved for Room Host ONLY
                    if (external.index == 0 && hostUserID != null && !hostUserID.isEmpty() && !external.userID.equals(hostUserID)) {
                        external.clear();
                    }
                    SeatModel local = seatList.get(external.index);
                    String newUserId = external.userID != null ? external.userID.trim() : "";

                    // If user moved to this seat from another seat, clear the other seat!
                    if (!newUserId.isEmpty()) {
                        for (int i = 0; i < totalSeats; i++) {
                            if (i != external.index && newUserId.equals(seatList.get(i).userID)) {
                                seatList.get(i).clear();
                            }
                        }
                    }

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
                            final String targetUid = local.userID;
                            UserProfileCache.getUserProfile(local.userID, profile -> {
                                if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                                    synchronized (SeatManager.this) {
                                        if (targetUid.equals(targetLocal.userID)) {
                                            targetLocal.equippedFrame = profile.equippedFrame;
                                            notifySeatsUpdated();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
            sanitizeDuplicateUsers();
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
            List<SeatModel> currentList = getSeats();
            for (SeatModel seat : currentList) {
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
            ref.setValue(getSeats());
        } catch (Exception e) {
            Log.e(TAG, "Error syncing seats to Firebase", e);
        }
    }

    // SEAT LIMIT SYNC FIX
    private void parseSeatsJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        try {
            JSONArray array = new JSONArray(jsonStr);
            synchronized (this) {
                int incomingCount = array.length();
                if (incomingCount == 9 || incomingCount == 17 || incomingCount == 21) {
                    if (this.totalSeats != incomingCount) {
                        this.totalSeats = incomingCount;
                        if (seatList.size() < incomingCount) {
                            while (seatList.size() < incomingCount) {
                                seatList.add(new SeatModel(seatList.size()));
                            }
                        } else if (seatList.size() > incomingCount) {
                            while (seatList.size() > incomingCount) {
                                seatList.remove(seatList.size() - 1);
                            }
                        }
                    }
                }
                for (int i = 0; i < array.length() && i < totalSeats; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    int index = obj.optInt("index", i);
                    if (index < 0 || index >= totalSeats) continue;

                    SeatModel model = seatList.get(index);
                    String parsedUserId = obj.optString("userID", "").trim();

                    // Security enforcement: Seat 0 is strictly reserved for Room Host ONLY
                    if (index == 0 && hostUserID != null && !hostUserID.isEmpty() && !parsedUserId.equals(hostUserID)) {
                        model.clear();
                    } else {
                        // If user moved to this seat from another seat, clear the other seat!
                        if (!parsedUserId.isEmpty()) {
                            for (int j = 0; j < totalSeats; j++) {
                                if (j != index && parsedUserId.equals(seatList.get(j).userID)) {
                                    seatList.get(j).clear();
                                }
                            }
                        }

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
                                final String targetUid = model.userID;
                                UserProfileCache.getUserProfile(model.userID, profile -> {
                                    if (profile != null && profile.equippedFrame != null && !profile.equippedFrame.trim().isEmpty()) {
                                        synchronized (SeatManager.this) {
                                            if (targetUid.equals(targetModel.userID)) {
                                                targetModel.equippedFrame = profile.equippedFrame;
                                                notifySeatsUpdated();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                sanitizeDuplicateUsers();
            }
            notifySeatsUpdated();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing seats JSON", e);
        }
    }
}

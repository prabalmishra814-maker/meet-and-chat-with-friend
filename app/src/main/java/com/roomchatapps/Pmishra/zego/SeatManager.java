package com.roomchatapps.Pmishra.zego;

import android.util.Log;

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

    public void resetSeats() {
        seatList.clear();
        for (int i = 0; i < TOTAL_SEATS; i++) {
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

    public void takeSeat(int index, String userID, String userName) {
        takeSeat(index, userID, userName, "");
    }

    public void takeSeat(int index, String userID, String userName, String avatar) {
        if (index < 0 || index >= TOTAL_SEATS) return;

        // Leave any existing seat first
        int existingIndex = findUserSeatIndex(userID);
        if (existingIndex != -1 && existingIndex != index) {
            seatList.get(existingIndex).clear();
        }

        SeatModel model = seatList.get(index);
        if (model.isClosed) return;

        model.userID = userID;
        model.userName = userName;
        model.userAvatar = avatar;
        model.isMicOn = true;
        model.isMuted = false;

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
    }

    public void leaveSeat(int index) {
        if (index < 0 || index >= TOTAL_SEATS) return;

        seatList.get(index).clear();
        notifySeatsUpdated();
        syncSeatsToExtraInfo();
    }

    public void muteSeat(int index, boolean isMuted) {
        if (index < 0 || index >= TOTAL_SEATS) return;

        SeatModel model = seatList.get(index);
        model.isMuted = isMuted;
        if (isMuted) {
            model.isMicOn = false;
        }

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
    }

    public void closeSeat(int index, boolean isClosed) {
        if (index < 0 || index >= TOTAL_SEATS) return;

        SeatModel model = seatList.get(index);
        model.isClosed = isClosed;
        if (isClosed) {
            model.clear();
            model.isClosed = true;
        }

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
    }

    public void kickUser(int index) {
        leaveSeat(index);
    }

    public void updateMicStatus(int index, boolean isMicOn) {
        if (index < 0 || index >= TOTAL_SEATS) return;

        SeatModel model = seatList.get(index);
        if (model.isMuted && isMicOn) {
            // Cannot unmute if host muted
            return;
        }
        model.isMicOn = isMicOn;

        notifySeatsUpdated();
        syncSeatsToExtraInfo();
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

    private void parseSeatsJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length() && i < TOTAL_SEATS; i++) {
                JSONObject obj = array.getJSONObject(i);
                int index = obj.optInt("index", i);
                if (index < 0 || index >= TOTAL_SEATS) continue;

                SeatModel model = seatList.get(index);
                model.userID = obj.optString("userID", "");
                model.userName = obj.optString("userName", "");
                model.userAvatar = obj.optString("userAvatar", "");
                model.isMicOn = obj.optBoolean("isMicOn", true);
                model.isMuted = obj.optBoolean("isMuted", false);
                model.isClosed = obj.optBoolean("isClosed", false);
            }
            notifySeatsUpdated();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing seats JSON", e);
        }
    }
}

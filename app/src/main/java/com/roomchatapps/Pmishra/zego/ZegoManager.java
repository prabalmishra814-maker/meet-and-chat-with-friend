package com.roomchatapps.Pmishra.zego;

import android.app.Application;
import android.util.Log;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ZegoManager {
    private static final String TAG = "ZegoManager";
    private static ZegoManager instance;
    private ZegoExpressEngine engine;
    private ZegoUser currentUser;
    private String currentRoomID;
    private boolean isInRoom = false;
    private final List<ZegoUser> roomUsers = new ArrayList<>();

    private final List<ZegoManagerListener> listeners = new ArrayList<>();

    public interface ZegoManagerListener {
        default void onLoginResult(int errorCode) {}
        default void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {}
        default void onUserJoined(ZegoUser user) {}
        default void onUserLeft(ZegoUser user) {}
        default void onRoomExtraInfoUpdate(String roomID, List<ZegoRoomExtraInfo> roomExtraInfoList) {}
        default void onRemoteMicStatusUpdate(String userID, boolean isOn) {}
        default void onAudioLevelUpdate(String userID, float soundLevel) {}
        default void onIMRecvBroadcastMessage(String roomID, List<ZegoBroadcastMessageInfo> messageList) {}
    }

    private ZegoManager() {}

    public static synchronized ZegoManager getInstance() {
        if (instance == null) {
            instance = new ZegoManager();
        }
        return instance;
    }

    public void init(Application application, long appID, String appSign) {
        if (engine != null) return;

        ZegoEngineConfig config = new ZegoEngineConfig();
        ZegoExpressEngine.setEngineConfig(config);

        // DEFAULT scenario in Production Env
        engine = ZegoExpressEngine.createEngine(appID, appSign, false, ZegoScenario.DEFAULT, application, eventHandler);
        
        im.zego.zegoexpress.entity.ZegoAudioConfig audioConfig = new im.zego.zegoexpress.entity.ZegoAudioConfig(im.zego.zegoexpress.constants.ZegoAudioConfigPreset.STANDARD_QUALITY);
        engine.setAudioConfig(audioConfig);
        
        engine.startSoundLevelMonitor(100); // Faster update (100ms) for speaking animations
        
        // Ensure volumes are maxed
        engine.setCaptureVolume(100);
        engine.setAllPlayStreamVolume(100);
        
        // Ensure speaker is on by default
        engine.setAudioRouteToSpeaker(true);
        Log.d(TAG, "ZegoExpressEngine initialized in Production Env");
    }

    public void loginRoom(String roomID, String userID, String userName, boolean isHost) {
        this.currentRoomID = roomID;
        this.currentUser = new ZegoUser(userID, userName);

        ZegoRoomConfig config = new ZegoRoomConfig();
        config.isUserStatusNotify = true;

        engine.loginRoom(roomID, currentUser, config, (errorCode, extendedData) -> {
            if (errorCode == 0) {
                Log.d(TAG, "Login successful");
                isInRoom = true;
                engine.muteAllPlayStreamAudio(false); // Ensure audio playback is enabled
                if (isHost) {
                    startPublishing();
                }
            } else {
                Log.e(TAG, "Login failed: " + errorCode);
            }
            for (ZegoManagerListener listener : listeners) {
                listener.onLoginResult(errorCode);
            }
        });
    }

    public void logoutRoom() {
        if (engine != null && currentRoomID != null) {
            engine.logoutRoom(currentRoomID);
            currentRoomID = null;
            isInRoom = false;
            roomUsers.clear();
        }
    }

    public void startPublishing() {
        if (engine != null && currentUser != null) {
            String streamID = currentUser.userID;
            Log.d(TAG, "Starting to publish stream: " + streamID);
            engine.muteMicrophone(false);
            engine.mutePublishStreamAudio(false);
            engine.startPublishingStream(streamID);
        }
    }

    public void stopPublishing() {
        if (engine != null) {
            engine.stopPublishingStream();
        }
    }

    public void setMicEnabled(boolean enabled) {
        if (engine != null) {
            Log.d(TAG, "Setting mic enabled: " + enabled);
            engine.muteMicrophone(!enabled);
            engine.mutePublishStreamAudio(!enabled);
        }
    }

    public void setSpeakerOn(boolean on) {
        if (engine != null) {
            engine.setAudioRouteToSpeaker(on);
        }
    }

    public boolean isSpeakerOn() {
        if (engine != null) {
            return engine.getAudioRouteType() == ZegoAudioRoute.SPEAKER;
        }
        return false;
    }

    public int getRoomUserCount() {
        return roomUsers.size() + 1; // +1 for self if not in list
    }

    public void sendInRoomTextMessage(String message) {
        if (engine != null && currentRoomID != null) {
            engine.sendBroadcastMessage(currentRoomID, message, (errorCode, messageID) -> {
                if (errorCode != 0) {
                    Log.e(TAG, "Send message failed: " + errorCode);
                }
            });
        }
    }

    public void addListener(ZegoManagerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ZegoManagerListener listener) {
        listeners.remove(listener);
    }

    public void setRoomExtraInfo(String key, String value) {
        if (engine != null && currentRoomID != null) {
            Log.d(TAG, "Setting RoomExtraInfo: " + key + " = " + value);
            engine.setRoomExtraInfo(currentRoomID, key, value, (errorCode) -> {
                if (errorCode != 0) {
                    Log.e(TAG, "Set RoomExtraInfo failed: " + errorCode + " for key: " + key);
                } else {
                    Log.d(TAG, "Set RoomExtraInfo success for key: " + key);
                }
            });
        } else {
            Log.e(TAG, "Cannot set RoomExtraInfo: engine=" + (engine != null) + ", room=" + currentRoomID);
        }
    }

    private final IZegoEventHandler eventHandler = new IZegoEventHandler() {
        @Override
        public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {
            Log.d(TAG, "Room state changed: " + roomID + ", reason=" + reason + ", errorCode=" + errorCode);
            for (ZegoManagerListener listener : listeners) {
                listener.onRoomStateChanged(roomID, reason, errorCode, extendedData);
            }
        }

        @Override
        public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
            if (updateType == ZegoUpdateType.ADD) {
                for (ZegoUser user : userList) {
                    boolean exists = false;
                    for (ZegoUser u : roomUsers) if (u.userID.equals(user.userID)) { exists = true; break; }
                    if (!exists) roomUsers.add(user);
                    for (ZegoManagerListener listener : listeners) listener.onUserJoined(user);
                }
            } else {
                for (ZegoUser user : userList) {
                    for (int i = 0; i < roomUsers.size(); i++) {
                        if (roomUsers.get(i).userID.equals(user.userID)) {
                            roomUsers.remove(i);
                            break;
                        }
                    }
                    for (ZegoManagerListener listener : listeners) listener.onUserLeft(user);
                }
            }
        }

        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
            if (updateType == ZegoUpdateType.ADD) {
                for (ZegoStream stream : streamList) {
                    Log.d(TAG, "Starting to play stream: " + stream.streamID);
                    engine.startPlayingStream(stream.streamID, (ZegoCanvas) null);
                }
            } else {
                for (ZegoStream stream : streamList) {
                    Log.d(TAG, "Stopping play stream: " + stream.streamID);
                    engine.stopPlayingStream(stream.streamID);
                }
            }
        }

        @Override
        public void onPublisherStateUpdate(String streamID, im.zego.zegoexpress.constants.ZegoPublisherState state, int errorCode, JSONObject extendedData) {
            Log.d(TAG, "Publisher state update: " + streamID + ", state=" + state + ", errorCode=" + errorCode);
            if (errorCode != 0) {
                Log.e(TAG, "Publisher error: " + errorCode);
            }
        }

        @Override
        public void onPlayerStateUpdate(String streamID, im.zego.zegoexpress.constants.ZegoPlayerState state, int errorCode, JSONObject extendedData) {
            Log.d(TAG, "Player state update: " + streamID + ", state=" + state + ", errorCode=" + errorCode);
            if (errorCode != 0) {
                Log.e(TAG, "Player error: " + errorCode);
            }
        }

        @Override
        public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
            for (ZegoManagerListener listener : listeners) {
                listener.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
            }
        }

        @Override
        public void onRemoteMicStateUpdate(String streamID, im.zego.zegoexpress.constants.ZegoRemoteDeviceState state) {
            // Since streamID == userID
            boolean isOn = state == im.zego.zegoexpress.constants.ZegoRemoteDeviceState.OPEN;
            for (ZegoManagerListener listener : listeners) {
                listener.onRemoteMicStatusUpdate(streamID, isOn);
            }
        }

        @Override
        public void onCapturedSoundLevelUpdate(float soundLevel) {
            if (currentUser != null) {
                for (ZegoManagerListener listener : listeners) {
                    listener.onAudioLevelUpdate(currentUser.userID, soundLevel);
                }
            }
        }

        @Override
        public void onRemoteSoundLevelUpdate(java.util.HashMap<String, Float> soundLevels) {
            for (java.util.Map.Entry<String, Float> entry : soundLevels.entrySet()) {
                String streamID = entry.getKey(); // streamID == userID
                for (ZegoManagerListener listener : listeners) {
                    listener.onAudioLevelUpdate(streamID, entry.getValue());
                }
            }
        }

        @Override
        public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
            for (ZegoManagerListener listener : listeners) {
                listener.onIMRecvBroadcastMessage(roomID, messageList);
            }
        }
    };
}

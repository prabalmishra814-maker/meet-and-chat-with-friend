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

    private final List<ZegoManagerListener> listeners = new ArrayList<>();

    public interface ZegoManagerListener {
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

        engine = ZegoExpressEngine.createEngine(appID, appSign, true, ZegoScenario.DEFAULT, application, eventHandler);
        engine.startSoundLevelMonitor(200);
        Log.d(TAG, "ZegoExpressEngine initialized");
    }

    public void loginRoom(String roomID, String userID, String userName, boolean isHost) {
        this.currentRoomID = roomID;
        this.currentUser = new ZegoUser(userID, userName);

        ZegoRoomConfig config = new ZegoRoomConfig();
        config.isUserStatusNotify = true;

        engine.loginRoom(roomID, currentUser, config, (errorCode, extendedData) -> {
            if (errorCode == 0) {
                Log.d(TAG, "Login successful");
                if (isHost) {
                    startPublishing();
                }
            } else {
                Log.e(TAG, "Login failed: " + errorCode);
            }
        });
    }

    public void logoutRoom() {
        if (engine != null && currentRoomID != null) {
            engine.logoutRoom(currentRoomID);
            currentRoomID = null;
        }
    }

    public void startPublishing() {
        if (engine != null && currentUser != null) {
            engine.startPublishingStream(currentRoomID + "_" + currentUser.userID);
            engine.mutePublishStreamAudio(false);
        }
    }

    public void stopPublishing() {
        if (engine != null) {
            engine.stopPublishingStream();
        }
    }

    public void setMicEnabled(boolean enabled) {
        if (engine != null) {
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
            engine.setRoomExtraInfo(currentRoomID, key, value, (errorCode) -> {
                if (errorCode != 0) {
                    Log.e(TAG, "Set RoomExtraInfo failed: " + errorCode);
                }
            });
        }
    }

    private final IZegoEventHandler eventHandler = new IZegoEventHandler() {
        @Override
        public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {
            for (ZegoManagerListener listener : listeners) {
                listener.onRoomStateChanged(roomID, reason, errorCode, extendedData);
            }
        }

        @Override
        public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
            for (ZegoUser user : userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoManagerListener listener : listeners) listener.onUserJoined(user);
                } else {
                    for (ZegoManagerListener listener : listeners) listener.onUserLeft(user);
                }
            }
        }

        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
            if (updateType == ZegoUpdateType.ADD) {
                for (ZegoStream stream : streamList) {
                    engine.startPlayingStream(stream.streamID, (ZegoCanvas) null);
                }
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
            String[] parts = streamID.split("_");
            if (parts.length > 1) {
                String userID = parts[parts.length - 1];
                boolean isOn = state == im.zego.zegoexpress.constants.ZegoRemoteDeviceState.OPEN;
                for (ZegoManagerListener listener : listeners) {
                    listener.onRemoteMicStatusUpdate(userID, isOn);
                }
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
                String streamID = entry.getKey();
                String[] parts = streamID.split("_");
                if (parts.length > 1) {
                    String userID = parts[parts.length - 1];
                    for (ZegoManagerListener listener : listeners) {
                        listener.onAudioLevelUpdate(userID, entry.getValue());
                    }
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

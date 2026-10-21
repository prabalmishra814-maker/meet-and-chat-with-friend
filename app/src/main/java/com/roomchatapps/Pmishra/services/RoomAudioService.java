package com.roomchatapps.Pmishra.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.RoomChatActivity;
import com.roomchatapps.Pmishra.utils.RoomFloatingManager;
import com.roomchatapps.Pmishra.zego.ZegoManager;

public class RoomAudioService extends Service {

    public static final String ACTION_START = "com.roomchatapps.Pmishra.action.START_ROOM_AUDIO";
    public static final String ACTION_STOP = "com.roomchatapps.Pmishra.action.STOP_ROOM_AUDIO";
    public static final String ACTION_LEAVE_ROOM = "com.roomchatapps.Pmishra.action.LEAVE_ROOM";

    private static final String CHANNEL_ID = "room_audio_bg_channel";
    private static final String CHANNEL_NAME = "Audio Room Service";
    private static final int NOTIFICATION_ID = 8881;

    private PowerManager.WakeLock wakeLock;
    private static boolean isServiceRunning = false;
    private String currentRoomId;
    private String currentRoomName;

    public static boolean isRunning() {
        return isServiceRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        acquireWakeLock();
        isServiceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP.equals(action)) {
                stopForegroundService();
                return START_NOT_STICKY;
            } else if (ACTION_LEAVE_ROOM.equals(action)) {
                try {
                    ZegoManager.getInstance().logoutRoom();
                    RoomFloatingManager.getInstance().clearRoomState();
                } catch (Exception e) {
                    Log.e("RoomAudioService", "Error logging out room", e);
                }
                stopForegroundService();
                return START_NOT_STICKY;
            }

            currentRoomId = intent.getStringExtra("roomID");
            currentRoomName = intent.getStringExtra("room_name");
            String img = intent.getStringExtra("img");

            Notification notification = buildNotification(
                    currentRoomName != null && !currentRoomName.trim().isEmpty() ? currentRoomName : "Voice Room Active",
                    "Room audio playing in background 🎙️",
                    currentRoomId,
                    img
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                int foregroundType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    foregroundType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
                }
                try {
                    startForeground(NOTIFICATION_ID, notification, foregroundType);
                } catch (Exception e) {
                    Log.e("RoomAudioService", "Error starting foreground with type, falling back", e);
                    startForeground(NOTIFICATION_ID, notification);
                }
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        }
        return START_STICKY;
    }

    private Notification buildNotification(String title, String contentText, String roomId, String img) {
        Intent contentIntent = new Intent(this, RoomChatActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (roomId != null) contentIntent.putExtra("roomID", roomId);
        if (title != null) contentIntent.putExtra("room_name", title);
        if (img != null) contentIntent.putExtra("img", img);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, contentIntent, flags);

        Intent leaveIntent = new Intent(this, RoomAudioService.class);
        leaveIntent.setAction(ACTION_LEAVE_ROOM);
        PendingIntent leavePendingIntent = PendingIntent.getService(this, 1, leaveIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.img_20260904_135725)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.img_20260904_135725, "Leave Room", leavePendingIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Audio Room continuous playback when app is minimized or screen is locked.");
            channel.setSound(null, null);
            channel.enableVibration(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void acquireWakeLock() {
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && wakeLock == null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RoomChat:AudioRoomWakeLock");
                wakeLock.acquire(30 * 60 * 1000L);
            }
        } catch (Exception e) {
            Log.e("RoomAudioService", "Error acquiring WakeLock", e);
        }
    }

    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            wakeLock = null;
        } catch (Exception e) {
            Log.e("RoomAudioService", "Error releasing WakeLock", e);
        }
    }

    private void stopForegroundService() {
        isServiceRunning = false;
        releaseWakeLock();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        releaseWakeLock();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

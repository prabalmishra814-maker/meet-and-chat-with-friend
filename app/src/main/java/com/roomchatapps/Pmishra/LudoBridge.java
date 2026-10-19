package com.roomchatapps.Pmishra;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * JavaScript Interface for Ludo Online 4-Player Matchmaking & Multiplayer Synchronization.
 * Uses Firebase Realtime Database for zero-latency real-time state sync across all online users.
 */
public class LudoBridge {
    private static final String TAG = "LudoBridge";
    private final Context context;
    private final WebView webView;
    private final String userId;
    private final String userName;
    private final String userAvatar;

    private final DatabaseReference ludoRef;
    private DatabaseReference activeRoomRef;
    private ValueEventListener roomEventListener;
    private String activeRoomId = null;
    private int myPlayerSlot = -1; // 0=Green, 1=Blue, 2=Yellow, 3=Red
    private boolean isSearching = false;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable = null;

    public LudoBridge(Context context, WebView webView, String userId, String userName, String userAvatar) {
        this.context = context;
        this.webView = webView;
        this.userId = (userId != null && !userId.trim().isEmpty()) ? userId : "user_" + System.currentTimeMillis();
        this.userName = (userName != null && !userName.trim().isEmpty()) ? userName : "Player_" + new Random().nextInt(9000 + 1000);
        this.userAvatar = userAvatar != null ? userAvatar : "";
        this.ludoRef = FirebaseDatabase.getInstance().getReference("ludo_online");
    }

    @JavascriptInterface
    public String getUserInfo() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("uid", userId);
            obj.put("name", userName);
            obj.put("avatar", userAvatar);
            return obj.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user info", e);
            return "{}";
        }
    }

    @JavascriptInterface
    public void findMatch(String mode) {
        if (isSearching) return;
        isSearching = true;

        Log.d(TAG, "findMatch requested by user: " + userId + " (" + userName + ")");

        // Step 1: Query open rooms
        ludoRef.child("rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String foundRoomId = null;
                DataSnapshot foundRoomSnap = null;

                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String state = roomSnap.child("state").getValue(String.class);
                    Long count = roomSnap.child("playerCount").getValue(Long.class);
                    if ("WAITING".equals(state) && count != null && count < 4) {
                        foundRoomId = roomSnap.getKey();
                        foundRoomSnap = roomSnap;
                        break;
                    }
                }

                if (foundRoomId != null && foundRoomSnap != null) {
                    joinExistingRoom(foundRoomId, foundRoomSnap);
                } else {
                    createNewRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error in findMatch", error.toException());
                isSearching = false;
            }
        });
    }

    private void createNewRoom() {
        String roomId = "ludo_room_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        activeRoomId = roomId;
        myPlayerSlot = 0; // Host is Slot 0 (Green or Yellow based on JS setup)

        DatabaseReference roomRef = ludoRef.child("rooms").child(roomId);
        activeRoomRef = roomRef;

        Map<String, Object> player0 = new HashMap<>();
        player0.put("uid", userId);
        player0.put("name", userName);
        player0.put("avatar", userAvatar);
        player0.put("slot", 0);
        player0.put("isBot", false);

        Map<String, Object> initialRoom = new HashMap<>();
        initialRoom.put("roomId", roomId);
        initialRoom.put("hostUid", userId);
        initialRoom.put("state", "WAITING");
        initialRoom.put("playerCount", 1);
        initialRoom.put("createdAt", System.currentTimeMillis());

        Map<String, Object> players = new HashMap<>();
        players.put("p0", player0);
        initialRoom.put("players", players);

        roomRef.setValue(initialRoom).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Created room: " + roomId);
            attachRoomListener();
            startMatchmakingTimer();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to create room", e);
            isSearching = false;
        });
    }

    private void joinExistingRoom(String roomId, DataSnapshot roomSnap) {
        activeRoomId = roomId;
        activeRoomRef = ludoRef.child("rooms").child(roomId);

        DataSnapshot playersSnap = roomSnap.child("players");
        int nextSlot = -1;
        for (int i = 0; i < 4; i++) {
            if (!playersSnap.hasChild("p" + i)) {
                nextSlot = i;
                break;
            }
        }

        if (nextSlot == -1) {
            // Room filled concurrently, try creating a new one
            createNewRoom();
            return;
        }

        myPlayerSlot = nextSlot;

        Map<String, Object> myPlayerData = new HashMap<>();
        myPlayerData.put("uid", userId);
        myPlayerData.put("name", userName);
        myPlayerData.put("avatar", userAvatar);
        myPlayerData.put("slot", nextSlot);
        myPlayerData.put("isBot", false);

        final int slotToJoin = nextSlot;
        activeRoomRef.child("players").child("p" + slotToJoin).setValue(myPlayerData).addOnSuccessListener(aVoid -> {
            long currentCount = playersSnap.getChildrenCount();
            long newCount = currentCount + 1;
            activeRoomRef.child("playerCount").setValue(newCount);

            Log.d(TAG, "Joined room " + roomId + " at slot p" + slotToJoin);
            attachRoomListener();

            if (newCount >= 4) {
                activeRoomRef.child("state").setValue("PLAYING");
            }
        });
    }

    private void attachRoomListener() {
        if (activeRoomRef == null) return;

        if (roomEventListener != null) {
            activeRoomRef.removeEventListener(roomEventListener);
        }

        roomEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                try {
                    JSONObject roomJson = new JSONObject();
                    roomJson.put("roomId", snapshot.child("roomId").getValue(String.class));
                    roomJson.put("state", snapshot.child("state").getValue(String.class));
                    roomJson.put("mySlot", myPlayerSlot);
                    roomJson.put("myUid", userId);

                    JSONArray playersArr = new JSONArray();
                    DataSnapshot playersSnap = snapshot.child("players");
                    for (int i = 0; i < 4; i++) {
                        DataSnapshot pSnap = playersSnap.child("p" + i);
                        JSONObject pObj = new JSONObject();
                        if (pSnap.exists()) {
                            pObj.put("slot", i);
                            pObj.put("uid", pSnap.child("uid").getValue(String.class));
                            pObj.put("name", pSnap.child("name").getValue(String.class));
                            pObj.put("avatar", pSnap.child("avatar").getValue(String.class));
                            pObj.put("isBot", pSnap.child("isBot").getValue(Boolean.class) == Boolean.TRUE);
                        } else {
                            pObj.put("slot", i);
                            pObj.put("uid", "");
                            pObj.put("name", "Searching...");
                            pObj.put("avatar", "");
                            pObj.put("isBot", false);
                        }
                        playersArr.put(pObj);
                    }
                    roomJson.put("players", playersArr);

                    // Game state action
                    DataSnapshot lastActionSnap = snapshot.child("lastAction");
                    if (lastActionSnap.exists()) {
                        JSONObject actionObj = new JSONObject();
                        actionObj.put("type", lastActionSnap.child("type").getValue(String.class));
                        actionObj.put("slot", lastActionSnap.child("slot").getValue(Integer.class));
                        actionObj.put("diceVal", lastActionSnap.child("diceVal").getValue(Integer.class));
                        actionObj.put("color", lastActionSnap.child("color").getValue(String.class));
                        actionObj.put("tokenId", lastActionSnap.child("tokenId").getValue(Integer.class));
                        actionObj.put("timestamp", lastActionSnap.child("timestamp").getValue(Long.class));
                        roomJson.put("lastAction", actionObj);
                    }

                    String jsonString = roomJson.toString();
                    runJs("if (window.onLudoRoomUpdated) window.onLudoRoomUpdated(" + jsonString + ");");

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing room snapshot", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Room listener cancelled", error.toException());
            }
        };

        activeRoomRef.addValueEventListener(roomEventListener);
    }

    private void startMatchmakingTimer() {
        if (timeoutRunnable != null) {
            timerHandler.removeCallbacks(timeoutRunnable);
        }

        // 12-second countdown: Fill remaining slots with Bots if 4 players haven't joined
        timeoutRunnable = () -> {
            if (activeRoomRef != null && myPlayerSlot == 0) {
                activeRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;
                        String state = snapshot.child("state").getValue(String.class);
                        if ("WAITING".equals(state)) {
                            // Fill missing slots with Bots
                            DataSnapshot playersSnap = snapshot.child("players");
                            String[] botNames = {"SPEEDY_BOT", "LUDO_KING_BOT", "VIPER_BOT"};
                            int botIdx = 0;
                            for (int i = 0; i < 4; i++) {
                                if (!playersSnap.hasChild("p" + i)) {
                                    Map<String, Object> bot = new HashMap<>();
                                    bot.put("uid", "bot_" + i);
                                    bot.put("name", botNames[botIdx % botNames.length]);
                                    bot.put("avatar", "assets/avatar_p" + (i + 1) + ".svg");
                                    bot.put("slot", i);
                                    bot.put("isBot", true);
                                    activeRoomRef.child("players").child("p" + i).setValue(bot);
                                    botIdx++;
                                }
                            }
                            activeRoomRef.child("playerCount").setValue(4);
                            activeRoomRef.child("state").setValue("PLAYING");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        };

        timerHandler.postDelayed(timeoutRunnable, 12000);
    }

    @JavascriptInterface
    public void sendGameAction(String actionType, String actionDataJson) {
        if (activeRoomRef == null) return;

        try {
            JSONObject data = new JSONObject(actionDataJson);
            Map<String, Object> action = new HashMap<>();
            action.put("type", actionType);
            action.put("slot", myPlayerSlot);
            action.put("timestamp", System.currentTimeMillis());

            if (data.has("diceVal")) action.put("diceVal", data.getInt("diceVal"));
            if (data.has("color")) action.put("color", data.getString("color"));
            if (data.has("tokenId")) action.put("tokenId", data.getInt("tokenId"));

            activeRoomRef.child("lastAction").setValue(action);
        } catch (Exception e) {
            Log.e(TAG, "Error sending game action", e);
        }
    }

    @JavascriptInterface
    public void leaveMatch() {
        isSearching = false;
        if (timeoutRunnable != null) {
            timerHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }

        if (activeRoomRef != null && roomEventListener != null) {
            activeRoomRef.removeEventListener(roomEventListener);
        }

        if (activeRoomRef != null && myPlayerSlot >= 0) {
            activeRoomRef.child("players").child("p" + myPlayerSlot).removeValue();
            activeRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot playersSnap = snapshot.child("players");
                        long count = playersSnap.getChildrenCount();
                        if (count == 0) {
                            activeRoomRef.removeValue();
                        } else {
                            activeRoomRef.child("playerCount").setValue(count);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        activeRoomRef = null;
        activeRoomId = null;
        myPlayerSlot = -1;
    }

    private void runJs(String js) {
        if (webView != null) {
            webView.post(() -> webView.evaluateJavascript(js, null));
        }
    }
}

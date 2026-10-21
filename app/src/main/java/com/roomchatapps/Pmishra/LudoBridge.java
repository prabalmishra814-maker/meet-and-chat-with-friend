package com.roomchatapps.Pmishra;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * JavaScript Interface for Ludo Online Matchmaking & Game Server Integration.
 * Realtime synchronization via Firebase Realtime Database for 1v1, 2v2, and 4v4 modes.
 */
public class LudoBridge {
    private static final String TAG = "LudoBridge";
    private final WebView webView;
    private final String userId;
    private final String userName;
    private final String userAvatar;

    private final DatabaseReference ludoRef;
    private DatabaseReference activeRoomRef;
    private ValueEventListener roomEventListener;
    private int myPlayerSlot = -1; // 0=Green, 1=Blue/Yellow, etc.
    private boolean isSearching = false;

    public LudoBridge(Context context, WebView webView, String userId, String userName, String userAvatar) {
        this.webView = webView;
        String rawUid = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : "user_" + System.currentTimeMillis();
        this.userId = rawUid;
        this.userName = (userName != null && !userName.trim().isEmpty()) ? userName : "Player_" + new Random().nextInt(9000) + 1000;
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
        isSearching = true;

        final String requestedMode = (mode != null && !mode.trim().isEmpty()) ? mode.trim() : "auto";
        final int maxPlayers = ("1v1".equalsIgnoreCase(requestedMode)) ? 2 : ("1v2".equalsIgnoreCase(requestedMode)) ? 3 : 4;

        Log.d(TAG, "findMatch requested by user: " + userId + " (" + userName + "), mode=" + requestedMode + ", maxPlayers=" + maxPlayers);

        // Query open rooms with state WAITING and actual player count < maxPlayers
        ludoRef.child("rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String foundRoomId = null;
                DataSnapshot foundRoomSnap = null;
                long now = System.currentTimeMillis();

                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String state = roomSnap.child("state").getValue(String.class);
                    String hostUid = roomSnap.child("hostUid").getValue(String.class);
                    Long createdAt = roomSnap.child("createdAt").getValue(Long.class);
                    DataSnapshot playersSnap = roomSnap.child("players");
                    long actualCount = playersSnap.exists() ? playersSnap.getChildrenCount() : 0;
                    Long roomMax = roomSnap.child("maxPlayers").getValue(Long.class);

                    int targetMax = (roomMax != null) ? roomMax.intValue() : 4;

                    // Ignore abandoned rooms older than 3 minutes
                    if (createdAt != null && (now - createdAt > 180000)) {
                        continue;
                    }

                    // Ensure user does NOT join a room if they are already host or in players list
                    boolean containsMe = false;
                    if (userId.equals(hostUid)) {
                        containsMe = true;
                    } else if (playersSnap.exists()) {
                        for (DataSnapshot pSnap : playersSnap.getChildren()) {
                            String pUid = pSnap.child("uid").getValue(String.class);
                            if (userId.equals(pUid)) {
                                containsMe = true;
                                break;
                            }
                        }
                    }

                    if ("WAITING".equals(state)) {
                        if (containsMe) {
                            // User is already host or player in this waiting room! Re-use this room!
                            foundRoomId = roomSnap.getKey();
                            foundRoomSnap = roomSnap;
                            break;
                        } else if (actualCount < targetMax) {
                            foundRoomId = roomSnap.getKey();
                            foundRoomSnap = roomSnap;
                            break;
                        }
                    }
                }

                if (foundRoomId != null && foundRoomSnap != null) {
                    joinExistingRoom(foundRoomId, foundRoomSnap, requestedMode, maxPlayers);
                } else {
                    createNewRoom(requestedMode, maxPlayers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error in findMatch", error.toException());
                isSearching = false;
            }
        });
    }

    private void createNewRoom(String mode, int maxPlayers) {
        final long now = System.currentTimeMillis();
        final String roomId = "ludo_room_" + now + "_" + new Random().nextInt(1000);
        myPlayerSlot = 0; // Host is Slot 0

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
        initialRoom.put("mode", mode);
        initialRoom.put("maxPlayers", maxPlayers);
        initialRoom.put("state", "WAITING");
        initialRoom.put("playerCount", 1);
        initialRoom.put("createdAt", now);

        Map<String, Object> players = new HashMap<>();
        players.put("p0", player0);
        initialRoom.put("players", players);

        roomRef.setValue(initialRoom).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Created waiting room for " + maxPlayers + " players (" + mode + "): " + roomId);
            attachRoomListener();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to create room", e);
            isSearching = false;
            activeRoomRef = null;
        });
    }

    private void joinExistingRoom(String roomId, DataSnapshot roomSnap, String mode, int maxPlayers) {
        DatabaseReference targetRoomRef = ludoRef.child("rooms").child(roomId);
        activeRoomRef = targetRoomRef;

        // Fetch fresh players snapshot to avoid race conditions
        targetRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot freshPlayersSnap) {
                int existingSlot = -1;
                if (freshPlayersSnap.exists()) {
                    for (DataSnapshot pSnap : freshPlayersSnap.getChildren()) {
                        String pUid = pSnap.child("uid").getValue(String.class);
                        if (userId.equals(pUid)) {
                            String slotKey = pSnap.getKey(); // e.g. "p0" or "p1"
                            if (slotKey != null && slotKey.startsWith("p")) {
                                try {
                                    existingSlot = Integer.parseInt(slotKey.substring(1));
                                } catch (NumberFormatException ignored) {}
                            }
                            break;
                        }
                    }
                }

                if (existingSlot != -1) {
                    myPlayerSlot = existingSlot;
                    Log.d(TAG, "User " + userId + " re-joining existing room " + roomId + " at slot p" + existingSlot);
                    attachRoomListener();
                    return;
                }

                int nextSlot = -1;
                for (int i = 0; i < maxPlayers; i++) {
                    if (!freshPlayersSnap.hasChild("p" + i)) {
                        nextSlot = i;
                        break;
                    }
                }

                if (nextSlot == -1) {
                    createNewRoom(mode, maxPlayers);
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
                attachRoomListener(); // Attach listener immediately

                targetRoomRef.child("players").child("p" + slotToJoin).setValue(myPlayerData).addOnSuccessListener(aVoid -> {
                    targetRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot updatedPlayersSnap) {
                            long newCount = updatedPlayersSnap.getChildrenCount();
                            targetRoomRef.child("playerCount").setValue(newCount);

                            Log.d(TAG, "Joined room " + roomId + " at slot p" + slotToJoin + " (" + newCount + "/" + maxPlayers + ")");

                            if (newCount >= maxPlayers) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("mode", maxPlayers == 2 ? "1v1" : maxPlayers == 3 ? "1v2" : "4v4");
                                updates.put("state", "PLAYING");
                                targetRoomRef.updateChildren(updates);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to join room " + roomId, e);
                    createNewRoom(mode, maxPlayers);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createNewRoom(mode, maxPlayers);
            }
        });
    }

    @JavascriptInterface
    public void forceStartMatch() {
        if (activeRoomRef == null) return;

        activeRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String state = snapshot.child("state").getValue(String.class);
                if (!"WAITING".equals(state)) return;

                DataSnapshot playersSnap = snapshot.child("players");
                long count = playersSnap.getChildrenCount();

                if (count >= 2) {
                    String finalMode = (count == 2) ? "1v1" : (count == 3) ? "1v2" : "4v4";
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("mode", finalMode);
                    updates.put("maxPlayers", count);
                    updates.put("state", "PLAYING");
                    activeRoomRef.updateChildren(updates);
                    Log.d(TAG, "forceStartMatch triggered with " + count + " players -> mode: " + finalMode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error in forceStartMatch", error.toException());
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
                    String roomMode = snapshot.child("mode").getValue(String.class);
                    Long maxP = snapshot.child("maxPlayers").getValue(Long.class);

                    int maxPlayers = (maxP != null) ? maxP.intValue() : 4;
                    roomJson.put("subMode", roomMode != null ? roomMode : "4v4");
                    roomJson.put("maxPlayers", maxPlayers);
                    roomJson.put("mySlot", myPlayerSlot);
                    roomJson.put("myUid", userId);

                    JSONArray playersArr = new JSONArray();
                    DataSnapshot playersSnap = snapshot.child("players");
                    for (int i = 0; i < maxPlayers; i++) {
                        DataSnapshot pSnap = playersSnap.child("p" + i);
                        JSONObject pObj = new JSONObject();
                        if (pSnap.exists()) {
                            pObj.put("slot", i);
                            pObj.put("uid", pSnap.child("uid").getValue(String.class));
                            pObj.put("name", pSnap.child("name").getValue(String.class));
                            pObj.put("avatar", pSnap.child("avatar").getValue(String.class));
                            pObj.put("isBot", false);
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

                    // Game action
                    DataSnapshot lastActionSnap = snapshot.child("lastAction");
                    if (lastActionSnap.exists()) {
                        JSONObject actionObj = new JSONObject();
                        actionObj.put("type", lastActionSnap.child("type").getValue(String.class));
                        actionObj.put("slot", lastActionSnap.child("slot").getValue(Integer.class));
                        actionObj.put("diceVal", lastActionSnap.child("diceVal").getValue(Integer.class));
                        actionObj.put("color", lastActionSnap.child("color").getValue(String.class));
                        actionObj.put("tokenId", lastActionSnap.child("tokenId").getValue(Integer.class));
                        actionObj.put("timestamp", lastActionSnap.child("timestamp").getValue(Long.class));
                        if (lastActionSnap.hasChild("actionSeq")) {
                            actionObj.put("actionSeq", lastActionSnap.child("actionSeq").getValue(Integer.class));
                        }
                        roomJson.put("lastAction", actionObj);
                    }

                    // Authoritative Game State Sync
                    DataSnapshot gameStateSnap = snapshot.child("gameState");
                    if (gameStateSnap.exists()) {
                        JSONObject stateObj = new JSONObject();
                        if (gameStateSnap.hasChild("turnSlot")) stateObj.put("turnSlot", gameStateSnap.child("turnSlot").getValue(Integer.class));
                        if (gameStateSnap.hasChild("phase")) stateObj.put("phase", gameStateSnap.child("phase").getValue(String.class));
                        if (gameStateSnap.hasChild("diceVal")) stateObj.put("diceVal", gameStateSnap.child("diceVal").getValue(Integer.class));
                        if (gameStateSnap.hasChild("consecutiveSixes")) stateObj.put("consecutiveSixes", gameStateSnap.child("consecutiveSixes").getValue(Integer.class));
                        if (gameStateSnap.hasChild("actionSeq")) stateObj.put("actionSeq", gameStateSnap.child("actionSeq").getValue(Integer.class));
                        if (gameStateSnap.hasChild("turnDeadline")) stateObj.put("turnDeadline", gameStateSnap.child("turnDeadline").getValue(Long.class));

                        DataSnapshot boardSnap = gameStateSnap.child("boardState");
                        if (boardSnap.exists()) {
                            JSONObject boardObj = new JSONObject();
                            for (DataSnapshot colorSnap : boardSnap.getChildren()) {
                                JSONArray posArr = new JSONArray();
                                for (DataSnapshot posSnap : colorSnap.getChildren()) {
                                    Long posVal = posSnap.getValue(Long.class);
                                    if (posVal != null) posArr.put(posVal.intValue());
                                }
                                boardObj.put(colorSnap.getKey(), posArr);
                            }
                            stateObj.put("boardState", boardObj);
                        }
                        roomJson.put("gameState", stateObj);
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

    @JavascriptInterface
    public void sendGameState(String gameStateJson) {
        if (activeRoomRef == null) return;

        try {
            JSONObject json = new JSONObject(gameStateJson);
            Map<String, Object> stateMap = new HashMap<>();

            if (json.has("turnSlot")) stateMap.put("turnSlot", json.getInt("turnSlot"));
            if (json.has("phase")) stateMap.put("phase", json.getString("phase"));
            if (json.has("diceVal")) stateMap.put("diceVal", json.getInt("diceVal"));
            if (json.has("consecutiveSixes")) stateMap.put("consecutiveSixes", json.getInt("consecutiveSixes"));
            if (json.has("actionSeq")) stateMap.put("actionSeq", json.getInt("actionSeq"));
            if (json.has("turnDeadline")) stateMap.put("turnDeadline", json.getLong("turnDeadline"));

            if (json.has("boardState")) {
                JSONObject boardObj = json.getJSONObject("boardState");
                Map<String, Object> boardMap = new HashMap<>();
                for (String color : new String[]{"green", "blue", "yellow", "red"}) {
                    if (boardObj.has(color)) {
                        JSONArray arr = boardObj.getJSONArray(color);
                        List<Integer> list = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            list.add(arr.getInt(i));
                        }
                        boardMap.put(color, list);
                    }
                }
                stateMap.put("boardState", boardMap);
            }

            Map<String, Object> roomUpdates = new HashMap<>();
            roomUpdates.put("gameState", stateMap);

            if (json.has("lastAction")) {
                JSONObject actionObj = json.getJSONObject("lastAction");
                Map<String, Object> actionMap = new HashMap<>();
                actionMap.put("type", actionObj.optString("type", ""));
                actionMap.put("slot", actionObj.optInt("slot", myPlayerSlot));
                actionMap.put("diceVal", actionObj.optInt("diceVal", 0));
                actionMap.put("color", actionObj.optString("color", ""));
                actionMap.put("tokenId", actionObj.optInt("tokenId", -1));
                actionMap.put("actionSeq", actionObj.optInt("actionSeq", 0));
                actionMap.put("timestamp", System.currentTimeMillis());
                stateMap.put("lastAction", actionMap);
                roomUpdates.put("lastAction", actionMap);
            }

            activeRoomRef.updateChildren(roomUpdates);
        } catch (Exception e) {
            Log.e(TAG, "Error sending game state", e);
        }
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
            if (data.has("actionSeq")) action.put("actionSeq", data.getInt("actionSeq"));

            activeRoomRef.child("lastAction").setValue(action);
        } catch (Exception e) {
            Log.e(TAG, "Error sending game action", e);
        }
    }

    @JavascriptInterface
    public void leaveMatch() {
        isSearching = false;

        if (activeRoomRef != null && roomEventListener != null) {
            activeRoomRef.removeEventListener(roomEventListener);
            roomEventListener = null;
        }

        if (activeRoomRef != null && myPlayerSlot >= 0) {
            DatabaseReference refToClean = activeRoomRef;
            int slotToClean = myPlayerSlot;

            refToClean.child("players").child("p" + slotToClean).removeValue();
            refToClean.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot playersSnap = snapshot.child("players");
                        long count = playersSnap.getChildrenCount();
                        if (count == 0) {
                            refToClean.removeValue();
                        } else {
                            refToClean.child("playerCount").setValue(count);
                            refToClean.child("state").setValue("WAITING");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        activeRoomRef = null;
        myPlayerSlot = -1;
    }

    private void runJs(String js) {
        if (webView != null) {
            webView.post(() -> webView.evaluateJavascript(js, null));
        }
    }
}

package com.roomchatapps.Pmishra.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages per-round bets and win payouts across all devices for Lucky Fruit & Greedy Game Leaderboards.
 */
public class GameLeaderboardManager {
    private static final String TAG = "GameLeaderboardManager";

    public interface LeaderboardCallback {
        void onLeaderboardLoaded(String jsonResult);
    }

    public static void recordBet(String gameName, long roundIndex, String userId, long betAmount) {
        if (gameName == null || userId == null || userId.isEmpty() || roundIndex <= 0 || betAmount <= 0) return;

        DatabaseReference userBetRef = FirebaseDatabase.getInstance().getReference("game_round_bets")
                .child(gameName)
                .child(String.valueOf(roundIndex))
                .child(userId);

        UserProfileCache.getUserProfile(userId, profile -> {
            String name = (profile != null && profile.name != null && !profile.name.isEmpty()) ? profile.name : "Player";
            String avatar = (profile != null && profile.avatarUrl != null) ? profile.avatarUrl : "";

            userBetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long existingBet = 0;
                    if (snapshot.exists() && snapshot.child("betAmount").getValue() != null) {
                        try {
                            existingBet = Long.parseLong(String.valueOf(snapshot.child("betAmount").getValue()));
                        } catch (Exception ignored) {}
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", userId);
                    data.put("userName", name);
                    data.put("userAvatar", avatar);
                    data.put("betAmount", existingBet + betAmount);
                    if (!snapshot.child("winAmount").exists()) {
                        data.put("winAmount", 0L);
                    }
                    data.put("timestamp", System.currentTimeMillis());

                    userBetRef.updateChildren(data);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }

    public static void recordWin(String gameName, long roundIndex, String userId, long winAmount) {
        if (gameName == null || userId == null || userId.isEmpty() || roundIndex <= 0 || winAmount <= 0) return;

        DatabaseReference userBetRef = FirebaseDatabase.getInstance().getReference("game_round_bets")
                .child(gameName)
                .child(String.valueOf(roundIndex))
                .child(userId);

        userBetRef.child("winAmount").setValue(winAmount);
    }

    public static void fetchRoundLeaderboard(String gameName, long roundIndex, LeaderboardCallback callback) {
        if (gameName == null || roundIndex <= 0) {
            if (callback != null) callback.onLeaderboardLoaded("[]");
            return;
        }

        DatabaseReference roundRef = FirebaseDatabase.getInstance().getReference("game_round_bets")
                .child(gameName)
                .child(String.valueOf(roundIndex));

        roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RoundParticipant> list = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            String uId = ds.child("userId").getValue(String.class);
                            String uName = ds.child("userName").getValue(String.class);
                            String uAvatar = ds.child("userAvatar").getValue(String.class);
                            Long betAmt = ds.child("betAmount").getValue(Long.class);
                            Long winAmt = ds.child("winAmount").getValue(Long.class);

                            if (uId != null && betAmt != null && betAmt > 0) {
                                RoundParticipant p = new RoundParticipant();
                                p.userId = uId;
                                p.userName = uName != null ? uName : "Player";
                                p.userAvatar = uAvatar != null ? uAvatar : "";
                                p.betAmount = betAmt;
                                p.winAmount = winAmt != null ? winAmt : 0;
                                list.add(p);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing round participant", e);
                        }
                    }
                }

                // Sort: Highest winAmount first; if winAmount equal, highest betAmount first
                Collections.sort(list, (a, b) -> {
                    if (b.winAmount != a.winAmount) {
                        return Long.compare(b.winAmount, a.winAmount);
                    }
                    return Long.compare(b.betAmount, a.betAmount);
                });

                JSONArray jsonArr = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    RoundParticipant p = list.get(i);
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("rank", i + 1);
                        obj.put("userId", p.userId);
                        obj.put("userName", p.userName);
                        obj.put("userAvatar", p.userAvatar);
                        obj.put("betAmount", p.betAmount);
                        obj.put("winAmount", p.winAmount);
                        jsonArr.put(obj);
                    } catch (Exception ignored) {}
                }

                if (callback != null) callback.onLeaderboardLoaded(jsonArr.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onLeaderboardLoaded("[]");
            }
        });
    }

    private static class RoundParticipant {
        String userId;
        String userName;
        String userAvatar;
        long betAmount;
        long winAmount;
    }
}

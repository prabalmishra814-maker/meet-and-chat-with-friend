package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.DayRewardConfig;
import com.roomchatapps.Pmishra.models.UserCheckInState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// DAILY CHECK-IN
public class DailyCheckInManager {

    public interface StateCallback {
        void onStateLoaded(UserCheckInState state);
        void onError(String error);
    }

    public interface ClaimCallback {
        void onSuccess(String message, DayRewardConfig claimedReward);
        void onError(String error);
    }

    public interface InternalGrantCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // DAILY CHECK-IN STORE REWARD
    public static List<DayRewardConfig> get7DayRewards() {
        List<DayRewardConfig> rewards = new ArrayList<>();
        // DAY 1: 2,500 Coins
        rewards.add(new DayRewardConfig(1, "Day 1", "2,500 Coins", "COIN", 2500L, null, "coin", null));
        // DAY 2: 5,000 Coins
        rewards.add(new DayRewardConfig(2, "Day 2", "5,000 Coins", "COIN", 5000L, null, "coin", null));
        // DAY 3: 10,000 Coins
        rewards.add(new DayRewardConfig(3, "Day 3", "10,000 Coins", "COIN", 10000L, null, "coin", null));
        // DAY 4: Toyota Car (Entry Effect)
        rewards.add(new DayRewardConfig(4, "Day 4", "Toyota Car", "ENTRANCE", 0L, "entrance_toyota_car", "ic_entrance_toyota_car", "Entry/toyota_car_entry.svga"));
        // DAY 5: Crown Circle (Frame)
        rewards.add(new DayRewardConfig(5, "Day 5", "Crown Circle", "FRAME", 0L, "frame_crown_circle", "ic_crown_gold_frame", "frame/crown_circle.svga"));
        // DAY 6: 50,000 Energy
        rewards.add(new DayRewardConfig(6, "Day 6", "50,000 Energy", "ENERGY", 50000L, null, "energy", null));
        // DAY 7: Golden Super Car (Entry Effect)
        rewards.add(new DayRewardConfig(7, "Day 7", "Golden Super Car", "ENTRANCE", 0L, "entrance_golden_super_car", "ic_entrance_golden_car", "Entry/golden_super_car.svga"));

        return rewards;
    }

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    public static DayRewardConfig getRewardForDay(int dayNumber) {
        List<DayRewardConfig> list = get7DayRewards();
        for (DayRewardConfig cfg : list) {
            if (cfg.getDayNumber() == dayNumber) {
                return cfg;
            }
        }
        return list.get(0);
    }

    // DAILY CHECK-IN
    public static void loadUserCheckInState(String uid, StateCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("daily_checkin");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserCheckInState state = new UserCheckInState();
                String today = getTodayDateString();

                if (snapshot.exists()) {
                    String lastClaimDate = snapshot.child("lastClaimDate").getValue(String.class);
                    if (lastClaimDate == null) lastClaimDate = "";

                    int currentDay = 1;
                    Object currentDayObj = snapshot.child("currentDay").getValue();
                    if (currentDayObj != null) {
                        try {
                            currentDay = Integer.parseInt(String.valueOf(currentDayObj));
                        } catch (Exception ignored) {}
                    }

                    Map<String, Boolean> claimedMap = new HashMap<>();
                    DataSnapshot claimedSnap = snapshot.child("claimedDays");
                    if (claimedSnap.exists()) {
                        for (DataSnapshot child : claimedSnap.getChildren()) {
                            Boolean val = child.getValue(Boolean.class);
                            if (val != null) {
                                claimedMap.put(child.getKey(), val);
                            }
                        }
                    }

                    state.setLastClaimDate(lastClaimDate);
                    state.setClaimedDays(claimedMap);

                    if (today.equalsIgnoreCase(lastClaimDate)) {
                        state.setTodayClaimed(true);
                        state.setCurrentDay(Math.min(7, Math.max(1, currentDay)));
                    } else {
                        state.setTodayClaimed(false);

                        int dayDiff = calculateDaysDifference(lastClaimDate, today);
                        if (dayDiff == 1) {
                            // Consecutively claimed yesterday!
                            Boolean lastDayClaimed = claimedMap.get("day_7");
                            if (currentDay >= 7 && (lastDayClaimed != null && lastDayClaimed)) {
                                // Day 7 was completed yesterday, reset cycle to Day 1
                                state.setCurrentDay(1);
                                state.setClaimedDays(new HashMap<>());
                            } else {
                                state.setCurrentDay(Math.min(7, currentDay));
                            }
                        } else if (dayDiff > 1) {
                            // Streak missed! Reset 7-day cycle back to Day 1
                            state.setCurrentDay(1);
                            state.setClaimedDays(new HashMap<>());
                        } else {
                            // First time or fresh state
                            state.setCurrentDay(1);
                        }
                    }
                } else {
                    state.setCurrentDay(1);
                    state.setTodayClaimed(false);
                    state.setLastClaimDate("");
                    state.setClaimedDays(new HashMap<>());
                }

                if (callback != null) callback.onStateLoaded(state);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError("Failed to load check-in state: " + error.getMessage());
            }
        });
    }

    // DAILY CHECK-IN ANTI DUPLICATE
    public static void claimReward(String uid, int dayNumber, ClaimCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        String today = getTodayDateString();
        String claimDedupeKey = today + "_day" + dayNumber;

        // 1. Check atomic claim deduplication node in Firebase
        DatabaseReference claimRef = FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("daily_checkin_claims").child(claimDedupeKey);

        claimRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot claimSnap) {
                if (claimSnap.exists()) {
                    if (callback != null) callback.onError("Today's reward has already been claimed.");
                    return;
                }

                // Reserve claim node to protect against rapid multi-click/duplicate calls
                claimRef.setValue(System.currentTimeMillis()).addOnCompleteListener(claimTask -> {
                    if (!claimTask.isSuccessful()) {
                        if (callback != null) callback.onError("Unable to claim reward. Please try again.");
                        return;
                    }

                    // 2. Fetch current state to ensure valid day claiming
                    loadUserCheckInState(uid, new StateCallback() {
                        @Override
                        public void onStateLoaded(UserCheckInState state) {
                            if (state.isTodayClaimed()) {
                                if (callback != null) callback.onError("Today's reward has already been claimed.");
                                return;
                            }

                            if (state.getCurrentDay() != dayNumber) {
                                if (callback != null) callback.onError("You can only claim Day " + state.getCurrentDay() + " today!");
                                return;
                            }

                            DayRewardConfig config = getRewardForDay(dayNumber);
                            if (config == null) {
                                claimRef.removeValue();
                                if (callback != null) callback.onError("Reward is temporarily unavailable. Please try again later.");
                                return;
                            }

                            // 3. Grant the specific reward type atomically
                            grantRewardToUser(uid, config, new InternalGrantCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    // 4. Update daily_checkin persistence node
                                    DatabaseReference checkInRef = FirebaseDatabase.getInstance().getReference("users")
                                            .child(uid).child("daily_checkin");

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("lastClaimDate", today);
                                    updates.put("claimedDays/day_" + dayNumber, true);

                                    int nextDay = (dayNumber < 7) ? (dayNumber + 1) : 7;
                                    updates.put("currentDay", nextDay);

                                    checkInRef.updateChildren(updates).addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            String successMsg = buildSuccessMessage(dayNumber, config);
                                            if (callback != null) callback.onSuccess(successMsg, config);
                                        } else {
                                            if (callback != null) callback.onError("Failed to record check-in progress.");
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    claimRef.removeValue();
                                    if (callback != null) callback.onError(error != null ? error : "Unable to claim reward. Please try again.");
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            claimRef.removeValue();
                            if (callback != null) callback.onError("Unable to load state: " + error);
                        }
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError("Database error: " + error.getMessage());
            }
        });
    }

    // DAILY CHECK-IN STORE REWARD
    private static void grantRewardToUser(String uid, DayRewardConfig config, InternalGrantCallback callback) {
        String type = config.getRewardType();
        if ("COIN".equalsIgnoreCase(type)) {
            WalletManager.addCoins(uid, config.getRewardAmount(), "DAILY_CHECKIN", "Daily Check-In",
                    "Day " + config.getDayNumber() + " Check-In Reward (+" + config.getRewardAmount() + " Coins)", new WalletManager.WalletCallback() {
                        @Override
                        public void onSuccess(String message, long newCoinBalance) {
                            if (callback != null) callback.onSuccess(message);
                        }

                        @Override
                        public void onError(String error) {
                            if (callback != null) callback.onError(error);
                        }
                    });
        } else if ("ENERGY".equalsIgnoreCase(type)) {
            WalletManager.addEnergyToUser(uid, config.getRewardAmount(), "Daily Check-In",
                    "Day " + config.getDayNumber() + " Check-In Reward (+" + config.getRewardAmount() + " Energy)");
            if (callback != null) callback.onSuccess("Energy added successfully!");
        } else if ("FRAME".equalsIgnoreCase(type) || "ENTRANCE".equalsIgnoreCase(type)) {
            StoreManager.grantStoreItemOwnership(uid, config.getItemId(), new StoreManager.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    if (callback != null) callback.onSuccess(message);
                }

                @Override
                public void onError(String error) {
                    if (callback != null) callback.onError(error);
                }
            });
        } else {
            if (callback != null) callback.onError("Unknown reward type.");
        }
    }

    private static String buildSuccessMessage(int dayNumber, DayRewardConfig config) {
        switch (dayNumber) {
            case 1:
                return "Day 1 reward claimed! +2,500 Coins";
            case 2:
                return "Day 2 reward claimed! +5,000 Coins";
            case 3:
                return "Day 3 reward claimed! +10,000 Coins";
            case 4:
                return "Toyota Car unlocked!";
            case 5:
                return "Crown Circle unlocked!";
            case 6:
                return "50,000 Energy added!";
            case 7:
                return "Golden Super Car unlocked!";
            default:
                return config.getRewardName() + " claimed!";
        }
    }

    private static int calculateDaysDifference(String startDateStr, String endDateStr) {
        if (startDateStr == null || startDateStr.trim().isEmpty() || endDateStr == null || endDateStr.trim().isEmpty()) {
            return -1;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            if (startDate == null || endDate == null) return -1;

            Calendar c1 = Calendar.getInstance();
            c1.setTime(startDate);
            c1.set(Calendar.HOUR_OF_DAY, 0);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            Calendar c2 = Calendar.getInstance();
            c2.setTime(endDate);
            c2.set(Calendar.HOUR_OF_DAY, 0);
            c2.set(Calendar.MINUTE, 0);
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);

            long diffInMillis = c2.getTimeInMillis() - c1.getTimeInMillis();
            return (int) (diffInMillis / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            return -1;
        }
    }
}

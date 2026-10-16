package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.AgencyHostModel;

public class AgencyManager {

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface AgencyCheckCallback {
        void onResult(boolean inAgency, String agencyId, String agencyName, long cooldownUntil);
        void onError(String error);
    }

    public static final long COOLDOWN_DURATION_MS = 0L;

    /**
     * Checks if user is currently in an agency
     */
    public static void checkUserAgencyStatus(String userId, AgencyCheckCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("Invalid User ID");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String agencyId = snapshot.child("agencyId").getValue(String.class);
                    String agencyName = snapshot.child("agencyName").getValue(String.class);

                    boolean inAgency = agencyId != null && !agencyId.isEmpty();

                    if (callback != null) {
                        callback.onResult(inAgency, agencyId, agencyName, 0L);
                    }
                } else {
                    if (callback != null) callback.onResult(false, null, null, 0L);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Joins an agency by Agency ID
     */
    public static void joinAgency(String userId, String targetAgencyId, ActionCallback callback) {
        if (userId == null || targetAgencyId == null || targetAgencyId.trim().isEmpty()) {
            if (callback != null) callback.onError("Please enter a valid Agency ID");
            return;
        }

        String formattedAgencyId = targetAgencyId.trim().toUpperCase();

        // 1. First check if user is already in an agency
        checkUserAgencyStatus(userId, new AgencyCheckCallback() {
            @Override
            public void onResult(boolean inAgency, String currentAgencyId, String currentAgencyName, long cooldownUntil) {
                if (inAgency) {
                    if (callback != null) callback.onError("You are already a member of Agency '" + currentAgencyName + "'. A host can belong to only 1 agency.");
                    return;
                }

                // 2. Search for the agency in Firebase
                searchAndJoinAgency(userId, formattedAgencyId, callback);
            }

            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

    private static void searchAndJoinAgency(String userId, String agencyId, ActionCallback callback) {
        DatabaseReference agenciesRef = FirebaseDatabase.getInstance().getReference("agencies");

        agenciesRef.orderByChild("agencyId").equalTo(agencyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildren().iterator().hasNext()) {
                    DataSnapshot agencySnap = snapshot.getChildren().iterator().next();
                    String ownerUid = agencySnap.getKey();
                    String agencyName = agencySnap.child("agencyName").getValue(String.class);
                    if (agencyName == null) agencyName = "Official Agency";

                    String status = agencySnap.child("status").getValue(String.class);
                    if (status != null && !"APPROVED".equalsIgnoreCase(status)) {
                        if (callback != null) callback.onError("Agency is currently not active.");
                        return;
                    }

                    // Perform join operation
                    executeJoin(userId, ownerUid, agencyId, agencyName, callback);

                } else {
                    // Try checking by owner UID direct key
                    agenciesRef.child(agencyId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot ownerSnap) {
                            if (ownerSnap.exists()) {
                                String agencyName = ownerSnap.child("agencyName").getValue(String.class);
                                if (agencyName == null) agencyName = "Official Agency";

                                executeJoin(userId, agencyId, ownerSnap.child("agencyId").getValue(String.class), agencyName, callback);
                            } else {
                                if (callback != null) callback.onError("Agency not found with ID: " + agencyId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (callback != null) callback.onError(error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    private static void executeJoin(String userId, String ownerUid, String agencyId, String agencyName, ActionCallback callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String hostName = snapshot.child("name").getValue(String.class);
                if (hostName == null) hostName = snapshot.child("userName").getValue(String.class);
                if (hostName == null) hostName = "Host User";

                String hostAvatar = snapshot.child("avtar").getValue(String.class);
                String profileId = snapshot.child("profileId").getValue(String.class);
                if (profileId == null) profileId = userId.substring(0, 6);

                long coins = 0;
                if (snapshot.child("coins").exists()) {
                    Long c = snapshot.child("coins").getValue(Long.class);
                    if (c != null) coins = c;
                }

                AgencyHostModel hostModel = new AgencyHostModel(
                        userId,
                        hostName,
                        hostAvatar,
                        profileId,
                        coins / 10,
                        coins,
                        coins,
                        0.0,
                        System.currentTimeMillis(),
                        "ACTIVE"
                );

                // Add to owner agency hosts node
                FirebaseDatabase.getInstance().getReference("agencies").child(ownerUid)
                        .child("hosts").child(userId).setValue(hostModel);

                // Update user profile node
                userRef.child("agencyId").setValue(agencyId != null ? agencyId : ownerUid);
                userRef.child("agencyName").setValue(agencyName);
                userRef.child("cooldownUntil").removeValue();

                // Notify Agency Owner
                NotificationHelper.sendNewHostJoinedNotification(ownerUid, hostName, profileId);

                if (callback != null) {
                    callback.onSuccess("🎉 Successfully joined Agency '" + agencyName + "'!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Leaves current agency immediately without cooldown
     */
    public static void leaveAgency(String userId, String ownerUid, ActionCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("Invalid User ID");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Fetch host name before leaving to send notification
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String hostName = snapshot.child("name").getValue(String.class);
                if (hostName == null) hostName = snapshot.child("userName").getValue(String.class);
                if (hostName == null) hostName = "Host User";

                userRef.child("agencyId").removeValue();
                userRef.child("agencyName").removeValue();
                userRef.child("cooldownUntil").removeValue();

                if (ownerUid != null && !ownerUid.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("agencies").child(ownerUid)
                            .child("hosts").child(userId).removeValue();

                    // Notify Agency Owner
                    NotificationHelper.sendHostLeftNotification(ownerUid, hostName);
                }

                if (callback != null) {
                    callback.onSuccess("You have successfully left the agency.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userRef.child("agencyId").removeValue();
                userRef.child("agencyName").removeValue();
                userRef.child("cooldownUntil").removeValue();

                if (ownerUid != null && !ownerUid.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("agencies").child(ownerUid)
                            .child("hosts").child(userId).removeValue();
                }

                if (callback != null) {
                    callback.onSuccess("You have successfully left the agency.");
                }
            }
        });
    }
}

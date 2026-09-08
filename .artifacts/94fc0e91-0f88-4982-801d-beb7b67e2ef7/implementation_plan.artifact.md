# Implementation Plan - Multi-Device Audio and Sync Fix

This plan addresses why voice chat is not working across two devices by optimizing the RTC connection, ensuring unique identification, and adding real-time visual feedback for audio levels.

## User Review Required

> [!IMPORTANT]
> - **Scenario Change**: We are switching from `STANDARD_CHATROOM` to `COMMUNICATION`. This scenario is better for low-latency, two-way audio on a wide range of devices.
> - **Test Environment**: We will set `isTestEnv` to `false` to ensure we are using ZEGO's production cluster, which is more reliable for multi-device testing.
> - **Unique Identification**: We will verify that each device uses a unique `userID`. Testing with the same account on two devices will cause audio to fail.

## Proposed Changes

### [ZEGO SDK Logic]

#### [MODIFY] [ZegoManager.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/zego/ZegoManager.java)
- Change `ZegoScenario` to `COMMUNICATION`.
- Set `isTestEnv` to `false` in `createEngine`.
- Add `engine.muteAllPlayStreamAudio(false)` to ensure incoming audio is never globally muted.
- Add `onPlayerStateUpdate` and `onPublisherStateUpdate` detailed logging to catch network blocks.
- Add `setCaptureVolume(100)` and `setPlayVolume(100)`.

#### [MODIFY] [SeatManager.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/zego/SeatManager.java)
- Ensure that if a user is already on a seat, they cannot "take" another seat without leaving the first one (prevents stream collisions).

---

### [Activity & UI]

#### [MODIFY] [RoomChatActivity.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/RoomChatActivity.java)
- Implement `onAudioLevelUpdate`: Show a "Speaking" glow on the seat when the sound level exceeds a threshold.
- Add a "Connection Status" toast (e.g., "Connected to Room," "Publishing Voice").
- Improve `userID` generation to ensure uniqueness even if extras are missing.

#### [MODIFY] [SeatAdapter.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/ui/SeatAdapter.java)
- Add a `setSpeaking(String userID, boolean speaking)` method to toggle the glow ring dynamically based on real-time volume.

## Verification Plan

### Manual Verification
1. **Device A (Host)**:
    - Enter room -> Toast "Host joined" -> Mic icon is ON.
    - Check Logcat for "Publisher state update: PUBLISHING".
2. **Device B (Audience)**:
    - Enter room -> Click empty seat -> "Take Seat" -> Mic icon is ON.
    - Verify Device B hears Device A.
    - Check Logcat for "Player state update: PLAYING".
3. **Audio Levels**:
    - Speak into Device A -> Verify Device A's seat glows on Device B's screen.
4. **Volume Check**:
    - Ensure device volume is up and speaker is selected.

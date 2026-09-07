# Implementation Plan - ZEGO UI Kit to Official SDK Migration

This plan details the complete removal of `ZegoUIKitPrebuilt` and its replacement with the official `ZegoExpressEngine` SDK and a fully custom Android UI, while preserving all existing voice room functionality.

## User Review Required

> [!IMPORTANT]
> - **Seat Management**: We will implement a custom seat management system using ZEGO's **Room Attributes (Extra Info)**. This replaces the internal seat logic of the UI Kit.
> - **UI Changes**: The existing Prebuilt UI will be replaced by a custom layout designed to match the current app's aesthetic (16 seats, custom background, etc.).

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/gradle/libs.versions.toml)
- Add `zegoExpress = "3.15.0"` (or latest stable).
- Add `zego-express = { group = "im.zego", name = "zegoexpress", version.ref = "zegoExpress" }`.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/build.gradle.kts)
- Remove `im.zego:zego_uikit_prebuilt_live_audio_room_android`.
- Add `im.zego:zegoexpress`.

---

### Core ZEGO Logic (New Classes)

#### [NEW] [ZegoManager.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/zego/ZegoManager.java)
- Singleton class to manage `ZegoExpressEngine`.
- Methods: `init(appID, appSign)`, `loginRoom(roomID, userID, userName)`, `logoutRoom()`, `startPublishing()`, `stopPublishing()`, `setSpeakerOn(boolean)`.
- Listener interface for UI updates (room state, user updates, audio quality, etc.).

#### [NEW] [SeatManager.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/zego/SeatManager.java)
- Logic to manage 16 seats using `ZegoExpressEngine.setRoomExtraInfo`.
- Tracks occupancy, muted status, and permissions for each seat.
- Synchronizes seat state across all participants in real-time.

#### [NEW] [ChatManager.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/zego/ChatManager.java)
- Handles in-room text messages using `sendInRoomTextMessage` and `onIMRecvInRoomTextMessage`.

---

### UI Implementation

#### [MODIFY] [activity_room_chat.xml](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/activity_room_chat.xml)
- Replace `fragment_container` with:
    - `AudioRoomBackgroundView` as the base.
    - `RecyclerView` for 16 Seats (GridLayoutManager).
    - `RecyclerView` for Chat Messages.
    - Custom Bottom Menu Bar (Mic, Speaker, Menu, Game, Gift, Settings).

#### [MODIFY] [AudioRoomBackgroundView.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/AudioRoomBackgroundView.java)
- Remove dependency on `com.zegocloud.uikit`.
- Add methods to update user count from the `ZegoManager`.

#### [NEW] [SeatView.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/ui/SeatView.java)
- Standalone custom view for an individual seat (based on the previous `CustomSeatForegroundView` logic).
- Displays avatar, frame, name, mic status, and speaking animation.

#### [NEW] [SeatAdapter.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/ui/SeatAdapter.java)
- Adapter for the 16-seat grid.

#### [NEW] [ChatAdapter.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/ui/ChatAdapter.java)
- Adapter for the in-room chat, using `item_room_chat_message.xml`.

---

### Activity Refactoring

#### [MODIFY] [RoomChatActivity.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/RoomChatActivity.java)
- Replace `ZegoUIKitPrebuiltLiveAudioRoomFragment` logic with `ZegoManager` and custom layout setup.
- Re-bind all bottom buttons to the new `ZegoManager` methods.
- Update UI states (Seat occupancy, Mic status) based on callbacks from `ZegoManager` and `SeatManager`.
- Preserve Firebase-based Gift and Reaction logic.

## Verification Plan

### Automated Tests
- Build project to ensure no dependency conflicts.
- Unit tests for `SeatManager` logic (mocking RoomExtraInfo).

### Manual Verification
- **Room Lifecycle**: Create, Join, and Leave room.
- **Audio**: Verify local mic publishing and remote audio playback.
- **Seats**: Verify taking/leaving seats and synchronization between two devices.
- **Chat**: Send and receive messages.
- **Gifts/Reactions**: Ensure existing Firebase-based features still work.
- **UI**: Verify 16-seat grid layout and animations.

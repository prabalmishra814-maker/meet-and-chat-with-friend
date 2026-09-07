# Implementation Plan - Audio Room SVGA Dialog & Launcher Revert

The goal is to revert the app launcher to its original state and update the `AudioRoomActivity` so that clicking the Gift button opens a simple dialog with the SVGA playback button.

## User Review Required

> [!IMPORTANT]
> - The app will now start with **SplashActivity** again.
> - The "Gifts" menu in the Audio Room will be replaced by a simple dialog containing a single button to play the SVGA animation.

## Proposed Changes

### [Component] Manifest & Launcher

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/AndroidManifest.xml)
- Revert **SplashActivity** as the `LAUNCHER`.
- Remove the `LAUNCHER` intent filter from **SvgaTestActivity**.

---

### [Component] Audio Room UI

#### [NEW] [dialog_svga_play.xml](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/dialog_svga_play.xml)
- A simple `LinearLayout` or `FrameLayout` containing a single `MaterialButton` with the text "Play SVGA Animation".

#### [MODIFY] [AudioRoomActivity.java](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/AudioRoomActivity.java)
- Update `showGiftDialog()`:
    - Inflate the new `dialog_svga_play.xml`.
    - Set a click listener on the button to play the SVGA animation full screen.
- Remove `setupGiftItemClick()` and other references to the old gift cards.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project builds.

### Manual Verification
- Launch the app and verify it starts with the Splash screen.
- Go to the Audio Room.
- Click the Gift FAB.
- Verify a simple dialog appears with one button.
- Click the button and verify the SVGA animation plays in full screen.

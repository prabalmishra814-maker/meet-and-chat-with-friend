# Implementation Plan - SVGA Animation on Heart Gift Click

The user wants to play `assets/betting_pk_bg.svga` in full screen when the "Heart" gift is clicked in `AudioRoomActivity`.

## Proposed Changes

### [Component] Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/gradle/libs.versions.toml)
- Add `svga-player = "2.6.1"` to `[versions]`.
- Add `svga-player = { group = "com.github.yyued", name = "SVGAPlayer-Android", version.ref = "svga-player" }` to `[libraries]`.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/build.gradle.kts)
- Add `implementation(libs.svga.player)` to the dependencies block.

### [Component] UI & Layout

#### [MODIFY] [activity_room.xml](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/activity_room.xml)
- Add `com.opensource.svgaplayer.SVGAImageView` at the bottom of the `ConstraintLayout` to serve as a full-screen animation overlay.

### [Component] Room Activity Logic

#### [MODIFY] [AudioRoomActivity.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/AudioRoomActivity.java)
- Initialize `SVGAParser` in `onCreate`.
- Implement `playSvgaAnimation(String fileName)` method to parse and play the SVGA file.
- Update `showGiftDialog` to call `playSvgaAnimation("betting_pk_bg.svga")` when the "Heart" gift (ID `cardHeart`) is clicked.
- Add an `SVGACallback` to hide the `SVGAImageView` once the animation completes.

## Verification Plan

### Automated Tests
- None, as this is primarily a UI animation task.

### Manual Verification
- Deploy the app.
- Open the Room Activity.
- Click the gift icon.
- Click the "Heart" gift.
- Verify that the SVGA animation `betting_pk_bg.svga` plays in full screen and disappears after finishing.

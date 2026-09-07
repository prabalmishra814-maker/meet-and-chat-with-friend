# Implementation Plan - Play SVGA in Dialog on Selection

When the user selects "Aladin" in the gift dialog, the static image will be replaced by an SVGA animation in the same position.

## Proposed Changes

### [Layout]

#### [MODIFY] [dialog_svga_play.xml](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/dialog_svga_play.xml)
- Wrap `ivAladdin` in a `FrameLayout`.
- Add `com.opensource.svgaplayer.SVGAImageView` with ID `svgaAladdin` inside the `FrameLayout`.
- Set `svgaAladdin` visibility to `gone` by default.
- Ensure both views share the same dimensions (80dp x 80dp).

### [Activity]

#### [MODIFY] [AudioRoomActivity.java](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/AudioRoomActivity.java)
- In `showGiftDialog()`:
    - Find `svgaAladdin` from the inflated view.
    - Update `ivAladdin` click listener:
        - Hide `ivAladdin`.
        - Show `svgaAladdin`.
        - Use the existing `svgaParser` to load `aladdin.svga`.
        - Set the decoded video item to `svgaAladdin` and start animation.

#### [MODIFY] [RoomChatActivity.java](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/RoomChatActivity.java)
- Apply the same changes as in `AudioRoomActivity.java` to `showGiftDialog()`.

## Verification Plan

### Manual Verification
- Open the gift dialog in both `AudioRoomActivity` and `RoomChatActivity`.
- Click on the Aladin icon.
- Verify that the image disappears and the SVGA animation starts playing in its place.
- Click "Send" and verify the animation plays on the main screen as before.

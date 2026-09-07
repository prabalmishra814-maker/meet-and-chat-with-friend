# Implementation Plan - Effect Settings UI

Implement the "Effect settings" sub-menu inside the Props Store. This will allow users to toggle various broadcast effects as seen in the provided screenshots.

## User Review Required

> [!IMPORTANT]
> I will add a click listener to the "Effect settings" row in the Props Store menu.
> Clicking it will open a new `BottomSheetDialog` containing a scrollable list of over 20 toggles for different game and gift effects.

## Proposed Changes

### [UI Components]

#### [MODIFY] [dialog_room_menu.xml](file:///C:/Users/PRABALMISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/dialog_room_menu.xml)
- Add `android:id="@+id/layoutEffectSettings"` to the bottom "Effect settings" `LinearLayout` to make it clickable.

#### [NEW] [dialog_effect_settings.xml](file:///C:/Users/PRABALMISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/dialog_effect_settings.xml)
- A bottom sheet layout with:
    - **Header**: Centered "Effect settings" text.
    - **Scrollable List**: A `NestedScrollView` containing a `LinearLayout` with all the settings mentioned in the screenshots.
    - **Setting Item**: Each item will have a `TextView` (Label) and a `SwitchCompat`.
    - **Footer**: A small info icon and the text "After closing designated broadcasts, you won't receive messages of this type."

#### [MODIFY] [RoomChatActivity.java](file:///C:/Users/PRABALMISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/RoomChatActivity.java)
- In `showRoomMenuDialog()`, find `layoutEffectSettings` and set a click listener to call `showEffectSettingsDialog()`.
- Implement `showEffectSettingsDialog()` to inflate and show `dialog_effect_settings.xml`.

### [Resources]

#### [MODIFY] [themes.xml](file:///C:/Users/PRABALMISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/values/themes.xml)
- Add a style for the setting rows to ensure consistent padding and text appearance.

## Verification Plan

### Manual Verification
- Deploy to device.
- Open the Grid/Menu in the room.
- Click on "Effect settings" at the bottom.
- Verify that a new bottom sheet opens with the list of switches.
- Scroll through the list to ensure all items are present and aligned correctly.

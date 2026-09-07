# Open Room Details Dialog from Dropdown

The goal is to open a `BottomSheetDialog` when the dropdown icon in the room header is clicked. This dialog will include a button to change the room's theme/background.

## Proposed Changes

### UI Components

#### [NEW] [dialog_room_details.xml](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/res/layout/dialog_room_details.xml)
- Design a clean bottom sheet layout:
    - Section for "Room Information".
    - A button or clickable row for "Themed Change".
    - Styling matching the app's dark/glass theme where applicable.

### Logic Changes

#### [MODIFY] [AudioRoomActivity.java](file:///C:/Users/PRABAL%20MISHRA/AndroidStudioProjects/Room_Chat/app/src/main/java/com/roomchatapps/Pmishra/AudioRoomActivity.java)
- Replace the current Toast in `ivTitleDropdown` click listener with a call to `showRoomDetailsDialog()`.
- Implement `showRoomDetailsDialog()`:
    - Use `BottomSheetDialog`.
    - Handle the "Themed Change" button click.
    - As a placeholder for "themed change", I will implement a background color toggle or show a Toast indicating the feature.

## Verification Plan

### Manual Verification
- Click the dropdown icon next to the room title.
- Verify the BottomSheet appears.
- Click the "Themed Change" button and verify it triggers an action.

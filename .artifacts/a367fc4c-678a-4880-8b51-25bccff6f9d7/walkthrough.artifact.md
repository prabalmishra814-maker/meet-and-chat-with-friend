# Edit Profile Status Bar Insets Walkthrough

Fixed status bar transparency and header layout insets in `activity_edit_profile.xml`.

## Changes Made

- Updated [activity_edit_profile.xml](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/meet-and-chat-with-friend/app/src/main/res/layout/activity_edit_profile.xml):
  - Adjusted `headerLayout` top padding to `36dp` so status bar transparency does not cause text/buttons overlap.
- Updated [EditProfileActivity.java](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/meet-and-chat-with-friend/app/src/main/java/com/roomchatapps/Pmishra/EditProfileActivity.java):
  - Applied `ViewCompat.setOnApplyWindowInsetsListener` to dynamically adjust `headerLayout` top padding according to the system status bar inset height.

## Verification Results

- Executed `gradle_build("app:assembleDebug")`: **Build finished successfully**.

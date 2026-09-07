# Fix "No matching client found" Build Error

The build is failing because the `applicationId` in `app/build.gradle.kts` (`com.roomchatapps.amstudio`) does not match the `package_name` in `google-services.json` (`com.roomchatapps.Pmishra`).

## Proposed Changes

### Configuration Files

#### [MODIFY] [google-services.json](file:///C:/Users/PRABAL MISHRA/AndroidStudioProjects/Room_Chat/app/google-services.json)
Update all occurrences of `com.roomchatapps.Pmishra` to `com.roomchatapps.amstudio` to match the project's current package name.

> [!IMPORTANT]
> While this will fix the build error, you should also ensure that the new package name (`com.roomchatapps.amstudio`) is registered in your Firebase Console for project `meet-and-chat-3abdb`. If it's not registered, Firebase services (Auth, Database, etc.) might fail at runtime.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:processDebugGoogleServices` to verify that the task now succeeds.
- Perform a clean build to ensure no other related issues exist.

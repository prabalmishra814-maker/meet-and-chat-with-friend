# Keep App Data Models for Firebase Realtime Database Reflection
-keep class com.roomchatapps.Pmishra.models.** { *; }
-keepclassmembers class com.roomchatapps.Pmishra.models.** { *; }
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
}

# Keep Firebase Database and Auth
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Glide Image Loading
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep Zego UIKit
-keep class im.zego.** { *; }
-dontwarn im.zego.**

# Keep OkHttp & Gson
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

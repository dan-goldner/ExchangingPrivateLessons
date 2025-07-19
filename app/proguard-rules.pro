# Hilt
-keepclassmembers class * {
    @dagger.hilt.android.** *;
}

# Glide generated API
-keep class com.bumptech.glide.GeneratedAppGlideModule { *; }
-keep class com.bumptech.glide.Glide{*;}
-keep class com.bumptech.glide.RequestManager{*;}

# Room schema
-keepclassmembers class androidx.room.RoomDatabase {
    *;
}

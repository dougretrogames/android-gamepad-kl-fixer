# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep Parcelable creators (required to pass models via Intent)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class com.dougretrogames.gamepadklfixer.model.** { *; }

# Keep Kotlin Parcelize runtime
-keep class kotlinx.parcelize.Parcelize { *; }

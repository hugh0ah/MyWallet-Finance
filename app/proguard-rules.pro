# Reglas ProGuard para Room y Gson
-keep class androidx.room.** { *; }
-keep class com.mywallet.finance.data.model.** { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keepattributes *Annotation*
-keepclassmembers enum * { *; }

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Room database rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * { @androidx.room.Dao *; }
-keep class * { @androidx.room.Database *; }

# Kotlinx Serialization rules
-keepclassmembers class * {
    *** Companion;
    *** $serializer;
}

# Ignore JVM-only library warnings in Ktor and standard libraries
-dontwarn java.lang.management.**
-dontwarn javax.naming.**
-dontwarn io.ktor.**


# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keep class com.extremestudios.photographyinvoicelite.** { *; }

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Prevent obfuscation of data classes used by Gson
-keep class com.extremestudios.photographyinvoicelite.InvoiceItem { *; }
-keep class com.extremestudios.photographyinvoicelite.CompletedInvoice { *; }

# Google Play Services Ads
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# WorkManager and Room (required by Play Services Ads / AdMob)
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.sqlite.db.SupportSQLiteOpenHelper$Callback { *; }
-keep class * implements androidx.startup.Initializer { *; }
-keep class androidx.startup.** { *; }
-keep class androidx.sqlite.** { *; }


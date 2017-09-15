# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Sean/Development/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
#-dontobfuscate
-dontwarn org.joda.time.**
-dontwarn com.google.common.**
-dontwarn okio.**
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.tozny.crypto.android.AesCbcWithIntegrity$PrngFixes$* { *; }

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class com.crittercism.** { *; }
-keepclassmembers public class com.crittercism.*
{
    *;
}

-keep public class com.localytics.** { *; }
-keepclassmembers class com.localytics.*
{
    *;
}

-keep class org.apache.** { *; }

-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn org.apache.http.**
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# The official support library.
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.util.** { *; }
-keep class android.support.v4.content.** { *; }
-keep class android.support.v4.view.** { *; }
-keep class android.support.v4.widget.** { *; }

#joda
-keep class org.joda.** { *; }
-keep interface org.joda.** { *; }
-keep enum org.joda.** { *; }

# Guava depends on the annotation and inject packages for its annotations, keep them both
-keep class javax.annotations.** { *; }
-keep class javax.mail.internet.** {*;}
-keep public class javax.inject.*
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }
-keep class org.slf4j.impl.** { *; }
-keep interface org.slf4j.**
-keep enum org.slf4j.**
-ignorewarnings

-keep class * {
    public private *;
}

-dontwarn org.w3c.dom.events.*

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class sun.misc.Unsafe { *; }
#your package path where your gson models are stored

# Acra

# keep this class so that logging will show 'ACRA' and not a obfuscated name like 'a'.
# Note: if you are removing log messages elsewhere in this file then this isn't necessary
-keep class org.acra.ACRA {*;}

# keep this around for some enums that ACRA needs
-keep class org.acra.ReportingInteractionMode {*;}

-keepnames class org.acra.sender.HttpSender$** {*;}
-keepnames class org.acra.ReportField {*;}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter {
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter {
    public void handleSilentException(java.lang.Throwable);
}


# Our app
-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-dontwarn android.webkit.WebResourceError

-dontwarn ui.EventLogFragment$setupAndShowFullscreenImage$1$1
-keep class com.hubble.** { *; }
-keep interface com.hubble.** { *; }
-keep class registration.hubble.** { *; }
-keep interface registration.hubble.** { *; }
-keep class base.hubble.** { *; }
-keep interface base.hubble.** { *; }
-keep class com.media.ffmpeg.** { *; }
-keep interface com.media.ffmpeg { *; }
-keep class com.cvision.stun.** { *; }
-keep interface com.cvision.stun { *; }
-keep class com.nxcomm.jstun_android.** { *; }
-keep interface com.nxcomm.jstun_android { *; }
-keep class com.hubble.p2p.** { *; }
-keep interface com.hubble.p2p { *; }
-keepclassmembers class ** {
    public void onEvent*(**);
}


-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-dontwarn android.webkit.WebResourceError

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
-keep interface com.nostra13.universalimageloader.core.assist.** { *; }
-keep class com.nostra13.universalimageloader.core.assist.** { *; }

-dontwarn org.apache.lang.**
-dontwarn java.net.**
-dontwarn com.android.org.conscrypt.**
-dontwarn org.apache.harmony.xnet.provider.jsse.**
-dontwarn crittercism.android.**
-dontwarn com.crittercism.internal.**

-keep class com.hubble.framework.** { *; }
-keep class com.hubble.registration.** { *; }
-keep class com.hubble.setup.** { *; }
-keep class com.hubble.ui.** { *; }
-keep class com.google.android.gms.wallet.** { *; }

-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep class android.support.v7.util.** { *; }
-keep class android.support.v7.content.** { *; }
-keep class android.support.v7.view.** { *; }
-keep class android.support.v7.widget.** { *; }

-keepattributes InnerClasses,Exceptions,EnclosingMethod
-keep class com.google.gson.** { *; }
-keep class okio.** { *; }

-keep,includedescriptorclasses class com.android.volley.** { *; }
-keep,includedescriptorclasses class com.localytics.android.** { *; }
-keep,includedescriptorclasses class com.actor.model.** { *; }
-keep,includedescriptorclasses class com.nxcomm.blinkhd.** { *; }

-dontwarn com.google.android.gms.wallet.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
-dontwarn com.fasterxml.jackson.core.json.JsonReadContext


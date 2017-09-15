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
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-allowaccessmodification
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-repackageclasses ''

-keep public class * extends android.app.Activity
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class * {
    public protected *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-dontwarn org.joda.time.**
-dontwarn com.google.common.**
-dontwarn okio.**

-assumenosideeffects class android.util.Log {
public static boolean isLoggable(java.lang.String, int);
public static int d(...);
public static int v(...);
public static int i(...);
public static int w(...);
public static int e(...);
}

-keep class com.tozny.crypto.android.AesCbcWithIntegrity$PrngFixes$* { *; }

-keep public class com.crittercism.** { *; }
-keepclassmembers public class com.crittercism.*{ *;}

-keep public class  com.zaius.** { *; }
-keepclassmembers class  com.zaius.* { *;}

-keep public class com.localytics.** { *; }
-keepclassmembers class com.localytics.*
{
    *;
}

-keep class org.apache.** { *; }

-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn org.apache.http.**

# The official support library.
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.util.** { *; }
-keep class android.support.v4.content.** { *; }
-keep class android.support.v4.view.** { *; }
-keep class android.support.v4.widget.** { *; }
-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep class android.support.v7.util.** { *; }
-keep class android.support.v7.content.** { *; }
-keep class android.support.v7.view.** { *; }
-keep class android.support.v7.widget.** { *; }

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn com.google.ads.**
##--End: proguard configuration for Gson  ---

-keep class com.example.model.** { *; }

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
-keep class com.securepreferences.** { *; }
-keep class com.discovery.** { *; }
-keepclassmembers class ** {
    public void onEvent*(**);
}

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


# ActiveAndroid
-keep class com.activeandroid.** { *; }
-keep class com.activeandroid.**.** { *; }
-keep class * extends com.activeandroid.Model
-keep class * extends com.activeandroid.serializer.TypeSerializer

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.hubble.framework.** { *; }
-keep class com.hubble.registration.** { *; }
-keep class com.hubble.setup.** { *; }
-keep class com.hubble.ui.** { *; }
-keep class com.google.android.gms.wallet.** { *; }
-keep class com.hubble.devcomm.** { *; }


-dontwarn org.w3c.dom.events.*
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



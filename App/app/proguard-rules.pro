# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/sangxiang/Library/Android/sdk/tools/proguard/proguard-android.txt
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
#忽略混淆的文件（规则）：
#1.如果使用了Gson之类的工具要使JavaBean类即实体类不被混淆。
#2.如果使用了自定义控件那么要保证它们不参与混淆。
#3.如果使用了枚举要保证枚举不被混淆。
#4.对第三方库中的类不进行混淆
#a.混淆时保护引用的第三方jar包
#如：-libraryjars libs/baidumapapi_v3_2_0.jar  #保护引用的第三方jar包不被混淆
#b.混淆时保护第三方jar包中的类不被混淆
#如：-keep class com.baidu.** { *; }   #让ProGuard不要警告找不到com.baidu.**这个包里面的类的相关引用
#-dontwarn com.baidu.**  #保持com.baidu.**这个包里面的所有类和所有方法不被混淆。

#Android系统组件，系统组件有固定的方法被系统调用。
#被Android Resource 文件引用到的。名字已经固定，也不能混淆，比如自定义的View 。
#Android Parcelable ，需要使用android 序列化的。
#其他Anroid 官方建议 不混淆的，如
#android.app.backup.BackupAgentHelper
#android.preference.Preference
#com.android.vending.licensing.ILicensingService
#Java序列化方法，系统序列化需要固定的方法。
#枚举 ，系统需要处理枚举的固定方法。
#本地方法，不能修改本地方法名
#annotations 注释
#数据库驱动
#有些resource 文件
#用到反射的地方

#心得：
#  1.grade构建必须没有warn和error，不然刷入的版本依旧是上一个版本，这里要特别注意warn！
#2.ClassNotFoundException，NoSuchMethodError
#
#     原因：这种异常会在好多情况下出现，比如：本地代码通过反射调用其他的类，但是经过了混淆之后，就会出现如上异常；调用了JNI之后，C或者C++和java代码进行交互的时候找不到java的类或者方法，导致发生了异常......等等，还有好多。
#
#     解决办法：只需要将被调用的Java类标注为不混淆即可。 -keep class package.classname{*;}
#
#     3.ExceptionInInitializerError
#
#     原因：这是由于类初始化的时候发生了异常。
#     解决办法：找到具体是哪里的类哪个方法哪个类初始化的时候发生的异常，然后解决问题。
#
#     注：遇到这个错误，首先要确认是不是因为第三方的jar包导致的。如果不是的话，就找本地代码，看是不是写的有问题。如果确实是因为第三方jar包的代码导致的，尽量找到源码或者反编译，查看问题到底是什么引起的，然后找到相应的配置在proguard里面配置。
#     例如：我们项目中碰到过一个问题，就是因为第三方的jar包里面有一个字段初始化的时候报了空指针，然后导致我们的代码报了上面的错。当时很奇怪，为什么第三方的jar包还能报错，最后调查了之后才发现，是因为人家用到了类的注解，而proguard在混淆优化的时候把注解去掉了，所以报了空指针，只需要在proguard里面加上保护注解就可以了-keepattributes *Annotation*
#
#4.ClassCastException
#
#     原因：类强制转换的时候出错。
#     解决办法：找到代码，看是代码写的问题，还是混淆后的问题。如果没有混淆正常运行的话，一般都是因为混淆后遇到了各种问题才报的错。我们项目中遇到的问题是因为没有让proguard保持泛型，所以强转的时候报错。只需要在proguard文件里面加上泛型即可-keepattributes Signature
#
#
#5.Resources$NotFoundException(resource not found)
#
#资源没有找到，是因为第三方jar包或者自己的代码是通过反射获得R文件中的资源，所以需要将R文件屏蔽掉
#    原因：代码进行了混淆，R文件没有了，所以通过反射获取的R文件找不到
#
#解决办法：在proguard文件里设置不混淆R文件    -keep class **.R$* { *; }
#
#6. Missing type parameter. or java.lang.ExceptionInInitializerError
#
#可能是泛型混淆了 泛型即可-keepattributes Signature
#指定代码的压缩级别
-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
 #预校验
-dontpreverify
 #混淆时是否记录日志
-verbose
 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保护注解
-keepattributes *Annotation*
#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature

###-----------基本配置-不能被混淆的------------
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class com.android.vending.licensing.ILicensingService


#support.v4/v7包不混淆
-keep class android.support.** { *; }
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.** { *; }
-keep public class * extends android.support.v7.**
-keep interface android.support.v7.app.** { *; }
-dontwarn android.support.**    # 忽略警告

#保持注解继承类不混淆
-keep class * extends java.lang.annotation.Annotation {*;}

#保持Serializable实现类不被混淆
-keepnames class * implements java.io.Serializable
#保持Serializable不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#保持枚举enum类不被混淆
-keepclassmembers enum * {
  public static **[] values();
 public static ** valueOf(java.lang.String);
}
#自定义组件不被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}


###-----------第三方jar包library混淆配置------------
#andpermission代码混淆
-keepclassmembers class ** {
    @com.yanzhenjie.permission.PermissionYes <methods>;
}
-keepclassmembers class ** {
    @com.yanzhenjie.permission.PermissionNo <methods>;
}

#butterknife代码混淆
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#BaseQuickAdapter
-keep class com.chad.library.adapter.** {
   *;
}


# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**


# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-keep class com.sangxiang.app.http.entity.**{*;}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
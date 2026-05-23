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

# Preserve line numbers for readable Crashlytics / Play Console stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepnames class kotlin.Pair { *; }
-keepnames class kotlin.Triple { *; }
# Keep all AI model classes - reflection is used in GemmaClient.toJsonMap()
# to introspect field names/types and build JSON schemas for AI prompts.
# Obfuscation would turn field names into a, b, c breaking prompt generation.
-keep class com.ilustris.sagai.core.ai.model.** { *; }
-keep class com.ilustris.sagai.core.ai.** { *; }

# Gson metadata for local database backups (SOS / settings restore list).
-keep class com.ilustris.sagai.core.database.backup.** { *; }

# Use ** (not *) to match nested feature packages like:
# saga.chat.data.model, saga.detail.data.model, characters.relations.data.model, etc.
-keep class com.ilustris.sagai.features.**.data.model.** { *; }

# Models in domain.model packages (e.g. ShareText in features.share.domain.model)
-keep class com.ilustris.sagai.features.**.domain.model.** { *; }

-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.client.features.** { *; }

-keepattributes Signature,InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

-keep class com.ilustris.sagai.features.playthrough.data.model.** { *; }
-keep class com.ilustris.sagai.core.narrative.** { *; }

# Gson core (TypeAdapter factories, etc.)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keepclassmembers class * {
 @com.google.gson.annotations.SerializedName <fields>;
 }
-keepclassmembers enum * {
 @com.google.gson.annotations.SerializedName <fields>;
 }

# Gson TypeToken used by GemmaClient structured AI responses.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
 -keepclassmembers class * extends androidx.work.Worker {
     public <init>(android.content.Context,androidx.work.WorkerParameters);
 }

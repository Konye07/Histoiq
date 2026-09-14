# Add project specific ProGuard rules here.

# kotlinx.serialization: the plugin generates a $serializer companion for every @Serializable
# class, discovered by Supabase's decode<T>()/decodeList<T>() calls via a generic type parameter —
# a call shape R8's static reachability analysis can miss, since nothing names these classes
# directly. Without these rules, a release build can silently strip or rename something the
# generated serializer needs, only surfacing as a runtime crash the first time a real user hits a
# Supabase call (never in a debug build, since isMinifyEnabled is false there).
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations
-keep,includedescriptorclasses class com.konye.historiq.**$$serializer { *; }
-keepclassmembers class com.konye.historiq.** {
    *** Companion;
}
-keepclasseswithmembers class com.konye.historiq.** {
    kotlinx.serialization.KSerializer serializer(...);
}

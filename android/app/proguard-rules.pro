# Preserve all classes from Kotlin libraries
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep @Parcelize annotated classes
-keep class kotlinx.parcelize.** { *; }

# Keep specific classes that are being removed
-keep class kotlin.jvm.internal.SourceDebugExtension { *; }

-dontwarn kotlin.jvm.internal.SourceDebugExtension
-dontwarn kotlinx.parcelize.Parcelize
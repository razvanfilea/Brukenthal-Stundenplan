# More optimisations
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Remove Kotlin Null-Checks
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# PDF Library
-keep class com.shockwave.**

# Add project specific ProGuard rules here.

# ViewModel — ViewModelProvider.Factory instantiates via reflection
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Fragment — FragmentManager re-creates by class name after rotation / process death
-keep class * extends androidx.fragment.app.Fragment { *; }

# Enum — values() / valueOf() looked up by name (e.g. Prefs serialization)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin data class componentN() — destructuring
-keepclassmembers class * {
    public ** component*();
}

# Source / line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Custom Views referenced from res/layout/*.xml — LayoutInflater resolves by exact name
-keep class farmyard.tractortrip.lab.OutlinedTextView { *; }
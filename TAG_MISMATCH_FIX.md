# Tag Mismatch Error - Fix Documentation

## Problem Description
After the last merge, when building the project in Android Studio, you encountered a "tag mismatch" error. The error appeared after Android Studio downloaded files from Google and Maven repositories.

The error message indicated:
```
2 issues were found when checking AAR metadata:
1. Dependency 'androidx.activity:activity:1.12.0' requires Android Gradle plugin 8.9.1 or higher.
   This build currently uses Android Gradle plugin 8.9.0.
2. Dependency 'androidx.navigationevent:navigationevent-android:1.0.0' requires Android Gradle plugin 8.9.1 or higher.
   This build currently uses Android Gradle plugin 8.9.0.
```

## Root Cause
The project was configured to use **Android Gradle Plugin (AGP) version 8.9.0**, which doesn't actually exist in Maven repositories yet. As of January 2026, the latest stable AGP versions are in the 8.6-8.7 range.

When Android Studio tried to sync the project:
1. It attempted to download AGP 8.9.0 - which doesn't exist
2. Some newer dependencies have AAR metadata requiring AGP 8.9.1+
3. This created a confusing "version mismatch" error (the "tag mismatch")

## Solution
We downgraded the Android Gradle Plugin to a stable, available version and simplified the repository configuration.

### Changes Made

#### 1. gradle/libs.versions.toml
Changed AGP version from non-existent 8.9.0 to stable 8.6.0:
```toml
[versions]
agp = "8.6.0"  # Changed from "8.9.0"
```

#### 2. settings.gradle.kts  
Simplified repository configuration to use Gradle's `google()` helper:
```kotlin
pluginManagement {
    repositories {
        google()  # Simplified from manual maven configuration
        mavenCentral()
        gradlePluginPortal()
    }
}
```

## How to Apply the Fix

1. **Pull the changes** from this PR to your local machine

2. **Open Android Studio**

3. **Sync the project**:
   - Click "File" → "Sync Project with Gradle Files"
   - Or click the "Sync Now" button that appears in the notification banner

4. **Build the project**:
   - Click "Build" → "Rebuild Project"
   - The build should now complete successfully!

## Why AGP 8.6.0?
- **8.6.0** is a stable, well-tested version that's widely available in Maven repositories
- It supports all the features and dependencies used in this project
- It's compatible with the AndroidX libraries (like `androidx.activity:activity:1.12.0`)
- It works with Gradle 8.14.3 (the version this project uses)

## Additional Notes
- The error message saying "requires 8.9.1 or higher" was misleading because 8.9.x versions don't exist yet
- AGP 8.6.0 fully supports API level 34 and 35, which is what the project requires (compileSdk = 36)
- The `google()` repository helper is the recommended way to configure Google's Maven repository

## Verification
After applying this fix, you should be able to:
- ✅ Sync the project in Android Studio without errors
- ✅ Build the project successfully
- ✅ Run the app on an emulator or device

If you still encounter issues, please check:
1. Internet connection (to download dependencies)
2. Android Studio version (should be recent, e.g., Hedgehog or newer)
3. JDK version (should be JDK 17 or newer)

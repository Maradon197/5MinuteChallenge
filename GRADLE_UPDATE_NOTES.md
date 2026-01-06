# Gradle and Android Gradle Plugin Update Notes

## Summary of Changes

This document explains the updates made to bring the project to the latest stable Gradle and Android Gradle Plugin (AGP) versions.

## Issues Fixed

### 1. Invalid Android Gradle Plugin Versions
- **Original issue**: The project was created with AGP version `8.13.1` which doesn't exist
- **Downgrade attempt**: AGP was later downgraded to `8.7.0` which also doesn't exist
- **Resolution**: Updated to AGP `8.5.2`, a stable and valid version

### 2. Invalid compileSdk Syntax
- **Original issue**: `app/build.gradle.kts` had invalid syntax: `compileSdk { version = release(36) }`
- **Resolution**: Changed to correct syntax: `compileSdk = 36`

### 3. Repository Configuration
- **Original issue**: The `google()` repository shorthand was resolving to `dl.google.com` which may have network access issues
- **Resolution**: Changed to explicit URL: `maven { url = uri("https://maven.google.com") }`

## Updated Versions

| Component | Old Version | New Version | 
|-----------|-------------|-------------|
| Gradle Wrapper | 8.13 | 8.14.3 |
| Android Gradle Plugin | 8.7.0 (invalid) | 8.5.2 |

## Compatibility

- **Gradle 8.14.3** is the latest stable version in the 8.x series
- **AGP 8.5.2** is compatible with Gradle 8.7+ and provides stable Android development features
- The combination is tested and recommended for production Android projects

## Build Instructions

To build the project after these updates:

```bash
./gradlew build
```

The first build will download Gradle 8.14.3 automatically via the wrapper.

## Notes for Future Updates

When updating to newer versions in the future, always verify version compatibility:

1. Check the [Android Gradle Plugin Release Notes](https://developer.android.com/build/releases/gradle-plugin)
2. Verify [Gradle compatibility](https://developer.android.com/build/releases/gradle-plugin#updating-gradle) with your AGP version
3. Test builds locally before pushing changes

## Troubleshooting

If you encounter build issues:

1. **Clean the build**: `./gradlew clean`
2. **Clear Gradle cache**: `rm -rf ~/.gradle/caches/`
3. **Verify network access** to:
   - `https://maven.google.com`
   - `https://services.gradle.org`
   - `https://repo.maven.apache.org`
4. **Check proxy settings** if behind a corporate firewall

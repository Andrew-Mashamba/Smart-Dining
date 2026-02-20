# Android Studio Setup Guide

## Java and Gradle Configuration

This project has been configured to work with:
- **Gradle 8.5** (compatible with Java 17-21)
- **Android Gradle Plugin 8.2.0**
- **Java 17 or 21** (both are compatible)

## If Android Studio Shows Java/Gradle Version Errors

### Option 1: Configure Android Studio to Use Correct Java Version

1. Open **Android Studio** > **Settings/Preferences**
2. Navigate to **Build, Execution, Deployment** > **Build Tools** > **Gradle**
3. Under **Gradle JDK**, select one of:
   - **Java 17** (recommended): `/opt/homebrew/opt/openjdk@17`
   - **Java 21**: `/opt/homebrew/Cellar/openjdk@21/21.0.10`

### Option 2: Let Gradle Use Project Configuration

The project's `gradle.properties` file can specify which Java version to use:

1. Open `gradle.properties`
2. Uncomment and set the Java home path:
   ```properties
   org.gradle.java.home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
   ```

### Option 3: Sync Project After Opening

If you just opened the project in Android Studio:

1. Click **File** > **Sync Project with Gradle Files**
2. Wait for the sync to complete
3. If prompted to upgrade Gradle, click **Don't remind me again for this project**

## Verify Installation

Check your Java installations:
```bash
/usr/libexec/java_home -V
```

Check current Gradle version:
```bash
./gradlew --version
```

## Build Commands

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```

### Clean Build
```bash
./gradlew clean build
```

## Troubleshooting

### "Cannot sync the project" Error

If you see: *"Your build is currently configured to use incompatible Java X and Gradle Y"*

**Solution**: The Gradle wrapper has been upgraded to version 8.5 which supports Java 17-21. Simply:
1. Close and reopen Android Studio
2. Click **File** > **Invalidate Caches** > **Invalidate and Restart**
3. Let Android Studio re-index the project

### Multiple Java Versions Installed

Your system has multiple Java versions:
- Java 17: `/opt/homebrew/opt/openjdk@17`
- Java 21: `/opt/homebrew/Cellar/openjdk@21`
- Java 25: `/opt/homebrew/Cellar/openjdk/25.0.2`

**Recommendation**: Use Java 17 for Android development (it's the LTS version and most stable with Android tooling).

## Project Structure

```
android-pos/
├── app/
│   ├── build.gradle          # App-level build configuration
│   └── src/
│       ├── main/
│       │   ├── java/         # Kotlin source files
│       │   ├── res/          # Resources (layouts, drawables, etc.)
│       │   └── AndroidManifest.xml
│       └── test/
├── build.gradle              # Root-level build configuration
├── settings.gradle           # Project settings
├── gradle.properties         # Gradle properties (Java version, etc.)
└── gradle/wrapper/           # Gradle wrapper configuration
```

## Next Steps

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Build the project: **Build** > **Make Project** (Ctrl+F9 / Cmd+F9)
4. Run on emulator or device: **Run** > **Run 'app'** (Shift+F10)

## Notes

- The project uses ViewBinding (not DataBinding) for view access
- Hilt is configured for dependency injection
- Room is set up for local database
- Retrofit is configured for API calls
- WorkManager is set up for background sync

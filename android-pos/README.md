# Sea Cliff POS - Android Application

Native Android application for waiter point of sale operations.

## Features

- **Offline-First**: Works without internet connection
- **Real-time Sync**: Automatic background synchronization
- **Order Management**: Create, edit, and manage orders
- **Table Management**: Assign orders to tables
- **Menu Browsing**: Browse and search menu items
- **Payment Processing**: Handle cash and digital payments
- **Offline Queue**: Queue orders when offline, sync when online

## Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **DI**: Hilt for dependency injection
- **Database**: Room (SQLite) for local storage
- **Networking**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines + Flow
- **Background Tasks**: WorkManager

## Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build Tool**: Gradle 8.2

### Libraries

- AndroidX Core, AppCompat, Material Design
- Lifecycle, ViewModel, LiveData
- Room Database
- Retrofit, OkHttp
- Hilt (Dependency Injection)
- WorkManager
- Coroutines
- Navigation Component

## Project Structure

```
app/src/main/java/com/seacliff/pos/
├── ui/                     # UI Layer
│   ├── activities/         # Activities
│   ├── fragments/          # Fragments
│   ├── adapters/           # RecyclerView adapters
│   └── viewmodels/         # ViewModels
├── data/                   # Data Layer
│   ├── model/              # Data models
│   ├── repository/         # Repositories
│   ├── local/              # Local database
│   │   ├── database/       # Room database
│   │   └── dao/            # Data Access Objects
│   └── remote/             # Remote API
│       ├── api/            # API service interfaces
│       └── dto/            # Data Transfer Objects
├── utils/                  # Utilities
├── di/                     # Dependency Injection modules
└── SeaCliffApp.kt          # Application class
```

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 34

### Installation

1. **Open in Android Studio**
   ```
   File -> Open -> select android-pos folder
   ```

2. **Configure API Endpoint**

   Create `local.properties` file in root:
   ```properties
   api.base.url=http://10.0.2.2:8000/api/
   api.timeout=30
   sync.interval=300
   offline.mode=true
   ```

3. **Sync Gradle**
   ```
   File -> Sync Project with Gradle Files
   ```

4. **Build Project**
   ```
   Build -> Make Project
   ```

## Running the App

### Development

1. **Connect device or start emulator**
2. **Run**
   ```
   Run -> Run 'app'
   ```

### Debug Build

```bash
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release.apk`

## Configuration

### API Endpoints

Update `local.properties`:

```properties
# For emulator (localhost)
api.base.url=http://10.0.2.2:8000/api/

# For physical device (same network)
api.base.url=http://192.168.1.100:8000/api/

# For production
api.base.url=https://api.seacliff.com/api/
```

### Sync Settings

```properties
# Sync interval in seconds (default: 300 = 5 minutes)
sync.interval=300

# API timeout in seconds (default: 30)
api.timeout=30
```

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Code Quality

### Lint

```bash
./gradlew lint
```

### Format Code

```bash
./gradlew ktlintFormat
```

## Building for Production

1. **Generate signing key**
   ```bash
   keytool -genkey -v -keystore seacliff-pos.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias seacliff-pos
   ```

2. **Configure signing** in `app/build.gradle`:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file("../seacliff-pos.jks")
               storePassword "your_password"
               keyAlias "seacliff-pos"
               keyPassword "your_password"
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
           }
       }
   }
   ```

3. **Build release APK**
   ```bash
   ./gradlew assembleRelease
   ```

## Troubleshooting

### Sync Issues

If Gradle sync fails:
1. `File -> Invalidate Caches -> Invalidate and Restart`
2. Delete `.gradle` folder and sync again
3. Update Gradle version in `gradle-wrapper.properties`

### Database Issues

Clear app data:
```bash
adb shell pm clear com.seacliff.pos
```

### Network Issues

Check `network_security_config.xml` for localhost access during development.

## Contributing

See [../CONTRIBUTING.md](../CONTRIBUTING.md) for contribution guidelines.

## License

Proprietary - All rights reserved

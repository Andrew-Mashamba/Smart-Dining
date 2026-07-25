# POS POS - Android Application

Native Android point-of-sale application for POS restaurant, built with modern Android architecture components and offline-first design.

## Features

### Core Functionality
- **Offline-First Architecture**: Full functionality without internet
- **Real-time Sync**: Automatic background sync every 5 minutes
- **Staff Authentication**: Secure login with role-based access
- **Table Management**: Visual grid with status indicators
- **Order Creation**: Cart-based ordering with menu browsing
- **Order Management**: Status tracking and updates
- **Menu Browsing**: Category filtering and search
- **Payment Processing**: Multiple payment methods support

## Architecture

**Pattern**: MVVM (Model-View-ViewModel) with Clean Architecture

```
app/src/main/java/com/seacliff/pos/
├── data/
│   ├── local/
│   │   ├── entity/         # 7 Room entities
│   │   ├── dao/            # 7 DAO interfaces
│   │   ├── database/       # AppDatabase
│   │   ├── converter/      # Type converters
│   │   └── prefs/          # SharedPreferences
│   ├── remote/
│   │   ├── api/            # Retrofit services
│   │   └── dto/            # API DTOs
│   └── repository/         # 5 repositories
├── ui/
│   ├── viewmodel/          # 5 ViewModels
│   ├── activities/         # 6 activities
│   └── adapters/           # 4 RecyclerView adapters
├── di/                     # Hilt DI modules
├── worker/                 # Background sync
└── util/                   # Resource wrapper
```

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9.20 |
| Min SDK | Android 7.0 | API 24 |
| Target SDK | Android 14 | API 34 |
| Database | Room | 2.6.1 |
| Network | Retrofit | 2.9.0 |
| DI | Hilt | 2.48 |
| Async | Coroutines | 1.7.3 |
| Lifecycle | ViewModel/LiveData | 2.7.0 |
| Background | WorkManager | 2.9.0 |
| Images | Glide | 4.16.0 |
| Logging | Timber | 5.0.1 |

## Implementation Details

### Data Layer

#### Room Entities (7)
1. **GuestEntity** - Customer data with loyalty points
2. **TableEntity** - Restaurant tables with status
3. **StaffEntity** - Staff authentication and roles
4. **MenuItemEntity** - Menu with categories and pricing
5. **OrderEntity** - Orders with offline sync support
6. **OrderItemEntity** - Order line items
7. **PaymentEntity** - Payment transactions

#### Repositories (5)
- **AuthRepository** - Authentication & session management
- **MenuRepository** - Menu with offline caching
- **TableRepository** - Table status management
- **OrderRepository** - Order CRUD with offline queue
- **PaymentRepository** - Payment processing

### Presentation Layer

#### ViewModels (5)
- **AuthViewModel** - Login/logout state
- **MenuViewModel** - Menu browsing with search
- **TableViewModel** - Table selection
- **OrderViewModel** - Cart management
- **PaymentViewModel** - Bill generation

#### Activities (6)
- **LoginActivity** - Staff authentication
- **MainActivity** - Dashboard
- **TablesActivity** - Table grid with filters
- **OrderActivity** - Menu + cart + checkout
- **OrdersActivity** - Order list with filters
- **MenuActivity** - Full menu management

#### Adapters (4)
- **TableAdapter** - Table cards with DiffUtil
- **MenuAdapter** - Menu items with images
- **CartAdapter** - Cart with quantity controls
- **OrderListAdapter** - Order cards with sync status

### Background Sync

**SyncWorker** (WorkManager)
- Periodic sync: 5-minute intervals
- Network constraints
- Exponential backoff retry
- Hilt integration

**SyncManager**
- Schedule periodic sync
- Trigger immediate sync
- Monitor sync status

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK API 34

### Installation

1. **Open Project**
   ```bash
   File → Open → android-pos/
   ```

2. **Configure API**

   Create `local.properties`:
   ```properties
   api.base.url="http://10.0.2.2:8000/api/"
   api.timeout=30
   sync.interval=300
   ```

3. **Sync Gradle**
   ```bash
   File → Sync Project with Gradle Files
   ```

4. **Build**
   ```bash
   ./gradlew assembleDebug
   ```

## Configuration

### API Endpoints

```properties
# Development (Emulator)
api.base.url="http://10.0.2.2:8000/api/"

# Development (Physical Device - same network)
api.base.url="http://192.168.1.100:8000/api/"

# Production
api.base.url="https://api.seacliff.com/api/"
```

### Sync Settings

```properties
sync.interval=300    # Seconds (5 minutes)
api.timeout=30       # Request timeout
```

## Running

### Debug
```bash
./gradlew installDebug
adb shell am start -n com.seacliff.pos.debug/com.seacliff.pos.ui.activities.LoginActivity
```

### Release
```bash
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

## API Integration

### Endpoints Used

```
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/auth/me

GET    /api/menu
GET    /api/menu/categories
PUT    /api/menu/{id}/availability

GET    /api/tables
PUT    /api/tables/{id}/status

POST   /api/orders
GET    /api/orders
PUT    /api/orders/{id}/status

POST   /api/payments
GET    /api/orders/{orderId}/bill

GET    /api/guests/phone/{phone}
POST   /api/guests
```

## Database Schema

```sql
-- 7 tables with foreign key relationships
guests
├─ orders (guest_id FK)
│  ├─ order_items (order_id FK, menu_item_id FK)
│  └─ payments (order_id FK)
tables
├─ orders (table_id FK)
staff
├─ orders (waiter_id FK)
menu_items
└─ order_items (menu_item_id FK)
```

## Offline Behavior

1. **Local-First Reads**: All data from Room database
2. **Offline Writes**: Orders marked `isSynced = false`
3. **Background Sync**: WorkManager syncs when online
4. **Conflict Resolution**: Last-write-wins
5. **Sync Indicators**: UI shows pending sync status

## Testing

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

## Default Credentials

Use these for testing (from Laravel seeders):

- **Waiter**: `waiter@seacliff.com` / `password`
- **Manager**: `manager@seacliff.com` / `password`
- **Admin**: `admin@seacliff.com` / `password`

## Screen Flow

```
LoginActivity
    ↓ (successful login)
MainActivity (Dashboard)
    ├─ TablesActivity
    │   └─ OrderActivity (create order)
    │       └─ [Order Created] → back to Tables
    ├─ OrdersActivity (view/manage orders)
    ├─ MenuActivity (view/update menu)
    └─ [Logout] → LoginActivity
```

## Key Features

### 1. Smart Cart Management
- Add/remove items
- Adjust quantities
- Add special instructions per item
- Real-time total calculation
- Clear cart option

### 2. Category Filtering
- All items
- Appetizers
- Main courses
- Desserts
- Drinks

### 3. Search Functionality
- Search by item name
- Search by description
- Real-time filtering

### 4. Status Tracking
```
Orders: pending → confirmed → preparing → ready → served → completed
Tables: available → occupied → available
Sync: pending → synced
```

### 5. Role-Based Access
- **Waiter**: Tables, Orders, Menu viewing
- **Manager**: + Payments, Reports
- **Admin**: Full access

## Security

- ✅ Token-based authentication
- ✅ Encrypted SharedPreferences
- ✅ HTTPS in production
- ✅ SQL injection prevention (Room)
- ✅ Input validation
- ✅ Session timeout

## Performance

- Lazy loading with RecyclerView
- Image caching (Glide)
- Database indexing
- DiffUtil for efficient updates
- Coroutine-based async
- ViewBinding (no findViewById)

## Known Limitations

1. Payment Activity UI incomplete
2. Full XML layouts need completion (using stubs)
3. Guest selection defaults to ID 1
4. Images require network
5. No receipt printing yet

## Future Enhancements

- [ ] Complete payment UI
- [ ] Bluetooth receipt printing
- [ ] Barcode scanning
- [ ] Push notifications
- [ ] Split bill
- [ ] Table reservations
- [ ] Analytics dashboard
- [ ] Staff performance tracking

## Troubleshooting

### Build Errors
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Database Issues
```bash
adb shell pm clear com.seacliff.pos
# Or uninstall/reinstall
adb uninstall com.seacliff.pos
./gradlew installDebug
```

### Network Issues
- Verify API_BASE_URL in BuildConfig
- Check Laravel backend is running
- Review network_security_config.xml

### Sync Not Working
- Check WorkManager logs: `adb logcat -s WM-SyncWorker`
- Verify network constraints
- Check for unsynced orders in database

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md)

## License

Proprietary - POS Restaurant Management System

## Support

- **Issues**: https://github.com/Andrew-Mashamba/HOSPITALITYSYSTEM/issues
- **Email**: info@seacliff.com

---

**Built with ❤️ using Kotlin & Jetpack**

**Generated with Claude Code** 🤖

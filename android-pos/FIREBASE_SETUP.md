# Firebase Cloud Messaging Setup Guide

## Step 1: Add Firebase to Android Project

### 1.1 Download `google-services.json`
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project or create a new one
3. Add Android app with package name: `com.seacliff.pos`
4. Download `google-services.json`
5. Place it in `app/` directory

### 1.2 Add Firebase Dependencies

**Project-level `build.gradle`:**
```gradle
buildscript {
    dependencies {
        // Add this line
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

**App-level `build.gradle`:**
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
    id 'com.google.gms.google-services' // Add this line
}

dependencies {
    // ... existing dependencies ...

    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
}
```

---

## Step 2: Request Notification Permission (Android 13+)

Add to your MainActivity or splash screen:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_NOTIFICATION_PERMISSION
        )
    }
}
```

Add to AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

---

## Step 3: Get FCM Token

In your Application class or MainActivity:

```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "Token: $token")
        // Send token to your server
        sendTokenToServer(token)
    }
}
```

---

## Step 4: Backend Integration

### API Endpoint for Token Storage
```kotlin
POST /api/staff/{staffId}/fcm-token
{
  "fcm_token": "string",
  "device_id": "string",
  "platform": "android"
}
```

### Sending Notifications from Backend (Laravel Example)

```php
use Google\Client;
use GuzzleHttp\Client as HttpClient;

public function sendOrderReadyNotification($orderId, $waiterId)
{
    $waiter = Staff::find($waiterId);
    $order = Order::find($orderId);

    if (!$waiter->fcm_token) {
        return;
    }

    $message = [
        'token' => $waiter->fcm_token,
        'data' => [
            'type' => 'order_ready',
            'order_id' => (string) $orderId,
            'table_name' => $order->table->name,
        ],
        'notification' => [
            'title' => 'Order Ready!',
            'body' => "Order for {$order->table->name} is ready to serve",
        ],
        'android' => [
            'priority' => 'high',
            'notification' => [
                'sound' => 'default',
                'channel_id' => 'order_updates',
            ],
        ],
    ];

    $this->sendFcmMessage($message);
}

private function sendFcmMessage($message)
{
    $client = new HttpClient();
    $accessToken = $this->getAccessToken();

    $response = $client->post(
        'https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send',
        [
            'headers' => [
                'Authorization' => 'Bearer ' . $accessToken,
                'Content-Type' => 'application/json',
            ],
            'json' => ['message' => $message],
        ]
    );

    return $response;
}
```

---

## Step 5: Notification Types

The app handles these notification types:

1. **order_ready**
   ```json
   {
     "type": "order_ready",
     "order_id": "123",
     "table_name": "Table 5"
   }
   ```

2. **order_status_update**
   ```json
   {
     "type": "order_status_update",
     "order_id": "123",
     "status": "preparing",
     "table_name": "Table 5"
   }
   ```

3. **payment_received**
   ```json
   {
     "type": "payment_received",
     "order_id": "123",
     "amount": "50000",
     "method": "card"
   }
   ```

4. **tip_received**
   ```json
   {
     "type": "tip_received",
     "order_id": "123",
     "amount": "5000",
     "table_name": "Table 5"
   }
   ```

5. **table_assignment**
   ```json
   {
     "type": "table_assignment",
     "table_name": "Table 5"
   }
   ```

---

## Step 6: Testing

### Test with Firebase Console:
1. Go to Firebase Console → Cloud Messaging
2. Click "Send test message"
3. Enter FCM token
4. Send notification

### Test with curl:
```bash
curl -X POST https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "token": "FCM_TOKEN_HERE",
      "data": {
        "type": "tip_received",
        "order_id": "123",
        "amount": "5000",
        "table_name": "Table 5"
      },
      "notification": {
        "title": "Tip Received!",
        "body": "You received TZS 5,000 from Table 5"
      }
    }
  }'
```

---

## Troubleshooting

### Token not received:
- Ensure google-services.json is in app/ directory
- Check that package name matches in Firebase Console
- Verify internet permission in manifest

### Notifications not showing:
- Check notification permission granted
- Verify notification channels created (Android O+)
- Check device's Do Not Disturb settings

### Background notifications:
- FCM handles background notifications automatically
- Foreground requires onMessageReceived implementation (already done)

---

## Implementation Status

✅ FCM Service created: `PosFirebaseMessagingService.kt`
✅ Notification channels defined
✅ Deep linking implemented
✅ All notification types handled
⚠️ Firebase dependencies need to be added to build.gradle
⚠️ google-services.json needs to be downloaded and added

---

**Next Steps:**
1. Add firebase dependencies to build.gradle
2. Download and add google-services.json
3. Request notification permission
4. Implement backend FCM token storage
5. Test notifications

---

*Last Updated: 2026-01-30*

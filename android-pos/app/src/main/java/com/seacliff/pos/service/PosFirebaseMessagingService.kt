package com.seacliff.pos.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.seacliff.pos.R
import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.ui.activities.MainActivity
import com.seacliff.pos.ui.activities.OrderDetailsActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PosFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM Token refreshed: $token")

        // Re-register with backend if user is logged in
        val authToken = preferencesManager.getToken()
        if (authToken != null && authToken.isNotEmpty()) {
            TokenManager.registerToken(apiService, authToken)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        if (data.isEmpty()) {
            Timber.d("FCM: Empty data message received")
            return
        }

        Timber.d("FCM Message received: type=${data["type"]}, order=${data["order_id"]}")

        when (data["type"]) {
            // New message types from guide
            "order_item_status_changed" -> handleItemStatusChanged(data)
            "order_item_ready" -> handleItemReady(data)
            "order_ready" -> handleOrderReady(data)

            // Legacy message types
            "order_status_update" -> handleOrderStatusUpdate(data)
            "payment_received" -> handlePaymentReceived(data)
            "tip_received" -> handleTipReceived(data)
            "table_assignment" -> handleTableAssignment(data)
            else -> handleGenericNotification(message)
        }
    }

    private fun handleItemStatusChanged(data: Map<String, String>) {
        val orderId = data["order_id"] ?: return
        val itemId = data["order_item_id"] ?: return
        val status = data["prep_status"] ?: return
        val itemName = data["menu_item_name"] ?: "Item"
        val table = data["table_name"] ?: ""
        val orderNumber = data["order_number"] ?: ""

        Timber.d("Item status changed: order=$orderId, item=$itemId, status=$status")

        // Broadcast to active UI
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(ACTION_ORDER_UPDATED).apply {
                putExtra("order_id", orderId)
                putExtra("order_item_id", itemId)
                putExtra("prep_status", status)
            }
        )

        // Show notification if app is in background
        if (!AppLifecycleObserver.isInForeground) {
            showNotification(
                title = "$itemName — ${status.replaceFirstChar { it.uppercase() }}",
                body = "Table $table • Order #$orderNumber",
                orderId = orderId,
                channelId = CHANNEL_ORDER_UPDATES
            )
        }
    }

    private fun handleItemReady(data: Map<String, String>) {
        val orderId = data["order_id"] ?: return
        val itemId = data["order_item_id"] ?: return
        val itemName = data["menu_item_name"] ?: "Item"
        val table = data["table_name"] ?: ""

        Timber.d("Item ready: order=$orderId, item=$itemName")

        // Broadcast to active UI
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(ACTION_ORDER_UPDATED).apply {
                putExtra("order_id", orderId)
                putExtra("order_item_id", itemId)
                putExtra("prep_status", "ready")
            }
        )

        // Always show notification for ready items — waiter needs to pick up
        showNotification(
            title = "$itemName is READY",
            body = "Table $table • Pick up now",
            orderId = orderId,
            channelId = CHANNEL_ORDER_READY,
            highPriority = true
        )
    }

    private fun handleOrderReady(data: Map<String, String>) {
        val orderId = data["order_id"] ?: return
        val orderNumber = data["order_number"] ?: ""
        val tableName = data["table_name"] ?: ""

        Timber.d("Order ready: order=$orderId, orderNumber=$orderNumber")

        // Broadcast to active UI
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(ACTION_ORDER_UPDATED).apply {
                putExtra("order_id", orderId)
                putExtra("status", "ready")
            }
        )

        // Always show high priority notification
        showNotification(
            title = "Order #$orderNumber READY",
            body = "All items ready — Table $tableName",
            orderId = orderId,
            channelId = CHANNEL_ORDER_READY,
            highPriority = true
        )
    }

    private fun handleOrderStatusUpdate(data: Map<String, String>) {
        val orderId = data["order_id"] ?: return
        val status = data["status"] ?: return
        val tableName = data["table_name"] ?: "Table"

        Timber.d("Order status update: order=$orderId, status=$status")

        // Broadcast to active UI
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(ACTION_ORDER_UPDATED).apply {
                putExtra("order_id", orderId)
                putExtra("status", status)
            }
        )

        val title = "Order Status Updated"
        val body = "Order for $tableName is now ${status.replaceFirstChar { it.uppercase() }}"

        showNotification(
            title = title,
            body = body,
            orderId = orderId,
            channelId = CHANNEL_ORDER_UPDATES
        )
    }

    private fun handlePaymentReceived(data: Map<String, String>) {
        val orderId = data["order_id"] ?: return
        val amount = data["amount"] ?: "0"
        val method = data["method"] ?: "digital"

        val title = "Payment Received"
        val body = "TZS $amount received via ${method.replaceFirstChar { it.uppercase() }}"

        showNotification(
            title = title,
            body = body,
            orderId = orderId,
            channelId = CHANNEL_PAYMENTS
        )
    }

    private fun handleTipReceived(data: Map<String, String>) {
        val orderId = data["order_id"] ?: return
        val amount = data["amount"] ?: "0"
        val tableName = data["table_name"] ?: "customer"

        val title = "Tip Received!"
        val body = "You received TZS $amount from $tableName"

        showNotification(
            title = title,
            body = body,
            orderId = orderId,
            channelId = CHANNEL_TIPS,
            highPriority = true
        )
    }

    private fun handleTableAssignment(data: Map<String, String>) {
        val tableName = data["table_name"] ?: return

        val title = "Table Assignment"
        val body = "You have been assigned to $tableName"

        showNotification(
            title = title,
            body = body,
            orderId = null,
            channelId = CHANNEL_GENERAL
        )
    }

    private fun handleGenericNotification(message: RemoteMessage) {
        val title = message.notification?.title ?: "SeaCliff POS"
        val body = message.notification?.body ?: ""

        showNotification(
            title = title,
            body = body,
            orderId = null,
            channelId = CHANNEL_GENERAL
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        orderId: String?,
        channelId: String,
        highPriority: Boolean = false
    ) {
        val intent = if (orderId != null) {
            Intent(this, OrderDetailsActivity::class.java).apply {
                putExtra("ORDER_ID", orderId.toLongOrNull() ?: 0L)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val notificationId = orderId?.hashCode() ?: System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = if (highPriority) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .apply {
                if (highPriority) {
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    setSound(soundUri)
                    setVibrate(longArrayOf(0, 500, 200, 500))
                }
            }
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    companion object {
        const val ACTION_ORDER_UPDATED = "com.seacliff.pos.ORDER_UPDATED"

        const val CHANNEL_ORDER_UPDATES = "order_updates"
        const val CHANNEL_ORDER_READY = "order_ready"
        const val CHANNEL_PAYMENTS = "payments"
        const val CHANNEL_TIPS = "tips"
        const val CHANNEL_GENERAL = "general"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)

                val channels = listOf(
                    NotificationChannel(
                        CHANNEL_ORDER_UPDATES,
                        "Order Updates",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Status changes for order items"
                    },
                    NotificationChannel(
                        CHANNEL_ORDER_READY,
                        "Orders Ready",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Items ready for pickup"
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 200, 500)
                    },
                    NotificationChannel(
                        CHANNEL_PAYMENTS,
                        "Payments",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Payment notifications"
                    },
                    NotificationChannel(
                        CHANNEL_TIPS,
                        "Tips",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Tip notifications"
                        enableVibration(true)
                    },
                    NotificationChannel(
                        CHANNEL_GENERAL,
                        "General",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "General notifications"
                    }
                )

                channels.forEach { manager.createNotificationChannel(it) }
            }
        }
    }
}

# Android POS - Waiter Implementation Plan

## Document Overview

This document translates the waiter's user journeys (from system documentation) into a detailed Android implementation plan for the SeaCliff POS application.

**Target Users:** Waiters/Waitstaff
**Platform:** Native Android (Kotlin)
**Architecture:** MVVM + Clean Architecture
**Device:** Android Tablets (min API 24, target API 34)

---

## Table of Contents

1. [Feature Overview](#feature-overview)
2. [User Journey Mapping](#user-journey-mapping)
3. [Screen Specifications](#screen-specifications)
4. [Data Layer Implementation](#data-layer-implementation)
5. [Business Logic Requirements](#business-logic-requirements)
6. [API Integration](#api-integration)
7. [Notification System](#notification-system)
8. [Offline Capabilities](#offline-capabilities)
9. [Implementation Phases](#implementation-phases)
10. [Testing Requirements](#testing-requirements)

---

## Feature Overview

### Core Capabilities

| Feature | Priority | Status | Complexity |
|---------|----------|--------|------------|
| Login & Authentication | CRITICAL | ✅ Complete | Low |
| Table Assignment View | CRITICAL | ✅ Complete | Medium |
| Order Creation (Manual) | CRITICAL | ✅ Complete | High |
| Menu Browsing & Selection | CRITICAL | ✅ Complete | Medium |
| Order Status Tracking | HIGH | ✅ Complete | Medium |
| Mark Order Served | HIGH | ⚠️ Partial | Low |
| Cash Payment Processing | HIGH | ⚠️ Stub | Medium |
| Tip Entry & Notification | MEDIUM | ❌ Missing | Medium |
| Multi-Table Management | MEDIUM | ⚠️ Partial | Medium |
| Guest Profile Display | LOW | ❌ Missing | Low |
| Upsell Suggestions | LOW | ❌ Missing | Medium |

**Legend:**
- ✅ Complete: Fully implemented
- ⚠️ Partial: Basic implementation, needs enhancement
- ❌ Missing: Not yet implemented

---

## User Journey Mapping

### Journey 1: Taking Orders (Waiter-Assisted)

#### Android Implementation Breakdown

**1.1 Receive Assignment**

**UI Components:**
```kotlin
// MainActivity.kt - Dashboard
- btnTables: Button → Navigate to TablesActivity
- Toolbar: Display waiter name and role
```

**Data Flow:**
```
AuthViewModel.getStaffName() → Display in toolbar
TableViewModel.loadTables() → Show assigned tables
```

**Implementation Status:** ✅ Complete

---

**1.2 Take Order Physically**

**Screen:** `OrderActivity.kt`

**UI Components:**
```kotlin
// Top Section
- Toolbar: "New Order - [Table Name]"
- TabLayout: Category filters (All, Appetizer, Main, Dessert, Drink)

// Left Panel - Menu
- RecyclerView (rvMenu): Menu items with MenuAdapter
- ProgressBar: Loading indicator
- SearchView: Item search (planned)

// Right Panel - Cart
- RecyclerView (rvCart): Cart items with CartAdapter
- TextView (tvTotal): Running total display
- Button (btnPlaceOrder): Submit order

// Menu Item Card
- TextView: Item name
- TextView: Description
- TextView: Price (TZS)
- ImageView: Item photo
- Button: Add to cart

// Cart Item Card
- TextView: Item name × quantity
- EditText: Special notes
- Spinner: Quantity selector
- IconButton: Remove item
```

**ViewModels:**
```kotlin
MenuViewModel {
  - menuItems: LiveData<Resource<List<MenuItem>>>
  - selectCategory(category: String)
  - searchMenu(query: String)
}

OrderViewModel {
  - cart: LiveData<List<CartItem>>
  - cartTotal: LiveData<Double>
  - addToCart(menuItem: MenuItem)
  - updateQuantity(cartItem: CartItem, quantity: Int)
  - updateNotes(cartItem: CartItem, notes: String)
  - removeFromCart(cartItem: CartItem)
  - clearCart()
}
```

**Implementation Status:** ✅ Complete

---

**1.3 Order Submission**

**Action Flow:**
```kotlin
// OrderActivity.kt
btnPlaceOrder.onClick {
  if (cart.isEmpty()) {
    Toast: "Cart is empty"
    return
  }

  orderViewModel.createOrder(
    guestId = selectedGuestId,  // From guest selection
    tableId = currentTableId,   // From intent
    notes = orderNotes          // Optional
  )
}

// OrderViewModel.kt
fun createOrder(guestId: Long, tableId: Long, notes: String?) {
  viewModelScope.launch {
    _createOrderState.value = Resource.Loading()

    val result = orderRepository.createOrder(
      Order(
        guestId = guestId,
        tableId = tableId,
        waiterId = authRepository.getCurrentStaffId(),
        items = cart.value,
        status = "pending",
        orderSource = "pos"
      )
    )

    _createOrderState.value = result
    if (result is Resource.Success) {
      clearCart()
    }
  }
}
```

**Critical Requirement:**
> Order ID must be compatible with WhatsApp orders (same database, same flow)

**Implementation Status:** ✅ Complete (basic), needs enhancement for guest selection

---

**1.4 Receive Notifications**

**Requirements:**
- Push notifications when order status changes
- In-app notification badges
- Sound/vibration alerts

**Planned Implementation:**
```kotlin
// NotificationService.kt (NEW - To be created)
class OrderNotificationService : FirebaseMessagingService() {
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    when (remoteMessage.data["type"]) {
      "order_ready" -> showOrderReadyNotification()
      "payment_received" -> showPaymentNotification()
      "tip_received" -> showTipNotification()
    }
  }
}

// NotificationHelper.kt (NEW)
object NotificationHelper {
  fun showOrderReadyNotification(orderId: Long, tableName: String) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_restaurant)
      .setContentTitle("Order Ready")
      .setContentText("Order for $tableName is ready to serve")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(orderId.toInt(), notification)
  }
}
```

**Implementation Status:** ❌ Missing (critical feature)

---

### Journey 2: Serving Orders

#### Android Implementation Breakdown

**2.1 Pick Up from Kitchen/Bar**

**Screen:** `OrdersActivity.kt` (already exists, needs enhancement)

**Current Implementation:**
```kotlin
// OrdersActivity.kt
- Tabs: All, Pending, Preparing, Ready, Served
- RecyclerView: Order list with OrderListAdapter
- SwipeRefresh: Pull to refresh orders

// OrderListAdapter item (needs enhancement)
- Order ID and table name
- Order items list
- Current status badge
- Timestamp
```

**Required Enhancements:**
```kotlin
// Add status-specific UI
when (order.status) {
  "ready" -> {
    itemView.setBackgroundColor(Color.GREEN_LIGHT)
    btnAction.text = "Serve"
    btnAction.visibility = View.VISIBLE
  }
  "preparing" -> {
    itemView.setBackgroundColor(Color.YELLOW_LIGHT)
    btnAction.visibility = View.GONE
  }
  "served" -> {
    itemView.setBackgroundColor(Color.GRAY_LIGHT)
    btnAction.text = "Bill"
    btnAction.visibility = View.VISIBLE
  }
}
```

**Implementation Status:** ⚠️ Partial (basic list exists, needs status actions)

---

**2.2 Serve Guest**

**Physical Action:** (No digital component - waiter delivers food)

**Optional Enhancement:**
```kotlin
// Add confirmation dialog
fun confirmServing(order: Order) {
  AlertDialog.Builder(context)
    .setTitle("Confirm Service")
    .setMessage("Mark order for ${order.tableName} as served?")
    .setPositiveButton("Served") { _, _ ->
      orderViewModel.markAsServed(order.id)
    }
    .setNegativeButton("Cancel", null)
    .show()
}
```

---

**2.3 Mark as Served**

**Implementation:**
```kotlin
// OrderViewModel.kt (NEW method needed)
fun markAsServed(orderId: Long) {
  viewModelScope.launch {
    _serveOrderState.value = Resource.Loading()

    val result = orderRepository.updateOrderStatus(
      orderId = orderId,
      status = "served",
      servedAt = System.currentTimeMillis()
    )

    _serveOrderState.value = result
  }
}

// OrderRepository.kt (NEW method)
suspend fun updateOrderStatus(
  orderId: Long,
  status: String,
  servedAt: Long
): Resource<Order> {
  return try {
    // Update local database
    orderDao.updateStatus(orderId, status, servedAt)

    // Sync with API
    val response = apiService.updateOrderStatus(orderId, status)

    if (response.isSuccessful) {
      Resource.Success(response.body())
    } else {
      Resource.Error("Failed to update order status")
    }
  } catch (e: Exception) {
    // Still update locally, mark for sync
    orderDao.markForSync(orderId)
    Resource.Success(orderDao.getOrder(orderId))
  }
}
```

**System Actions (Backend):**
- Send running bill summary to guest WhatsApp
- Update manager dashboard
- Trigger analytics update

**Implementation Status:** ❌ Missing

---

### Journey 3: Additional Orders (Mid-Service)

#### Implementation Requirements

**Option A: WhatsApp Order (Waiter Receives Notification)**

**Requirement:** Real-time notification when guest adds items via WhatsApp

```kotlin
// Notification handling
onMessageReceived("new_order_item") {
  val orderId = data["order_id"]
  val tableName = data["table_name"]

  showNotification(
    title = "New Item Added",
    message = "$tableName added items via WhatsApp",
    action = "VIEW_ORDER"
  )

  // Refresh order list
  orderViewModel.refreshOrders()
}
```

**Implementation Status:** ❌ Missing

---

**Option B: Waiter Adds via POS**

**Screen Flow:**
```
OrdersActivity → Click on order → OrderDetailsActivity (NEW)
  → Add Items button → Reuse OrderActivity with existing order context
```

**New Screen Required:**
```kotlin
// OrderDetailsActivity.kt (NEW - To be created)
class OrderDetailsActivity : AppCompatActivity() {

  private val orderId by lazy { intent.getLongExtra("order_id", 0) }

  override fun onCreate(savedInstanceState: Bundle?) {
    setupUI()
    loadOrderDetails()
  }

  private fun setupUI() {
    // Order header
    binding.tvOrderId.text = "Order #${order.id}"
    binding.tvTableName.text = order.tableName
    binding.tvStatus.text = order.status

    // Items list
    binding.rvItems.adapter = OrderItemsAdapter()

    // Actions
    binding.btnAddItems.onClick {
      navigateToOrderActivity(orderId, existingOrder = true)
    }

    binding.btnMarkServed.onClick {
      orderViewModel.markAsServed(orderId)
    }

    binding.btnGenerateBill.onClick {
      navigateToPaymentActivity(orderId)
    }
  }
}
```

**Implementation Status:** ❌ Missing (critical for full waiter workflow)

---

### Journey 4: Payment Processing

#### Scenario A: Digital Payment

**Waiter's Role:** Passive notification recipient

**Notification Implementation:**
```kotlin
// Firebase Cloud Messaging handler
onMessageReceived("payment_received") {
  val orderId = data["order_id"]
  val amount = data["amount"]
  val tip = data["tip"]

  showNotification(
    title = "💰 Payment Received",
    message = "Table ${data["table_name"]}: TZS $amount" +
             if (tip > 0) "\n🎉 Tip: TZS $tip" else "",
    sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
  )

  // Update tip tracking
  staffViewModel.addTip(tip, orderId)
}
```

**Implementation Status:** ❌ Missing

---

#### Scenario B: Cash Payment

**Screen:** `PaymentActivity.kt` (exists but stubbed)

**Required Implementation:**
```kotlin
// PaymentActivity.kt (ENHANCE EXISTING)
class PaymentActivity : AppCompatActivity() {

  private val orderId by lazy { intent.getLongExtra("order_id", 0) }
  private val paymentViewModel: PaymentViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPaymentBinding.inflate(layoutInflater)
    setContentView(binding.root)

    loadBill()
    setupPaymentMethods()
    setupTipEntry()
  }

  private fun loadBill() {
    paymentViewModel.generateBill(orderId).observe(this) { bill ->
      binding.tvSubtotal.text = "TZS ${bill.subtotal}"
      binding.tvTax.text = "TZS ${bill.tax}"
      binding.tvServiceCharge.text = "TZS ${bill.serviceCharge}"
      binding.tvTotal.text = "TZS ${bill.total}"
      binding.rvItems.adapter = BillItemsAdapter(bill.items)
    }
  }

  private fun setupPaymentMethods() {
    binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
      when (checkedId) {
        R.id.rbCash -> showCashPayment()
        R.id.rbCard -> showCardPayment()
        R.id.rbMobileMoney -> showMobileMoneyPayment()
      }
    }
  }

  private fun showCashPayment() {
    binding.layoutCashPayment.visibility = View.VISIBLE

    // Cash received input
    binding.etCashReceived.addTextChangedListener {
      val received = it.toString().toDoubleOrNull() ?: 0.0
      val total = currentBill.total
      val change = received - total

      binding.tvChange.text = "Change: TZS ${String.format("%.0f", change)}"
      binding.btnConfirmPayment.isEnabled = received >= total
    }

    binding.btnConfirmPayment.onClick {
      processCashPayment()
    }
  }

  private fun setupTipEntry() {
    // Quick tip buttons
    binding.btnTip10.onClick { addTip(currentBill.total * 0.10) }
    binding.btnTip15.onClick { addTip(currentBill.total * 0.15) }
    binding.btnTip20.onClick { addTip(currentBill.total * 0.20) }

    // Custom tip
    binding.btnCustomTip.onClick {
      showCustomTipDialog()
    }
  }

  private fun processCashPayment() {
    val cashReceived = binding.etCashReceived.text.toString().toDouble()
    val tipAmount = binding.etTipAmount.text.toString().toDoubleOrNull() ?: 0.0

    paymentViewModel.processCashPayment(
      orderId = orderId,
      amountReceived = cashReceived,
      tipAmount = tipAmount,
      paymentMethod = "cash"
    )
  }
}

// PaymentViewModel.kt (ENHANCE EXISTING)
class PaymentViewModel @Inject constructor(
  private val paymentRepository: PaymentRepository
) : ViewModel() {

  private val _billState = MutableLiveData<Resource<Bill>>()
  val billState: LiveData<Resource<Bill>> = _billState

  fun generateBill(orderId: Long): LiveData<Bill> {
    viewModelScope.launch {
      _billState.value = Resource.Loading()
      val result = paymentRepository.generateBill(orderId)
      _billState.value = result
    }
    return billState.map { it.data }
  }

  fun processCashPayment(
    orderId: Long,
    amountReceived: Double,
    tipAmount: Double,
    paymentMethod: String
  ) {
    viewModelScope.launch {
      val payment = Payment(
        orderId = orderId,
        amount = amountReceived - tipAmount,
        method = paymentMethod,
        status = "completed",
        paidAt = System.currentTimeMillis()
      )

      paymentRepository.processPayment(payment)

      if (tipAmount > 0) {
        paymentRepository.processTip(
          orderId = orderId,
          amount = tipAmount,
          waiterId = authRepository.getCurrentStaffId()
        )
      }
    }
  }
}
```

**UI Layout Required:**
```xml
<!-- activity_payment.xml (ENHANCE EXISTING) -->
<ScrollView>
  <LinearLayout orientation="vertical">

    <!-- Bill Summary -->
    <CardView>
      <RecyclerView id="rvItems" /> <!-- Bill items -->
      <TextView id="tvSubtotal" />
      <TextView id="tvTax" />
      <TextView id="tvServiceCharge" />
      <TextView id="tvTotal" style="bold" />
    </CardView>

    <!-- Payment Method Selection -->
    <RadioGroup id="rgPaymentMethod">
      <RadioButton id="rbCash" text="Cash" />
      <RadioButton id="rbCard" text="Card" />
      <RadioButton id="rbMobileMoney" text="Mobile Money" />
    </RadioGroup>

    <!-- Cash Payment Section -->
    <LinearLayout id="layoutCashPayment">
      <EditText id="etCashReceived" inputType="numberDecimal" />
      <TextView id="tvChange" />
    </LinearLayout>

    <!-- Tip Section -->
    <CardView>
      <TextView text="Add Tip (Optional)" />
      <LinearLayout orientation="horizontal">
        <Button id="btnTip10" text="10%" />
        <Button id="btnTip15" text="15%" />
        <Button id="btnTip20" text="20%" />
        <Button id="btnCustomTip" text="Custom" />
      </LinearLayout>
      <EditText id="etTipAmount" />
    </CardView>

    <!-- Action Button -->
    <Button id="btnConfirmPayment" text="Confirm Payment" />

  </LinearLayout>
</ScrollView>
```

**Implementation Status:** ⚠️ Stub exists, needs full implementation

---

### Journey 5: Handling Tips

#### Implementation Requirements

**Tip Notification Display:**

```kotlin
// Create TipNotificationView (NEW)
class TipNotificationView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

  init {
    inflate(context, R.layout.view_tip_notification, this)
  }

  fun show(tipAmount: Double, tableName: String) {
    binding.tvTipAmount.text = "TZS ${String.format("%.0f", tipAmount)}"
    binding.tvTipMessage.text = "Tip from $tableName"

    // Animate in
    animate()
      .alpha(1f)
      .translationY(0f)
      .setDuration(300)
      .start()

    // Auto-hide after 5 seconds
    postDelayed({ hide() }, 5000)
  }

  private fun hide() {
    animate()
      .alpha(0f)
      .translationY(-100f)
      .setDuration(300)
      .withEndAction { visibility = View.GONE }
      .start()
  }
}

// MainActivity.kt (Add tip display)
private fun observeTips() {
  staffViewModel.newTips.observe(this) { tip ->
    binding.tipNotificationView.show(tip.amount, tip.tableName)
    playTipSound()
  }
}
```

**Tip Tracking Dashboard:**

```kotlin
// Add to MainActivity or create TipsActivity
fun showTipsSummary() {
  staffViewModel.getTodaysTips().observe(this) { tips ->
    binding.tvTodayTips.text = "Today's Tips: TZS ${tips.sum()}"
    binding.tvTipCount.text = "${tips.size} tips"
  }
}
```

**Implementation Status:** ❌ Missing

---

### Journey 6: Multi-Table Management

#### Current Implementation Analysis

**TablesActivity.kt:**
- ✅ Grid layout with 3 columns
- ✅ Filter by status (All, Available, Occupied)
- ✅ Click to navigate to OrderActivity

**Required Enhancements:**

```kotlin
// TablesActivity.kt (ENHANCE)
class TablesActivity : AppCompatActivity() {

  // Add assigned tables filter
  private fun setupFilters() {
    binding.chipMyTables.setOnClickListener {
      val currentWaiterId = authViewModel.getCurrentStaffId()
      tableViewModel.filterByWaiter(currentWaiterId)
    }

    binding.chipAllTables.setOnClickListener {
      tableViewModel.showAllTables()
    }
  }

  // Add active orders indicator
  private fun setupRecyclerView() {
    tableAdapter = TableAdapter(
      onTableClick = { table -> navigateToOrder(table) },
      onTableLongClick = { table -> showTableOptions(table) }
    )
  }

  private fun showTableOptions(table: Table) {
    val options = when {
      table.hasActiveOrders -> arrayOf("View Orders", "Add Order", "Clear Table")
      else -> arrayOf("Start Order", "Mark Reserved")
    }

    AlertDialog.Builder(this)
      .setTitle(table.name)
      .setItems(options) { _, which ->
        handleTableAction(table, which)
      }
      .show()
  }
}

// TableAdapter.kt (ENHANCE)
class TableAdapter : ListAdapter<Table, TableViewHolder>() {

  override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
    val table = getItem(position)

    holder.binding.apply {
      tvTableName.text = table.name
      tvCapacity.text = "${table.capacity} seats"

      // Status indicator
      when (table.status) {
        "available" -> cardView.setCardBackgroundColor(Color.GREEN_LIGHT)
        "occupied" -> cardView.setCardBackgroundColor(Color.RED_LIGHT)
        "reserved" -> cardView.setCardBackgroundColor(Color.YELLOW_LIGHT)
      }

      // Active orders badge
      if (table.activeOrderCount > 0) {
        badgeOrders.visibility = View.VISIBLE
        badgeOrders.text = table.activeOrderCount.toString()
      } else {
        badgeOrders.visibility = View.GONE
      }

      // Waiter assignment
      if (table.assignedWaiterId == currentWaiterId) {
        ivMyTable.visibility = View.VISIBLE
      }
    }
  }
}
```

**Table Entity Enhancement:**
```kotlin
// TableEntity.kt (ENHANCE EXISTING)
@Entity(tableName = "tables")
data class TableEntity(
  @PrimaryKey val id: Long,
  val name: String,
  val location: String,
  val capacity: Int,
  val status: String,

  // NEW FIELDS
  @ColumnInfo(name = "assigned_waiter_id")
  val assignedWaiterId: Long? = null,

  @ColumnInfo(name = "active_order_count")
  val activeOrderCount: Int = 0,

  @ColumnInfo(name = "last_activity_at")
  val lastActivityAt: Long? = null,

  val createdAt: Long,
  val updatedAt: Long
)
```

**Implementation Status:** ⚠️ Partial (basic grid exists, needs enhancements)

---

## Screen Specifications

### Complete Screen Inventory

| Screen | Status | Priority | Notes |
|--------|--------|----------|-------|
| LoginActivity | ✅ Complete | CRITICAL | Working |
| MainActivity | ✅ Complete | CRITICAL | Dashboard |
| TablesActivity | ⚠️ Enhance | CRITICAL | Needs multi-table features |
| OrderActivity | ✅ Complete | CRITICAL | Order creation |
| OrdersActivity | ⚠️ Enhance | HIGH | Needs status actions |
| OrderDetailsActivity | ❌ Missing | HIGH | View/edit existing order |
| PaymentActivity | ⚠️ Stub | HIGH | Needs full implementation |
| MenuActivity | ✅ Complete | MEDIUM | Menu management |
| TipsActivity | ❌ Missing | MEDIUM | Tips dashboard |
| ProfileActivity | ❌ Missing | LOW | Waiter profile & stats |

---

### Screen Flow Diagram

```
LoginActivity
    ↓ (successful login)
MainActivity (Dashboard)
    ├─── TablesActivity
    │       ├─── [New Order] → OrderActivity → [Submit] → Back to Tables
    │       └─── [Existing Order] → OrderDetailsActivity
    │                                   ├─── Add Items → OrderActivity
    │                                   ├─── Mark Served → Update & Back
    │                                   └─── Generate Bill → PaymentActivity
    │                                           └─── Confirm Payment → Thank You → Tables
    │
    ├─── OrdersActivity
    │       ├─── Filter by Status (All/Pending/Preparing/Ready/Served)
    │       └─── Click Order → OrderDetailsActivity
    │
    ├─── MenuActivity (View/Update Menu)
    │
    ├─── TipsActivity (View Tips Summary)
    │
    └─── ProfileActivity (Stats & Settings)
         └─── Logout → LoginActivity
```

---

## Data Layer Implementation

### Required Entities (Status Check)

| Entity | Status | Enhancements Needed |
|--------|--------|---------------------|
| GuestEntity | ✅ Exists | Add favorite items, preferences |
| TableEntity | ✅ Exists | Add waiter assignment, order count |
| StaffEntity | ✅ Exists | Complete |
| MenuItemEntity | ✅ Exists | Complete |
| OrderEntity | ✅ Exists | Add served_at timestamp |
| OrderItemEntity | ✅ Exists | Complete |
| PaymentEntity | ✅ Exists | Add tip_amount field |
| TipEntity | ❌ Missing | **CREATE NEW** |

---

### New Entity Required: TipEntity

```kotlin
// TipEntity.kt (NEW - To be created)
@Entity(
  tableName = "tips",
  foreignKeys = [
    ForeignKey(
      entity = OrderEntity::class,
      parentColumns = ["id"],
      childColumns = ["order_id"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = PaymentEntity::class,
      parentColumns = ["id"],
      childColumns = ["payment_id"],
      onDelete = ForeignKey.SET_NULL
    ),
    ForeignKey(
      entity = StaffEntity::class,
      parentColumns = ["id"],
      childColumns = ["waiter_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["order_id"]),
    Index(value = ["waiter_id"]),
    Index(value = ["created_at"])
  ]
)
data class TipEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "order_id")
  val orderId: Long,

  @ColumnInfo(name = "payment_id")
  val paymentId: Long? = null,

  @ColumnInfo(name = "waiter_id")
  val waiterId: Long,

  val amount: Double,

  @ColumnInfo(name = "method")
  val method: String, // "digital" or "cash"

  @ColumnInfo(name = "is_synced")
  val isSynced: Boolean = false,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis()
)
```

---

### New DAO Required: TipDao

```kotlin
// TipDao.kt (NEW - To be created)
@Dao
interface TipDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(tip: TipEntity): Long

  @Query("SELECT * FROM tips WHERE waiter_id = :waiterId ORDER BY created_at DESC")
  fun getTipsByWaiter(waiterId: Long): Flow<List<TipEntity>>

  @Query("""
    SELECT * FROM tips
    WHERE waiter_id = :waiterId
    AND created_at >= :startDate
    AND created_at <= :endDate
    ORDER BY created_at DESC
  """)
  suspend fun getTipsByDateRange(
    waiterId: Long,
    startDate: Long,
    endDate: Long
  ): List<TipEntity>

  @Query("SELECT SUM(amount) FROM tips WHERE waiter_id = :waiterId AND created_at >= :startDate")
  fun getTotalTipsSince(waiterId: Long, startDate: Long): Flow<Double?>

  @Query("SELECT * FROM tips WHERE is_synced = 0")
  suspend fun getUnsyncedTips(): List<TipEntity>

  @Query("UPDATE tips SET is_synced = 1 WHERE id = :tipId")
  suspend fun markAsSynced(tipId: Long)
}
```

---

### Repository Enhancements

**OrderRepository.kt - Add Methods:**
```kotlin
// OrderRepository.kt (ENHANCE EXISTING)
class OrderRepository @Inject constructor(
  private val orderDao: OrderDao,
  private val apiService: ApiService
) {

  // EXISTING METHODS...

  // NEW METHODS NEEDED:

  suspend fun updateOrderStatus(
    orderId: Long,
    status: String,
    servedAt: Long? = null
  ): Resource<Order> {
    return try {
      // Update locally
      orderDao.updateStatus(orderId, status)
      if (servedAt != null) {
        orderDao.updateServedAt(orderId, servedAt)
      }

      // Sync to API
      val response = apiService.updateOrderStatus(orderId, mapOf("status" to status))

      if (response.isSuccessful) {
        Resource.Success(response.body()?.toEntity())
      } else {
        // Mark for offline sync
        orderDao.markForSync(orderId)
        Resource.Success(orderDao.getOrder(orderId))
      }
    } catch (e: Exception) {
      orderDao.markForSync(orderId)
      Resource.Error(e.message ?: "Failed to update order")
    }
  }

  suspend fun getOrdersByStatus(status: String): Flow<List<Order>> {
    return orderDao.getOrdersByStatus(status)
  }

  suspend fun getActiveOrdersByWaiter(waiterId: Long): Flow<List<Order>> {
    return orderDao.getOrdersByWaiter(waiterId)
      .map { orders -> orders.filter { it.status != "completed" && it.status != "cancelled" } }
  }
}
```

---

**PaymentRepository.kt - Add Methods:**
```kotlin
// PaymentRepository.kt (ENHANCE EXISTING)
class PaymentRepository @Inject constructor(
  private val paymentDao: PaymentDao,
  private val tipDao: TipDao,
  private val apiService: ApiService
) {

  // NEW METHODS NEEDED:

  suspend fun generateBill(orderId: Long): Resource<Bill> {
    return try {
      val response = apiService.generateBill(orderId)
      if (response.isSuccessful) {
        Resource.Success(response.body()?.toBill())
      } else {
        Resource.Error("Failed to generate bill")
      }
    } catch (e: Exception) {
      Resource.Error(e.message ?: "Network error")
    }
  }

  suspend fun processCashPayment(payment: PaymentEntity): Resource<PaymentEntity> {
    return try {
      // Save locally
      val paymentId = paymentDao.insert(payment)

      // Sync to API
      val response = apiService.processPayment(payment.toDto())

      if (response.isSuccessful) {
        paymentDao.updateSyncStatus(paymentId, true)
        Resource.Success(response.body()?.toEntity())
      } else {
        Resource.Error("Payment processing failed")
      }
    } catch (e: Exception) {
      Resource.Error(e.message ?: "Failed to process payment")
    }
  }

  suspend fun processTip(tip: TipEntity): Resource<TipEntity> {
    return try {
      val tipId = tipDao.insert(tip)

      val response = apiService.processTip(tip.toDto())

      if (response.isSuccessful) {
        tipDao.markAsSynced(tipId)
        Resource.Success(response.body()?.toEntity())
      } else {
        Resource.Error("Tip processing failed")
      }
    } catch (e: Exception) {
      Resource.Error(e.message ?: "Failed to process tip")
    }
  }
}
```

---

## Business Logic Requirements

### Order State Machine

```kotlin
// OrderStateManager.kt (NEW - To be created)
object OrderStateManager {

  enum class OrderStatus {
    PENDING,      // Just created
    CONFIRMED,    // Accepted by kitchen/bar
    PREPARING,    // Being prepared
    READY,        // Ready to serve
    SERVED,       // Delivered to guest
    COMPLETED,    // Payment received
    CANCELLED     // Cancelled
  }

  private val allowedTransitions = mapOf(
    OrderStatus.PENDING to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
    OrderStatus.CONFIRMED to setOf(OrderStatus.PREPARING, OrderStatus.CANCELLED),
    OrderStatus.PREPARING to setOf(OrderStatus.READY, OrderStatus.CANCELLED),
    OrderStatus.READY to setOf(OrderStatus.SERVED, OrderStatus.CANCELLED),
    OrderStatus.SERVED to setOf(OrderStatus.COMPLETED),
    OrderStatus.COMPLETED to emptySet(),
    OrderStatus.CANCELLED to emptySet()
  )

  fun canTransition(from: OrderStatus, to: OrderStatus): Boolean {
    return allowedTransitions[from]?.contains(to) ?: false
  }

  fun getNextStates(current: OrderStatus): Set<OrderStatus> {
    return allowedTransitions[current] ?: emptySet()
  }

  fun getWaiterActions(status: OrderStatus): List<String> {
    return when (status) {
      OrderStatus.PENDING -> listOf("Cancel Order")
      OrderStatus.CONFIRMED -> listOf("Cancel Order")
      OrderStatus.PREPARING -> listOf("Cancel Order")
      OrderStatus.READY -> listOf("Mark as Served", "Cancel Order")
      OrderStatus.SERVED -> listOf("Generate Bill")
      OrderStatus.COMPLETED -> emptyList()
      OrderStatus.CANCELLED -> emptyList()
    }
  }
}
```

---

### Bill Calculation Logic

```kotlin
// BillCalculator.kt (NEW - To be created)
object BillCalculator {

  data class Bill(
    val orderId: Long,
    val items: List<BillItem>,
    val subtotal: Double,
    val tax: Double,
    val serviceCharge: Double,
    val total: Double
  )

  data class BillItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
  )

  private const val TAX_RATE = 0.18        // 18% VAT
  private const val SERVICE_CHARGE_RATE = 0.10  // 10% service charge

  fun calculate(orderItems: List<OrderItem>): Bill {
    val items = orderItems.map { item ->
      BillItem(
        name = item.menuItemName,
        quantity = item.quantity,
        unitPrice = item.unitPrice,
        subtotal = item.quantity * item.unitPrice
      )
    }

    val subtotal = items.sumOf { it.subtotal }
    val tax = subtotal * TAX_RATE
    val serviceCharge = subtotal * SERVICE_CHARGE_RATE
    val total = subtotal + tax + serviceCharge

    return Bill(
      orderId = orderItems.first().orderId,
      items = items,
      subtotal = subtotal,
      tax = tax,
      serviceCharge = serviceCharge,
      total = total
    )
  }

  fun calculateChange(amountPaid: Double, billTotal: Double): Double {
    return amountPaid - billTotal
  }
}
```

---

## API Integration

### Required Endpoints (Status Check)

| Endpoint | Method | Purpose | Status |
|----------|--------|---------|--------|
| `/api/auth/login` | POST | Staff login | ✅ Exists |
| `/api/tables` | GET | Get tables | ✅ Exists |
| `/api/tables/{id}/status` | PUT | Update table status | ✅ Exists |
| `/api/menu` | GET | Get menu items | ✅ Exists |
| `/api/orders` | POST | Create order | ✅ Exists |
| `/api/orders` | GET | Get orders | ✅ Exists |
| `/api/orders/{id}` | GET | Get order details | ❌ Missing |
| `/api/orders/{id}/status` | PUT | Update order status | ✅ Exists |
| `/api/orders/{id}/serve` | POST | Mark order served | ❌ Missing |
| `/api/orders/{id}/bill` | GET | Generate bill | ✅ Exists |
| `/api/payments` | POST | Process payment | ✅ Exists |
| `/api/tips` | POST | Process tip | ❌ Missing |
| `/api/staff/{id}/tips` | GET | Get waiter tips | ❌ Missing |
| `/api/staff/{id}/performance` | GET | Get waiter stats | ❌ Missing |

---

### New API Methods Required

```kotlin
// ApiService.kt (ADD TO EXISTING)
interface ApiService {

  // EXISTING ENDPOINTS...

  // NEW ENDPOINTS NEEDED:

  @GET("orders/{id}")
  suspend fun getOrderDetails(
    @Path("id") orderId: Long
  ): Response<OrderDto>

  @POST("orders/{id}/serve")
  suspend fun markOrderServed(
    @Path("id") orderId: Long,
    @Body data: Map<String, Any> = mapOf("served_at" to System.currentTimeMillis())
  ): Response<OrderDto>

  @POST("tips")
  suspend fun processTip(
    @Body tip: TipDto
  ): Response<TipDto>

  @GET("staff/{id}/tips")
  suspend fun getWaiterTips(
    @Path("id") staffId: Long,
    @Query("start_date") startDate: Long? = null,
    @Query("end_date") endDate: Long? = null
  ): Response<List<TipDto>>

  @GET("staff/{id}/performance")
  suspend fun getWaiterPerformance(
    @Path("id") staffId: Long,
    @Query("period") period: String = "today" // today, week, month
  ): Response<PerformanceDto>
}
```

---

### DTOs (Data Transfer Objects)

```kotlin
// TipDto.kt (NEW)
data class TipDto(
  val id: Long? = null,
  val order_id: Long,
  val payment_id: Long? = null,
  val waiter_id: Long,
  val amount: Double,
  val method: String,
  val created_at: Long
)

// Extension functions
fun TipDto.toEntity() = TipEntity(
  id = id ?: 0,
  orderId = order_id,
  paymentId = payment_id,
  waiterId = waiter_id,
  amount = amount,
  method = method,
  isSynced = true,
  createdAt = created_at
)

fun TipEntity.toDto() = TipDto(
  id = id,
  order_id = orderId,
  payment_id = paymentId,
  waiter_id = waiterId,
  amount = amount,
  method = method,
  created_at = createdAt
)
```

```kotlin
// PerformanceDto.kt (NEW)
data class PerformanceDto(
  val waiter_id: Long,
  val period: String,
  val orders_served: Int,
  val average_service_time: Double, // minutes
  val total_sales: Double,
  val total_tips: Double,
  val tip_count: Int,
  val guest_satisfaction: Double? // 0-5 rating
)
```

---

## Notification System

### Firebase Cloud Messaging Integration

**Setup Requirements:**
```kotlin
// 1. Add to build.gradle
dependencies {
  implementation 'com.google.firebase:firebase-messaging:23.3.1'
  implementation 'com.google.firebase:firebase-analytics:21.5.0'
}

// 2. Add google-services.json to app/
```

**Notification Channels:**
```kotlin
// NotificationChannels.kt (NEW)
object NotificationChannels {

  const val CHANNEL_ORDER_READY = "order_ready"
  const val CHANNEL_PAYMENT = "payment"
  const val CHANNEL_TIP = "tip"
  const val CHANNEL_GENERAL = "general"

  fun createChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(NotificationManager::class.java)

      // Order Ready Channel
      val orderReadyChannel = NotificationChannel(
        CHANNEL_ORDER_READY,
        "Order Ready",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifications when orders are ready to serve"
        enableVibration(true)
        setSound(
          Uri.parse("android.resource://${context.packageName}/raw/order_ready"),
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        )
      }

      // Payment Channel
      val paymentChannel = NotificationChannel(
        CHANNEL_PAYMENT,
        "Payments",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Payment confirmations"
      }

      // Tip Channel
      val tipChannel = NotificationChannel(
        CHANNEL_TIP,
        "Tips",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Tip notifications"
        enableVibration(true)
      }

      notificationManager.createNotificationChannels(
        listOf(orderReadyChannel, paymentChannel, tipChannel)
      )
    }
  }
}
```

**FCM Service:**
```kotlin
// PosFirebaseMessagingService.kt (NEW)
@AndroidEntryPoint
class PosFirebaseMessagingService : FirebaseMessagingService() {

  @Inject lateinit var notificationHelper: NotificationHelper

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val data = remoteMessage.data
    val type = data["type"] ?: return

    when (type) {
      "order_ready" -> handleOrderReady(data)
      "payment_received" -> handlePaymentReceived(data)
      "tip_received" -> handleTipReceived(data)
      "new_order_item" -> handleNewOrderItem(data)
      "order_cancelled" -> handleOrderCancelled(data)
    }
  }

  private fun handleOrderReady(data: Map<String, String>) {
    val orderId = data["order_id"]?.toLongOrNull() ?: return
    val tableName = data["table_name"] ?: "Table"

    notificationHelper.showOrderReadyNotification(orderId, tableName)
  }

  private fun handlePaymentReceived(data: Map<String, String>) {
    val orderId = data["order_id"]?.toLongOrNull() ?: return
    val amount = data["amount"]?.toDoubleOrNull() ?: return

    notificationHelper.showPaymentNotification(orderId, amount)
  }

  private fun handleTipReceived(data: Map<String, String>) {
    val tipAmount = data["tip_amount"]?.toDoubleOrNull() ?: return
    val tableName = data["table_name"] ?: "Guest"

    notificationHelper.showTipNotification(tipAmount, tableName)
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    // Send token to server
    sendTokenToServer(token)
  }

  private fun sendTokenToServer(token: String) {
    // TODO: Send FCM token to backend
  }
}
```

**Notification Helper:**
```kotlin
// NotificationHelper.kt (NEW)
@Singleton
class NotificationHelper @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private val notificationManager =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  fun showOrderReadyNotification(orderId: Long, tableName: String) {
    val intent = Intent(context, OrderDetailsActivity::class.java).apply {
      putExtra("order_id", orderId)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      orderId.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_ORDER_READY)
      .setSmallIcon(R.drawable.ic_restaurant)
      .setContentTitle("🍽️ Order Ready")
      .setContentText("$tableName - Order is ready to serve")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
      .setVibrate(longArrayOf(0, 500, 200, 500))
      .build()

    notificationManager.notify(orderId.toInt(), notification)
  }

  fun showPaymentNotification(orderId: Long, amount: Double) {
    val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_PAYMENT)
      .setSmallIcon(R.drawable.ic_payment)
      .setContentTitle("💰 Payment Received")
      .setContentText("TZS ${String.format("%,.0f", amount)}")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(orderId.toInt() + 10000, notification)
  }

  fun showTipNotification(tipAmount: Double, tableName: String) {
    val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_TIP)
      .setSmallIcon(R.drawable.ic_tip)
      .setContentTitle("🎉 You received a tip!")
      .setContentText("$tableName tipped TZS ${String.format("%,.0f", tipAmount)}")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
      .setVibrate(longArrayOf(0, 300, 100, 300, 100, 300))
      .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }
}
```

---

## Offline Capabilities

### Current Implementation (Already Complete)

✅ **Local-First Reads:** All data from Room database
✅ **Offline Writes:** Orders marked with `isSynced = false`
✅ **Background Sync:** WorkManager syncs every 5 minutes
✅ **Conflict Resolution:** Last-write-wins

### Enhancement: Offline Queue Visibility

**Show Pending Sync Indicator:**
```kotlin
// OrderListAdapter.kt (ENHANCE)
override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
  val order = getItem(position)

  holder.binding.apply {
    // Existing UI...

    // NEW: Sync status indicator
    if (!order.isSynced) {
      ivSyncStatus.setImageResource(R.drawable.ic_sync_pending)
      ivSyncStatus.visibility = View.VISIBLE
      tvSyncStatus.text = "Pending sync..."
      tvSyncStatus.visibility = View.VISIBLE
    } else {
      ivSyncStatus.visibility = View.GONE
      tvSyncStatus.visibility = View.GONE
    }
  }
}
```

**Manual Sync Trigger:**
```kotlin
// MainActivity.kt (ADD TO EXISTING)
override fun onOptionsItemSelected(item: MenuItem): Boolean {
  return when (item.itemId) {
    R.id.action_sync -> {
      triggerManualSync()
      true
    }
    // ... existing code
  }
}

private fun triggerManualSync() {
  // Show progress
  binding.progressSync.visibility = View.VISIBLE

  // Trigger immediate sync via WorkManager
  val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    .build()

  WorkManager.getInstance(this).enqueue(syncRequest)

  // Observe sync completion
  WorkManager.getInstance(this)
    .getWorkInfoByIdLiveData(syncRequest.id)
    .observe(this) { workInfo ->
      when (workInfo.state) {
        WorkInfo.State.SUCCEEDED -> {
          binding.progressSync.visibility = View.GONE
          Toast.makeText(this, "Sync complete", Toast.LENGTH_SHORT).show()
        }
        WorkInfo.State.FAILED -> {
          binding.progressSync.visibility = View.GONE
          Toast.makeText(this, "Sync failed", Toast.LENGTH_SHORT).show()
        }
        else -> { /* Still running */ }
      }
    }
}
```

---

## Implementation Phases

### Phase 1: Critical Missing Features (Week 1)

**Priority: CRITICAL**

#### Tasks:
1. ✅ **Create OrderDetailsActivity**
   - View existing order details
   - Add items to existing order
   - Mark order as served
   - Generate bill

2. ✅ **Implement Mark as Served functionality**
   - Add `markAsServed()` method to OrderViewModel
   - Add `updateOrderStatus()` to OrderRepository
   - Add serve button to OrderDetailsActivity

3. ✅ **Complete PaymentActivity**
   - Cash payment flow
   - Change calculation
   - Tip entry UI
   - Payment confirmation

4. ✅ **Add TipEntity & TipDao**
   - Create entity and DAO
   - Integrate with payment flow
   - Add tip notifications

**Deliverables:**
- Waiter can view and manage existing orders
- Waiter can mark orders as served
- Waiter can process cash payments with tips
- Tips are tracked and notified

**Testing:**
- Create order → Mark as served → Verify status update
- Process cash payment → Enter tip → Verify both recorded
- Offline mode → Mark served → Sync when online

---

### Phase 2: Notifications & Real-Time (Week 2)

**Priority: HIGH**

#### Tasks:
1. ✅ **Firebase Cloud Messaging setup**
   - Add FCM dependencies
   - Create notification channels
   - Implement PosFirebaseMessagingService

2. ✅ **NotificationHelper implementation**
   - Order ready notifications
   - Payment notifications
   - Tip notifications

3. ✅ **Backend webhook integration**
   - Send FCM token to server
   - Backend sends push notifications on events

**Deliverables:**
- Real-time notifications when orders ready
- Instant payment confirmations
- Tip notifications with sound/vibration

**Testing:**
- Kitchen marks order done → Waiter receives notification
- Guest pays digitally → Waiter receives payment notification
- Guest includes tip → Waiter receives tip notification

---

### Phase 3: Multi-Table Enhancements (Week 3)

**Priority: MEDIUM-HIGH**

#### Tasks:
1. ✅ **Enhance TablesActivity**
   - Add "My Tables" filter
   - Show active order count badge
   - Add waiter assignment indicator
   - Long-press for table options

2. ✅ **Enhance TableEntity**
   - Add `assignedWaiterId` field
   - Add `activeOrderCount` field
   - Add migration for new fields

3. ✅ **Create TipsActivity**
   - Today's tips summary
   - Tips breakdown by order
   - Weekly/monthly tips view

**Deliverables:**
- Waiter can filter to see only assigned tables
- Visual indicators for active orders
- Tips dashboard showing earnings

**Testing:**
- Assign waiter to tables → Filter shows correct tables
- Multiple orders per table → Badge shows count
- Receive multiple tips → Dashboard shows total

---

### Phase 4: Guest Intelligence & Upselling (Week 4)

**Priority: MEDIUM**

#### Tasks:
1. ✅ **Guest profile display**
   - Show guest name and history
   - Display favorite items
   - Show visit count and average spend

2. ✅ **Upsell suggestions**
   - "Frequently bought together" recommendations
   - Time-based offers (happy hour)
   - High-margin item suggestions

3. ✅ **Guest search and selection**
   - Search by phone number
   - Create new guest profile
   - Link order to guest

**Deliverables:**
- Waiter sees guest history when taking order
- Smart upsell suggestions in order screen
- Easy guest profile creation

**Testing:**
- Take order for returning guest → See favorite items
- Order during happy hour → See time-based offers
- New guest → Create profile seamlessly

---

### Phase 5: Performance & Analytics (Week 5)

**Priority: LOW-MEDIUM**

#### Tasks:
1. ✅ **ProfileActivity creation**
   - Today's stats (orders, sales, tips)
   - Performance metrics
   - Settings and logout

2. ✅ **API integration for analytics**
   - GET /staff/{id}/performance
   - Display charts/graphs
   - Export reports

3. ✅ **Performance optimizations**
   - Image caching with Glide
   - Database query optimization
   - Reduce API calls

**Deliverables:**
- Waiter can view personal performance
- Manager can compare waiter performance
- Optimized app performance

**Testing:**
- View stats after serving multiple orders
- Check accuracy of metrics
- Test performance with large datasets

---

## Testing Requirements

### Unit Tests

**Required Test Files:**

```kotlin
// OrderViewModelTest.kt
class OrderViewModelTest {

  @Test
  fun `addToCart increases cart size`() {
    // Given
    val menuItem = MenuItem(id = 1, name = "Test", price = 10.0)

    // When
    viewModel.addToCart(menuItem)

    // Then
    assertEquals(1, viewModel.cart.value?.size)
  }

  @Test
  fun `createOrder fails when cart empty`() {
    // When
    viewModel.createOrder(guestId = 1, tableId = 1)

    // Then
    assertTrue(viewModel.createOrderState.value is Resource.Error)
  }

  @Test
  fun `updateQuantity updates cart total`() {
    // Given
    val item = CartItem(menuItem = MenuItem(price = 10.0), quantity = 2)
    viewModel.addToCart(item.menuItem)

    // When
    viewModel.updateQuantity(item, 5)

    // Then
    assertEquals(50.0, viewModel.cartTotal.value)
  }
}

// PaymentViewModelTest.kt
class PaymentViewModelTest {

  @Test
  fun `calculateChange returns correct amount`() {
    // Given
    val bill = 10000.0
    val paid = 12000.0

    // When
    val change = BillCalculator.calculateChange(paid, bill)

    // Then
    assertEquals(2000.0, change)
  }

  @Test
  fun `processTip creates tip entity`() = runTest {
    // Given
    val tip = TipEntity(orderId = 1, waiterId = 1, amount = 500.0)

    // When
    paymentRepository.processTip(tip)

    // Then
    val tips = tipDao.getTipsByWaiter(1).first()
    assertTrue(tips.isNotEmpty())
  }
}

// OrderStateManagerTest.kt
class OrderStateManagerTest {

  @Test
  fun `canTransition from READY to SERVED is true`() {
    val result = OrderStateManager.canTransition(
      OrderStatus.READY,
      OrderStatus.SERVED
    )
    assertTrue(result)
  }

  @Test
  fun `canTransition from COMPLETED to SERVED is false`() {
    val result = OrderStateManager.canTransition(
      OrderStatus.COMPLETED,
      OrderStatus.SERVED
    )
    assertFalse(result)
  }
}
```

---

### Integration Tests

```kotlin
// OrderFlowIntegrationTest.kt
@HiltAndroidTest
class OrderFlowIntegrationTest {

  @Test
  fun `complete order flow from creation to served`() = runTest {
    // 1. Create order
    val order = orderRepository.createOrder(testOrder)
    assertNotNull(order)

    // 2. Update to preparing
    orderRepository.updateOrderStatus(order.id, "preparing")
    assertEquals("preparing", orderDao.getOrder(order.id).status)

    // 3. Update to ready
    orderRepository.updateOrderStatus(order.id, "ready")

    // 4. Mark as served
    orderRepository.markAsServed(order.id)
    val served = orderDao.getOrder(order.id)
    assertEquals("served", served.status)
    assertNotNull(served.servedAt)
  }

  @Test
  fun `offline order syncs when connection restored`() = runTest {
    // 1. Create order offline
    networkSimulator.disable()
    val order = orderRepository.createOrder(testOrder)
    assertFalse(order.isSynced)

    // 2. Restore connection
    networkSimulator.enable()

    // 3. Trigger sync
    syncWorker.doWork()

    // 4. Verify synced
    val synced = orderDao.getOrder(order.id)
    assertTrue(synced.isSynced)
  }
}
```

---

### UI Tests (Espresso)

```kotlin
// OrderActivityTest.kt
@RunWith(AndroidJUnit4::class)
class OrderActivityTest {

  @get:Rule
  val activityRule = ActivityScenarioRule(OrderActivity::class.java)

  @Test
  fun `clicking menu item adds to cart`() {
    // Click first menu item
    onView(withId(R.id.rvMenu))
      .perform(RecyclerViewActions.actionOnItemAtPosition<MenuAdapter.ViewHolder>(0, click()))

    // Verify cart has 1 item
    onView(withId(R.id.rvCart))
      .check(matches(hasChildCount(1)))
  }

  @Test
  fun `placing order with empty cart shows error`() {
    // Click place order
    onView(withId(R.id.btnPlaceOrder)).perform(click())

    // Verify toast message
    onView(withText("Cart is empty"))
      .inRoot(ToastMatcher())
      .check(matches(isDisplayed()))
  }

  @Test
  fun `successful order creation navigates back`() {
    // Add item to cart
    onView(withId(R.id.rvMenu))
      .perform(RecyclerViewActions.actionOnItemAtPosition<MenuAdapter.ViewHolder>(0, click()))

    // Place order
    onView(withId(R.id.btnPlaceOrder)).perform(click())

    // Wait for success
    Thread.sleep(2000)

    // Verify activity finished
    assertTrue(activityRule.scenario.state == Lifecycle.State.DESTROYED)
  }
}
```

---

## Success Criteria

### Feature Completeness

- [ ] ✅ Waiter can log in and see assigned tables
- [ ] ✅ Waiter can create new orders
- [ ] ✅ Waiter can add items to existing orders
- [ ] ✅ Waiter can mark orders as served
- [ ] ✅ Waiter can process cash payments
- [ ] ✅ Waiter can enter tips
- [ ] ✅ Waiter receives notifications when orders ready
- [ ] ✅ Waiter receives payment confirmations
- [ ] ✅ Waiter can view tips received
- [ ] ✅ Waiter can manage multiple tables
- [ ] ✅ App works offline with auto-sync

### Performance Benchmarks

- Order creation < 2 seconds
- Table list load < 1 second
- Menu browsing smooth (60fps)
- Offline sync < 5 seconds per order
- No ANR (Application Not Responding) errors

### Quality Metrics

- Unit test coverage > 70%
- Integration test coverage > 50%
- Zero critical bugs
- All flows tested end-to-end
- Accessibility score > 80%

---

## Appendix

### Color Scheme (Status Indicators)

```xml
<!-- colors.xml -->
<color name="status_pending">#FFA726</color>      <!-- Orange -->
<color name="status_confirmed">#42A5F5</color>    <!-- Blue -->
<color name="status_preparing">#FFEE58</color>    <!-- Yellow -->
<color name="status_ready">#66BB6A</color>        <!-- Green -->
<color name="status_served">#78909C</color>       <!-- Blue Grey -->
<color name="status_completed">#9E9E9E</color>    <!-- Grey -->
<color name="status_cancelled">#EF5350</color>    <!-- Red -->
```

---

### String Resources

```xml
<!-- strings.xml -->
<string name="order_placed_success">Order placed successfully!</string>
<string name="order_served_success">Order marked as served</string>
<string name="payment_success">Payment processed successfully</string>
<string name="tip_recorded">Tip recorded: TZS %1$.0f</string>
<string name="cart_empty">Cart is empty</string>
<string name="confirm_serve">Mark order for %1$s as served?</string>
<string name="change_due">Change: TZS %1$.0f</string>
<string name="tip_received_title">You received a tip!</string>
<string name="tip_received_message">%1$s tipped TZS %2$.0f</string>
```

---

### Keyboard Shortcuts (for Tablet)

```kotlin
// MainActivity.kt
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
  return when (keyCode) {
    KeyEvent.KEYCODE_T -> {
      // Ctrl+T → Tables
      startActivity(Intent(this, TablesActivity::class.java))
      true
    }
    KeyEvent.KEYCODE_O -> {
      // Ctrl+O → Orders
      startActivity(Intent(this, OrdersActivity::class.java))
      true
    }
    KeyEvent.KEYCODE_P -> {
      // Ctrl+P → Payment
      // Open last active order payment
      true
    }
    else -> super.onKeyDown(keyCode, event)
  }
}
```

---

## Document Version

**Version:** 1.0
**Date:** 2026-01-30
**Author:** Technical Implementation Team
**Status:** Draft for Implementation

**Change Log:**
- v1.0 (2026-01-30): Initial document creation based on user journey analysis

---

**END OF DOCUMENT**

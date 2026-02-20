package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.data.local.entity.OrderEntity
import com.seacliff.pos.data.local.entity.OrderItemEntity
import com.seacliff.pos.data.repository.OrderItemDetail
import com.seacliff.pos.data.repository.OrderRepository
import com.seacliff.pos.data.remote.dto.OrderItemRequest
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CartItem(
    val menuItem: MenuItemEntity,
    val quantity: Int = 1,
    val notes: String? = null
) {
    val subtotal: Double
        get() = menuItem.price * quantity

    fun copy(quantity: Int = this.quantity, notes: String? = this.notes): CartItem {
        return CartItem(menuItem, quantity, notes)
    }
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> = _cart

    private val _cartTotal = MutableLiveData<Double>(0.0)
    val cartTotal: LiveData<Double> = _cartTotal

    private val _createOrderState = MutableLiveData<Resource<OrderEntity>>()
    val createOrderState: LiveData<Resource<OrderEntity>> = _createOrderState

    private val _orders = MutableLiveData<List<OrderEntity>>()
    val orders: LiveData<List<OrderEntity>> = _orders

    private val _orderItems = MutableLiveData<List<OrderItemEntity>>()
    val orderItems: LiveData<List<OrderItemEntity>> = _orderItems

    private val _currentOrder = MutableLiveData<Resource<OrderEntity>>()
    val currentOrder: LiveData<Resource<OrderEntity>> = _currentOrder

    private val _updateStatusResult = MutableLiveData<Resource<Unit>>()
    val updateStatusResult: LiveData<Resource<Unit>> = _updateStatusResult

    private val _activeOrdersForTable = MutableLiveData<List<OrderEntity>>(emptyList())
    val activeOrdersForTable: LiveData<List<OrderEntity>> = _activeOrdersForTable

    private val _syncRefreshing = MutableLiveData(false)
    val syncRefreshing: LiveData<Boolean> = _syncRefreshing

    init {
        fetchAllOrdersFromApi()
    }

    fun loadActiveOrdersForTable(tableId: Long) {
        viewModelScope.launch {
            orderRepository.getActiveOrdersByTable(tableId).collect { orders ->
                _activeOrdersForTable.value = orders
            }
        }
    }

    fun addToCart(menuItem: MenuItemEntity) {
        val currentCart = _cart.value.orEmpty().toMutableList()

        val existingIndex = currentCart.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existingIndex >= 0) {
            val existingItem = currentCart[existingIndex]
            currentCart[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentCart.add(CartItem(menuItem))
        }

        _cart.value = currentCart.toList()
        calculateCartTotal()

        Timber.d("Added ${menuItem.name} to cart. Cart size: ${currentCart.size}")
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentCart = _cart.value.orEmpty().toMutableList()
        currentCart.removeAll { it.menuItem.id == cartItem.menuItem.id }
        _cart.value = currentCart.toList()
        calculateCartTotal()
    }

    fun updateQuantity(cartItem: CartItem, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(cartItem)
        } else {
            val currentCart = _cart.value.orEmpty().toMutableList()
            val index = currentCart.indexOfFirst { it.menuItem.id == cartItem.menuItem.id }
            if (index >= 0) {
                currentCart[index] = currentCart[index].copy(quantity = quantity)
                _cart.value = currentCart.toList()
                calculateCartTotal()
            }
        }
    }

    fun updateNotes(cartItem: CartItem, notes: String?) {
        val currentCart = _cart.value.orEmpty().toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == cartItem.menuItem.id }
        if (index >= 0) {
            currentCart[index] = currentCart[index].copy(notes = notes)
            _cart.value = currentCart.toList()
        }
    }

    private fun calculateCartTotal() {
        val total = _cart.value?.sumOf { it.subtotal } ?: 0.0
        _cartTotal.value = total
    }

    fun createOrder(guestId: Long, tableId: Long, notes: String? = null) {
        val cartItems = _cart.value ?: return

        if (cartItems.isEmpty()) {
            _createOrderState.value = Resource.Error("Cart is empty")
            return
        }

        val orderItems = cartItems.map { cartItem ->
            OrderItemRequest(
                menuItemId = cartItem.menuItem.id,
                quantity = cartItem.quantity,
                specialInstructions = cartItem.notes
            )
        }
        val localOrderItems = cartItems.map { cartItem ->
            OrderItemDetail(
                menuItemId = cartItem.menuItem.id,
                quantity = cartItem.quantity,
                unitPrice = cartItem.menuItem.price,
                notes = cartItem.notes
            )
        }

        viewModelScope.launch {
            orderRepository.createOrder(guestId, tableId, orderItems, notes, localOrderItems).collect { resource ->
                _createOrderState.value = resource

                if (resource is Resource.Success) {
                    clearCart()
                }
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _cartTotal.value = 0.0
    }

    /** Fetch all orders directly from API. Used for "All" tab. */
    fun fetchAllOrdersFromApi() {
        viewModelScope.launch {
            _syncRefreshing.value = true
            when (val result = orderRepository.fetchOrdersFromApi()) {
                is Resource.Success -> {
                    val orders = result.data ?: emptyList()
                    Timber.d("Fetched ${orders.size} orders from API")
                    _orders.value = orders
                }
                is Resource.Error -> {
                    Timber.e("API fetch failed: ${result.message}, falling back to local")
                    // Fallback to local if API fails
                    orderRepository.getOrders().collect { orders ->
                        _orders.value = orders
                    }
                }
                is Resource.Loading -> {}
            }
            _syncRefreshing.value = false
        }
    }

    /** Load all orders from local DB. */
    fun loadAllOrders() {
        fetchAllOrdersFromApi()
    }

    /** Sync/refresh orders from API. */
    fun syncAndLoadOrders() {
        fetchAllOrdersFromApi()
    }

    /** Fetch orders by status directly from API. */
    fun loadOrdersByStatus(status: String) {
        viewModelScope.launch {
            _syncRefreshing.value = true
            when (val result = orderRepository.fetchOrdersByStatusFromApi(status)) {
                is Resource.Success -> {
                    _orders.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    // Fallback to local if API fails
                    orderRepository.getOrdersByStatus(status).collect { orders ->
                        _orders.value = orders
                    }
                }
                is Resource.Loading -> {}
            }
            _syncRefreshing.value = false
        }
    }

    fun loadOrderItems(orderId: Long) {
        viewModelScope.launch {
            orderRepository.getOrderItems(orderId).collect { items ->
                _orderItems.value = items
            }
        }
    }

    fun updateOrderStatus(orderId: Long, status: String) {
        _updateStatusResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, status)
            _updateStatusResult.value = result
            if (result is Resource.Success) {
                loadAllOrders()
            }
        }
    }

    fun markAsServed(orderId: Long) {
        _updateStatusResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = orderRepository.markAsServed(orderId)
            _updateStatusResult.value = result
            if (result is Resource.Success) {
                loadAllOrders()
            }
        }
    }

    fun getOrderById(orderId: Long) {
        _currentOrder.value = Resource.Loading()
        viewModelScope.launch {
            val result = orderRepository.getOrderById(orderId)
            _currentOrder.value = result
        }
    }

    fun getOrderItems(orderId: Long) {
        viewModelScope.launch {
            // Try API first for order items with names
            when (val result = orderRepository.fetchOrderItemsFromApi(orderId)) {
                is Resource.Success -> {
                    Timber.d("Fetched ${result.data?.size ?: 0} order items from API")
                    _orderItems.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    Timber.e("API fetch failed for order items: ${result.message}, falling back to local")
                    // Fallback to local
                    orderRepository.getOrderItems(orderId).collect { items ->
                        _orderItems.value = items
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun getCartItemCount(): Int {
        return _cart.value?.sumOf { it.quantity } ?: 0
    }
}

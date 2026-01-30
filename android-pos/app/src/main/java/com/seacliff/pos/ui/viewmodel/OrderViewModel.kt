package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.data.local.entity.OrderEntity
import com.seacliff.pos.data.local.entity.OrderItemEntity
import com.seacliff.pos.data.repository.OrderRepository
import com.seacliff.pos.data.remote.dto.OrderItemRequest
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CartItem(
    val menuItem: MenuItemEntity,
    var quantity: Int = 1,
    var notes: String? = null
) {
    val subtotal: Double
        get() = menuItem.price * quantity
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _cart = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cart: LiveData<MutableList<CartItem>> = _cart

    private val _cartTotal = MutableLiveData<Double>(0.0)
    val cartTotal: LiveData<Double> = _cartTotal

    private val _createOrderState = MutableLiveData<Resource<OrderEntity>>()
    val createOrderState: LiveData<Resource<OrderEntity>> = _createOrderState

    private val _orders = MutableLiveData<List<OrderEntity>>()
    val orders: LiveData<List<OrderEntity>> = _orders

    private val _orderItems = MutableLiveData<List<OrderItemEntity>>()
    val orderItems: LiveData<List<OrderItemEntity>> = _orderItems

    init {
        loadTodayOrders()
    }

    fun addToCart(menuItem: MenuItemEntity) {
        val currentCart = _cart.value ?: mutableListOf()

        val existingItem = currentCart.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            currentCart.add(CartItem(menuItem))
        }

        _cart.value = currentCart
        calculateCartTotal()

        Timber.d("Added ${menuItem.name} to cart. Cart size: ${currentCart.size}")
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentCart = _cart.value ?: mutableListOf()
        currentCart.remove(cartItem)
        _cart.value = currentCart
        calculateCartTotal()
    }

    fun updateQuantity(cartItem: CartItem, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(cartItem)
        } else {
            cartItem.quantity = quantity
            _cart.value = _cart.value
            calculateCartTotal()
        }
    }

    fun updateNotes(cartItem: CartItem, notes: String?) {
        cartItem.notes = notes
        _cart.value = _cart.value
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
                notes = cartItem.notes
            )
        }

        viewModelScope.launch {
            orderRepository.createOrder(guestId, tableId, orderItems, notes).collect { resource ->
                _createOrderState.value = resource

                if (resource is Resource.Success) {
                    clearCart()
                }
            }
        }
    }

    fun clearCart() {
        _cart.value = mutableListOf()
        _cartTotal.value = 0.0
    }

    fun loadTodayOrders() {
        viewModelScope.launch {
            orderRepository.getTodayOrders().collect { orders ->
                _orders.value = orders
            }
        }
    }

    fun loadOrdersByStatus(status: String) {
        viewModelScope.launch {
            orderRepository.getOrdersByStatus(status).collect { orders ->
                _orders.value = orders
            }
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
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, status)
            if (result is Resource.Success) {
                loadTodayOrders()
            }
        }
    }

    fun getCartItemCount(): Int {
        return _cart.value?.sumOf { it.quantity } ?: 0
    }
}

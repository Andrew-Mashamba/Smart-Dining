package com.seacliff.pos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.seacliff.pos.R
import com.seacliff.pos.databinding.ActivityOrderBinding
import com.seacliff.pos.ui.adapters.CartAdapter
import com.seacliff.pos.ui.adapters.MenuAdapter
import com.seacliff.pos.ui.adapters.OrderListAdapter
import com.seacliff.pos.ui.viewmodel.MenuViewModel
import com.seacliff.pos.ui.viewmodel.OrderViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private val menuViewModel: MenuViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var cartAdapter: CartAdapter
    private lateinit var existingOrdersAdapter: OrderListAdapter

    private var tableId: Long = 0
    private var tableName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tableId = intent.getLongExtra("table_id", 0)
        tableName = intent.getStringExtra("table_name") ?: ""

        setupToolbar()
        setupRecyclerViews()
        setupTabs()
        setupCart()
        observeViewModels()

        orderViewModel.loadActiveOrdersForTable(tableId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "New Order - $tableName"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerViews() {
        // Menu RecyclerView
        menuAdapter = MenuAdapter { menuItem ->
            orderViewModel.addToCart(menuItem)
            Toast.makeText(this, "${menuItem.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = menuAdapter
        }

        // Cart RecyclerView
        cartAdapter = CartAdapter(
            onQuantityChanged = { cartItem, quantity ->
                orderViewModel.updateQuantity(cartItem, quantity)
            },
            onNotesChanged = { cartItem, notes ->
                orderViewModel.updateNotes(cartItem, notes)
            },
            onRemoveClicked = { cartItem ->
                orderViewModel.removeFromCart(cartItem)
            }
        )

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = cartAdapter
        }

        existingOrdersAdapter = OrderListAdapter { order ->
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }
        binding.rvExistingOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = existingOrdersAdapter
        }
    }

    private fun setupTabs() {
        val categories = listOf("All", "Appetizer", "Main", "Dessert", "Drink")

        categories.forEach { category ->
            binding.tabCategories.addTab(
                binding.tabCategories.newTab().setText(category)
            )
        }

        binding.tabCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = tab?.text.toString().lowercase()
                menuViewModel.selectCategory(if (category == "all") "all" else category)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCart() {
        binding.btnPlaceOrder.setOnClickListener {
            if (orderViewModel.getCartItemCount() == 0) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // For now, use guest_id = 1 (or create guest selection)
            orderViewModel.createOrder(guestId = 1, tableId = tableId)
        }
    }

    private fun observeViewModels() {
        // Observe menu items
        menuViewModel.menuItems.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { menuAdapter.submitList(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe cart
        orderViewModel.cart.observe(this) { cartItems ->
            cartAdapter.submitList(cartItems)
            binding.tvCartEmpty.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe cart total
        orderViewModel.cartTotal.observe(this) { total ->
            binding.tvTotal.text = "Total: TZS ${String.format("%,.0f", total)}"
        }

        orderViewModel.activeOrdersForTable.observe(this) { orders ->
            binding.sectionExistingOrders.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
            existingOrdersAdapter.submitList(orders)
        }

        // Observe order creation
        orderViewModel.createOrderState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnPlaceOrder.isEnabled = false
                    binding.progressOrder.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.btnPlaceOrder.isEnabled = true
                    binding.progressOrder.visibility = View.GONE
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.btnPlaceOrder.isEnabled = true
                    binding.progressOrder.visibility = View.GONE
                    val msg = resource.message ?: "Order failed"
                    if (msg == "Session expired. Please log in again.") {
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        })
                        finish()
                    } else {
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                            .setAction("Try again") { orderViewModel.createOrder(guestId = 1, tableId = tableId) }
                            .show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_order, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_cart -> {
                orderViewModel.clearCart()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

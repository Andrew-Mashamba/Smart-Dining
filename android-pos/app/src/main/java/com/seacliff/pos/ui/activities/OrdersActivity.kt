package com.seacliff.pos.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.seacliff.pos.databinding.ActivityOrdersBinding
import com.seacliff.pos.service.PosFirebaseMessagingService
import com.seacliff.pos.ui.adapters.OrderListAdapter
import com.seacliff.pos.ui.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var orderAdapter: OrderListAdapter

    // Broadcast receiver for FCM order updates
    private val orderUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra("order_id")
            Timber.d("Received order update broadcast: orderId=$orderId")
            // Refresh the order list
            orderViewModel.syncAndLoadOrders()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        observeViewModel()

        orderViewModel.syncAndLoadOrders()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Orders"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderListAdapter { order ->
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = orderAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            orderViewModel.syncAndLoadOrders()
        }
    }

    private fun setupTabs() {
        val statuses = listOf("All", "Pending", "Preparing", "Ready", "Served")

        statuses.forEach { status ->
            binding.tabStatus.addTab(
                binding.tabStatus.newTab().setText(status)
            )
        }

        binding.tabStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val status = tab?.text.toString().lowercase()
                if (status == "all") {
                    orderViewModel.loadAllOrders()
                } else {
                    orderViewModel.loadOrdersByStatus(status)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        orderViewModel.orders.observe(this) { orders ->
            timber.log.Timber.d("OrdersActivity received ${orders.size} orders")
            orderAdapter.submitList(orders)
            binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
        orderViewModel.syncRefreshing.observe(this) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
        }
    }

    override fun onResume() {
        super.onResume()
        // Register broadcast receiver for FCM order updates
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                orderUpdateReceiver,
                IntentFilter(PosFirebaseMessagingService.ACTION_ORDER_UPDATED)
            )
    }

    override fun onPause() {
        super.onPause()
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(orderUpdateReceiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

package com.seacliff.pos.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.seacliff.pos.databinding.ActivityOrdersBinding
import com.seacliff.pos.ui.adapters.OrderListAdapter
import com.seacliff.pos.ui.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var orderAdapter: OrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        observeViewModel()
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
            // Navigate to order details
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = orderAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            orderViewModel.loadTodayOrders()
            binding.swipeRefresh.isRefreshing = false
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
                    orderViewModel.loadTodayOrders()
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
            orderAdapter.submitList(orders)
            binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

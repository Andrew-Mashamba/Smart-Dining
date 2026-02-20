package com.seacliff.pos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.seacliff.pos.databinding.ActivityOrderDetailsBinding
import com.seacliff.pos.ui.adapter.OrderItemAdapter
import com.seacliff.pos.ui.viewmodel.OrderViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OrderDetailsActivity : AppCompatActivity() {

    private var _binding: ActivityOrderDetailsBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var orderItemAdapter: OrderItemAdapter

    private var orderId: Long = 0
    private var currentStatus: String = ""

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("sw", "TZ")).apply {
        currency = Currency.getInstance("TZS")
    }

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getLongExtra("ORDER_ID", 0)

        if (orderId == 0L) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadOrderDetails()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Order Details"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        orderItemAdapter = OrderItemAdapter()
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailsActivity)
            adapter = orderItemAdapter
        }
    }

    private fun setupListeners() {
        binding.btnMarkServed.setOnClickListener {
            markOrderAsServed()
        }

        binding.btnGenerateBill.setOnClickListener {
            generateBill()
        }
    }

    private fun loadOrderDetails() {
        binding.progressBar.visibility = View.VISIBLE

        orderViewModel.getOrderById(orderId)

        orderViewModel.currentOrder.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { order ->
                        displayOrderDetails(order)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayOrderDetails(order: com.seacliff.pos.data.local.entity.OrderEntity) {
        currentStatus = order.status

        // Order info
        binding.tvOrderId.text = "#${order.id}"
        binding.tvTableName.text = "Table ${order.tableId}"
        binding.tvCreatedTime.text = order.createdAt?.let { dateFormatter.format(it) } ?: "N/A"

        // Status chip
        binding.chipStatus.text = order.status.capitalize()
        binding.chipStatus.setChipBackgroundColorResource(getStatusColor(order.status))

        // Total amount
        binding.tvTotalAmount.text = currencyFormatter.format(order.totalAmount)

        // Notes
        if (!order.notes.isNullOrEmpty()) {
            binding.cardNotes.visibility = View.VISIBLE
            binding.tvNotes.text = order.notes
        } else {
            binding.cardNotes.visibility = View.GONE
        }

        // Load order items
        loadOrderItems(order.id)

        // Update button visibility based on status
        updateActionButtons(order.status)
    }

    private fun loadOrderItems(orderId: Long) {
        orderViewModel.getOrderItems(orderId)

        orderViewModel.orderItems.observe(this) { items ->
            orderItemAdapter.submitList(items)
        }
    }

    private fun updateActionButtons(status: String) {
        when (status) {
            "pending", "confirmed", "preparing" -> {
                binding.btnMarkServed.visibility = View.GONE
                binding.btnGenerateBill.visibility = View.GONE
            }
            "ready" -> {
                binding.btnMarkServed.visibility = View.VISIBLE
                binding.btnGenerateBill.visibility = View.GONE
            }
            "served" -> {
                binding.btnMarkServed.visibility = View.GONE
                binding.btnGenerateBill.visibility = View.VISIBLE
            }
            "completed", "cancelled" -> {
                binding.btnMarkServed.visibility = View.GONE
                binding.btnGenerateBill.visibility = View.GONE
            }
        }
    }

    private fun markOrderAsServed() {
        AlertDialog.Builder(this)
            .setTitle("Mark as Served")
            .setMessage("Confirm that this order has been served to the customer?")
            .setPositiveButton("Confirm") { _, _ ->
                performMarkServed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performMarkServed() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnMarkServed.isEnabled = false

        orderViewModel.updateOrderStatus(orderId, "served")

        orderViewModel.updateStatusResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnMarkServed.isEnabled = true

                    Toast.makeText(this, "Order marked as served", Toast.LENGTH_SHORT).show()

                    // Reload order details
                    loadOrderDetails()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnMarkServed.isEnabled = true

                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun generateBill() {
        // Navigate to payment activity
        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("ORDER_ID", orderId)
            putExtra("TOTAL_AMOUNT", orderViewModel.currentOrder.value?.data?.totalAmount ?: 0.0)
        }
        startActivityForResult(intent, REQUEST_PAYMENT)
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "pending" -> android.R.color.holo_orange_dark
            "confirmed" -> android.R.color.holo_blue_dark
            "preparing" -> android.R.color.holo_orange_light
            "ready" -> android.R.color.holo_green_light
            "served" -> android.R.color.holo_green_dark
            "completed" -> android.R.color.darker_gray
            "cancelled" -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PAYMENT && resultCode == RESULT_OK) {
            // Payment completed successfully
            Toast.makeText(this, "Payment completed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val REQUEST_PAYMENT = 1001
    }
}

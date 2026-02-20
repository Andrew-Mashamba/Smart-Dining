package com.seacliff.pos.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.seacliff.pos.R
import com.seacliff.pos.data.local.entity.PaymentEntity
import com.seacliff.pos.data.local.entity.TipEntity
import com.seacliff.pos.databinding.ActivityPaymentBinding
import com.seacliff.pos.ui.viewmodel.OrderViewModel
import com.seacliff.pos.ui.viewmodel.PaymentViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {

    private var _binding: ActivityPaymentBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()

    private var orderId: Long = 0
    private var totalAmount: Double = 0.0
    private var subtotal: Double = 0.0
    private var tax: Double = 0.0
    private var tipAmount: Double = 0.0
    private var amountReceived: Double = 0.0
    private var selectedPaymentMethod: String = "cash"

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("sw", "TZ")).apply {
        currency = Currency.getInstance("TZS")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get order details from intent
        orderId = intent.getLongExtra("ORDER_ID", 0)
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)

        if (orderId == 0L) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupUI()
        setupListeners()
        calculateAmounts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Payment"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupUI() {
        // Calculate tax (18% VAT)
        subtotal = totalAmount / 1.18
        tax = totalAmount - subtotal

        binding.tvSubtotal.text = currencyFormatter.format(subtotal)
        binding.tvTax.text = currencyFormatter.format(tax)
        binding.tvTotalAmount.text = currencyFormatter.format(totalAmount)
    }

    private fun setupListeners() {
        // Payment method selection
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCash -> {
                    selectedPaymentMethod = "cash"
                    binding.cardCashPayment.visibility = View.VISIBLE
                }
                R.id.rbCard -> {
                    selectedPaymentMethod = "card"
                    binding.cardCashPayment.visibility = View.GONE
                }
                R.id.rbMobile -> {
                    selectedPaymentMethod = "mobile"
                    binding.cardCashPayment.visibility = View.GONE
                }
            }
        }

        // Amount received input (for cash)
        binding.etAmountReceived.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateChange()
            }
        })

        // Quick tip buttons
        binding.btnTip10.setOnClickListener {
            val tip = totalAmount * 0.10
            binding.etTipAmount.setText(tip.toString())
            tipAmount = tip
        }

        binding.btnTip15.setOnClickListener {
            val tip = totalAmount * 0.15
            binding.etTipAmount.setText(tip.toString())
            tipAmount = tip
        }

        binding.btnTip20.setOnClickListener {
            val tip = totalAmount * 0.20
            binding.etTipAmount.setText(tip.toString())
            tipAmount = tip
        }

        // Custom tip input
        binding.etTipAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tipAmount = s?.toString()?.toDoubleOrNull() ?: 0.0
            }
        })

        // Confirm payment button
        binding.btnConfirmPayment.setOnClickListener {
            processPayment()
        }
    }

    private fun calculateAmounts() {
        // Initial calculations already done in setupUI
    }

    private fun calculateChange() {
        val amountReceivedText = binding.etAmountReceived.text?.toString() ?: "0"
        amountReceived = amountReceivedText.toDoubleOrNull() ?: 0.0

        val change = amountReceived - totalAmount
        binding.tvChange.text = if (change >= 0) {
            currencyFormatter.format(change)
        } else {
            currencyFormatter.format(0.0)
        }
    }

    private fun processPayment() {
        // Validate payment
        if (selectedPaymentMethod == "cash") {
            if (amountReceived < totalAmount) {
                Toast.makeText(
                    this,
                    "Amount received is less than total",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage(buildConfirmationMessage())
            .setPositiveButton("Confirm") { _, _ ->
                submitPayment()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildConfirmationMessage(): String {
        val builder = StringBuilder()
        builder.append("Payment Method: ${selectedPaymentMethod.capitalize()}\n")
        builder.append("Amount: ${currencyFormatter.format(totalAmount)}\n")

        if (selectedPaymentMethod == "cash") {
            builder.append("Received: ${currencyFormatter.format(amountReceived)}\n")
            val change = amountReceived - totalAmount
            if (change > 0) {
                builder.append("Change: ${currencyFormatter.format(change)}\n")
            }
        }

        if (tipAmount > 0) {
            builder.append("\nTip: ${currencyFormatter.format(tipAmount)}")
        }

        return builder.toString()
    }

    private fun submitPayment() {
        // Create payment entity
        val payment = PaymentEntity(
            orderId = orderId,
            amount = totalAmount,
            method = selectedPaymentMethod,
            status = "completed",
            transactionId = generateTransactionId(),
            createdAt = Date(),
            updatedAt = Date()
        )

        // Submit payment
        paymentViewModel.processPayment(payment)

        // If there's a tip, save it
        if (tipAmount > 0) {
            val tip = TipEntity(
                orderId = orderId,
                waiterId = 1, // TODO: Get current waiter ID
                amount = tipAmount,
                method = selectedPaymentMethod
            )
            paymentViewModel.saveTip(tip)
        }

        // Observe payment result
        paymentViewModel.paymentResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnConfirmPayment.isEnabled = false
                    binding.btnConfirmPayment.text = "Processing..."
                }
                is Resource.Success -> {
                    Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()

                    // Mark order as completed
                    orderViewModel.updateOrderStatus(orderId, "completed")

                    // Show success message and finish
                    AlertDialog.Builder(this)
                        .setTitle("Payment Complete")
                        .setMessage(buildReceiptMessage())
                        .setPositiveButton("Done") { _, _ ->
                            setResult(RESULT_OK)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
                is Resource.Error -> {
                    binding.btnConfirmPayment.isEnabled = true
                    binding.btnConfirmPayment.text = "Confirm Payment"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun buildReceiptMessage(): String {
        val builder = StringBuilder()
        builder.append("Order #$orderId\n\n")
        builder.append("Subtotal: ${currencyFormatter.format(subtotal)}\n")
        builder.append("VAT (18%): ${currencyFormatter.format(tax)}\n")
        builder.append("Total: ${currencyFormatter.format(totalAmount)}\n\n")

        builder.append("Payment Method: ${selectedPaymentMethod.capitalize()}\n")

        if (selectedPaymentMethod == "cash") {
            builder.append("Received: ${currencyFormatter.format(amountReceived)}\n")
            val change = amountReceived - totalAmount
            if (change > 0) {
                builder.append("Change: ${currencyFormatter.format(change)}\n")
            }
        }

        if (tipAmount > 0) {
            builder.append("\nTip: ${currencyFormatter.format(tipAmount)}\n")
            builder.append("Thank you for your generosity!")
        }

        builder.append("\n\nThank you for dining with us!")

        return builder.toString()
    }

    private fun generateTransactionId(): String {
        return "TXN${System.currentTimeMillis()}"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

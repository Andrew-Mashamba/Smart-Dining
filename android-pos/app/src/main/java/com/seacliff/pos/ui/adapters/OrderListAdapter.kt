package com.seacliff.pos.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seacliff.pos.R
import com.seacliff.pos.data.local.entity.OrderEntity
import com.seacliff.pos.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderListAdapter(
    private val onOrderClick: (OrderEntity) -> Unit
) : ListAdapter<OrderEntity, OrderListAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderEntity) {
            binding.apply {
                tvOrderId.text = "Order #${order.id}"
                tvTableName.text = order.tableName?.takeIf { it.isNotBlank() } ?: "Table ${order.tableId}"
                val waiterText = order.waiterName?.takeIf { it.isNotBlank() }?.let { "Waiter: $it" } ?: ""
                tvWaiter.text = waiterText
                tvWaiter.visibility = if (waiterText.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                val amount = if (order.totalAmount > 0) order.totalAmount else (order.subtotal + order.tax)
                tvTotal.text = "TZS ${String.format("%,.0f", amount)}"
                tvStatus.text = order.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                tvTime.text = order.createdAt?.let { dateFormat.format(it) } ?: ""

                // Status color
                val statusColor = when (order.status) {
                    "pending" -> R.color.status_pending
                    "confirmed" -> R.color.status_confirmed
                    "preparing" -> R.color.status_preparing
                    "ready" -> R.color.status_ready
                    "served" -> R.color.status_served
                    "completed" -> R.color.status_completed
                    "cancelled" -> R.color.status_cancelled
                    else -> R.color.gray
                }

                statusIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, statusColor)
                )

                // Sync indicator
                if (!order.isSynced) {
                    ivSyncStatus.setImageResource(R.drawable.ic_sync_pending)
                } else {
                    ivSyncStatus.setImageResource(R.drawable.ic_sync_done)
                }

                root.setOnClickListener {
                    onOrderClick(order)
                }
            }
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<OrderEntity>() {
        override fun areItemsTheSame(oldItem: OrderEntity, newItem: OrderEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderEntity, newItem: OrderEntity): Boolean {
            return oldItem == newItem
        }
    }
}

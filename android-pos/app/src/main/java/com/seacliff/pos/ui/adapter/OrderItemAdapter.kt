package com.seacliff.pos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seacliff.pos.data.local.entity.OrderItemEntity
import com.seacliff.pos.databinding.ItemOrderItemBinding
import java.text.NumberFormat
import java.util.*

class OrderItemAdapter : ListAdapter<OrderItemEntity, OrderItemAdapter.OrderItemViewHolder>(OrderItemDiffCallback()) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("sw", "TZ")).apply {
        currency = Currency.getInstance("TZS")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderItemViewHolder(
        private val binding: ItemOrderItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderItemEntity) {
            binding.apply {
                // Item name and quantity
                val name = orderItem.itemName ?: "Item #${orderItem.menuItemId}"
                tvItemName.text = "${orderItem.quantity}x $name"

                // Unit price
                tvUnitPrice.text = currencyFormatter.format(orderItem.unitPrice)

                // Subtotal
                val subtotal = orderItem.quantity * orderItem.unitPrice
                tvSubtotal.text = currencyFormatter.format(subtotal)

                // Notes
                if (!orderItem.notes.isNullOrEmpty()) {
                    tvNotes.text = orderItem.notes
                    tvNotes.visibility = android.view.View.VISIBLE
                } else {
                    tvNotes.visibility = android.view.View.GONE
                }

                // Status
                chipStatus.text = orderItem.status.capitalize()
                chipStatus.setChipBackgroundColorResource(getStatusColor(orderItem.status))
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "pending" -> android.R.color.holo_orange_dark
                "preparing" -> android.R.color.holo_orange_light
                "ready" -> android.R.color.holo_green_light
                "served" -> android.R.color.holo_green_dark
                else -> android.R.color.darker_gray
            }
        }
    }

    class OrderItemDiffCallback : DiffUtil.ItemCallback<OrderItemEntity>() {
        override fun areItemsTheSame(oldItem: OrderItemEntity, newItem: OrderItemEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderItemEntity, newItem: OrderItemEntity): Boolean {
            return oldItem == newItem
        }
    }
}

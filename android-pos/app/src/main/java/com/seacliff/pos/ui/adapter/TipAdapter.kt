package com.seacliff.pos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seacliff.pos.data.local.entity.TipEntity
import com.seacliff.pos.databinding.ItemTipBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TipAdapter : ListAdapter<TipEntity, TipAdapter.TipViewHolder>(TipDiffCallback()) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("sw", "TZ")).apply {
        currency = Currency.getInstance("TZS")
    }

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = ItemTipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TipViewHolder(
        private val binding: ItemTipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: TipEntity) {
            binding.apply {
                tvTipAmount.text = currencyFormatter.format(tip.amount)
                tvTipMethod.text = tip.method.capitalize()
                tvOrderId.text = "Order #${tip.orderId}"
                tvTime.text = timeFormatter.format(Date(tip.createdAt))
            }
        }
    }

    class TipDiffCallback : DiffUtil.ItemCallback<TipEntity>() {
        override fun areItemsTheSame(oldItem: TipEntity, newItem: TipEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TipEntity, newItem: TipEntity): Boolean {
            return oldItem == newItem
        }
    }
}

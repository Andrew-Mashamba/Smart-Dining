package com.seacliff.pos.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seacliff.pos.R
import com.seacliff.pos.data.local.entity.TableEntity
import com.seacliff.pos.databinding.ItemTableBinding

class TableAdapter(
    private val onTableClick: (TableEntity) -> Unit
) : ListAdapter<TableEntity, TableAdapter.TableViewHolder>(TableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TableViewHolder(
        private val binding: ItemTableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(table: TableEntity) {
            binding.apply {
                tvTableName.text = table.name
                tvCapacity.text = "${table.capacity} seats"
                tvLocation.text = table.location.capitalize()

                // Set status indicator
                val statusColor = when (table.status) {
                    "available" -> R.color.status_available
                    "occupied" -> R.color.status_occupied
                    "reserved" -> R.color.status_reserved
                    else -> R.color.gray
                }

                statusIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, statusColor)
                )

                tvStatus.text = table.status.capitalize()

                // Enable/disable click based on status
                root.isEnabled = table.status == "available"
                root.alpha = if (table.status == "available") 1.0f else 0.6f

                root.setOnClickListener {
                    if (table.status == "available") {
                        onTableClick(table)
                    }
                }
            }
        }
    }

    private class TableDiffCallback : DiffUtil.ItemCallback<TableEntity>() {
        override fun areItemsTheSame(oldItem: TableEntity, newItem: TableEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TableEntity, newItem: TableEntity): Boolean {
            return oldItem == newItem
        }
    }
}

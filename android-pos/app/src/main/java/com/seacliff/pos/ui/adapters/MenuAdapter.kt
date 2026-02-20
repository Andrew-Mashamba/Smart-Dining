package com.seacliff.pos.ui.adapters

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seacliff.pos.R
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.databinding.ItemMenuBinding

class MenuAdapter(
    private val onItemClick: (MenuItemEntity) -> Unit
) : ListAdapter<MenuItemEntity, MenuAdapter.MenuViewHolder>(MenuDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MenuViewHolder(
        private val binding: ItemMenuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItemEntity) {
            binding.apply {
                tvName.text = menuItem.name
                val desc = menuItem.description?.trim().orEmpty()
                tvDescription.text = desc
                tvDescription.visibility = if (desc.isEmpty()) View.GONE else View.VISIBLE
                tvPrice.text = "TZS ${String.format("%,.0f", menuItem.price)}"
                tvCategory.text = menuItem.category?.replaceFirstChar { it.uppercase() } ?: "Uncategorized"
                tvPrepTime.text = "${menuItem.preparationTime} min"

                // Load image
                if (!menuItem.imageUrl.isNullOrEmpty()) {
                    Glide.with(binding.root.context)
                        .load(menuItem.imageUrl)
                        .placeholder(R.drawable.ic_placeholder_food)
                        .into(ivImage)
                } else {
                    ivImage.setImageResource(R.drawable.ic_placeholder_food)
                }

                // Availability indicator
                root.alpha = if (menuItem.isAvailable) 1.0f else 0.5f
                tvAvailable.text = if (menuItem.isAvailable) "Available" else "Unavailable"

                root.setOnClickListener {
                    if (menuItem.isAvailable) {
                        onItemClick(menuItem)
                    }
                }
            }
        }
    }

    private class MenuDiffCallback : DiffUtil.ItemCallback<MenuItemEntity>() {
        override fun areItemsTheSame(oldItem: MenuItemEntity, newItem: MenuItemEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MenuItemEntity, newItem: MenuItemEntity): Boolean {
            return oldItem == newItem
        }
    }
}

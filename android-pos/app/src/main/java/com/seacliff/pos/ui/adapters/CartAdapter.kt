package com.seacliff.pos.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seacliff.pos.databinding.ItemCartBinding
import com.seacliff.pos.ui.viewmodel.CartItem

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onNotesChanged: (CartItem, String?) -> Unit,
    private val onRemoveClicked: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                tvName.text = cartItem.menuItem.name
                tvPrice.text = "TZS ${String.format("%,.0f", cartItem.menuItem.price)}"
                tvSubtotal.text = "TZS ${String.format("%,.0f", cartItem.subtotal)}"
                tvQuantity.text = cartItem.quantity.toString()
                etNotes.setText(cartItem.notes ?: "")

                // Quantity controls
                btnDecrease.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        onQuantityChanged(cartItem, cartItem.quantity - 1)
                    }
                }

                btnIncrease.setOnClickListener {
                    onQuantityChanged(cartItem, cartItem.quantity + 1)
                }

                // Notes
                etNotes.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val notes = etNotes.text.toString().trim()
                        onNotesChanged(cartItem, if (notes.isEmpty()) null else notes)
                    }
                }

                // Remove button
                btnRemove.setOnClickListener {
                    onRemoveClicked(cartItem)
                }
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.menuItem.id == newItem.menuItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}

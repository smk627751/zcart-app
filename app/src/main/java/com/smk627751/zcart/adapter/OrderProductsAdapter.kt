package com.smk627751.zcart.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.smk627751.zcart.R
import com.smk627751.zcart.dto.Product

class OrderProductsAdapter(private val orders: Array<Product>, val updateTotalPrice : (totalPrice : Double) -> Unit) : RecyclerView.Adapter<OrderProductsAdapter.ViewHolder>() {
    val quantities = mutableMapOf<String, Int>()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ShapeableImageView = itemView.findViewById(R.id.product_image)
        val name : TextView = itemView.findViewById(R.id.product_name)
        val price : TextView = itemView.findViewById(R.id.product_price)
        val increaseQuantity : Button = itemView.findViewById(R.id.increase_quantity)
        val decreaseQuantity : Button = itemView.findViewById(R.id.decrease_quantity)
        val quantity : TextView = itemView.findViewById(R.id.quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_order_card, parent, false)
        var sum = 0
        orders.forEach {
            quantities[it.id] = 1
            sum += it.price
        }
        updateTotalPrice(sum.toDouble())
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = orders[position]
        holder.name.text = product.name
        holder.price.text = product.price.toString()
        Glide.with(holder.itemView)
            .load(product.image)
            .into(holder.image)
        holder.quantity.setText(quantities[product.id].toString())
        holder.increaseQuantity.setOnClickListener {
            var q = holder.quantity.text.toString().toInt()
            if (q == 10)
            {
                return@setOnClickListener
            }
            q++
            holder.quantity.text = q.toString()
            quantities[product.id] = q
            updateTotalPrice(quantities.values.sumOf { it * product.price }.toDouble())
        }
        holder.decreaseQuantity.setOnClickListener {
            var q = holder.quantity.text.toString().toInt()
            if (q > 1) {
                q--
            }
            holder.quantity.text = q.toString()
            quantities[product.id] = q
            updateTotalPrice(quantities.values.sumOf { it * product.price }.toDouble())
        }
        holder.quantity.addTextChangedListener { text ->
            val quantity = text.toString().toIntOrNull()
            if (quantity!= null && quantity > 10)
            {
                holder.quantity.text = 10.toString()
                return@addTextChangedListener
            }
            if (quantity != null && quantity > 0) {
                quantities[product.id] = quantity
                updateTotalPrice(quantities.values.sumOf { it * product.price }.toDouble())
            }
            else {
                holder.quantity.text = 1.toString()
                quantities[product.id] = 1
                updateTotalPrice(quantities.values.sumOf { it * product.price }.toDouble())
            }
        }
    }
    fun getQuantities(): Int {
        return quantities.values.sum()
    }
}
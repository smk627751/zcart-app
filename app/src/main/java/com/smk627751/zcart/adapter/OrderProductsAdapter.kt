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
import com.smk627751.zcart.Utility
import com.smk627751.zcart.dto.Product

class OrderProductsAdapter(private val orders: Array<Product>, val updateTotalPrice : (totalPrice : Double) -> Unit) : RecyclerView.Adapter<OrderProductsAdapter.ViewHolder>() {
    private val quantities = mutableMapOf<Product, MutableMap<Double,Int>>()
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
        var sum = 0.0
        orders.forEach {
            quantities[it] = mutableMapOf(it.price to 1)
            sum += it.price
        }
        updateTotalPrice(sum)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = orders[position]
        holder.name.text = product.name
        holder.price.text = Utility.formatNumberIndianSystem(product.price)
        Glide.with(holder.itemView)
            .load(product.image)
            .into(holder.image)
        holder.quantity.setText(quantities[product]?.get(product.price).toString())
        holder.increaseQuantity.setOnClickListener {
            var q = holder.quantity.text.toString().toInt()
            if (q == 10)
            {
                Utility.makeToast(holder.itemView.context, "Maximum quantity reached")
                return@setOnClickListener
            }
            q++
            holder.quantity.text = q.toString()
            quantities[product]?.set(product.price, q)
            updateTotalPrice(getTotalPrice())
        }
        holder.decreaseQuantity.setOnClickListener {
            var q = holder.quantity.text.toString().toInt()
            if (q > 1) {
                q--
            }
            holder.quantity.text = q.toString()
            quantities[product]?.set(product.price, q)
            updateTotalPrice(getTotalPrice())
        }
    }
    private fun getTotalPrice(): Double {
        return quantities.values.sumOf { it.map { it.key * it.value }.sum() }
    }
    fun getQuantities(): Int {
        return quantities.values.sumOf { it.values.sum() }
    }
    fun getProducts(): MutableMap<Product, MutableMap<Double, Int>> {
        return quantities
    }
}
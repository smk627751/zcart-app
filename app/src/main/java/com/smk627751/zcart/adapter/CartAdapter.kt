package com.smk627751.zcart.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.smk627751.zcart.activity.DetailViewActivity
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.dto.Product

class CartAdapter(val cartItems: List<Product>,val isCart: Boolean = true,val deleteCallback: (Product) -> Unit) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    constructor(cartItems: List<Product>) : this(cartItems,false, {})
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val logo = view.findViewById<ShapeableImageView>(R.id.logo)
        val productName = view.findViewById<TextView>(R.id.product_name)
        val price = view.findViewById<TextView>(R.id.product_price)
        val closeButton = view.findViewById<Button>(R.id.close_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartItems[position]
        Glide.with(holder.itemView)
            .load(cartItem.image)
            .into(holder.logo)
        holder.productName.text = cartItem.name
        holder.price.text = "₹${cartItem.price}"
        holder.itemView.setOnClickListener{
            Intent(holder.itemView.context, DetailViewActivity::class.java)
                .also {
                    it.putExtra("product", cartItem)
                    holder.itemView.context.startActivity(it)
                }
        }
        if (isCart)
        {
            holder.closeButton.setOnClickListener{
                Utility.showAlertDialog(holder.itemView.context, "Are you sure you want to delete this item?") {
                    deleteCallback(cartItem)
                }
            }
        }
        else
        {
            holder.closeButton.visibility = View.GONE
        }
    }
}
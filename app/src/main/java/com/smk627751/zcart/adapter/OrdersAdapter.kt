package com.smk627751.zcart.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.smk627751.zcart.R
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.activity.OrderDetailsViewActivity
import com.smk627751.zcart.Utility

class OrdersAdapter(options: FirestoreRecyclerOptions<Order>) : FirestoreRecyclerAdapter<Order, OrdersAdapter.ViewHolder>(options){
    var callBack : (msg : String) -> Unit = {}
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderImages = itemView.findViewById<LinearLayout>(R.id.order_images)
        val orderImage = itemView.findViewById<ShapeableImageView>(R.id.order_image)
//        val orderId = itemView.findViewById<TextView>(R.id.order_id)
        val date = itemView.findViewById<TextView>(R.id.date)
//        val quantity = itemView.findViewById<TextView>(R.id.quantity)
        val totalPrice = itemView.findViewById<TextView>(R.id.total_price)
        val orderStatus = itemView.findViewById<TextView>(R.id.order_status)
//        val viewDetails = itemView.findViewById<TextView>(R.id.view_details)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Order) {
//        holder.orderId.text = model.id
        holder.orderImages.removeAllViews()
        holder.date.text = Utility.formatTime(model.timestamp)
//        holder.quantity.text = model.quantity.toString()
        holder.totalPrice.text = Utility.formatNumberIndianSystem(model.totalPrice)
        holder.orderStatus.text = model.status
        holder.itemView.setOnClickListener {
            Intent(holder.itemView.context, OrderDetailsViewActivity::class.java).also {
                it.putExtra("order", model)
                startActivity(holder.itemView.context, it, null)
            }
        }
        Repository.getProducts(model.products){
            if (it.isNotEmpty())
            {
//                Glide.with(holder.itemView.context)
//                    .load(it[0].image)
//                    .into(holder.orderImage)
//                if (it.size > 1)
//                {
                    it.forEach {
                        val orderImageView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.order_image,holder.orderImages,false)
                        holder.orderImages.addView(orderImageView)
                        Glide.with(holder.itemView.context)
                            .load(it.image)
                            .into(orderImageView as ShapeableImageView)
                    }
//                    val first = holder.itemView.findViewById<ShapeableImageView>(R.id.order_image)
//                    first.isVisible = true
//                    Glide.with(holder.itemView.context)
//                        .load(it[0].image)
//                        .into(first)
//                    Glide.with(holder.itemView.context)
//                        .load(it[1].image)
//                        .into(holder.orderImage)
//                }
            }
        }
    }

    private fun notifyNewDataInserted() {
        callBack("New data inserted")
    }
}
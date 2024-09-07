package com.smk627751.zcart.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smk627751.zcart.R
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.Utility
import com.smk627751.zcart.activity.DetailViewActivity
import com.smk627751.zcart.activity.OrderDetailsViewActivity
import com.smk627751.zcart.dto.Notification
import com.smk627751.zcart.dto.OrderNotification
import com.smk627751.zcart.dto.ReviewNotification

class NotificationAdapter(private val notifications : MutableList<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val text = itemView.findViewById<TextView>(R.id.text)
        val image = itemView.findViewById<ImageView>(R.id.image)
        val time = itemView.findViewById<TextView>(R.id.time)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.text.text = notification.text
        holder.time.text = Utility.formatTime(notification.timestamp)
        if (notification.isRead)
        {
            holder.itemView.alpha = 0.5f
        }
        else holder.itemView.alpha = 1.0f
        when(notification)
        {
            is OrderNotification -> {
                holder.image.setImageResource(R.drawable.outline_shopping_cart_24)
                holder.itemView.setOnClickListener{
                    notification.isRead = true
                    Repository.updateNotification(notification)
                    Intent(holder.itemView.context, OrderDetailsViewActivity::class.java)
                        .also {
                            it.putExtra("order_id", notification.orderId)
                            holder.itemView.context.startActivity(it)
                        }
                }
            }
            is ReviewNotification -> {
                holder.image.setImageResource(R.drawable.reviews_24px)
                holder.itemView.setOnClickListener{
                    notification.isRead = true
                    Repository.updateNotification(notification)
                    Intent(holder.itemView.context, DetailViewActivity::class.java)
                        .also {
                            it.putExtra("product_id", notification.productId)
                            holder.itemView.context.startActivity(it)
                        }
                }
            }
            else -> Log.e("uuid","Unknown notification type")
        }
    }
    fun updateData(newNotifications : List<Notification>)
    {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
}
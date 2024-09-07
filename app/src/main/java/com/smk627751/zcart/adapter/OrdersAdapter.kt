package com.smk627751.zcart.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.R
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.activity.OrderDetailsViewActivity
import com.smk627751.zcart.Utility

class OrdersAdapter(options: FirestoreRecyclerOptions<Order>) : FirestoreRecyclerAdapter<Order, OrdersAdapter.ViewHolder>(options){
    var callBack : (msg : String) -> Unit = {}
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId = itemView.findViewById<TextView>(R.id.order_id)
        val date = itemView.findViewById<TextView>(R.id.date)
        val quantity = itemView.findViewById<TextView>(R.id.quantity)
        val totalPrice = itemView.findViewById<TextView>(R.id.total_price)
        val orderStatus = itemView.findViewById<TextView>(R.id.order_status)
        val viewDetails = itemView.findViewById<TextView>(R.id.view_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Order) {
        holder.orderId.text = model.id
        holder.date.text = Utility.formatTime(model.timestamp)
        holder.quantity.text = model.quantity.toString()
        holder.totalPrice.text = model.totalPrice.toString()
        holder.orderStatus.text = model.status
        holder.viewDetails.setOnClickListener {
            Intent(holder.itemView.context, OrderDetailsViewActivity::class.java).also {
                it.putExtra("order", model)
                startActivity(holder.itemView.context, it, null)
            }
        }
    }

//    override fun onChildChanged(
//        type: ChangeEventType,
//        snapshot: DocumentSnapshot,
//        newIndex: Int,
//        oldIndex: Int
//    ) {
//        super.onChildChanged(type, snapshot, newIndex, oldIndex)
//        if (type == ChangeEventType.ADDED)
//        {
//            notifyNewDataInserted()
//        }
//    }

    private fun notifyNewDataInserted() {
        callBack("New data inserted")
    }
}
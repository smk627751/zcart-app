package com.smk627751.zcart.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.activity.DetailViewActivity
import com.smk627751.zcart.dto.Product

class ProductViewAdapter(options: FirestoreRecyclerOptions<Product>) : FirestoreRecyclerAdapter<Product, ProductViewAdapter.ViewHolder>(options) {
    val currentList = snapshots
    constructor(options: FirestoreRecyclerOptions<Product>, width:Int):this(options)
    {
        this.width = width
    }
    var width : Int = LayoutParams.MATCH_PARENT
    private val products = arrayListOf<String>()
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.product_name)
        val price = view.findViewById<TextView>(R.id.product_price)
        val image = view.findViewById<ImageView>(R.id.product_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Product) {
        ViewCompat.setTransitionName(holder.itemView, "image_transition")
        products.add(model.name)
        holder.name.text = model.name
        holder.price.text = Utility.formatNumberIndianSystem(model.price)
        Glide.with(holder.itemView)
            .load(model.image)
            .placeholder(R.drawable.baseline_image_24)
            .into(holder.image)
        holder.itemView.setOnClickListener{view ->
            val intent = Intent(holder.itemView.context, DetailViewActivity::class.java).apply {
                    putExtra("product", model)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                view.context as Activity,
                holder.image,
                "image_transition"
            )
            (view.context as Activity).startActivityForResult(intent, 1,options.toBundle())
        }
        holder.itemView.layoutParams.width = width
    }
    fun updateData(options: FirestoreRecyclerOptions<Product>) {
        this.updateOptions(options)
        notifyDataSetChanged()
    }
}
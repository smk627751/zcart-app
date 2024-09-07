package com.smk627751.zcart.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smk627751.zcart.R
import com.smk627751.zcart.dto.Review
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.Utility
import com.smk627751.zcart.viewmodel.DetailViewModel

class ReviewsAdapter(private val reviews: Map<String,Review>, private val viewModel: DetailViewModel) : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.logo)
        val username: TextView = view.findViewById(R.id.username)
        val delete: Button = view.findViewById(R.id.action_delete)
        val date: TextView = view.findViewById(R.id.date)
        val ratings: LinearLayout = view.findViewById(R.id.ratings)
        val reviewText: TextView = view.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews.values.elementAt(position)
        viewModel.getUser(review.userId){
            if (it != null) {
                holder.username.text = if(it.name == Repository.user?.name) "You" else it.name
                if (review.isEdited)
                {
                    holder.username.text = "${holder.username.text} (Edited)"
                }
                Glide.with(holder.itemView)
                    .load(it.image)
                    .transform(CircleCrop())
                    .error(R.drawable.baseline_person_24)
                    .into(holder.logo)
                holder.date.text = Utility.formatTime(review.timestamp)
            }
        }
        holder.ratings.removeAllViews()
        val fullStar = review.rating / 1
        val halfStar = review.rating % 1
        for(i in 1..fullStar.toInt())
        {
            holder.ratings.addView(ImageView(holder.itemView.context).apply {
                setImageResource(R.drawable.baseline_star_24)
            })
        }
        if(halfStar > 0)
        {
            holder.ratings.addView(ImageView(holder.itemView.context).apply {
                setImageResource(R.drawable.baseline_star_half_24)
            })
        }
        holder.reviewText.text = review.text
        if (viewModel.isCurrentUserOrVendor(review.userId))
        {
            holder.delete.visibility = View.VISIBLE
            holder.delete.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Are you sure want to delete?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteReview(review){
                            Utility.makeToast(holder.itemView.context,"Review deleted successfully")
                            notifyItemRemoved(position)
                        }
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .create()
                    .show()
            }
        }
        else
        {
            holder.delete.visibility = View.GONE
            holder.itemView.setOnClickListener {}
        }
    }
}
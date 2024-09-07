package com.smk627751.zcart.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smk627751.zcart.R

class CategoryAdapter(private val categories: List<String>, private val listener: OnCategorySelectedListener) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private val selectedCategories = mutableListOf<String>()
    fun interface OnCategorySelectedListener {
        fun onCategorySelected(selectedCategories: List<String>)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.category_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filter_chip, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val scale = holder.itemView.context.resources.displayMetrics.density
        holder.textView.text = category
        if (!selectedCategories.contains(category)) {
            holder.itemView.setBackgroundResource(R.drawable.unselected_item)
            holder.itemView.layoutParams.width = (80 * scale + 0.5f).toInt()
        }
        else
        {
            holder.itemView.setBackgroundResource(R.drawable.selected_item)
            holder.itemView.layoutParams.width = (140 * scale + 0.5f).toInt()
        }
        holder.itemView.setOnClickListener {
            if (selectedCategories.contains(category)) {
                selectedCategories.remove(category)
                listener.onCategorySelected(selectedCategories)
                holder.itemView.setBackgroundResource(R.drawable.unselected_item)
                holder.itemView.layoutParams.width = (80 * scale + 0.5f).toInt()
            } else {
                selectedCategories.add(category)
                listener.onCategorySelected(selectedCategories)
                holder.itemView.setBackgroundResource(R.drawable.selected_item)
                holder.itemView.layoutParams.width = (140 * scale + 0.5f).toInt()
            }
        }
    }
    fun getSelectedCategories(): List<String> {
        return selectedCategories
    }
}
package com.smk627751.zcart.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smk627751.zcart.R

class SearchViewAdapter(val callback: (Map.Entry<String, String>) -> Unit) : RecyclerView.Adapter<SearchViewAdapter.ViewHolder>() {
    private var searchList: Map<String, String> = emptyMap()
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val text = view.findViewById<TextView>(R.id.search_text)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item_layout, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
       return searchList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = searchList.entries.elementAt(position)
        holder.text.text = item.key
        holder.view.setOnClickListener {
            callback(item)
        }
    }
    fun updateList(newList: Map<String, String>) {
        searchList = newList
        notifyDataSetChanged()
    }
}
package com.smk627751.zcart.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.smk627751.zcart.R
import com.smk627751.zcart.adapter.NotificationAdapter
import com.smk627751.zcart.viewmodel.NotificationViewModel

class NotificationViewFragment : Fragment() {
    lateinit var viewModel: NotificationViewModel
    lateinit var buttonGroup : MaterialButtonToggleGroup
    lateinit var recyclerView : RecyclerView
    lateinit var adapter : NotificationAdapter
    var setBadge: ((Int) -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_notification_view, container, false)
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        // Initialize views
        buttonGroup = view.findViewById(R.id.button_group)
        recyclerView = view.findViewById(R.id.recycler_view)
        // Set up RecyclerView
        buttonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.all -> viewModel.getNotifications("all")
                    R.id.orders -> viewModel.getNotifications("orders")
                    R.id.reviews -> viewModel.getNotifications("reviews")
                }
            }
        }
        adapter = NotificationAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        // Observe notifications
        viewModel.notifications.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            setBadge?.invoke(it.filter { !it.isRead }.size)
        }
        viewModel.getNotifications("all")
        return view
    }
}
package com.smk627751.zcart.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButtonToggleGroup
import com.smk627751.zcart.R
import com.smk627751.zcart.adapter.NotificationAdapter
import com.smk627751.zcart.dto.Notification
import com.smk627751.zcart.viewmodel.NotificationViewModel

class NotificationViewFragment : Fragment() {
    lateinit var viewModel: NotificationViewModel
    lateinit var shimmerFrameLayout : ShimmerFrameLayout
    lateinit var buttonGroup : MaterialButtonToggleGroup
    lateinit var noNotificationFoundView : View
    lateinit var recyclerView : RecyclerView
    lateinit var adapter : NotificationAdapter
    var shimmerRunnable : Runnable? = null
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
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout)
        buttonGroup = view.findViewById(R.id.button_group)
        noNotificationFoundView = view.findViewById(R.id.no_notification_found_view)
        recyclerView = view.findViewById(R.id.recycler_view)
        // Set up shimmer layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            shimmerFrameLayout.startShimmerAnimation()
            shimmerFrameLayout.visibility = View.VISIBLE
        }
        noNotificationFoundView.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
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
        updateVisibility(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        // Observe notifications
        viewModel.notifications.observe(viewLifecycleOwner) {
            updateVisibility(it)
            adapter.updateData(it)
            setBadge?.invoke(it.filter { !it.isRead }.size)
        }
        viewModel.getNotifications("all")
        return view
    }
    private fun updateVisibility(notifications : List<Notification>)
    {
        if (notifications.isNotEmpty())
        {
            showShimmer {
                recyclerView.visibility = View.VISIBLE
                noNotificationFoundView.visibility = View.GONE
            }
        }
        else
        {
            recyclerView.visibility = View.GONE
            noNotificationFoundView.visibility = View.VISIBLE
            shimmerFrameLayout.stopShimmerAnimation()
            shimmerFrameLayout.visibility = View.GONE
        }
    }
    private fun showShimmer(callback: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
        {
            noNotificationFoundView.visibility = View.GONE
            callback()
            return
        }
        shimmerFrameLayout.removeCallbacks(shimmerRunnable)
        noNotificationFoundView.visibility = View.GONE
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmerAnimation()
        shimmerRunnable = Runnable {
            callback()
            shimmerFrameLayout.stopShimmerAnimation()
            shimmerFrameLayout.visibility = View.GONE
        }
        shimmerFrameLayout.postDelayed(shimmerRunnable, 500)
    }

    fun scrollToTop() {
        recyclerView.smoothScrollToPosition(0)
    }
}
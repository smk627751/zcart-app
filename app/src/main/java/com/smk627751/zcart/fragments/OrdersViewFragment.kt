package com.smk627751.zcart.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.smk627751.zcart.R
import com.smk627751.zcart.adapter.OrdersAdapter
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.viewmodel.OrdersViewModel

class OrdersViewFragment : Fragment() {
    lateinit var viewModel: OrdersViewModel
    val orderStatus = arrayOf("Order placed", "Order shipped", "Out for delivery", "Order delivered","Order cancelled")
    lateinit var toolbar: MaterialToolbar
    lateinit var category: MaterialToolbar
    lateinit var shimmerFrameLayout: ShimmerFrameLayout
    lateinit var categoryView : ChipGroup
    lateinit var ordersView : RecyclerView
    lateinit var noOrdersView : LinearLayout
    var adapter: OrdersAdapter? = null
    var shimmerRunnable : Runnable? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders_view, container, false)
        viewModel = ViewModelProvider(this)[OrdersViewModel::class.java]

        toolbar = view.findViewById(R.id.toolbar)
        category = view.findViewById(R.id.filter_toolbar)
        categoryView = view.findViewById(R.id.category_view)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout)
        ordersView = view.findViewById(R.id.orders_view)
        noOrdersView = view.findViewById(R.id.no_orders_found_view)
        noOrdersView.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        viewModel.isLoading.observe(viewLifecycleOwner){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@observe
            if (it)
            {
                shimmerFrameLayout.visibility = View.VISIBLE
                shimmerFrameLayout.startShimmerAnimation()
            }
            else
            {
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
            }
        }
        viewModel.isVendor.observe(viewLifecycleOwner) {
            if (!it)
            {
                toolbar.menu.clear()
            }
            else
            {
                toolbar.inflateMenu(R.menu.search)
                val searchView = toolbar.menu.findItem(R.id.search).actionView as SearchView
                searchView.queryHint = "Enter order ID"
                searchView.setBackgroundResource(R.drawable.edit_text_bg)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query.isNullOrEmpty()) viewModel.getOrders()
                        else viewModel.getOrders(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText.isNullOrEmpty()) viewModel.getOrders()
                        else viewModel.getOrders(newText)
                        return true
                    }
                })
            }
        }
        viewModel.itemCount.observe(viewLifecycleOwner){itemCount ->
            Log.d("TAG", "onCreateView: $itemCount")
            if (itemCount == 0) {
                noOrdersView.visibility = View.VISIBLE
                ordersView.visibility = View.GONE
            } else if (itemCount > 0) {
                ordersView.visibility = View.VISIBLE
                noOrdersView.visibility = View.GONE
            }
        }
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.filter)
            {
                category.isVisible = !category.isVisible
            }
            true
        }
        var index = 0
        orderStatus.forEach { status ->
            val view = layoutInflater.inflate(R.layout.filter_chip, categoryView, false) as Chip
            view.id = index++
            view.text = status
            view.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.getOrders(status)
                else viewModel.getOrders()
            }
            categoryView.addView(view)
        }
        viewModel.options.observe(viewLifecycleOwner) { options ->
            setAdapter(options)
        }
        return view
    }
    override fun onStart() {
        super.onStart()
        adapter?.notifyDataSetChanged()
        adapter?.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
    private fun setAdapter(options: FirestoreRecyclerOptions<Order>?)
    {
        adapter = options?.let { OrdersAdapter(it) }
        ordersView.adapter = adapter
        adapter?.startListening()
        ordersView.layoutManager = LinearLayoutManager(requireContext())
    }

    fun scrollToTop() {
        ordersView.smoothScrollToPosition(0)
    }
}
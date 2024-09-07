package com.smk627751.zcart.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.smk627751.zcart.R
import com.smk627751.zcart.adapter.OrdersAdapter
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.viewmodel.OrdersViewModel

class OrdersViewFragment : Fragment() {
    lateinit var viewModel: OrdersViewModel
    lateinit var toolbar: MaterialToolbar
    lateinit var searchView : SearchView
    lateinit var ordersView : RecyclerView
    lateinit var noOrdersView : LinearLayout
    var adapter: OrdersAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders_view, container, false)
        viewModel = ViewModelProvider(this)[OrdersViewModel::class.java]

        toolbar = view.findViewById(R.id.toolbar)
        ordersView = view.findViewById(R.id.orders_view)
        noOrdersView = view.findViewById(R.id.no_orders_found_view)
        searchView = toolbar.menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
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
    private fun setAdapter(options : FirestoreRecyclerOptions<Order>)
    {
        adapter = OrdersAdapter(options)
        ordersView.adapter = adapter
        adapter?.startListening()
        ordersView.layoutManager = LinearLayoutManager(requireContext())
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                updateViewVisibility()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                updateViewVisibility()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                updateViewVisibility()
            }
        })
        updateViewVisibility()
    }
    private fun updateViewVisibility() {
        if (adapter?.itemCount == 0) {
            noOrdersView.visibility = View.VISIBLE
            ordersView.visibility = View.GONE
        } else {
            noOrdersView.visibility = View.GONE
            ordersView.visibility = View.VISIBLE
        }
    }
}
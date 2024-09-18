package com.smk627751.zcart.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.smk627751.zcart.activity.PlaceOrderActivity
import com.smk627751.zcart.R
import com.smk627751.zcart.adapter.CartAdapter
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.viewmodel.CartViewModel

class CartViewFragment : Fragment() {
    lateinit var viewModel: CartViewModel
    lateinit var toolbar: MaterialToolbar
    lateinit var shimmerFrameLayout: ShimmerFrameLayout
    lateinit var fragmentContainer : FrameLayout
    lateinit var cartItemsView : RecyclerView
    lateinit var noProductView : LinearLayout
//    lateinit var myOrders : Button
    lateinit var buyNowButton : FloatingActionButton
    var adapter: CartAdapter = CartAdapter(listOf()){
        cartItem -> viewModel.removeFromCart(cartItem)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.cart_view, container, false)
        viewModel = ViewModelProvider(this)[CartViewModel::class.java]
        toolbar = view.findViewById(R.id.toolbar)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout)
        fragmentContainer = view.findViewById(R.id.fragment_container)
        cartItemsView = view.findViewById(R.id.cart_items_view)
        noProductView = view.findViewById(R.id.no_product_found_view)
//        myOrders = view.findViewById(R.id.my_orders)
        cartItemsView.layoutManager = LinearLayoutManager(context)
        buyNowButton = view.findViewById(R.id.buy_now_button)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmerAnimation()
        }
//        myOrders.visibility = View.VISIBLE
//        toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.my_orders -> {
//                    fragmentContainer.visibility = View.VISIBLE
//                    childFragmentManager.beginTransaction().apply {
//                        replace(R.id.fragment_container, OrdersViewFragment())
//                        addToBackStack(null)
//                        commit()
//                    }
//                    true
//                }
//
//                else -> false
//            }
//        }
        viewModel.cartItems.observe(viewLifecycleOwner) {cartItems ->
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmerAnimation()
            setAdapter(cartItems)
            buyNowButton.isVisible = cartItems.isNotEmpty()
        }

        buyNowButton.setOnClickListener {
            Intent(context, PlaceOrderActivity::class.java)
                .also {
                    it.putExtra("products", adapter.cartItems.toTypedArray())
                    startActivity(it)
                }
        }
        return view
    }
    private fun setAdapter(cartItems : List<Product>)
    {
        adapter = CartAdapter(cartItems){ cartItem ->
            viewModel.removeFromCart(cartItem)
        }
        cartItemsView.adapter = adapter
        updateViewVisibility()
    }
    private fun updateViewVisibility() {
        if (adapter.itemCount == 0) {
            noProductView.visibility = View.VISIBLE
            cartItemsView.visibility = View.GONE
        } else {
            noProductView.visibility = View.GONE
            cartItemsView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCartItems()
    }

    fun scrollToTop() {
        cartItemsView.smoothScrollToPosition(0)
    }
}

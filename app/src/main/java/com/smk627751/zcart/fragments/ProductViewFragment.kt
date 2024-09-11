package com.smk627751.zcart.fragments

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ViewSwitcher
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.smk627751.zcart.R
import com.smk627751.zcart.Repository.Repository.listenProducts
import com.smk627751.zcart.adapter.ProductViewAdapter
import com.smk627751.zcart.adapter.SearchViewAdapter
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.viewmodel.ProductViewModel


class ProductViewFragment : Fragment() {

    lateinit var viewModel: ProductViewModel
    lateinit var parentLayout: ViewGroup
    lateinit var categoryList : View
    lateinit var categoryView : ChipGroup
    lateinit var searchBar: SearchBar
    lateinit var searchView: SearchView
    lateinit var searchList : RecyclerView
    lateinit var noProductView : LinearLayout
    private var shimmerRunnable: Runnable? = null
    lateinit var shimmerFrameLayout: ShimmerFrameLayout
    var searchViewAdapter: SearchViewAdapter? = null
    lateinit var recyclerView: RecyclerView
    var adapter: ProductViewAdapter? = null
    var isLoadingMore = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.product_view, container, false)
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        viewModel.options.observe(viewLifecycleOwner) { options ->
            setAdapter(null)
            showShimmer {
                setAdapter(options)
                setSearchViewAdapter()
                updateViewVisibility()
            }
        }
        viewModel.filterList.observe(viewLifecycleOwner) { filteredList ->
            searchViewAdapter?.updateList(filteredList)
        }
        parentLayout = view.findViewById(R.id.parent_layout)
        categoryList = view.findViewById(R.id.category_list)
        categoryView = view.findViewById(R.id.category_view)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout)
        searchBar = view.findViewById(R.id.search_bar)
        searchView = view.findViewById(R.id.search_view)
        searchList = view.findViewById(R.id.search_recycler_view)
        noProductView = view.findViewById(R.id.no_product_found_view)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = if (resources.configuration.orientation == ORIENTATION_PORTRAIT){
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
        else StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = DefaultItemAnimator()

        noProductView.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        shimmerFrameLayout.startShimmerAnimation()
        setupCategoryList()
        setupSearchView()

        return view
    }

    private fun setupCategoryList() {
        val categories = viewModel.getCategories()
        categories.forEach { category ->
            val categoryView = layoutInflater.inflate(R.layout.filter_chip, categoryView, false) as Chip
            categoryView.findViewById<Chip>(R.id.category_text).text = category
            categoryView.setOnClickListener {
                searchBar.setText("")
                if (!viewModel.selectedItemsList.contains(category)) {
                    viewModel.selectedItemsList.add(category)
                } else {
                    viewModel.selectedItemsList.remove(category)
                }
                viewModel.getProductsByCategory(viewModel.selectedItemsList.toString())
            }
            this.categoryView.addView(categoryView)
        }
    }

    private fun setupSearchView() {
        searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_menu -> {
                    categoryList.isVisible = !categoryList.isVisible
                    true
                }

                else -> false
            }
        }
        searchView.setupWithSearchBar(searchBar)
        searchList.layoutManager = LinearLayoutManager(requireContext())
        searchView.editText.addTextChangedListener { text ->
            viewModel.setFilterList(text.toString())
        }
        searchView.editText.setOnEditorActionListener { v, actionId, _ ->
            val query = v.text.toString()
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                query.let { viewModel.getProductsByName(it) }
                searchBar.setText(query)
                searchView.hide()
                true
            }
            else {
                false
            }
        }
    }

    private fun setAdapter(options: FirestoreRecyclerOptions<Product>?) {
        adapter = options?.let { ProductViewAdapter(it) }
        recyclerView.adapter = adapter
        adapter?.startListening()
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
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
//                val visibleItemCount = layoutManager.childCount
//                val totalItemCount = layoutManager.itemCount
//                val firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null)
//                val firstVisibleItemPosition = firstVisibleItemPositions.minOrNull() ?: 0
//                // Check if end of the list is reached
//                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
//                    && firstVisibleItemPosition >= 0) {
//                    // Load more data
////                    shimmerFrameLayout.visibility = View.VISIBLE
////                    shimmerFrameLayout.startShimmerAnimation()
//                    loadMoreProducts()
//                }
//            }
//        })
    }
    fun loadMoreProducts() {
        isLoadingMore = true
        // Get current snapshots
        val currentList = adapter?.currentList
        if (currentList?.isEmpty() == true) return

        val lastVisibleProduct = currentList?.getSnapshot(currentList.size - 1)

        // Fetch more products
        val newOptions = listenProducts(
            searchQuery = "",  // Modify based on your need
            type = "all",  // Modify based on your need
            limit = 5,
            lastVisibleProduct = lastVisibleProduct
        )
        recyclerView.post {
            adapter?.updateData(newOptions)
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmerAnimation()
        }
        isLoadingMore = false
    }
    private fun updateViewVisibility() {
        // Log to check the current item count
        val itemCount = adapter?.itemCount ?: 0
        val isRecyclerViewEmpty = recyclerView.childCount == 0

        Log.d("ProductViewFragment", "Item Count: $itemCount, RecyclerView Child Count: $isRecyclerViewEmpty")

        val viewSwitcher = view?.findViewById<ViewSwitcher>(R.id.view_switcher)

        // Switch the view based on item count
        if (itemCount == 0) {
            Log.d("ProductViewFragment", "Showing no product found view")
            view?.findViewById<View>(R.id.no_product_found_view)?.visibility = View.VISIBLE
            viewSwitcher?.findViewById<View>(R.id.recycler_view)?.visibility = View.GONE
        } else if (itemCount > 0) {
            Log.d("ProductViewFragment", "Showing recycler view")
            viewSwitcher?.findViewById<View>(R.id.recycler_view)?.visibility = View.VISIBLE
            view?.findViewById<View>(R.id.no_product_found_view)?.visibility = View.GONE
        }
    }
    private fun showShimmer(callback: () -> Unit) {
        shimmerFrameLayout.removeCallbacks(shimmerRunnable)
        noProductView.visibility = View.GONE
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmerAnimation()
        shimmerRunnable = Runnable {
                callback()
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
        }
        shimmerFrameLayout.postDelayed(shimmerRunnable, 1000)
    }
    private fun setSearchViewAdapter()
    {
        searchViewAdapter = SearchViewAdapter {
            when(it.value)
            {
                "category" -> viewModel.getProductsByCategory(it.key)
                "name" -> viewModel.getProductsByName(it.key)
            }
            searchBar.setText(it.key)
            searchView.hide()
        }
        searchList.adapter = searchViewAdapter
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
}

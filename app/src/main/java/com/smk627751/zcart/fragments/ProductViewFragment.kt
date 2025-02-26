package com.smk627751.zcart.fragments

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.Toolbar
import android.widget.ViewSwitcher
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.R.attr.duration
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.smk627751.zcart.R
import com.smk627751.zcart.adapter.ProductViewAdapter
import com.smk627751.zcart.adapter.SearchViewAdapter
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.viewmodel.ProductViewModel


class ProductViewFragment : Fragment() {

    lateinit var viewModel: ProductViewModel
    lateinit var parentLayout: ViewGroup
    lateinit var categoryList : MaterialToolbar
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.product_view, container, false)
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@observe
            if (it) {
                shimmerFrameLayout.visibility = View.VISIBLE
                shimmerFrameLayout.startShimmerAnimation()
            } else {
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
            }
        }
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
            GridLayoutManager(context,2)
        }
        else GridLayoutManager(context,4)
        recyclerView.itemAnimator = DefaultItemAnimator()

        noProductView.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        shimmerFrameLayout.startShimmerAnimation()
        setupCategoryList()
        setupSearchView()

        return view
    }

    private fun setupCategoryList() {
        this.categoryView.removeAllViews()
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
                    val transitionSet = TransitionSet().apply {
                        addTransition(Fade())
                        addTransition(ChangeBounds())
                        duration = 200
                    }
                    TransitionManager.beginDelayedTransition(categoryList, transitionSet).also {
                        categoryList.isVisible = !categoryList.isVisible
                    }
                    true
                }

                else -> false
            }
        }
        searchList.layoutManager = LinearLayoutManager(requireContext())
        searchView.editText.addTextChangedListener { text ->
            viewModel.setFilterList(text.toString())
        }
        searchView.editText.setOnEditorActionListener { v, actionId, _ ->
            val query = v.text.toString()
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                query.let { viewModel.getProductsByName(it) }
                searchBar.setText(query)
                setupCategoryList()
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
            Handler().postDelayed({
                view?.findViewById<View>(R.id.no_product_found_view)?.visibility = View.VISIBLE
            },1500)
            viewSwitcher?.findViewById<View>(R.id.recycler_view)?.visibility = View.GONE
        } else if (itemCount > 0) {
            Log.d("ProductViewFragment", "Showing recycler view")
            viewSwitcher?.findViewById<View>(R.id.recycler_view)?.visibility = View.VISIBLE
            view?.findViewById<View>(R.id.no_product_found_view)?.visibility = View.GONE
        }
    }
    private fun showShimmer(callback: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
        {
            noProductView.visibility = View.GONE
            callback()
            return
        }
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
            setupCategoryList()
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

    fun scrollToTop() {
        recyclerView.smoothScrollToPosition(0)
    }
}

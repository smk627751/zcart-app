package com.smk627751.zcart.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.Utility.hideSoftKeyboard
import com.smk627751.zcart.adapter.ReviewsAdapter
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.viewmodel.DetailViewModel
import io.noties.markwon.Markwon

class DetailViewActivity : AppCompatActivity() {
    lateinit var viewModel: DetailViewModel
    lateinit var parent : ViewGroup
    lateinit var toolbar: MaterialToolbar
    private lateinit var detailView: NestedScrollView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private var shimmerRunnable : Runnable? = null
    lateinit var name : TextView
    lateinit var price : TextView
    private lateinit var description : TextView
    private lateinit var consolidatedRating : TextView
    private lateinit var fivestar : LinearProgressIndicator
    private lateinit var fourstar : LinearProgressIndicator
    private lateinit var threestar : LinearProgressIndicator
    private lateinit var twostar : LinearProgressIndicator
    private lateinit var onestar : LinearProgressIndicator
    lateinit var image : ImageView
    private lateinit var ratingLayout : LinearLayout
    private lateinit var ratingBar : RatingBar
    private lateinit var placeOrderButton : Button
    private lateinit var addToCartButton : Button
    private lateinit var reviewField: EditText
    private lateinit var sendButton : Button
    private lateinit var reviewSection : RecyclerView
    var adapter: ReviewsAdapter? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.product_detail_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        Utility.registerInternetReceiver(this)
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]
        intent.getSerializableExtra("product")?.let { viewModel.setProduct(it as Product)}
        intent.getStringExtra("product_id")?.let { viewModel.setProduct(it) }

        parent = findViewById(R.id.main)
        toolbar = findViewById(R.id.toolbar)
        detailView = findViewById(R.id.details_view)
        shimmerFrameLayout = findViewById(R.id.shimmer_layout)
        image = findViewById(R.id.product_image)
        name = findViewById(R.id.product_name)
        price = findViewById(R.id.product_price)
        description = findViewById(R.id.product_description)
        ratingLayout = findViewById(R.id.rating_layout)
        consolidatedRating = findViewById(R.id.consolidated_rating)
        fivestar = findViewById(R.id.five_star_bar)
        fourstar = findViewById(R.id.four_star_bar)
        threestar = findViewById(R.id.three_star_bar)
        twostar = findViewById(R.id.two_star_bar)
        onestar = findViewById(R.id.one_star_bar)
        ratingBar = findViewById(R.id.rating_bar)
        reviewField = findViewById(R.id.review_text)
        placeOrderButton = findViewById(R.id.place_order_button)
        addToCartButton = findViewById(R.id.add_to_cart_button)
        sendButton = findViewById(R.id.send_button)
        reviewSection = findViewById(R.id.review_section)
        val markwon = Markwon.create(this)
        detailView.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmerAnimation()
        }
        ViewCompat.setTransitionName(image, "image_transition")
        viewModel.product.observe(this) {
            if (it == null)
            {
                finish()
                return@observe
            }
//            val transition = TransitionInflater.from(this).inflateTransition(android.R.transition.move)
//            transition.duration = 200 // Set transition duration (in milliseconds)
//            window.sharedElementEnterTransition = transition
//            window.sharedElementExitTransition = transition
            shimmerFrameLayout.stopShimmerAnimation()
            shimmerFrameLayout.visibility = View.GONE
            detailView.visibility = View.VISIBLE
            Glide.with(this)
                .load(it.image)
                .error(R.drawable.baseline_image_24)
                .into(image)
            name.text = it.name
            price.text = Utility.formatNumberIndianSystem(it.price)
            description.text = markwon.toMarkdown(it.description)
            viewModel.checkIfVendor()
            adapter = ReviewsAdapter(viewModel.product.value?.reviews ?: mutableMapOf(),viewModel)
            reviewSection.adapter = adapter
            reviewSection.layoutManager = LinearLayoutManager(this)
            consolidatedRating.text = viewModel.consolidatedRating().toString()
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
                hideSoftKeyboard(this)
            }
            setUpratingBars(viewModel.getIndividualRating())
//            setUpMenu(false)
        }
        detailView.setOnTouchListener {_,_ ->
            reviewField.clearFocus()
            Utility.hideSoftKeyboard(this)
            false
        }
        val review = viewModel.currentUserReview()
        if (review != null)
        {
            reviewField.setText(review.text)
            ratingBar.rating = review.rating
        }
        viewModel.isPurchased.observe(this){
            if (it)
            {
                ratingBar.visibility = View.VISIBLE
                findViewById<RelativeLayout>(R.id.review_layout).visibility = View.VISIBLE
            }
        }
        viewModel.isVendor.observe(this){isVendor ->
            if (isVendor)
            {
                setUpView()
            }
            else{
                viewModel.isPurchasedProduct()
            }
        }

        placeOrderButton.setOnClickListener {
            Intent(this, PlaceOrderActivity::class.java).also {
                it.putExtra("products", arrayOf(viewModel.product.value))
                startActivityForResult(it,1)
            }
        }
        addToCartButton.setOnClickListener {
            viewModel.addToCart{
                Utility.makeToast(this, "Product added to cart")
//                Intent(this, HomeActivity::class.java).also {
//                    it.putExtra("replacement","cart")
//                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(it)
//                }
                setResult(1,Intent().apply {
                    putExtra("replacement","cart")
                })
                finish()
            }
        }

        sendButton.setOnClickListener {
            if (reviewField.text.isEmpty()||reviewField.text.isBlank())
            {
                Utility.makeToast(this, "Please enter a review")
                return@setOnClickListener
            }
            viewModel.addReview(ratingBar.rating,reviewField.text.toString()){
                Utility.makeToast(this, "Review added successfully")
                reviewField.text.clear()
                adapter?.notifyDataSetChanged()
                reviewSection.smoothScrollToPosition(adapter!!.itemCount - 1)
            }
        }
    }

    private fun setUpratingBars(individualRating: MutableMap<Int, Float>){
        fivestar.progress = ((individualRating[5]!! * 100).toInt())
        fourstar.progress = ((individualRating[4]!! * 100).toInt())
        threestar.progress = ((individualRating[3]!! * 100).toInt())
        twostar.progress = ((individualRating[2]!! * 100).toInt())
        onestar.progress = ((individualRating[1]!! * 100).toInt())
    }

    private fun setUpView()
    {
        findViewById<LinearLayout>(R.id.customer_button_layout).visibility = View.GONE
        ratingBar.visibility = View.GONE
        findViewById<RelativeLayout>(R.id.review_layout).visibility = View.GONE
        viewModel.checkIfOwnProduct { isOwnProduct ->
            if (isOwnProduct) {
                setUpMenu(true)
            }
        }
    }
    private fun setUpMenu(isOwnProduct: Boolean) {
        toolbar.menu.clear()
        if (isOwnProduct)
        {
            toolbar.inflateMenu(R.menu.popup_menu)
            toolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
//                        showBottomSheet()
                        Intent(this,AddProductActivity::class.java).apply {
                            putExtra("product",viewModel.product.value)
                            putExtra("mode","edit")
                            startActivity(this).also {
                                finish()
                            }
                            overridePendingTransition(R.anim.slide_up,R.anim.fade_in)
                        }
                        true
                    }
                    R.id.action_delete -> {
                        showAlertDialog()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    private fun showAlertDialog()
    {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Are you sure want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteProduct {
                    Utility.makeToast(this, "Product deleted successfully")
                    finish()
                }
            }
            .setNegativeButton("No") { _, _ -> }
            .create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1)
        {
            setResult(1,Intent().apply {
                putExtra("replacement","orders")
            })
            finish()
        }
    }
}
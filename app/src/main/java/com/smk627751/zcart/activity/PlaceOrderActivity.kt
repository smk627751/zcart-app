package com.smk627751.zcart.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.hbb20.CountryCodePicker
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.adapter.OrderProductsAdapter
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.viewmodel.PlaceOrderViewModel

class PlaceOrderActivity : AppCompatActivity() {
    lateinit var viewModel: PlaceOrderViewModel
    lateinit var parent:CoordinatorLayout
    lateinit var toolbar: MaterialToolbar
    lateinit var productsView : RecyclerView
    var adapter: OrderProductsAdapter? = null
    lateinit var username : EditText
    lateinit var deliveryAddress : TextView
    lateinit var ccp : CountryCodePicker
    lateinit var phone : TextView
    lateinit var totalPrice : TextView
    lateinit var placeOrderButton : Button
    lateinit var progress : ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_place_order)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        Utility.registerInternetReceiver(this)
        viewModel = ViewModelProvider(this)[PlaceOrderViewModel::class.java]
        viewModel.setProducts(intent.getSerializableExtra("products") as Array<Product>)
        viewModel.products.observe(this) {products ->
            adapter = OrderProductsAdapter(products){
                totalPrice.text = it.toString()
            }
            setUpView()
        }
        parent = findViewById(R.id.main)
        toolbar = findViewById(R.id.toolbar)
        productsView = findViewById(R.id.products_view)
        username = findViewById(R.id.username)
        deliveryAddress = findViewById(R.id.delivery_address)
        ccp = findViewById(R.id.ccp)
        phone = findViewById(R.id.phone_number)
        totalPrice = findViewById(R.id.total_price)
        placeOrderButton = findViewById(R.id.place_order_button)
        progress = findViewById(R.id.progress)

        parent.setOnClickListener {_ ->
            Utility.hideSoftKeyboard(this)
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }

        placeOrderButton.setOnClickListener {
            if (username.text.isEmpty() || deliveryAddress.text.isEmpty() || phone.text.isEmpty()) {
                Utility.makeToast(this, "Please fill all the fields")
                return@setOnClickListener
            }
            setProgress(true)

            viewModel.placeOrder(adapter!!.itemCount,adapter!!.getQuantities(),totalPrice.text.toString(),username.text.toString(),deliveryAddress.text.toString(),"${ccp.selectedCountryCodeWithPlus}${phone.text}"){
                Utility.makeToast(this, "Order Placed Successfully")
                setProgress(false)
                finish()
            }
        }
    }
    private fun setUpView()
    {
        productsView.adapter = adapter
        productsView.layoutManager = LinearLayoutManager(this).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        username.setText(viewModel.getUsername())
        deliveryAddress.text = viewModel.getDeliveryAddress()
        phone.text = viewModel.getPhoneNumber().replaceFirst(ccp.selectedCountryCodeWithPlus, "")
    }
    private fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            placeOrderButton.visibility = View.GONE
            progress.visibility = View.VISIBLE
        }
        else
        {
            placeOrderButton.visibility = View.VISIBLE
            progress.visibility = View.GONE
        }
    }
}
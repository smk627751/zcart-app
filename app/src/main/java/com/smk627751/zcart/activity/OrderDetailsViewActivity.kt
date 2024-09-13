package com.smk627751.zcart.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.adapter.CartAdapter
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.viewmodel.OrderDetailViewModel

class OrderDetailsViewActivity : AppCompatActivity() {
    lateinit var viewModel: OrderDetailViewModel
    private lateinit var toolbar : MaterialToolbar
    private lateinit var orderId : TextView
    private lateinit var date : TextView
    private lateinit var quantity : TextView
    private lateinit var totalPrice : TextView
    private lateinit var orderStatusText : TextView
    private lateinit var orderStatusLayout : LinearLayout
    private lateinit var orderStatus : Spinner
    private lateinit var orderStatusIndicator : LinearLayout
    lateinit var products : RecyclerView
    lateinit var name : TextView
    private lateinit var deliveryAddress : TextView
    lateinit var phone : TextView
    private lateinit var updateOrderStatus : Button
    private lateinit var cancelOrder : Button
    var prevStatus = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_details_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        Utility.registerInternetReceiver(this)
        viewModel = ViewModelProvider(this)[OrderDetailViewModel::class.java]
        intent.getSerializableExtra("order")?.let { viewModel.setOrder(it as Order)}
        intent.getStringExtra("order_id")?.let { viewModel.setOrder(it) }

        toolbar = findViewById(R.id.toolbar)
        orderId = findViewById(R.id.order_id)
        date = findViewById(R.id.date)
        quantity = findViewById(R.id.quantity)
        totalPrice = findViewById(R.id.total_price)
        orderStatusLayout = findViewById(R.id.order_status_layout)
        orderStatusText = findViewById(R.id.order_status_text)
        orderStatus = findViewById(R.id.order_status)
        orderStatusIndicator = findViewById(R.id.order_status_bar)
        products = findViewById(R.id.products_section)
        name = findViewById(R.id.username)
        deliveryAddress = findViewById(R.id.delivery_address)
        phone = findViewById(R.id.phone_number)
        updateOrderStatus = findViewById(R.id.update_order_status)
        cancelOrder = findViewById(R.id.cancel_order)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        viewModel.isVendor.observe(this) {
            if (it)
            {
                updateOrderStatus.visibility = View.VISIBLE
                orderStatus.visibility = View.VISIBLE
            }
            else {
                updateOrderStatus.visibility = View.GONE
                orderStatus.visibility = View.GONE
                orderStatusIndicator.visibility = View.VISIBLE
            }
        }
        viewModel.order.observe(this) {
            if (it != null)
            {
                prevStatus = it.status
                setUpView(it)
                updateOrderStatus(orderStatusIndicator,it.status)
            }
            else
            {
                finish()
            }
        }
    }

    private fun setUpView(order: Order)
    {
        name.text = order.name
        deliveryAddress.text = order.deliveryAddress
        phone.text = order.phone
        orderId.text = order.id
        orderId.paintFlags = orderId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        date.text = Utility.formatTime(order.timestamp)
        quantity.text = order.quantity.toString()
        totalPrice.text = "₹${order.totalPrice}"
        var orderStatusList = viewModel.orderStatus.keys.toList()
        val startIndex = orderStatusList.indexOf(order.status)
        orderStatusList = orderStatusList.subList(if(startIndex != -1) startIndex else 0, orderStatusList.size)
        orderStatus.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, orderStatusList)
        orderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {
                order.status = viewModel.orderStatus.keys.toList()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        orderId.setOnClickListener {
            copyToClipboard(order.id)
            Utility.makeToast(this, "Order ID copied to clipboard")
        }
        orderStatus.setSelection(orderStatusList.indexOf(order.status))
        if (order.status == "Order cancelled" || order.status == "Order delivered") {
            updateOrderStatus.visibility = View.GONE
            cancelOrder.visibility = View.GONE
            orderStatusLayout.visibility = View.GONE
            orderStatusText.text = order.status

            if (order.status == "Order cancelled") {
                orderStatusText.visibility = View.VISIBLE
                orderStatusIndicator.visibility = View.GONE
            } else {
                orderStatusIndicator.visibility = View.VISIBLE
                orderStatusText.visibility = View.GONE
            }
        } else {
            orderStatusLayout.visibility = View.VISIBLE
            orderStatusText.visibility = View.GONE
        }
        viewModel.getProducts{
            products.adapter = CartAdapter(it)
            products.layoutManager = LinearLayoutManager(this)
        }

        updateOrderStatus.setOnClickListener {
            if (order.status == prevStatus)
            {
                Utility.makeToast(this, "Status not changed")
                return@setOnClickListener
            }
            viewModel.updateOrderStatus(order.status) {
                Utility.makeToast(this, "Order status updated")
                finish()
            }
        }
        cancelOrder.setOnClickListener {
            Utility.showAlertDialog(this, "Are you sure you want to cancel this order?") {
                viewModel.updateOrderStatus("Order cancelled") {
                    Utility.makeToast(this, "Order cancelled")
                    finish()
                }
            }
        }
    }
    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }
    private fun updateOrderStatus(view: View, status: String) {
        val indicator = view.findViewById<LinearLayout>(R.id.order_status_indicator)
        val imageViews = findImageView(indicator)
        val bars = findBars(indicator)
        val count = viewModel.getCount(status)

        if (count in imageViews.indices)
        {
            if (count < 3)
            {
                imageViews[count].animation = AnimationUtils.loadAnimation(this, R.anim.blink)
            }
            for (i in 0..count) {
                imageViews[i].setImageResource(R.drawable.baseline_circle_colored_24)
                if (i-1 in bars.indices)
                {
                    bars[i-1].setBackgroundColor(resources.getColor(R.color.black))
                }
            }
        }
    }
    private fun findImageView(view: View): List<ImageView> {
        val imageViews = mutableListOf<ImageView>()
        if (view is LinearLayout) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is ImageView) {
                    imageViews.add(child)
                }
            }
        }
        return imageViews
    }
    private fun findBars(view: View): List<View> {
        val views = mutableListOf<View>()
        if (view is LinearLayout) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is View && child !is ImageView) {
                    views.add(child)
                }
            }
        }
        return views
    }
}
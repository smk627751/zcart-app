package com.smk627751.zcart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.dto.Product

class OrderDetailViewModel : ViewModel() {
    var orderStatus = mutableMapOf(
        "Order placed" to 0,
        "Order shipped" to 1,
        "Out for delivery" to 2,
        "Order delivered" to 3
    )
        private set
    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order> = _order
    private val _isVendor = MutableLiveData<Boolean>()
    val isVendor: LiveData<Boolean> = _isVendor
    init {
        Repository.isVendor {
            _isVendor.value = it
        }
    }
    fun setOrder(order: Order) {
        _order.value = order
    }
    fun setOrder(orderId: String) {
        Repository.getOrderById(orderId) {
            _order.value = it
        }
    }
    fun getCount(status: String) : Int
    {
        return orderStatus[status] ?: -1
    }
    fun updateOrderStatus(status: String,callBack: () -> Unit) {
        if (status == "Order placed")
        {
            return
        }
        _order.value?.status = status
        Repository.updateOrderStatus(_order.value?.id ?: "", status) {
            Repository.getUserDataById(_order.value?.customerId ?: ""){
                if (it != null) {
                    Log.i("uuid", "updateOrderStatus: ${it.fcmToken}")
                    Repository.sendNotification(it.fcmToken,_order.value?.status!!)
                }
                callBack()
            }
        }
    }
    fun getProducts(callBack: (products: List<Product>) -> Unit) {
        Repository.getProducts(_order.value?.products ?: listOf()) {
            callBack(it)
        }
    }
}
package com.smk627751.zcart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.Utility
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.dto.OrderNotification
import com.smk627751.zcart.dto.Product

class PlaceOrderViewModel : ViewModel() {
    private val _products = MutableLiveData<Array<Product>>()
    val products: LiveData<Array<Product>> = _products

    fun setProducts(products: Array<Product>) {
        _products.value = products
    }
    fun validate(name : String, address : String, phone : String) : Boolean
    {
        return name.isNotEmpty()
                && address.isNotEmpty()
                && phone.isNotEmpty()
                && phone.length == 10
                && phone.all { it.isDigit() }
    }
    fun placeOrder(products: MutableMap<Product, MutableMap<Double, Int>>, itemCount : Int, quantities : Int, totalPrice : String, username: String, deliveryAddress: String,phone : String, callBack: () -> Unit) {
        if (products != null)
        {
//            val order = Order(
//                Utility.generateOrderId(),
//                products.value!!.map { it.vendorId },
//                Repository.currentUserId,
//                products.value!!.map { it.id },
//                itemCount,
//                quantities,
//                totalPrice.replace("₹", "").toDouble(),
//                "Order placed",
//                username,
//                deliveryAddress,
//                phone,
//                System.currentTimeMillis()
//            )
            val vendors = mutableSetOf<String>()
            products.keys.forEach {
                vendors.add(it.vendorId)
            }
            vendors.forEach { vendorId ->
                val ownProducts = products.filter { it.key.vendorId == vendorId }
//                var price = 0.0
//                ownProducts.values.forEach{
//                    it.forEach {
//                        price += it.key * it.value
//                    }
//                }
                val order = Order(
                        Utility.generateOrderId(),
                        vendorId,
                        Repository.currentUserId,
                        ownProducts.keys.map { it.id },
                        ownProducts.size,
                        ownProducts.values.sumOf { it.values.sum() },
                        ownProducts.values.sumOf { it.map { it.key * it.value }.sum() },
                        "Order placed",
                        username,
                        deliveryAddress,
                        phone,
                        System.currentTimeMillis()
                    )
                Repository.placeOrder(order,ownProducts.keys.toTypedArray()){
                    callBack()
                }
                Repository.addNotification(OrderNotification("${getUsername()} placed an order",vendorId).apply {
                    this.orderId = order.id
                })
            }
//            Repository.placeOrder(order,products.value!!){
//                callBack()
//                products.value!!.forEach {
//                    vendors.add(it.vendorId)
//                }
//                vendors.forEach {
//                    Repository.addNotification(OrderNotification("${getUsername()} placed an order",it).apply {
//                        this.orderId = order.id
//                    })
//                }
//            }
        }
    }
    fun getUsername(): String {
        return Repository.user?.name ?: ""
    }
    fun getDeliveryAddress(): String {
        return Repository.user?.address ?: ""
    }

    fun getPhoneNumber(): String {
        return Repository.user?.phone ?: ""
    }
}
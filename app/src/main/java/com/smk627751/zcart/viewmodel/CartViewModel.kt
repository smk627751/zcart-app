package com.smk627751.zcart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Product

class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<Product>>()
    val cartItems: LiveData<List<Product>> = _cartItems
    init {
        getCartItems()
    }
    fun getCartItems() {
        Repository.getCartItems {
            _cartItems.value = it
        }
    }
    fun removeFromCart(product: Product) {
        Repository.removeFromCart(product) {
            getCartItems()
        }
    }
}
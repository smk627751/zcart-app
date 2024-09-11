package com.smk627751.zcart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Order

class OrdersViewModel : ViewModel() {
    private val _options = MutableLiveData<FirestoreRecyclerOptions<Order>>()
    val options: LiveData<FirestoreRecyclerOptions<Order>> = _options
    val _isVendor = MutableLiveData<Boolean>()
    val isVendor: LiveData<Boolean> = _isVendor
    init {
        getOrders()
        isVendor()
    }
    fun isVendor() {
        Repository.isVendor {
            _isVendor.value = it
        }
    }
    fun getOrders(query: String = "all") {
        Repository.getOrders(query){
            _options.value = it
        }
    }
}
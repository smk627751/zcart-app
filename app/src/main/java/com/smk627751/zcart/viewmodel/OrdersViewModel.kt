package com.smk627751.zcart.viewmodel

import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Order

class OrdersViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading
    private val _itemCount = MutableLiveData<Int>()
    val itemCount : LiveData<Int> = _itemCount
    private val _options = MutableLiveData<FirestoreRecyclerOptions<Order>>()
    val options: LiveData<FirestoreRecyclerOptions<Order>> = _options
    private val _isVendor = MutableLiveData<Boolean>()
    val isVendor: LiveData<Boolean> = _isVendor
    init {
        load()
        Handler().postDelayed({
            getOrders()
            isVendor()
        }, 500)
    }
    fun load()
    {
        _isLoading.value = true
    }
    fun isVendor() {
        Repository.isVendor {
            _isVendor.value = it
        }
    }
    fun getOrders(query: String = "all") {
        load()
        Repository.getOrders(query){count,options ->
            _options.value = options
            _itemCount.value = count
            Log.d("TAG", "getOrders: $count")
            Handler().postDelayed({
                _isLoading.value = false
            },500)
        }
    }
}
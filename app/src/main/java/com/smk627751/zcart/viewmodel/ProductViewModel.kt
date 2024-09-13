package com.smk627751.zcart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Product

class ProductViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _itemCount = MutableLiveData<Int>()
    val itemCount : LiveData<Int> = _itemCount
    val selectedItemsList = mutableListOf<String>()
    private var searchList : MutableMap<String,String> = mutableMapOf()
    private val _filterList = MutableLiveData<Map<String,String>>()
    val filterList : LiveData<Map<String,String>> = _filterList
    private val _options = MutableLiveData<FirestoreRecyclerOptions<Product>>()
    val options: LiveData<FirestoreRecyclerOptions<Product>> = _options

    init {
        load()
        getProducts()
        setSearchList()
    }
    fun load()
    {
        _isLoading.value = true
    }
    fun setFilterList(query: String){
        _filterList.value = searchList.filter { it.key.toLowerCase().contains(query.toLowerCase()) }
    }
    private fun setSearchList(){
        Repository.getProductsName { products ->
            products.forEach {
                if (!searchList.containsKey(it))
                {
                    searchList[it] = "name"
                }
            }
        }
        Repository.category.forEach {
            if (!searchList.containsKey(it))
            {
                searchList[it] = "category"
            }
        }
        _filterList.value = searchList
    }
    fun getCategories(): List<String> {
        return Repository.genericCategory
    }

    private fun getProducts() {
        _options.value = Repository.listenProducts(""){
            _itemCount.value = it
        }.also { _isLoading.value = false }
    }

    fun getProductsByCategory(category: String) {
        _options.value = Repository.listenProducts(
            category.replace("[","").replace("]",""),
            "category"
        ){
            _itemCount.value = it
        }.also { _isLoading.value = false }
    }

    fun getProductsByName(name: String) {
        _options.value = Repository.listenProducts(name, "name"){
            _itemCount.value = it
        }.also { _isLoading.value = false }
    }
}

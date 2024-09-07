package com.smk627751.zcart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Product

class ProductViewModel : ViewModel() {
    val selectedItemsList = mutableListOf<String>()
    private var searchList : MutableMap<String,String> = mutableMapOf()
    private val _filterList = MutableLiveData<Map<String,String>>()
    val filterList : LiveData<Map<String,String>> = _filterList
    private val _options = MutableLiveData<FirestoreRecyclerOptions<Product>>()
    val options: LiveData<FirestoreRecyclerOptions<Product>> = _options

    init {
        getProducts()
        setSearchList()
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
        _options.value = Repository.listenProducts("")
    }

    fun getProductsByCategory(category: String) {
        _options.value = Repository.listenProducts(category.replace("[","").replace("]",""),"category")
    }

    fun getProductsByName(name: String) {
        _options.value = Repository.listenProducts(name,"name")
    }
}

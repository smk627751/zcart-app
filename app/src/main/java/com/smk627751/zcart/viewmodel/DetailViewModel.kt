package com.smk627751.zcart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.dto.Review
import com.smk627751.zcart.dto.ReviewNotification
import com.smk627751.zcart.dto.User

class DetailViewModel : ViewModel() {
    private val _isPurchased = MutableLiveData<Boolean>()
    /** LiveData for the purchased status */
    val isPurchased: LiveData<Boolean> = _isPurchased

    private val _rating = MutableLiveData<MutableMap<Int, Int>>()
    /** LiveData for the rating */
    val rating: LiveData<MutableMap<Int, Int>> = _rating
    private val _product = MutableLiveData<Product>()
    /** LiveData for the product */
    val product: LiveData<Product> = _product
    private val _isVendor = MutableLiveData<Boolean>()
    /** LiveData for the vendor */
    val isVendor: LiveData<Boolean> = _isVendor
    /**Function to check if the user is a vendor*/
    fun checkIfVendor() {
        Repository.isVendor {
            _isVendor.value = it
        }
    }
    fun checkIfOwnProduct(callback: (Boolean) -> Unit) {
        Repository.isOwnProduct(_product.value!!.id){
            callback(it)
        }
    }
    fun isPurchasedProduct()
    {
        Repository.isPurchased(_product.value!!.id){
            Log.d("isPurchased in viewmodel",it.toString())
            _isPurchased.value = it
        }
    }
    fun consolidatedRating() : Float
    {
        var rating = 0f
        _product.value?.reviews?.values?.forEach {
            rating += it.rating
        }
        return if (_product.value?.reviews?.size!! > 0) rating / _product.value?.reviews?.size!! else 0f
    }
    fun getIndividualRating() : MutableMap<Int,Float>
    {
        val _5 = _product.value?.reviews?.values?.count { it.rating == 5.0f }
            ?.div(_product.value?.reviews?.size!!.toFloat())
        val _4 = _product.value?.reviews?.values?.count { it.rating == 4.0f }
            ?.div(_product.value?.reviews?.size!!.toFloat())
        val _3 = _product.value?.reviews?.values?.count { it.rating == 3.0f }
            ?.div(_product.value?.reviews?.size!!.toFloat())
        val _2 = _product.value?.reviews?.values?.count { it.rating == 2.0f }
            ?.div(_product.value?.reviews?.size!!.toFloat())
        val _1 = _product.value?.reviews?.values?.count { it.rating == 1.0f }
            ?.div(_product.value?.reviews?.size!!.toFloat())
        return mutableMapOf(5 to (_5 ?: 0f),4 to (_4 ?: 0f),3 to (_3 ?: 0f),2 to (_2 ?: 0f),1 to (_1 ?: 0f))
    }
    /**Function to get the user by Id*/
    fun getUser(id: String,callback: (user: User?) -> Unit)
    {
        Repository.getUserDataById(id){
            callback(it)
        }
    }
    /**Function to set the product*/
    fun setProduct(product: Product) {
        _product.value = product
    }
    fun setProduct(id: String) {
        Repository.getProductById(id) {
            _product.value = it
        }
    }
    /**Function to update the product*/
    fun updateProduct(product: Product) {
        _product.value = product
    }
    /**Function to delete the product*/
    fun deleteProduct(callback: () -> Unit) {
        Repository.deleteProduct(_product.value!!.id)
        {
            callback()
        }
    }
    /**Function to get the current user review*/
    fun currentUserReview(): Review? {
        return _product.value?.reviews?.get(Repository.currentUserId)
    }
    /**Function to add the product to the cart*/
    fun addToCart(callback: () -> Unit) {
        Repository.addToCart(_product.value!!.id) {
            callback()
        }
    }
    fun isCurrentUserOrVendor(id: String) : Boolean
    {
        return _product.value?.vendorId == Repository.currentUserId || id == Repository.currentUserId
    }
    /**Function to add the review to the product*/
    fun addReview(rating : Float,text : String,callback: () -> Unit) {
        val review = Review(Repository.currentUserId,rating,text.trim())
        if (_product.value?.reviews?.containsKey(Repository.currentUserId)!!)
        {
            review.isEdited = true
        }
        _product.value?.reviews?.set(Repository.currentUserId, review)
        Repository.updateProductToDb(_product.value!!) {
            _product.value = _product.value
            Repository.addNotification(ReviewNotification(text = "${Repository.user?.name} added a review", vendorId = _product.value!!.vendorId).apply {
                this.productId = _product.value!!.id
            })
            callback()
        }
    }
    /**Function to delete the review from the product*/
    fun deleteReview(review: Review,callback: () -> Unit) {
        _product.value?.reviews?.remove(review.userId)
        Repository.updateProductToDb(_product.value!!) {
            _product.value = _product.value
            callback()
        }
    }
}
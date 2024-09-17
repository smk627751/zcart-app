package com.smk627751.zcart.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.dto.Vendor

class ProfileViewModel : ViewModel() {
    /** LiveData for error messages*/
    val nameError = MutableLiveData<String?>()
    val addressError = MutableLiveData<String?>()
    val zipcodeError = MutableLiveData<String?>()
    val phoneError = MutableLiveData<String?>()
    private val _user = MutableLiveData<User>()
    /** LiveData for user data*/
    val user: LiveData<User> = _user
    private val _isVendor = MutableLiveData<Boolean>()
    /** LiveData for vendor status*/
    val isVendor: LiveData<Boolean> = _isVendor
    init {
        checkIfVendor()
        getUserData()
    }
    fun vendorHasProducts() : Boolean {
        return user.value is Vendor && (user.value as Vendor).products.isNotEmpty()
    }
    fun setUserData(user: User) {
        _user.value = user
    }
    fun isOwnProfile(email: String) : Boolean {
        return email == Repository.user?.email
    }
    fun getMyProducts() : FirestoreRecyclerOptions<Product> {
        return Repository.listenProducts("", "myProducts"){}
    }
    /**Function to check if the user is a vendor*/
    fun checkIfVendor() {
        Repository.isVendor {
            _isVendor.value = it
        }
    }
    /** Function to get user data*/
    fun getUserData() {
        Repository.getUserData {
            _user.value = it
        }
    }
    /**Function to update user data*/
    fun updateUserData(user: User, callBack: () -> Unit) {
        Repository.updateUserData(user) {
            _user.value = user
            callBack()
        }
    }
    /**Function to upload image
     * @param uri - The URI of the image to be uploaded
     * @param callBack - The callback function to be executed after the image is uploaded*/
    fun uploadImage(uri : Uri?, callBack : (url : String) -> Unit) {
        Repository.uploadImage("${Repository.IMAGE_PATH}/${user.value?.name}.jpg",uri, callBack)
    }
    fun validate(name: String, address: String, zipcode: String, phone: String): Boolean {
        var isValid = true
        if (name.isEmpty() || name.isBlank()) {
            nameError.value = "Enter your name"
            isValid = false
        }
        else {
            nameError.value = null
        }
        if (address.isEmpty() || address.isBlank()) {
            addressError.value = "Enter your address"
            isValid = false
        }
        else {
            addressError.value = null
        }
        if (zipcode.isEmpty() || zipcode.isBlank()) {
            zipcodeError.value = "Enter your zipcode"
            isValid = false
        }
        else {
            zipcodeError.value = null
        }
        // Phone validation
        if (phone.isEmpty() || phone.isBlank()) {
            phoneError.value = "Enter your phone number"
            isValid = false
        }
        else if (!phone.matches(Regex("\\d+"))) {
            phoneError.value = "Phone number must contain only digits"
            isValid = false
        }
        if (phone.startsWith("0")) {
            phoneError.value = "Phone number cannot start with 0"
            isValid = false
        }
        else if (phone.length != 10) {
            phoneError.value = "Invalid phone number"
            isValid = false
        } else {
            phoneError.value = null
        }
        return isValid
    }
    fun resetPassword(email : String, callBack: () -> Unit)
    {
        Repository.forgotPassword(email,callBack)
    }
    /**Function to logout*/
    fun logout(callBack: () -> Unit) {
        Repository.logout().also {
            callBack()
        }
    }

}
package com.smk627751.zcart.viewmodel
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Product
import java.util.UUID

class AddProductViewModel : ViewModel() {
    var isModified = false

    private val _product = MutableLiveData<Product>()
    val product : LiveData<Product> = _product

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri
    private val _productName = MutableLiveData<String>()
    val productName : LiveData<String> = _productName
    private val _productPrice = MutableLiveData<String>()
    val productPrice : LiveData<String> = _productPrice
    private val _productDescription = MutableLiveData<String>()
    val productDescription : LiveData<String> = _productDescription

    private val _imageUriError = MutableLiveData<String?>()
    val imageUriError : LiveData<String?> = _imageUriError
    private val _productNameError = MutableLiveData<String?>()
    val productNameError : LiveData<String?> = _productNameError
    private val _productPriceError = MutableLiveData<String?>()
    val productPriceError : LiveData<String?> = _productPriceError
    private val _productDescriptionError = MutableLiveData<String?>()
    val productDescriptionError : LiveData<String?> = _productDescriptionError

    private val _mode = MutableLiveData<String>()
    val mode : LiveData<String> = _mode
    var selectedItems = mutableListOf<String>()

    fun setProduct(product: Product) {
        _product.value = product
    }

    fun setMode(mode: String) {
        _mode.value = mode
    }
    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
        isModified = true
    }
    fun setProductName(name: String) {
        _productName.value = name
        isModified = true
    }
    fun setProductPrice(price: String) {
        _productPrice.value = price
        isModified = true
    }
    fun setProductDescription(description: String) {
        _productDescription.value = description
        isModified = true
    }

    fun addProduct(callback: (msg: String) -> Unit) {
        val product = _product.value
        val productId = product?.id ?: UUID.randomUUID().toString()
        Repository.uploadImage("${Repository.IMAGE_PATH}/${productId}.jpg", _imageUri.value) { imageUrl ->
            when (_mode.value) {
                "add" -> {
                    Repository.addProductToDb(
                        Product(
                            productId,
                            Repository.currentUserId,
                            imageUrl,
                            _productName.value ?: "",
                            _productPrice.value?.toDouble() ?: 0.0,
                            _productDescription.value ?: "",
                            selectedItems,
                            mutableMapOf()
                        )
                    ) {
                        callback("Product Added")
                    }
                }
                "edit" -> {
                    product?.let {
                        Repository.updateProductToDb(
                            it.copy(
                                image = imageUrl.ifEmpty { it.image },
                                name = _productName.value ?: it.name ?: "",
                                price = _productPrice.value?.toDouble() ?: it.price ?: 0.0,
                                description = _productDescription.value ?: it.description ?: "",
                                category = selectedItems
                            )
                        ) {
                            callback("Product Updated")
                        }
                    }
                }
            }
        }
    }

    fun validate(): Boolean {
        var isValid = true
        if (imageUri.value == null)
        {
            _imageUriError.value = "Please select an image"
            isValid = false
        }
        else {
            _imageUriError.value = null
        }
        if (_productName.value.isNullOrEmpty()) {
            _productNameError.value = "Product name is required"
            isValid = false
        } else {
            _productNameError.value = null
        }
        if (_productPrice.value.isNullOrEmpty()) {
            _productPriceError.value = "Product price is required"
            isValid = false
        } else {
            _productPriceError.value = null
        }
        if (_productDescription.value.isNullOrEmpty()) {
            _productDescriptionError.value = "Product description is required"
            isValid = false
        } else {
            _productDescriptionError.value = null
        }
        return isValid
    }
}
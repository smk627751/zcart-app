package com.smk627751.zcart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Notification

class HomeViewModel : ViewModel() {
    val homeId = 1
    val profileId = 2
    val addId = 3
    val ordersId = 4
    val notificationsId = 5
    val cartId = 6
    init {
        getNotifications()
    }
    private val _isLandscape = MutableLiveData(false)
    val isLandscape: LiveData<Boolean> get() = _isLandscape

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

    private val _isVendor = MutableLiveData<Boolean>()
    val isVendor: LiveData<Boolean> get() = _isVendor

    fun setLandscape(landscape: Boolean) {
        _isLandscape.value = landscape
    }
    fun getNotifications() {
        Repository.getNotifications {
            _notifications.value = it
        }
    }
    fun checkIfVendor() {
        Repository.isVendor {
            _isVendor.value = it
        }
    }
}

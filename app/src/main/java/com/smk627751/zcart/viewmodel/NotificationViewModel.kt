package com.smk627751.zcart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Notification
import com.smk627751.zcart.dto.OrderNotification
import com.smk627751.zcart.dto.ReviewNotification

class NotificationViewModel : ViewModel() {
    val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications
    fun getNotifications(type : String) {
       Repository.getNotifications{
           when(type)
           {
               "all" -> _notifications.value = it
               "orders" -> _notifications.value = it.filter { it is OrderNotification }
               "reviews" -> _notifications.value = it.filter { it is ReviewNotification }
           }
       }
    }
}
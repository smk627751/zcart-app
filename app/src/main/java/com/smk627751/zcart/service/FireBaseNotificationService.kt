package com.smk627751.zcart.service

import com.google.firebase.messaging.FirebaseMessagingService

class FireBaseNotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
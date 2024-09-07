package com.smk627751.zcart.dto

import java.util.UUID

open class Notification(
    val id : String = UUID.randomUUID().toString(),
    val text : String = "",
    val vendorId : String = "",
    val type : String = "",
    val timestamp : Long = System.currentTimeMillis(),
    var isRead : Boolean = false
)
package com.smk627751.zcart.dto

import com.smk627751.zcart.Utility
import java.util.UUID

open class Notification(
    val id : String = Utility.generateId(),
    val text : String = "",
    val vendorId : String = "",
    val type : String = "",
    val timestamp : Long = System.currentTimeMillis(),
    var isRead : Boolean = false
)
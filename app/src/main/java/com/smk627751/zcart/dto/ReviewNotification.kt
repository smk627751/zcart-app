package com.smk627751.zcart.dto

class ReviewNotification(text : String, vendorId : String) : Notification(text = text, vendorId = vendorId, type = "Review") {
    var productId : String = ""
    constructor() : this("", "")
}
package com.smk627751.zcart.dto

class OrderNotification(text : String, vendorId : String) : Notification(text = text,vendorId = vendorId, type = "Order") {
    var orderId : String = ""
    constructor() : this("","")
}
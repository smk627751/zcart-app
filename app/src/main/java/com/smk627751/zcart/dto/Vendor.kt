package com.smk627751.zcart.dto

class Vendor(
    name: String,
    email: String,
    phone : String,
    accountType: String,
    address: String = "",
    zipcode: Int = 0,
    image: String = "",
    fcmToken : String = ""
) : User(name, email, phone, accountType, address, zipcode, image, fcmToken) {
    var products = mutableListOf<String>()
    var orders = mutableListOf<String>()
    constructor() : this("","", "", "")
    override fun toString(): String {
        return "Vendor(name=$name, email=$email, accountType=$accountType, products=$products, orders=$orders)"
    }
}
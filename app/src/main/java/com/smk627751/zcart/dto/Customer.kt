package com.smk627751.zcart.dto

class Customer(
    name: String,
    email: String,
    phone: String,
    accountType: String,
    address: String = "",
    zipcode: Int = 0,
    image: String = "",
    fcmToken : String = ""
) : User(name, email,phone,accountType, address, zipcode, image,fcmToken){
    var cartItems = mutableListOf<String>()
    var myOrders = mutableListOf<String>()
    constructor() : this("", "", "", "")
    override fun toString(): String {
        return "Customer(name=$name, email=$email, accountType=$accountType, cartItems=$cartItems, myOrders=$myOrders)"
    }
}
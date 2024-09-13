package com.smk627751.zcart.dto

import java.io.Serializable

class Order(
    val id: String,
    val vendorIds: String,
    val customerId: String,
    val products: List<String>,
    val items: Int,
    val quantity: Int,
    val totalPrice: Double,
    var status: String,
    val name: String,
    val deliveryAddress: String,
    val phone: String,
    val timestamp: Long
) : Serializable
{
    constructor() : this("", "","", mutableListOf(), 0, 0,0.0, "", "","", "",0)
    override fun toString(): String {
        return "Order(id='$id', customerId='$customerId', products=$products, items=$items, quantity=$quantity, totalPrice=$totalPrice, status='$status', name='$name',deliveryAddress='$deliveryAddress', timestamp=$timestamp)"
    }
}
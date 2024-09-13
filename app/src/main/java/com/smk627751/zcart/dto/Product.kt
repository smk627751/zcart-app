package com.smk627751.zcart.dto

import java.io.Serializable

data class Product(
    val id: String,
    val vendorId: String,
    val image: String = "",
    val name: String,
    val price: Double,
    val description: String = "",
    val category: MutableList<String>,
    val reviews: MutableMap<String,Review>?,
    val searchName : String = name.lowercase()
): Serializable
{
    constructor() : this("", "","", "", 0.0,"", mutableListOf<String>(),mutableMapOf<String,Review>())
}
package com.smk627751.zcart.dto

import java.io.Serializable

data class Product(
    val id: String,
    val vendorId: String,
    val image: String = "",
    val name: String,
    val price: Long,
    val description: String = "",
    val category: MutableList<String>,
    val reviews: MutableMap<String,Review>?
): Serializable
{
    constructor() : this("", "","", "", 0,"", mutableListOf<String>(),mutableMapOf<String,Review>())
}
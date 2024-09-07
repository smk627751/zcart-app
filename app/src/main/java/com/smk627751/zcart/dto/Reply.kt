package com.smk627751.zcart.dto

import java.io.Serializable

data class Reply (
    val userId: String,
    val text : String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
{
    constructor(): this("","", System.currentTimeMillis())
}
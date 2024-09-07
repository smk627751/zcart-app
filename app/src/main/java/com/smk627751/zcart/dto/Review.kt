package com.smk627751.zcart.dto

import java.io.Serializable

data class Review (
    val userId: String,
    val rating : Float,
    val text : String,
    val reply : MutableList<Reply> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis(),
    var isEdited : Boolean = false
) : Serializable
{
    constructor() : this("",0f,"", mutableListOf())
}

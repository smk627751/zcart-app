package com.smk627751.zcart.dto

abstract class User()
{
    lateinit var image : String
    lateinit var name : String
    lateinit var email : String
    lateinit var phone : String
    lateinit var accountType : String
    lateinit var address : String
    var zipcode : Int = 0
    var fcmToken : String = ""
    constructor(
        name : String,
        email : String,
        phone : String,
        accountType : String,
        address : String,
        zipcode : Int,
        image : String,
        fcmToken : String
    ) : this()
    {
        this.name = name
        this.email = email
        this.phone = phone
        this.accountType = accountType
        this.address = address
        this.zipcode = zipcode
        this.image = image
        this.fcmToken = fcmToken
    }
}
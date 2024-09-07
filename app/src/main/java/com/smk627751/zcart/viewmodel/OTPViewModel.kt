package com.smk627751.zcart.viewmodel

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Customer
import com.smk627751.zcart.dto.Vendor

class OTPViewModel : ViewModel() {
    private val _bundle = MutableLiveData<Bundle>()
    val phone = MutableLiveData<String>()
    fun setBundle(bundle: Bundle) {
        _bundle.value = bundle
    }
    private val _otp = MutableLiveData<String>()
    val otp: LiveData<String> = _otp
    fun setPhone(phone: String) {
        this.phone.value = phone
    }
    fun setOTP(otp: String) {
        _otp.value = otp
    }
    fun verifyOTP(otp: String, success: () -> Unit, failure: (e: Exception) -> Unit){
        Repository.verifyOTP(_otp.value!!, otp,success,failure)
    }
    fun sendOTP(activity: Activity, failure: (e: Exception) -> Unit) {
        phone.value?.let {
            Repository.sendOTP(it,activity,{
                setOTP(it)
            },{
                failure(it)
            })
        }
    }
    fun resendOTP(activity: Activity, failure: (e: Exception) -> Unit) {
        phone.value?.let {
            Repository.resendOTP(it,activity,{
                setOTP(it)
            },{
                failure(it)
            })
        }
    }
}
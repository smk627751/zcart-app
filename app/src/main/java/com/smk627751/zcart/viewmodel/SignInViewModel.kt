package com.smk627751.zcart.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.sharedpreferences.Session

class SignInViewModel : ViewModel() {
    val _email = MutableLiveData<String>()
    val email : LiveData<String> = _email
    val _password = MutableLiveData<String>()
    val password : LiveData<String> = _password

    val _emailError = MutableLiveData<String?>()
    val emailError : LiveData<String?> = _emailError
    val _passwordError = MutableLiveData<String?>()
    val passwordError : LiveData<String?> = _passwordError

    fun setEmail(email: String) {
        _email.value = email
    }
    fun setPassword(password: String) {
        _password.value = password
    }
    fun validate() : Boolean
    {
        var isValid = true
        if (email.value.isNullOrEmpty())
        {
            _emailError.value = "Email is required"
            isValid = false
        }
        else
        {
            _emailError.value = null
        }
        if (password.value.isNullOrEmpty())
        {
            _passwordError.value = "Password is required"
            isValid = false
        }
        else
        {
            _passwordError.value = null
        }
        return isValid
    }
    //Function to set session
    fun setSession(context: Context,callBack : () -> Unit)
    {
        Repository.setSession(Session(context)).also {
            if(Repository.isLoggedIn())
            {
                callBack()
            }
        }
    }
    //Function to sign in
    fun signIn(success: () -> Unit, failure: (e: Exception) -> Unit) {
        if (_email.value != null && _password.value != null)
        {
            Repository.signIn(
                _email.value!!, _password.value!!,
                {
                    success()
                },
                {
                    failure(it)
                }
            )
        }
    }
    //Function to reset password
    fun forgotPassword(email: String, function: () -> Unit) {
        Repository.forgotPassword(email){
            function()
        }
    }
}
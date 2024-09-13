package com.smk627751.zcart.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.sharedpreferences.Session

class SignInViewModel : ViewModel() {
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
    fun signIn(email: String, password: String, success: () -> Unit, failure: (e: Exception) -> Unit) {
        Repository.signIn(email,password,
            {
                success()
            },
            {
                failure(it)
            }
        )
    }
    //Function to reset password
    fun forgotPassword(email: String, function: () -> Unit) {
        Repository.forgotPassword(email){
            function()
        }
    }
}
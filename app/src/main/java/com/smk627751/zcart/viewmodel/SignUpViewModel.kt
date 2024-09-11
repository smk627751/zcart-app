package com.smk627751.zcart.viewmodel

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.dto.Customer
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.dto.Vendor
import java.util.regex.Pattern

class SignUpViewModel : ViewModel() {

    // LiveData for form validation
    private val user = MutableLiveData<User>()
    var password = ""
        private set
    val nameError = MutableLiveData<String?>()
    val emailError = MutableLiveData<String?>()
    val phoneError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val confirmPasswordError = MutableLiveData<String?>()
    val accountTypeError = MutableLiveData<String?>()

    fun setUser(
        name: String,
        email: String,
        phone: String,
        accountType: String
    )
    {
        user.value = when(accountType)
        {
            "Customer" -> Customer(name,email,phone,accountType)
            "Vendor" -> Vendor(name,email,phone,accountType)
            else -> null
        }
    }
    // Function to validate the form
    fun validate(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        isAccountTypeSelected: Boolean
    ): Boolean {
        var isValid = true

        // Name validation
        if (name.isEmpty()) {
            nameError.value = "Enter your name"
            isValid = false
        } else {
            nameError.value = null
        }

        // Email validation
        if (email.isEmpty()|| email.isBlank())
        {
            emailError.value = "Enter your email"
            isValid = false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.value = "Invalid email address"
            isValid = false
        } else {
            emailError.value = null
        }

        // Phone validation
        if (phone.isEmpty() || phone.isBlank()) {
            phoneError.value = "Enter your phone number"
            isValid = false
        }
        else if (!phone.matches(Regex("\\d+"))) {
            phoneError.value = "Phone number must contain only digits"
            isValid = false
        }
        if (phone.startsWith("0")) {
            phoneError.value = "Phone number cannot start with 0"
            isValid = false
        }
        else if (phone.length != 10) {
            phoneError.value = "Invalid phone number"
            isValid = false
        } else {
            phoneError.value = null
        }

        // Password validation
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[$@&!#%]).{8,}$"
        if (password.isEmpty() || password.isBlank()) {
            passwordError.value = "Enter your password"
            isValid = false
        }
        else if (!Pattern.matches(passwordPattern, password)) {
            passwordError.value = "Password must contain at least 1 uppercase, 1 lowercase, 1 number, 1 special character, and be at least 8 characters long"
            isValid = false
        } else {
            passwordError.value = null
        }

        // Confirm password validation
        if (confirmPassword != password) {
            confirmPasswordError.value = "Password doesn't match"
            isValid = false
        } else {
            confirmPasswordError.value = null
            this.password = password
        }

        // Account type validation
        if (!isAccountTypeSelected) {
            accountTypeError.value = "Select an account type"
            isValid = false
        } else {
            accountTypeError.value = null
        }

        return isValid
    }
    fun signUp(success : () -> Unit, failure: (e: Exception) -> Unit)
    {
        user.value?.let { Repository.signUp(
            it.email,
            password,
            {
                Repository.addUserToDb(it).also {
                    success()
                }
            },
            {
                failure(it)
            }
        ) }
    }
}

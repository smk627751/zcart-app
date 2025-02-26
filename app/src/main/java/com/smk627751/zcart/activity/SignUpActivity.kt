package com.smk627751.zcart.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.viewmodel.SignUpViewModel

class SignUpActivity : AppCompatActivity() {
    lateinit var viewModel: SignUpViewModel
    lateinit var parent: ViewGroup
    lateinit var nameLayout : TextInputLayout
    lateinit var name : EditText
    lateinit var emailLayout : TextInputLayout
    lateinit var email : EditText
    lateinit var ccp : CountryCodePicker
    lateinit var phoneLayout : TextInputLayout
    lateinit var phone : EditText
    lateinit var passwordLayout : TextInputLayout
    lateinit var password : EditText
    lateinit var confirmPasswordLayout : TextInputLayout
    lateinit var confirmPassword : EditText
    lateinit var radioGroup: RadioGroup
    lateinit var signUp : Button
    lateinit var progress : ProgressBar
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Utility.registerInternetReceiver(this)
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        parent = findViewById(R.id.main)
        nameLayout = findViewById(R.id.name_layout)
        name = findViewById(R.id.name)
        emailLayout = findViewById(R.id.email_layout)
        email = findViewById(R.id.email)
        ccp = findViewById(R.id.ccp)
        phoneLayout = findViewById(R.id.phone_layout)
        phone = findViewById(R.id.phone)
        passwordLayout = findViewById(R.id.password_layout)
        password = findViewById(R.id.password)
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout)
        confirmPassword = findViewById(R.id.confirm_password)
        radioGroup = findViewById(R.id.radio_group)
        signUp = findViewById(R.id.sign_up_button)
        progress = findViewById(R.id.progress)

        parent.setOnTouchListener { _, _ ->
            name.clearFocus()
            email.clearFocus()
            password.clearFocus()
            confirmPassword.clearFocus()
            phone.clearFocus()
            Utility.hideSoftKeyboard(this)
            false
        }

        viewModel.nameError.observe(this) {
            nameLayout.error = it
        }

        viewModel.emailError.observe(this) {
            emailLayout.error = it
        }

        viewModel.phoneError.observe(this) {
            phoneLayout.error = it
        }

        viewModel.passwordError.observe(this) {
            passwordLayout.error = it
        }

        viewModel.confirmPasswordError.observe(this) {
            confirmPasswordLayout.error = it
        }

        viewModel.accountTypeError.observe(this) {
            if (it != null) {
                Utility.makeToast(this, it)
            }
        }
        var accountType : String = ""
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            accountType = when(checkedId)
            {
                R.id.customer -> "Customer"
                R.id.vendor -> "Vendor"
                else -> ""
            }
            Log.d("SignUpActivity",accountType)
        }
        signUp.setOnClickListener {
            if (viewModel.validate(name.text.toString(),email.text.toString(),phone.text.toString(),password.text.toString(),confirmPassword.text.toString(),accountType != ""))
            {
                viewModel.setUser(
                    name.text.toString(),
                    email.text.toString(),
                    "${ccp.selectedCountryCodeWithPlus}${phone.text}",
                    accountType
                ).also {
//                    OTPBottomSheet("${ccp.selectedCountryCodeWithPlus}${phone.text}"){
//                        viewModel.signUp({
//                            Utility.makeToast(this, "Verification email sent").also {
//                                Intent(this, SignInActivity::class.java).also {
//                                    startActivity(it)
//                                }
//                            }
//                        },{
//                            Utility.makeToast(this, it.message.toString())
//                        })
//                    }.show(supportFragmentManager,"OTP")
                    setProgress(true)
                    viewModel.signUp({
                        setProgress(false)
                        Utility.makeToast(this, "Verification email sent").also {
                            Intent(this, SignInActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                    },{
                        setProgress(false)
                        if (it.message == "No Internet connection")
                        {
                            Utility.makeSnackBar(parent, "No Internet connection"){
                                signUp.performClick()
                            }
                        }
                        else
                        {
                            Utility.makeToast(this, it.message.toString())
                        }
                    })
                }
            }
        }

        val signIn : TextView = findViewById(R.id.sign_in)
        signIn.setOnClickListener{
            Intent(this, SignInActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun showAlert(success: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Are you sure you want to continue?")
            .setMessage("Once you finish signing up, you can't change your account type.")
            .setPositiveButton("Ok") { _, _ ->
                success()
            }.setNegativeButton("Cancel") { _, _ ->

            }
            .show()
    }

    private fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            signUp.visibility = View.GONE
            progress.visibility = View.VISIBLE
        }
        else
        {
            signUp.visibility = View.VISIBLE
            progress.visibility = View.GONE
        }
    }
}
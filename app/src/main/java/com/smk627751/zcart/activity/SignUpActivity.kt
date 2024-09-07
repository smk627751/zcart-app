package com.smk627751.zcart.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.NestedScrollingParent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hbb20.CountryCodePicker
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.bottomsheet.OTPBottomSheet
import com.smk627751.zcart.viewmodel.SignUpViewModel

class SignUpActivity : AppCompatActivity() {
    lateinit var viewModel: SignUpViewModel
    lateinit var parent: ScrollView
    lateinit var name : EditText
    lateinit var email : EditText
    lateinit var ccp : CountryCodePicker
    lateinit var phone : EditText
    lateinit var toggleButton1 : ImageButton
    lateinit var password : EditText
    lateinit var toggleButton2 : ImageButton
    lateinit var confirmPassword : EditText
    lateinit var radioGroup: RadioGroup
    lateinit var signUp : Button
    lateinit var progress : ProgressBar
    var isPasswordVisible = false
    var isConfirmPasswordVisible = false
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
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        parent = findViewById(R.id.main)
        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        ccp = findViewById(R.id.ccp)
        phone = findViewById(R.id.phone)
        toggleButton1 = findViewById(R.id.toggle_button1)
        password = findViewById(R.id.password)
        toggleButton2 = findViewById(R.id.toggle_button2)
        confirmPassword = findViewById(R.id.confirm_password)
        radioGroup = findViewById(R.id.radio_group)
        signUp = findViewById(R.id.sign_up_button)
        progress = findViewById(R.id.progress)

        parent.setOnTouchListener { _, _ ->
            Utility.hideSoftKeyboard(this)
            false
        }

        viewModel.nameError.observe(this) {
            name.error = it
        }

        viewModel.emailError.observe(this) {
            email.error = it
        }

        viewModel.phoneError.observe(this) {
            phone.error = it
        }

        viewModel.passwordError.observe(this) {
            password.error = it
        }

        viewModel.confirmPasswordError.observe(this) {
            confirmPassword.error = it
        }

        viewModel.accountTypeError.observe(this) {
            if (it != null) {
                Utility.makeToast(this, it)
            }
        }
        toggleButton1.setOnClickListener {
            if (isPasswordVisible)
            {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton1.setImageResource(R.drawable.baseline_visibility_24)
                isPasswordVisible = false
            }
            else
            {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton1.setImageResource(R.drawable.baseline_visibility_off_24)
                isPasswordVisible = true
            }
        }
        toggleButton2.setOnClickListener {
            if (isConfirmPasswordVisible)
            {
                confirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton2.setImageResource(R.drawable.baseline_visibility_24)
                isConfirmPasswordVisible = false
            }
            else{
                confirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton2.setImageResource(R.drawable.baseline_visibility_off_24)
                isConfirmPasswordVisible = true
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
            if (viewModel.validate(name.text.toString(),email.text.toString(),"${phone.text}",password.text.toString(),confirmPassword.text.toString(),accountType != ""))
            {
                viewModel.setUser(
                    name.text.toString(),
                    email.text.toString(),
                    "${ccp.selectedCountryCodeWithPlus}${phone.text}",
                    password.text.toString(),
                    confirmPassword.text.toString(),
                    accountType
                ).also {
                    OTPBottomSheet("${ccp.selectedCountryCodeWithPlus}${phone.text}"){
                        viewModel.signUp({
                            Utility.makeToast(this, "Verification email sent").also {
                                Intent(this, SignInActivity::class.java).also {
                                    startActivity(it)
                                }
                            }
                        },{
                            Utility.makeToast(this, it.message.toString())
                        })
                    }.show(supportFragmentManager,"OTP")
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

    fun setProgress(inProgress : Boolean)
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
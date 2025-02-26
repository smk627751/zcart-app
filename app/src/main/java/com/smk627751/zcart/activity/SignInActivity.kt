package com.smk627751.zcart.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.NestedScrollingParent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.receiver.InternetStateChangeReceiver
import com.smk627751.zcart.viewmodel.SignInViewModel


class SignInActivity : AppCompatActivity() {
    lateinit var viewModel: SignInViewModel
    lateinit var parent: ViewGroup
    lateinit var emailLayout : TextInputLayout
    lateinit var email : EditText
    lateinit var passwordLayout : TextInputLayout
    lateinit var password : EditText
    lateinit var forgotPassword : Button
    lateinit var container : LinearLayout
    lateinit var signIn : Button
    lateinit var progress : ProgressBar
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Utility.registerInternetReceiver(this)
        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]
        viewModel.setSession(this){
                goToHome()
        }
        parent = findViewById(R.id.main)
        emailLayout = findViewById(R.id.email_layout)
        email = findViewById(R.id.email)
        passwordLayout = findViewById(R.id.password_layout)
        password = findViewById(R.id.password)
        forgotPassword = findViewById(R.id.forgot_password)
        container = findViewById(R.id.container)
        signIn = findViewById(R.id.sign_in_button)
        progress = findViewById(R.id.progress)

        email.addTextChangedListener {
            viewModel.setEmail(it.toString())
        }
        password.addTextChangedListener {
            viewModel.setPassword(it.toString())
        }
        viewModel.emailError.observe(this) {
            emailLayout.error = it
        }
        viewModel.passwordError.observe(this) {
            passwordLayout.error = it
        }
        parent.setOnTouchListener { _, _ ->
            email.clearFocus()
            password.clearFocus()
            Utility.hideSoftKeyboard(this)
            false
        }
        forgotPassword.setOnClickListener {
            forgotPassword.isEnabled = false
            if (email.text.isEmpty() || email.text.isBlank())
            {
                forgotPassword.isEnabled = true
                emailLayout.error = "Email is required"
                return@setOnClickListener
            }
            viewModel.forgotPassword(email.text.toString()){
                forgotPassword.isEnabled = true
                Utility.makeToast(this, "Email sent")
            }
        }
        signIn.setOnClickListener {
            if (!viewModel.validate())
                return@setOnClickListener
            setProgress(true)
            emailLayout.error = null
            passwordLayout.error = null
            viewModel.signIn(
                {
                    setProgress(false)
                    goToHome()
                },
                {
                    setProgress(false)
                    if (it.message == "No Internet connection")
                    {
                        Utility.makeSnackBar(parent, "No Internet connection"){
                            signIn.performClick()
                        }
                    }
                    else
                    {
                        passwordLayout.error = it.message
                    }
                }
            )
        }
        val signUp : TextView = findViewById(R.id.sign_up)
        signUp.setOnClickListener{
            Intent(this, SignUpActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            signIn.visibility = View.GONE
            progress.visibility = View.VISIBLE
        }
        else
        {
            signIn.visibility = View.VISIBLE
            progress.visibility = View.GONE
        }
    }
    private fun goToHome()
    {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}
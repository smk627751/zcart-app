package com.smk627751.zcart.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.NestedScrollingParent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.receiver.InternetStateChangeReceiver
import com.smk627751.zcart.viewmodel.SignInViewModel


class SignInActivity : AppCompatActivity() {
    lateinit var viewModel: SignInViewModel
    lateinit var parent: RelativeLayout
    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var toggleButton : ImageButton
    lateinit var forgotPassword : Button
    lateinit var signIn : Button
    lateinit var progress : ProgressBar
    var isPasswordVisible : Boolean = false
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
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        toggleButton = findViewById(R.id.toggle_button)
        forgotPassword = findViewById(R.id.forgot_password)
        signIn = findViewById(R.id.sign_in_button)
        progress = findViewById(R.id.progress)

        parent.setOnTouchListener { _, _ ->
            Utility.hideSoftKeyboard(this)
            false
        }
        toggleButton.setOnClickListener {
            if (isPasswordVisible)
            {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.baseline_visibility_24)
                isPasswordVisible = false
            }
            else
            {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.setImageResource(R.drawable.baseline_visibility_off_24)
                isPasswordVisible = true
            }
        }
        forgotPassword.setOnClickListener {
            forgotPassword.isEnabled = false
            if (email.text.isEmpty())
            {
                forgotPassword.isEnabled = true
                email.error = "Email is required"
                return@setOnClickListener
            }
            viewModel.forgotPassword(email.text.toString()){
                forgotPassword.isEnabled = true
                Utility.makeToast(this, "Email sent")
            }
        }
        signIn.setOnClickListener {
            if (email.text.isEmpty() || password.text.isEmpty())
                return@setOnClickListener
            setProgress(true)
            viewModel.signIn(email.text.toString(),password.text.toString(),
                {
                    setProgress(false)
                    goToHome()
                },
                {
                    setProgress(false)
                    Utility.makeToast(this, it.message.toString())
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
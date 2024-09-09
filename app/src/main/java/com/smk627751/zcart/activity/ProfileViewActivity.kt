package com.smk627751.zcart.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.appbar.MaterialToolbar
import com.smk627751.zcart.R
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.fragments.ProfileViewFragment
import com.smk627751.zcart.viewmodel.ProfileViewModel

class ProfileViewActivity : AppCompatActivity() {
    lateinit var viewModel : ProfileViewModel
    lateinit var parent : ViewGroup
    lateinit var toolbar: MaterialToolbar
    lateinit var imageView : ImageView
    lateinit var name : TextView
    lateinit var productTitle : TextView
    lateinit var productList : RecyclerView
    lateinit var email : TextView
    lateinit var address : TextView
    lateinit var zipcode : TextView
    lateinit var phone : TextView
    lateinit var user: User
    lateinit var resetPassword : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = intent
        val user = intent.getSerializableExtra("user") as User
        parent = findViewById(R.id.main)
        toolbar = findViewById(R.id.toolbar)
        imageView = findViewById(R.id.profile_image)
        name = findViewById(R.id.profile_name)
        email = findViewById(R.id.profile_email)
        phone = findViewById(R.id.profile_phone_field)
        productTitle = findViewById(R.id.my_products_text)
        productList = findViewById(R.id.product_list)
        address = findViewById(R.id.profile_address_field)
        zipcode = findViewById(R.id.profile_zipcode_field)
        resetPassword = findViewById(R.id.reset_password)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        viewModel.setUserData(user)
        viewModel.user.observe(this) {
            setUpProfile(it)
        }
    }
    private fun setUpProfile(user : User)
    {
        this.user = user
        Glide.with(this)
            .load(user.image)
            .placeholder(R.drawable.baseline_person_24)
            .transform(CircleCrop())
            .error(R.drawable.baseline_person_24)
            .into(imageView)
            .clearOnDetach()
        name.text = user.name
        address.text = user.address
        zipcode.text = user.zipcode.toString()
        phone.text = user.phone
        email.visibility = View.GONE
        if (!viewModel.isOwnProfile(user.email))
        {
            toolbar.menu.clear()
            toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
            toolbar.setNavigationOnClickListener {
                finish()
            }
            toolbar.title = user.email
            resetPassword.visibility = ViewGroup.GONE
        }
    }
}
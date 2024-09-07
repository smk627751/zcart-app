package com.smk627751.zcart.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.appbar.MaterialToolbar
import com.hbb20.CountryCodePicker
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.Utility.hideSoftKeyboard
import com.smk627751.zcart.bottomsheet.OTPBottomSheet
import com.smk627751.zcart.dto.Customer
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.dto.Vendor
import com.smk627751.zcart.viewmodel.ProfileViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class ProfileEditActivity : AppCompatActivity() {
    lateinit var viewModel: ProfileViewModel
    lateinit var parent : ViewGroup
    lateinit var toolbar: MaterialToolbar
    lateinit var imageView : ImageView
    lateinit var nameField : EditText
    lateinit var address : TextView
    lateinit var zipcode : TextView
    lateinit var ccp : CountryCodePicker
    lateinit var phone : TextView
    lateinit var progress: ProgressBar
    lateinit var saveButton : Button
    lateinit var user: User
    lateinit var prevPhone : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_edit_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Utility.registerInternetReceiver(this)
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialize views
        parent = findViewById(R.id.main)
        toolbar = findViewById(R.id.toolbar)
        imageView = findViewById(R.id.profile_image)
        nameField = findViewById(R.id.profile_name_et)
        ccp = findViewById(R.id.ccp)
        phone = findViewById(R.id.phone_field)
        address = findViewById(R.id.profile_address_field)
        zipcode = findViewById(R.id.profile_zipcode_field)
        saveButton = findViewById(R.id.save_button)
        progress = findViewById(R.id.progress)

        parent.setOnClickListener {
            nameField.clearFocus()
            hideSoftKeyboard(this)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        // Set click listener for image view
        imageView.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                    Build.VERSION_CODES.R) >= 2) {
                Intent(MediaStore.ACTION_PICK_IMAGES)
            }
            else Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        // Observe user data
        viewModel.user.observe(this) {user ->
            setUpProfile(user)
        }
        viewModel.nameError.observe(this) {
            nameField.error = it
        }
        viewModel.phoneError.observe(this) {
            phone.error = it
        }
        viewModel.addressError.observe(this) {
            address.error = it
        }
        viewModel.zipcodeError.observe(this) {
            zipcode.error = it
        }
        // Set click listener for save button
        saveButton.setOnClickListener {
            Log.e("phone", "${ccp.selectedCountryCodeWithPlus}${phone.text}")
            Log.e("prev", prevPhone)
            if (!viewModel.validate(nameField.text.toString(), address.text.toString(), zipcode.text.toString(), phone.text.toString()))
            {
                return@setOnClickListener
            }
            if (prevPhone != "${ccp.selectedCountryCodeWithPlus}${phone.text}")
            {
                OTPBottomSheet("${ccp.selectedCountryCodeWithPlus}${phone.text}"){
                    updateProfile(user)
                }.show(supportFragmentManager, "OTP")
            }
            else
            {
                updateProfile(user)
            }
        }
    }
    private fun updateProfile(user: User)
    {
        setProgress(true)
        viewModel.uploadImage(imageView.tag?.let { it as Uri }){
            val imageUrl = it.ifEmpty { user.image }
            when(user) {
                is Vendor -> Vendor(
                    nameField.text.toString(),
                    user.email,
                    "${ccp.selectedCountryCodeWithPlus}${phone.text}",
                    user.accountType, address.text.toString(),
                    zipcode.text.toString().toInt(),
                    imageUrl
                ).apply {
                    products = user.products
                    orders = user.orders
                }

                is Customer -> Customer(
                    nameField.text.toString(),
                    user.email,
                    "${ccp.selectedCountryCodeWithPlus}${phone.text}",
                    user.accountType, address.text.toString(),
                    zipcode.text.toString().toInt(),
                    imageUrl
                ).apply {
                    cartItems = user.cartItems
                    myOrders = user.myOrders
                }

                else -> null
            }?.let { user ->
                viewModel.updateUserData(user){
                    setProgress(false)
                    Utility.makeToast(this,"Updated successfully")
                    finish()
                }
            }
        }
    }
    fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            saveButton.visibility = View.GONE
            progress.visibility = View.VISIBLE
        }
        else
        {
            saveButton.visibility = View.VISIBLE
            progress.visibility = View.GONE
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
        nameField.setText(user.name)
        address.text = user.address
        zipcode.text = user.zipcode.toString()
        phone.text = user.phone.replace(ccp.selectedCountryCodeWithPlus, "")
        prevPhone = user.phone
    }
    // Function to handle result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            UCrop.of(data?.data!!, Uri.fromFile(File(cacheDir, "${UUID.randomUUID()}_cropped.jpg")))
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .start(this)
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            UCrop.getOutput(data!!).also {
                imageView.setImageURI(it)
                imageView.tag = it
            }
        }
    }
}
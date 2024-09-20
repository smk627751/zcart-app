package com.smk627751.zcart.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.Utility.hideSoftKeyboard
import com.smk627751.zcart.Utility.saveBitmapToFile
import com.smk627751.zcart.bottomsheet.ImageOptionDialogFragment
import com.smk627751.zcart.bottomsheet.OTPBottomSheet
import com.smk627751.zcart.dto.Customer
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.dto.Vendor
import com.smk627751.zcart.viewmodel.ProfileViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProfileEditActivity : AppCompatActivity() {
    lateinit var viewModel: ProfileViewModel
    lateinit var parent : ViewGroup
    lateinit var toolbar: MaterialToolbar
    lateinit var scrollView: NestedScrollView
    lateinit var imageView : ImageView
    lateinit var nameLayout : TextInputLayout
    lateinit var nameField : EditText
    lateinit var addressLayout : TextInputLayout
    lateinit var address : EditText
    lateinit var zipcodeLayout : TextInputLayout
    lateinit var zipcode : EditText
    lateinit var ccp : CountryCodePicker
    lateinit var phoneLayout : TextInputLayout
    lateinit var phone : EditText
    lateinit var progress: ProgressBar
    lateinit var saveButton : Button
    lateinit var user: User
    lateinit var prevPhone : String
    lateinit var bottomSheet : ImageOptionDialogFragment
    @SuppressLint("ClickableViewAccessibility")
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
        scrollView = findViewById(R.id.scroll_view)
        imageView = findViewById(R.id.profile_image)
        nameLayout = findViewById(R.id.name_layout)
        nameField = findViewById(R.id.profile_name_et)
        ccp = findViewById(R.id.ccp)
        addressLayout = findViewById(R.id.address_layout)
        zipcodeLayout = findViewById(R.id.zipcode_layout)
        phoneLayout = findViewById(R.id.phone_input_layout)
        phone = findViewById(R.id.phone_field)
        address = findViewById(R.id.profile_address_field)
        zipcode = findViewById(R.id.profile_zipcode_field)
        saveButton = findViewById(R.id.save_button)
        progress = findViewById(R.id.progress)

        scrollView.setOnTouchListener { _, _ ->
            nameField.clearFocus()
            address.clearFocus()
            zipcode.clearFocus()
            phone.clearFocus()
            hideSoftKeyboard(this)
            false
        }
        // Set click listener for toolbar
        toolbar.setNavigationOnClickListener {
            hideSoftKeyboard(this)
            onBackPressed()
        }
        bottomSheet = ImageOptionDialogFragment.newInstance{option ->
            when(option)
            {
                "camera" -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                        startActivityForResult(it, 2)
                    }
                }
                "gallery" -> {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                            Build.VERSION_CODES.R) >= 2) {
                        Intent(MediaStore.ACTION_PICK_IMAGES)
                    }
                    else Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 1)
                }
                "delete" -> {
                    imageView.setImageResource(R.drawable.baseline_person_24)
                    imageView.tag = null
                    user.image = ""
                }
            }
        }
        // Set click listener for image view
        imageView.setOnClickListener {
            bottomSheet.show(supportFragmentManager, "image")
        }
        // Observe user data
        viewModel.user.observe(this) {user ->
            setUpProfile(user)
        }
        viewModel.nameError.observe(this) {
            nameLayout.error = it
        }
        viewModel.phoneError.observe(this) {
            phoneLayout.error = it
        }
        viewModel.addressError.observe(this) {
            addressLayout.error = it
        }
        viewModel.zipcodeError.observe(this) {
            zipcodeLayout.error = it
        }
        // Set click listener for save button
        saveButton.setOnClickListener {
            if (!viewModel.validate(nameField.text.toString(), address.text.toString(), zipcode.text.toString(), phone.text.toString()))
            {
                return@setOnClickListener
            }
//            if (prevPhone != "${ccp.selectedCountryCodeWithPlus}${phone.text}")
//            {
//                OTPBottomSheet("${ccp.selectedCountryCodeWithPlus}${phone.text}"){
//                    updateProfile(user)
//                }.show(supportFragmentManager, "OTP")
//            }
//            else
//            {
//                updateProfile(user)
//            }
            updateProfile(user)
        }
    }
    private fun updateProfile(user: User)
    {
        setProgress(true)
        viewModel.uploadImage(imageView.tag?.let { it as Uri }){
            val imageUrl = it.ifEmpty { user.image }
            when(user) {
                is Vendor -> Vendor(
                    nameField.text.toString().trim(),
                    user.email.trim(),
                    "${ccp.selectedCountryCodeWithPlus}${phone.text}",
                    user.accountType,
                    address.text.toString().trim(),
                    zipcode.text.toString().toInt(),
                    imageUrl
                ).apply {
                    products = user.products
                    orders = user.orders
                }

                is Customer -> Customer(
                    nameField.text.toString(),
                    user.email.trim(),
                    "${ccp.selectedCountryCodeWithPlus}${phone.text}",
                    user.accountType,
                    address.text.toString().trim(),
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
    private fun setProgress(inProgress : Boolean)
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
    @SuppressLint("ClickableViewAccessibility")
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
        address.setText(user.address)
        zipcode.setText(user.zipcode.toString())
        ccp.setCountryForPhoneCode(user.phone.substring(0,user.phone.length - 10).toInt())
        phone.setText(user.phone.substring(user.phone.length - 10))
        prevPhone = user.phone
        address.setOnTouchListener { v, event ->
            if (v.canScrollVertically(1) || v.canScrollVertically(-1)) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                if (event.action == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }
    // Function to handle result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            UCrop.of(data?.data!!, Uri.fromFile(File(cacheDir, "${UUID.randomUUID()}_cropped.jpg")))
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1000, 1000)
                .start(this)
        }
        if (requestCode == 2 && resultCode == RESULT_OK)
        {
            val bitmap : Bitmap = data?.extras?.get("data") as Bitmap
            val imageUri = saveBitmapToFile(this,bitmap)
            UCrop.of(imageUri, Uri.fromFile(File(cacheDir, "${UUID.randomUUID()}_cropped.jpg")))
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .start(this)
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            UCrop.getOutput(data!!).also {
                imageView.setImageURI(it).also {
                    imageView.invalidate()
                    imageView.requestLayout()
                }
                imageView.tag = it
            }
        }
    }
}
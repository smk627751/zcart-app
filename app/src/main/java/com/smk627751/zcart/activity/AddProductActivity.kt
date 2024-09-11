package com.smk627751.zcart.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.R.id.design_bottom_sheet
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smk627751.zcart.R
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.Utility
import com.smk627751.zcart.Utility.saveBitmapToFile
import com.smk627751.zcart.dto.Product
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.Serializable
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var parent : ViewGroup
    private lateinit var toolbar: MaterialToolbar
    private lateinit var productName: EditText
    private lateinit var chipGroup: ChipGroup
    private var categoryLayout: LinearLayout? = null
    private lateinit var productPrice: EditText
    private lateinit var productDescription: EditText
    private lateinit var addProductBtn: Button
    lateinit var progress: ProgressBar
    var filteredList = Repository.category
    private val selectedItems = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_product_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        parent = findViewById(R.id.main)
        toolbar = findViewById(R.id.toolbar)
        imageView = findViewById(R.id.product_image)
        productName = findViewById(R.id.product_name)
        chipGroup = findViewById(R.id.chip_group)
        productPrice = findViewById(R.id.product_price)
        productDescription = findViewById(R.id.product_description)
        addProductBtn = findViewById(R.id.add_product_button)!!
        progress = findViewById(R.id.progress)
        val intent = intent
        var product = intent.getSerializableExtra("product") as Product?
        var mode = intent.getStringExtra("mode")
        if (savedInstanceState != null) {
            imageView.tag = savedInstanceState.getParcelable("imageUri")
            if (imageView.tag != null)
            {
                imageView.setImageURI(imageView.tag as Uri)
            }
            selectedItems.addAll(savedInstanceState.getStringArrayList("selectedItems") ?: emptyList())
            selectedItems.forEach {
                createChip(it)
            }
            product = savedInstanceState.getSerializable("product") as Product?
            mode = savedInstanceState.getString("mode")
        }
        when(mode)
        {
            "add" -> {
                toolbar.title = "Add Product"
            }
            "edit" -> {
                toolbar.title = "Edit Product"
            }
        }
        toolbar.setNavigationOnClickListener {
            if (validate())
            {
                Utility.showAlertDialog(this,"Are you sure you want to close?"){
                    finish()
                    overridePendingTransition(R.anim.fade_out,R.anim.slide_down)
                }
            }
            else {
                finish()
                overridePendingTransition(R.anim.fade_out,R.anim.slide_down)
            }
        }
        imageView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                    Build.VERSION_CODES.R) >= 2) {
                Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                    type = "image/*"
                    startActivityForResult(this,1)
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
                }
                else
                {
                    Intent(Intent.ACTION_PICK).apply {
                        type = "image/*"
                        startActivityForResult(this,1)
                    }
                }
            }
        }
        if (product != null)
        {
            Glide.with(this)
                .load(product.image)
                .placeholder(R.drawable.baseline_image_24)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageView.setImageResource(R.drawable.baseline_image_24)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource != null) {
                            val uri = saveBitmapToFile(applicationContext,resource.toBitmap())
                            imageView.tag = uri
                            imageView.setImageURI(uri)
                            imageView.requestLayout()
                            return true
                        }
                        return false
                    }

                })
                .into(imageView)
            productName.setText(product.name)
            productPrice.setText(product.price.toString())
            productDescription.setText(product.description)
            selectedItems.addAll(product.category)
            product.category.forEach {
                createChip(it)
            }
            if (mode == "edit")
            {
                addProductBtn.text = "Update Product"
            }
        }
        chipGroup.setOnClickListener{
            categoryLayout?.parent?.let { (it as ViewGroup).removeView(categoryLayout) }
            loadCategoryLayout()
            MaterialAlertDialogBuilder(this)
                .setTitle("Select Categories")
                .setView(categoryLayout)
                .setPositiveButton("OK") { _, _ ->
                    chipGroup.removeAllViews()
                    selectedItems.forEach {
                        createChip(it)
                    }
                    categoryLayout = null
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
                .show()
        }
        addProductBtn.setOnClickListener {
            if (!validate()) return@setOnClickListener
            if (imageView.tag == null && (product?.image?.isEmpty() == true || mode == "add"))
            {
                Utility.makeToast(this,"Please select an image")
                return@setOnClickListener
            }
            val productId = product?.id ?: UUID.randomUUID().toString()
            val imageUri = imageView.tag as Uri
            setProgress(true)
            Repository.uploadImage("${Repository.IMAGE_PATH}/${productId}.jpg", imageUri){ imageUrl ->
                if (mode != null) {
                    addProduct(product,mode,productId,imageUrl) { msg ->
                        setProgress(false)
                        Utility.makeToast(this, msg)
                        finish()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("imageUri", imageView.tag as Uri?)
        outState.putStringArrayList("selectedItems", ArrayList(selectedItems))
        outState.putSerializable("product", intent.getSerializableExtra("product"))
        outState.putString("mode", intent.getStringExtra("mode"))
    }
    private fun validate() : Boolean
    {
        if (productName.text.isEmpty() || productName.text.toString().isBlank())
        {
            productName.error = "Enter product name"
            return false
        }
        if (productPrice.text.isEmpty() || productPrice.text.toString().isBlank())
        {
            productPrice.error = "Enter product price"
            return false
        }
        if (productDescription.text.isEmpty() || productDescription.text.toString().isBlank())
        {
            productDescription.error = "Enter product description"
            return false
        }
        return true
    }
    private fun loadCategoryLayout()
    {
        categoryLayout = View.inflate(this, R.layout.category_layout, null) as LinearLayout
        val chipGroup = categoryLayout?.findViewById<ChipGroup>(R.id.chip_group)
        val searchView = categoryLayout?.findViewById<SearchView>(R.id.search_view)
        val listView = categoryLayout?.findViewById<ListView>(R.id.list_view)
        selectedItems.forEach {
            val chip = View.inflate(this, R.layout.entry_chip, null) as Chip
            chip.text = it
            chip.setOnCloseIconClickListener { _ ->
                chipGroup?.removeView(chip)
                selectedItems.remove(it)
            }
            chipGroup?.addView(chip)
        }
        listView?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, Repository.category)
        listView?.setOnItemClickListener { _, _, position, _ ->
            if (!selectedItems.contains(filteredList[position]))
            {
                val chip = View.inflate(this, R.layout.entry_chip, null) as Chip
                chip.text = filteredList[position]
                chip.setOnCloseIconClickListener { _ ->
                    chipGroup?.removeView(chip)
                    selectedItems.remove(filteredList[position])
                }
                chipGroup?.addView(chip)
                selectedItems.add(filteredList[position])
                chipGroup?.post {
                    chipGroup.parent?.requestChildFocus(chipGroup, chip)
                }
            }
        }
        searchView?.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                updateAdapter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                updateAdapter(newText ?: "")
                return true
            }
            fun updateAdapter(text : String)
            {
                filteredList = Repository.category.filter { it.contains(text, true) }
                listView?.adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, filteredList)
            }
        })
    }
    private fun createChip(text : String)
    {
        val chip = View.inflate(this, R.layout.entry_chip, null) as Chip
        chip.text = text
        chip.setOnCloseIconClickListener { _ ->
            chipGroup.removeView(chip)
            selectedItems.remove(text)
        }
        chipGroup.addView(chip)
    }
    private fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            addProductBtn.visibility = View.GONE
            progress.visibility = View.VISIBLE
        }
        else
        {
            addProductBtn.visibility = View.VISIBLE
            progress.visibility = View.GONE
        }
    }
    private fun addProduct(product: Product?,mode: String, productId : String, imageUrl : String, callBack : (msg : String) -> Unit)
    {
        when(mode)
        {
            "add" -> {
                Repository.addProductToDb(
                    Product(
                        productId,
                        Repository.currentUserId,
                        imageUrl,
                        productName.text.toString(),
                        productPrice.text.toString().toLong(),
                        productDescription.text.toString(),
                        selectedItems,
                        mutableMapOf()
                    )
                ){
                    callBack("Product Added")
                }
            }
            "edit" -> {
                if (product != null) {
                    Repository.updateProductToDb(
                        Product(
                        product.id,
                        Repository.currentUserId,
                        imageUrl.ifEmpty { product.image },
                        productName.text.toString(),
                        productPrice.text.toString().toLong(),
                        productDescription.text.toString(),
                        selectedItems,
                        product.reviews
                    )
                    ){
                        callBack("Product Updated")
                    }
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
                UCrop.of(data?.data!!, Uri.fromFile(
                    File(
                    cacheDir,
                    "${UUID.randomUUID()}_cropped.jpg"
                )
                ))
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                    .start(this)
            }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            imageView.setImageURI(resultUri)
            imageView.tag = resultUri
            imageView.requestLayout()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.fade_out,R.anim.slide_down)
    }
}
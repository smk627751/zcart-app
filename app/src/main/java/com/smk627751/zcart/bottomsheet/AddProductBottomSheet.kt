package com.smk627751.zcart.bottomsheet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.LayoutInflater
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
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smk627751.zcart.R
import com.google.android.material.R.id.design_bottom_sheet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.Utility
import com.smk627751.zcart.dto.Product
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class AddProductBottomSheet(context: Context, val product: Product? = null, private val mode: String = "add") : BottomSheetDialogFragment() {
    lateinit var imageView: ImageView
    lateinit var parent : ViewGroup
    private lateinit var productName: EditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var categoryField: HorizontalScrollView
    private var categoryLayout: LinearLayout? = null
    private lateinit var productPrice: EditText
    private lateinit var productDescription: EditText
    private lateinit var addProductBtn: Button
    lateinit var progress: ProgressBar
    private lateinit var closeBtn: Button
    var filteredList = Repository.category
    private val selectedItems = mutableListOf<String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_product_view,container,false)
        parent = view.findViewById(R.id.main)!!
        imageView = view.findViewById(R.id.product_image)!!
        productName = view.findViewById(R.id.product_name)!!
        categoryField = view.findViewById(R.id.category_field)!!
        chipGroup = view.findViewById(R.id.chip_group)!!
        productPrice = view.findViewById(R.id.product_price)!!
        productDescription = view.findViewById(R.id.product_description)!!
        addProductBtn = view.findViewById(R.id.add_product_button)!!
        progress = view.findViewById(R.id.progress)!!
        closeBtn = view.findViewById(R.id.close_button)!!

        closeBtn.setOnClickListener {
            context?.let { it1 ->
                Utility.showAlertDialog(it1,"Are you sure you want to close?"){
                    dismiss()
                }
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
                if (context?.let { it1 -> ContextCompat.checkSelfPermission(it1, Manifest.permission.MANAGE_EXTERNAL_STORAGE) }
                    != PackageManager.PERMISSION_GRANTED) {
                    activity?.let { it1 ->
                        ActivityCompat.requestPermissions(
                            it1,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
                    }
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
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                categoryLayout?.parent?.let { (it as ViewGroup).removeView(categoryLayout) }
                loadCategoryLayout()
                context?.let {
                    MaterialAlertDialogBuilder(it)
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
                }?.show()
                return true
            }
        })
        categoryField.setOnTouchListener {_,event ->
            gestureDetector.onTouchEvent(event)
        }
        addProductBtn.setOnClickListener {
            if (!validate()) return@setOnClickListener
            val productId = product?.id ?: UUID.randomUUID().toString()
            val imageUri = if(imageView.tag != null) imageView.tag as Uri else null
            setProgress(true)
            Repository.uploadImage("${Repository.IMAGE_PATH}/${productId}.jpg", imageUri){ imageUrl ->
                addProduct(mode,productId,imageUrl) { msg ->
                    setProgress(false)
                    context?.let { it1 -> Utility.makeToast(it1, msg) }
                    dismiss()
                    activity?.finish()
                }
            }
        }
        setupFullHeight()
        return view
    }
    private fun setupFullHeight() {
        val bottomSheet = view?.findViewById<View>(design_bottom_sheet)
        val behavior = (dialog as BottomSheetDialog).behavior
        if (bottomSheet != null) {
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
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
        categoryLayout = View.inflate(context, R.layout.category_layout, null) as LinearLayout
        val chipGroup = categoryLayout?.findViewById<ChipGroup>(R.id.chip_group)
        val searchView = categoryLayout?.findViewById<SearchView>(R.id.search_view)
        val listView = categoryLayout?.findViewById<ListView>(R.id.list_view)
        selectedItems.forEach {
            val chip = View.inflate(context, R.layout.entry_chip, null) as Chip
            chip.text = it
            chip.setOnCloseIconClickListener { _ ->
                chipGroup?.removeView(chip)
                selectedItems.remove(it)
            }
            chipGroup?.addView(chip)
        }
        listView?.adapter = context?.let {ArrayAdapter(it, android.R.layout.simple_list_item_1, Repository.category)}
        listView?.setOnItemClickListener { _, _, position, _ ->
            if (!selectedItems.contains(filteredList[position]))
            {
                val chip = View.inflate(context, R.layout.entry_chip, null) as Chip
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
        searchView?.setOnQueryTextListener(object : OnQueryTextListener{
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
                listView?.adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, filteredList)}
            }
        })
    }
    private fun createChip(text : String)
    {
        val chip = View.inflate(context, R.layout.entry_chip, null) as Chip
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
    private fun addProduct(mode: String, productId : String, imageUrl : String, callBack : (msg : String) -> Unit)
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
                        productPrice.text.toString().toInt(),
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
                    Repository.updateProductToDb(Product(
                        product.id,
                        Repository.currentUserId,
                        imageUrl.ifEmpty { product.image },
                        productName.text.toString(),
                        productPrice.text.toString().toInt(),
                        productDescription.text.toString(),
                        selectedItems,
                        product.reviews
                    )){
                        callBack("Product Updated")
                    }
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            activity?.let {
                UCrop.of(data?.data!!, Uri.fromFile(File(
                    it.cacheDir,
                    "${UUID.randomUUID()}_cropped.jpg"
                )))
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                    .start(it)
            }
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)

            imageView.setImageURI(resultUri)
            imageView.tag = resultUri
        }
    }
}
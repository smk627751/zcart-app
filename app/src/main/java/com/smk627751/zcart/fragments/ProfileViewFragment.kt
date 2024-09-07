package com.smk627751.zcart.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smk627751.zcart.R
import com.smk627751.zcart.activity.ProfileEditActivity
import com.smk627751.zcart.activity.SignInActivity
import com.smk627751.zcart.adapter.ProductViewAdapter
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.viewmodel.ProfileViewModel

class ProfileViewFragment : Fragment() {
    lateinit var viewModel: ProfileViewModel
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.profile_view, container, false)
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialize views
        parent = view.findViewById(R.id.main)
        toolbar = view.findViewById(R.id.toolbar)
        imageView = view.findViewById(R.id.profile_image)
        name = view.findViewById(R.id.profile_name)
        email = view.findViewById(R.id.profile_email)
        phone = view.findViewById(R.id.profile_phone_field)
        productTitle = view.findViewById(R.id.my_products_text)
        productList = view.findViewById(R.id.product_list)
        address = view.findViewById(R.id.profile_address_field)
        zipcode = view.findViewById(R.id.profile_zipcode_field)

        toolbar.setOnMenuItemClickListener {
            when(it.itemId)
            {
                R.id.menu_edit -> {
                    Intent(context, ProfileEditActivity::class.java).also {
                        startActivity(it)
                    }
                    true
                }
                R.id.menu_logout -> {
                    showAlertDialog()
                    true
                }

                else -> false
            }
        }
        viewModel.user.observe(viewLifecycleOwner) {user ->
            setUpProfile(user)
        }
        // Observe isVendor
        viewModel.isVendor.observe(viewLifecycleOwner) { isVendor ->
            if (isVendor)
            {
                setUpCatalog()
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserData()
        viewModel.checkIfVendor()
    }
    // Function to set up profile
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
        email.text = user.email
        address.text = user.address
        zipcode.text = user.zipcode.toString()
        phone.text = user.phone
    }
    // Function to set up catalog
    private fun setUpCatalog()
    {
        productTitle.visibility = View.VISIBLE
        productList.visibility = View.VISIBLE
        productList.adapter = ProductViewAdapter(viewModel.getMyProducts(),(200 * resources.displayMetrics.density).toInt()).also {
            it.startListening()
        }
        productList.layoutManager = LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
    }
    // Function to show alert dialog
    private fun showAlertDialog()
    {
         val context = requireContext()
            MaterialAlertDialogBuilder(context)
                .setTitle("Are you sure want to Logout?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.logout{
                        Intent(context, SignInActivity::class.java).also {
                            startActivity(it)
                            activity?.finish()
                        }
                    }
                }
                .setNegativeButton("No") { _, _ ->

                }
                .create()
                .show()
    }
}

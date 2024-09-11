package com.smk627751.zcart.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
import com.smk627751.zcart.R
import com.smk627751.zcart.Repository.Repository
import com.smk627751.zcart.Utility
import com.smk627751.zcart.dto.Notification
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.dto.Review
import com.smk627751.zcart.fragments.CartViewFragment
import com.smk627751.zcart.fragments.NotificationViewFragment
import com.smk627751.zcart.fragments.OrdersViewFragment
import com.smk627751.zcart.fragments.ProductViewFragment
import com.smk627751.zcart.fragments.ProfileViewFragment
import com.smk627751.zcart.viewmodel.HomeViewModel
import com.yalantis.ucrop.UCrop
import java.util.UUID

class HomeActivity : AppCompatActivity() {
    lateinit var fragmentContainer: FragmentContainerView
    var bottomNavigationView: BottomNavigationView? = null
    var navigationRailView: NavigationRailView? = null
    private lateinit var viewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        fragmentContainer = findViewById(R.id.fragment_container)
        for (i in 0 until 50) {
//            Repository.addProductToDb(
//                Product(
//                    "$i",
//                    "",
//                    "",
//                    "Dummy",
//                    60000,
//                    "",
//                    mutableListOf<String>(),
//                    mutableMapOf<String, Review>()
//                )
//            ){}
//            Repository.db.collection("products").document(i.toString()).delete()
//            Repository.db.collection("products").whereEqualTo("name","Dummy").get().addOnCompleteListener {
//                it.result.documents.forEach {
//                    it.reference.delete()
//                }
//            }
        }
//        Utility.registerInternetReceiver(this)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.setLandscape(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        when(val view: View = findViewById(R.id.navigation)){
            is BottomNavigationView -> bottomNavigationView = view
            is NavigationRailView -> navigationRailView = view
        }
        viewModel.notifications.observe(this) { notifications ->
            setBadge(notifications,viewModel.isLandscape.value!!)
        }
        viewModel.isVendor.observe(this) { isVendor ->
            setupMenu(isVendor,viewModel.isLandscape.value!!)
            setUpNavigation(isVendor,viewModel.isLandscape.value!!)
            if (supportFragmentManager.backStackEntryCount == 0) {
                setFragment(ProductViewFragment())
            }
        }

        viewModel.checkIfVendor()
    }

    private fun setBadge(notifications : List<Notification>,isLandscape: Boolean) {
        val view = if (isLandscape) navigationRailView else bottomNavigationView
        val badge = view?.getOrCreateBadge(viewModel.notificationsId)
        if (badge != null) {
            badge.isVisible = true
            badge.number = notifications.filter { !it.isRead }.size
            if (badge.number == 0)
                badge.isVisible = false
            badge.backgroundColor = ContextCompat.getColor(this, R.color.black)
            badge.badgeTextColor = ContextCompat.getColor(this, R.color.white)
            badge.maxNumber = 99
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewModel.setLandscape(true)
        } else {
            viewModel.setLandscape(false)
        }
        if (newConfig.isNightModeActive)
        {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
        else
        {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_navigation_color)
        }
    }
    private fun setUpNavigation(isVendor: Boolean, isLandscape: Boolean) {
        if (resources.configuration.isNightModeActive)
        {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
        else
        {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_navigation_color)
        }
        val view = if (isLandscape) navigationRailView else bottomNavigationView
        if (isVendor)
        {
            view?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    viewModel.homeId -> {
                        setFragment(ProductViewFragment())
                        true
                    }

                    viewModel.profileId -> {
                        setFragment(ProfileViewFragment())
                        true
                    }

                    viewModel.ordersId -> {
                        setFragment(OrdersViewFragment())
                        true
                    }

                    viewModel.addId -> {
//                        showAddProductBottomSheet()
                       Intent(this,AddProductActivity::class.java).apply {
                            putExtra("mode","add")
                            startActivity(this)
                           overridePendingTransition(R.anim.slide_up,R.anim.fade_in)
                        }
                        false
                    }

                    viewModel.notificationsId -> {
                        setFragment(NotificationViewFragment().apply {
                            this.setBadge = {
                                val view = if (isLandscape) navigationRailView else bottomNavigationView
                                val badge = view?.getOrCreateBadge(this@HomeActivity.viewModel.notificationsId)
                                if (badge != null && it > 0) {
                                    badge.isVisible = true
                                    badge.number = it
                                    badge.backgroundColor = ContextCompat.getColor(this@HomeActivity, R.color.black)
                                    badge.badgeTextColor = ContextCompat.getColor(this@HomeActivity, R.color.white)
                                    badge.maxNumber = 99
                                }
                            }
                        })
                        true
                    }

                    else -> false
                }
            }
        }
        else
        {
            view?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    viewModel.homeId -> {
                        setFragment(ProductViewFragment())
                        true
                    }

                    viewModel.profileId -> {
                        setFragment(ProfileViewFragment())
                        true
                    }

                    viewModel.ordersId -> {
                        setFragment(OrdersViewFragment())
                        true
                    }

                    viewModel.cartId -> {
                        setFragment(CartViewFragment())
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupMenu(isVendor: Boolean, isLandscape: Boolean) {
        val menu = if (isLandscape) navigationRailView?.menu else bottomNavigationView?.menu
        menu?.clear()
        if (isVendor) {
            addMenuItem(menu,viewModel.homeId,"Home",R.drawable.home_icon)
            addMenuItem(menu,viewModel.ordersId,"Orders",R.drawable.cart_icon)
            addMenuItem(menu,viewModel.addId,"Add",R.drawable.add_icon)
            addMenuItem(menu,viewModel.notificationsId,"Notification",R.drawable.notification_icon).also {
                viewModel.getNotifications()
            }
            addMenuItem(menu,viewModel.profileId,"Profile",R.drawable.profile_icon)

        } else {
            addMenuItem(menu,viewModel.homeId,"Home",R.drawable.home_icon)
            addMenuItem(menu,viewModel.cartId,"Cart",R.drawable.cart_icon)
            addMenuItem(menu,viewModel.profileId,"Profile",R.drawable.profile_icon)
        }
    }
    private fun addMenuItem(menu: Menu?, id: Int, title: String, iconRes: Int) {
        menu?.add(Menu.NONE, id, Menu.NONE, title)?.setIcon(iconRes)
    }

    private fun setFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null && currentFragment::class == fragment::class)
        {
            return
        }
        supportFragmentManager.beginTransaction().apply {
            addToBackStack(null)
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (currentFragment) {
            is ProductViewFragment -> bottomNavigationView?.selectedItemId = viewModel.homeId
            is OrdersViewFragment -> bottomNavigationView?.selectedItemId = viewModel.ordersId
            is ProfileViewFragment -> bottomNavigationView?.selectedItemId = viewModel.profileId
            is CartViewFragment -> bottomNavigationView?.selectedItemId = viewModel.cartId
            is NotificationViewFragment -> bottomNavigationView?.selectedItemId = viewModel.notificationsId
        }
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }
}

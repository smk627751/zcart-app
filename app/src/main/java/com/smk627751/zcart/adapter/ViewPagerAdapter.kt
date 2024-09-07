package com.smk627751.zcart.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smk627751.zcart.fragments.CartViewFragment
import com.smk627751.zcart.fragments.ProductViewFragment
import com.smk627751.zcart.fragments.ProfileViewFragment

class ViewPagerAdapter(context: FragmentActivity,val isVendor : Boolean = false) : FragmentStateAdapter(context) {
    override fun getItemCount(): Int {
        if(isVendor)
            return 2
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        if(isVendor)
        {
            return when (position) {
                0 -> ProductViewFragment()
                1 -> ProfileViewFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
        else
        {
            return when (position) {
                0 -> ProductViewFragment()
                1 -> CartViewFragment()
                2 -> ProfileViewFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }
}
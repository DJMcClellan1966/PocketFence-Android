package com.pocketfence.android.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pocketfence.android.ui.BlockedSitesFragment
import com.pocketfence.android.ui.DashboardFragment
import com.pocketfence.android.ui.DevicesFragment
import com.pocketfence.android.ui.TimeLimitsFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 4
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DashboardFragment()
            1 -> DevicesFragment()
            2 -> BlockedSitesFragment()
            3 -> TimeLimitsFragment()
            else -> DashboardFragment()
        }
    }
}

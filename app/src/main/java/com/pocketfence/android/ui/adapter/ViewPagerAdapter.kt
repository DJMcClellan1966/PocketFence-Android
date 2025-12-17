package com.pocketfence.android.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pocketfence.android.ui.BlockedSitesFragment
import com.pocketfence.android.ui.ChildDevicesFragment
import com.pocketfence.android.ui.DashboardFragment
import com.pocketfence.android.ui.DevicesFragment
import com.pocketfence.android.ui.TimeLimitsFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DashboardFragment()
            1 -> DevicesFragment()
            2 -> ChildDevicesFragment()
            3 -> BlockedSitesFragment()
            4 -> TimeLimitsFragment()
            else -> DashboardFragment()
        }
    }
}

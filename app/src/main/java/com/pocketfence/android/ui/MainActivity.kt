package com.pocketfence.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.pocketfence.android.R
import com.pocketfence.android.databinding.ActivityMainBinding
import com.pocketfence.android.monetization.AdManager
import com.pocketfence.android.ui.adapter.ViewPagerAdapter
import com.pocketfence.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity that hosts all fragments.
 * Uses ViewPager2 with TabLayout for navigation.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var adManager: AdManager
    
    private var tabChangeCount = 0
    private val TAB_CHANGES_BEFORE_AD = 5 // Show ad every 5 tab changes
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filter { !it.value }.keys
        
        if (deniedPermissions.isNotEmpty()) {
            val message = when {
                deniedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    "Location permission is needed to detect connected devices and monitor network activity. " +
                    "App functionality will be limited."
                }
                deniedPermissions.contains(Manifest.permission.POST_NOTIFICATIONS) && 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    "Notification permission is needed to keep you informed about protection status and blocked sites. " +
                    "You can still use the app, but you won't receive notifications."
                }
                else -> {
                    "Some permissions were denied. App functionality may be limited."
                }
            }
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        setupViewPager()
        requestPermissions()
    }
    
    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.title_dashboard)
                1 -> getString(R.string.title_devices)
                2 -> getString(R.string.title_child_devices)
                3 -> getString(R.string.title_blocked_sites)
                4 -> getString(R.string.title_time_limits)
                else -> ""
            }
        }.attach()
        
        // Disable swipe for now to simplify navigation
        binding.viewPager.isUserInputEnabled = true
        
        // Add tab selection listener to show interstitial ads occasionally
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                tabChangeCount++
                
                // Show interstitial ad every N tab changes
                if (tabChangeCount >= TAB_CHANGES_BEFORE_AD) {
                    tabChangeCount = 0
                    adManager.showInterstitialAd(this@MainActivity)
                }
            }
            
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
    
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Android 13+ (API 33+): Request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Location permission - needed for WiFi scanning on Android 9+ (API 28+)
        // Show rationale if user previously denied
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show rationale dialog
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("PocketFence needs location permission to detect and monitor devices connected to your WiFi network. " +
                            "This is required by Android for WiFi scanning and does not track your physical location.")
                    .setPositiveButton("Grant") { _, _ ->
                        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                        continuePermissionRequest(permissionsToRequest)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this, 
                            "Location permission is required for device monitoring", 
                            Toast.LENGTH_LONG).show()
                    }
                    .show()
                return
            } else {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        
        // WiFi state permissions (usually auto-granted)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_WIFI_STATE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CHANGE_WIFI_STATE)
        }
        
        continuePermissionRequest(permissionsToRequest)
    }
    
    private fun continuePermissionRequest(permissionsToRequest: List<String>) {
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

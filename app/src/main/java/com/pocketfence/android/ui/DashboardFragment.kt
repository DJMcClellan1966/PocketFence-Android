package com.pocketfence.android.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pocketfence.android.R
import com.pocketfence.android.databinding.FragmentDashboardBinding
import com.pocketfence.android.monetization.AdManager
import com.pocketfence.android.monetization.BillingManager
import com.pocketfence.android.service.MonitoringService
import com.pocketfence.android.service.VpnFilterService
import com.pocketfence.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard fragment showing protection status and quick settings.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()
    
    @Inject
    lateinit var adManager: AdManager
    
    @Inject
    lateinit var billingManager: BillingManager
    
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(requireContext(), R.string.error_vpn_setup, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        setupAds()
    }
    
    private fun setupAds() {
        // Load banner ad if not premium
        binding.adView?.let { adView ->
            adManager.loadBannerAd(adView)
        }
    }
    
    private fun setupUI() {
        binding.toggleProtectionButton.setOnClickListener {
            val isActive = viewModel.protectionStatus.value.isActive
            if (isActive) {
                stopProtection()
            } else {
                startProtection()
            }
        }
        
        binding.notificationsSwitch.apply {
            isChecked = viewModel.getNotificationsEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.setNotificationsEnabled(isChecked)
            }
        }
        
        binding.blockUnknownSwitch.apply {
            isChecked = viewModel.getBlockUnknownDevices()
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.setBlockUnknownDevices(isChecked)
            }
        }
        
        binding.strictModeSwitch.apply {
            isChecked = viewModel.getStrictMode()
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.setStrictMode(isChecked)
            }
        }
        
        // Premium button click handler
        binding.goPremiumButton.setOnClickListener {
            showPremiumDialog()
        }
        
        // Observe premium status to hide/show premium card
        viewLifecycleOwner.lifecycleScope.launch {
            billingManager.premiumStatus.collect { isPremium ->
                updatePremiumUI(isPremium)
            }
        }
    }
    
    private fun showPremiumDialog() {
        val dialog = PremiumDialog.newInstance()
        dialog.show(parentFragmentManager, PremiumDialog.TAG)
    }
    
    private fun updatePremiumUI(isPremium: Boolean) {
        // Hide premium card if user has premium
        binding.premiumCard.visibility = if (isPremium) ViewGroup.GONE else ViewGroup.VISIBLE
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.protectionStatus.collect { status ->
                updateUI(status.isActive, status.connectedDevicesCount, status.blockedSitesToday)
            }
        }
    }
    
    private fun updateUI(isActive: Boolean, deviceCount: Int, blockedCount: Int) {
        if (isActive) {
            binding.statusText.text = getString(R.string.status_active)
            binding.statusText.setTextColor(requireContext().getColor(R.color.status_active))
            binding.toggleProtectionButton.text = getString(R.string.stop_protection)
            binding.vpnStatusText.text = getString(R.string.vpn_connected)
        } else {
            binding.statusText.text = getString(R.string.status_inactive)
            binding.statusText.setTextColor(requireContext().getColor(R.color.status_inactive))
            binding.toggleProtectionButton.text = getString(R.string.start_protection)
            binding.vpnStatusText.text = getString(R.string.vpn_disconnected)
        }
        
        binding.devicesCountText.text = deviceCount.toString()
        binding.blockedCountText.text = blockedCount.toString()
    }
    
    private fun startProtection() {
        val intent = VpnService.prepare(requireContext())
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }
    
    private fun startVpnService() {
        val vpnIntent = Intent(requireContext(), VpnFilterService::class.java).apply {
            action = VpnFilterService.ACTION_START
        }
        requireContext().startService(vpnIntent)
        
        val monitorIntent = Intent(requireContext(), MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_START_MONITORING
        }
        requireContext().startService(monitorIntent)
        
        viewModel.setProtectionActive(true)
        Toast.makeText(requireContext(), "Protection started", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopProtection() {
        val vpnIntent = Intent(requireContext(), VpnFilterService::class.java).apply {
            action = VpnFilterService.ACTION_STOP
        }
        requireContext().startService(vpnIntent)
        
        val monitorIntent = Intent(requireContext(), MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_STOP_MONITORING
        }
        requireContext().startService(monitorIntent)
        
        viewModel.setProtectionActive(false)
        Toast.makeText(requireContext(), "Protection stopped", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

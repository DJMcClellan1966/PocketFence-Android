package com.pocketfence.android.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pocketfence.android.R
import com.pocketfence.android.databinding.DialogPremiumBinding
import com.pocketfence.android.monetization.BillingManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dialog for purchasing premium features.
 */
@AndroidEntryPoint
class PremiumDialog : DialogFragment() {
    
    private var _binding: DialogPremiumBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var billingManager: BillingManager
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPremiumBinding.inflate(layoutInflater)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
        
        setupUI()
        observeBillingState()
        
        return dialog
    }
    
    private fun setupUI() {
        // Monthly subscription button
        binding.monthlyButton.setOnClickListener {
            launchSubscriptionFlow(isYearly = false)
        }
        
        // Yearly subscription button
        binding.yearlyButton.setOnClickListener {
            launchSubscriptionFlow(isYearly = true)
        }
        
        // Legacy one-time purchase button
        binding.purchaseButton.setOnClickListener {
            launchPurchaseFlow()
        }
        
        binding.restoreButton.setOnClickListener {
            restorePurchases()
        }
    }
    
    private fun launchSubscriptionFlow(isYearly: Boolean) {
        if (!billingManager.isReady()) {
            Toast.makeText(
                requireContext(),
                "Billing service not ready. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        billingManager.launchSubscriptionFlow(requireActivity(), isYearly)
    }
    
    private fun observeBillingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            billingManager.purchaseState.collect { state ->
                when (state) {
                    is BillingManager.PurchaseState.Idle -> {
                        showLoading(false)
                    }
                    is BillingManager.PurchaseState.Loading -> {
                        showLoading(true)
                    }
                    is BillingManager.PurchaseState.Success -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        dismiss()
                    }
                    is BillingManager.PurchaseState.Error -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        // Observe premium status for restore purchases
        viewLifecycleOwner.lifecycleScope.launch {
            billingManager.premiumStatus.collect { isPremium ->
                if (isPremium && !isLoading) {
                    // Premium was restored or purchased
                    dismiss()
                }
            }
        }
    }
    
    private var isLoading = false
    
    private fun launchPurchaseFlow() {
        if (!billingManager.isReady()) {
            Toast.makeText(
                requireContext(),
                "Billing service not ready. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        billingManager.launchPurchaseFlow(requireActivity())
    }
    
    private fun restorePurchases() {
        showLoading(true)
        billingManager.restorePurchases()
        
        // Check status after a brief moment for billing to query
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(3000) // Allow time for billing query
            showLoading(false)
            
            if (!billingManager.isPremium()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.restore_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.restore_successful),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        isLoading = show
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.monthlyButton.isEnabled = !show
        binding.yearlyButton.isEnabled = !show
        binding.purchaseButton.isEnabled = !show
        binding.restoreButton.isEnabled = !show
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val TAG = "PremiumDialog"
        
        fun newInstance(): PremiumDialog {
            return PremiumDialog()
        }
    }
}

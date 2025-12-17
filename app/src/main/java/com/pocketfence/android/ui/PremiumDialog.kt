package com.pocketfence.android.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    
    private fun setupUI() {
        binding.purchaseButton.setOnClickListener {
            launchPurchaseFlow()
        }
        
        binding.restoreButton.setOnClickListener {
            restorePurchases()
        }
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
    }
    
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
        
        // Give it a moment to check
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            showLoading(false)
            
            if (billingManager.isPremium()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.restore_successful),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.restore_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
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

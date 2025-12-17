package com.pocketfence.android.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for handling Google Play Billing.
 * Manages in-app purchases for premium features.
 */
@Singleton
class BillingManager @Inject constructor(
    private val context: Context,
    private val monetizationRepository: MonetizationRepository
) {
    private var billingClient: BillingClient? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private val _premiumStatus = MutableStateFlow(false)
    val premiumStatus: StateFlow<Boolean> = _premiumStatus.asStateFlow()
    
    companion object {
        private const val TAG = "BillingManager"
        
        // Product IDs - These should match your Google Play Console configuration
        const val PREMIUM_PRODUCT_ID = "premium_version"
        
        // For testing, use these reserved product IDs:
        // "android.test.purchased" - always succeeds
        // "android.test.canceled" - always canceled
        // "android.test.refunded" - always refunded
        // "android.test.item_unavailable" - always unavailable
    }
    
    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        data class Success(val message: String) : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }
    
    /**
     * Initialize the billing client and connect to Google Play.
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                handlePurchasesUpdate(billingResult, purchases)
            }
            .enablePendingPurchases()
            .build()
        
        startConnection()
    }
    
    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    // Query existing purchases
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected. Will retry connection.")
                // Try to reconnect
                startConnection()
            }
        })
    }
    
    /**
     * Query existing purchases to restore premium status.
     */
    private fun queryPurchases() {
        coroutineScope.launch {
            try {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                
                val purchasesResult = billingClient?.queryPurchasesAsync(params)
                purchasesResult?.let { result ->
                    if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        handlePurchases(result.purchasesList)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying purchases", e)
            }
        }
    }
    
    /**
     * Launch the purchase flow for the premium product.
     */
    fun launchPurchaseFlow(activity: Activity) {
        if (!isReady()) {
            _purchaseState.value = PurchaseState.Error("Billing service not ready")
            return
        }
        
        _purchaseState.value = PurchaseState.Loading
        
        coroutineScope.launch {
            try {
                // Query product details
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
                
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
                
                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient?.queryProductDetails(params)
                }
                
                if (productDetailsResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productDetails = productDetailsResult.productDetailsList?.firstOrNull()
                    
                    if (productDetails != null) {
                        // Launch purchase flow
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                        
                        val flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()
                        
                        withContext(Dispatchers.Main) {
                            billingClient?.launchBillingFlow(activity, flowParams)
                        }
                    } else {
                        _purchaseState.value = PurchaseState.Error("Premium product not available")
                    }
                } else {
                    _purchaseState.value = PurchaseState.Error("Failed to load product details")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching purchase flow", e)
                _purchaseState.value = PurchaseState.Error("Purchase failed: ${e.message}")
            }
        }
    }
    
    private fun handlePurchasesUpdate(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase")
            _purchaseState.value = PurchaseState.Idle
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
            _purchaseState.value = PurchaseState.Error("Purchase failed")
        }
    }
    
    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.products.contains(PREMIUM_PRODUCT_ID)) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                    // User has premium
                    setPremiumStatus(true)
                    _purchaseState.value = PurchaseState.Success("Premium activated!")
                    Log.d(TAG, "Premium purchase verified")
                }
            }
        }
        
        // If no premium purchase found, ensure premium is disabled
        if (purchases.none { 
            it.products.contains(PREMIUM_PRODUCT_ID) && 
            it.purchaseState == Purchase.PurchaseState.PURCHASED 
        }) {
            setPremiumStatus(false)
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        coroutineScope.launch {
            try {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                val result = withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(params)
                }
                
                if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error acknowledging purchase", e)
            }
        }
    }
    
    private fun setPremiumStatus(isPremium: Boolean) {
        _premiumStatus.value = isPremium
        // Persist premium status
        coroutineScope.launch {
            monetizationRepository.setPremiumStatus(isPremium)
        }
    }
    
    /**
     * Check if the user has premium features.
     */
    fun isPremium(): Boolean {
        return _premiumStatus.value
    }
    
    /**
     * Check if billing client is ready.
     */
    fun isReady(): Boolean {
        return billingClient?.isReady == true
    }
    
    /**
     * Restore purchases - useful after reinstall or on new device.
     */
    fun restorePurchases() {
        queryPurchases()
    }
    
    /**
     * Clean up resources.
     */
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}

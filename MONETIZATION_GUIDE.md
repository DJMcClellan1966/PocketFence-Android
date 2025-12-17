# Monetization Implementation Documentation

## Overview

PocketFence-Android now includes monetization features to generate revenue while maintaining a good user experience. The implementation includes both Google AdMob for advertising and Google Play Billing for in-app purchases.

## Components

### 1. AdMob Integration (Advertisement)

#### Features:
- **Banner Ads**: Displayed at the top of the Dashboard fragment
- **Interstitial Ads**: Shown at natural break points (every 5 tab changes)
- **Premium User Respect**: Ads are automatically hidden for premium users

#### Configuration Files:
- **build.gradle**: Added Google Mobile Ads SDK dependency
  ```gradle
  implementation 'com.google.android.gms:play-services-ads:22.6.0'
  ```
- **AndroidManifest.xml**: Added AdMob App ID metadata
  ```xml
  <meta-data
      android:name="com.google.android.gms.ads.APPLICATION_ID"
      android:value="ca-app-pub-3940256099942544~3347511713"/>
  ```

#### Key Classes:
- **AdManager** (`monetization/AdManager.kt`): Manages ad loading and display
  - Singleton class injected via Hilt
  - Handles banner ad loading
  - Manages interstitial ad lifecycle
  - Implements frequency capping (3 minutes between interstitial ads)
  - Automatically respects premium status

#### Usage Example:
```kotlin
@Inject
lateinit var adManager: AdManager

// Load banner ad
adManager.loadBannerAd(binding.adView)

// Show interstitial ad
adManager.showInterstitialAd(activity) {
    // Ad closed callback
}
```

### 2. Google Play Billing (In-App Purchases)

#### Features:
- **Premium Purchase**: One-time purchase to unlock premium features
- **Purchase Restoration**: Users can restore purchases on new devices
- **Premium Benefits**:
  - Ad-free experience
  - Advanced statistics (future feature)
  - Multiple user profiles (future feature)
  - Priority support

#### Configuration Files:
- **build.gradle**: Added Google Play Billing Library
  ```gradle
  implementation 'com.android.billingclient:billing-ktx:6.1.0'
  ```

#### Key Classes:
- **BillingManager** (`monetization/BillingManager.kt`): Manages billing operations
  - Connects to Google Play Billing
  - Handles purchase flow
  - Verifies and acknowledges purchases
  - Restores previous purchases
  - Emits purchase state via StateFlow

- **MonetizationRepository** (`monetization/MonetizationRepository.kt`): Data persistence
  - Stores premium status in SharedPreferences
  - Tracks ad metrics
  - Manages monetization-related data

#### Product IDs:
- Premium Version: `premium_version`
  - **Note**: This must be configured in Google Play Console

#### Usage Example:
```kotlin
@Inject
lateinit var billingManager: BillingManager

// Launch purchase flow
billingManager.launchPurchaseFlow(activity)

// Check premium status
if (billingManager.isPremium()) {
    // Show premium features
}

// Restore purchases
billingManager.restorePurchases()
```

### 3. User Interface Components

#### PremiumDialog (`ui/PremiumDialog.kt`):
- Material Design dialog for premium purchases
- Lists premium features
- Handles purchase flow
- Includes restore purchases button
- Shows loading state during transactions

#### Dashboard Updates:
- Banner ad view at the top
- Premium upgrade card (hidden for premium users)
- "Go Premium" button

## Setup Instructions

### 1. Google AdMob Setup

1. **Create an AdMob Account**:
   - Go to https://admob.google.com/
   - Sign in with your Google account
   - Accept terms and conditions

2. **Create an App in AdMob**:
   - Click "Apps" → "Add App"
   - Select "Android"
   - Enter app name: "PocketFence"
   - Copy the App ID

3. **Create Ad Units**:
   - **Banner Ad Unit**:
     - Go to "Ad units" → "Add ad unit"
     - Select "Banner"
     - Name: "Dashboard Banner"
     - Copy the Ad Unit ID
   
   - **Interstitial Ad Unit**:
     - Select "Interstitial"
     - Name: "Tab Change Interstitial"
     - Copy the Ad Unit ID

4. **Update Configuration**:
   - Replace test IDs in `AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.gms.ads.APPLICATION_ID"
         android:value="YOUR_ADMOB_APP_ID"/>
     ```
   
   - Replace test IDs in `strings.xml`:
     ```xml
     <string name="banner_ad_unit_id">YOUR_BANNER_AD_UNIT_ID</string>
     <string name="interstitial_ad_unit_id">YOUR_INTERSTITIAL_AD_UNIT_ID</string>
     ```

### 2. Google Play Billing Setup

1. **Configure Products in Play Console**:
   - Go to Google Play Console
   - Select your app
   - Navigate to "Monetization" → "Products" → "In-app products"
   - Click "Create product"
   
   **Product Details**:
   - Product ID: `premium_version`
   - Name: "PocketFence Premium"
   - Description: "Unlock premium features and remove ads"
   - Price: Set your desired price (e.g., $4.99)
   - Status: Active

2. **Test Purchases**:
   - Add test accounts in Play Console under "Settings" → "License testing"
   - Use license test accounts to test purchases without charges

### 3. Privacy Policy & Compliance

1. **Update Privacy Policy**:
   - Mention data collection by AdMob
   - Include information about personalized ads
   - Explain how user data is used

2. **GDPR Compliance** (if targeting EU users):
   - Implement consent management platform (CMP)
   - Use Google's UMP SDK for consent
   - Show consent form before loading ads

3. **Store Listing**:
   - Update app description to mention premium features
   - Add in-app purchases category
   - Include ads disclosure

## Testing

### Testing Ads:

1. **Using Test Ads**:
   - The current implementation uses test ad unit IDs
   - Test ads will always load successfully
   - No earnings from test ads

2. **Testing on Real Device**:
   - Enable test mode in AdMob settings
   - Add your device ID as a test device
   - Check logcat for ad loading status

### Testing Purchases:

1. **Test Product IDs**:
   - Use reserved product IDs for testing:
     - `android.test.purchased` - Always successful
     - `android.test.canceled` - Always canceled
     - `android.test.refunded` - Always refunded

2. **License Testing**:
   - Add test account email in Play Console
   - Install app from Play Store (internal test track)
   - Make test purchases (no charges)

3. **Verification**:
   - Check purchase flow completes
   - Verify ads are hidden after purchase
   - Test restore purchases functionality
   - Check premium status persists after app restart

## Best Practices

### Ad Placement:
- ✅ Banner ads in non-intrusive locations
- ✅ Interstitial ads at natural break points
- ✅ Frequency capping to avoid annoyance
- ✅ Respect premium users (no ads)
- ❌ Don't show ads during critical operations
- ❌ Don't show too many ads in short time

### Premium Features:
- ✅ Clear value proposition
- ✅ Reasonable pricing
- ✅ Easy purchase flow
- ✅ Restore purchases option
- ✅ Premium status persists
- ❌ Don't lock core functionality behind paywall
- ❌ Don't make free version unusable

### User Experience:
- ✅ Graceful error handling
- ✅ Loading states during purchases
- ✅ Clear success/failure messages
- ✅ Offline mode handling
- ✅ Fast ad loading

## Monitoring & Analytics

### Key Metrics to Track:

1. **Ad Performance**:
   - Impressions per user
   - Click-through rate (CTR)
   - eCPM (effective cost per mille)
   - Fill rate

2. **Purchase Metrics**:
   - Conversion rate (free to premium)
   - Average revenue per user (ARPU)
   - Retention rate (premium vs free)
   - Refund rate

3. **User Behavior**:
   - Time to first purchase
   - Features used by premium users
   - Ad dismissal patterns
   - Purchase funnel dropoff

### Google AdMob Dashboard:
- Monitor ad revenue
- Check fill rates
- Analyze user engagement
- Review policy compliance

### Google Play Console:
- Track purchase revenue
- Monitor conversion rates
- Analyze user reviews
- Check refund rates

## Troubleshooting

### Common Issues:

1. **Ads Not Loading**:
   - Check internet connection
   - Verify ad unit IDs are correct
   - Check AdMob account status
   - Review logcat for errors
   - Ensure app ID in manifest is correct

2. **Purchase Flow Not Working**:
   - Verify billing library is initialized
   - Check product ID matches Play Console
   - Ensure app is signed with release key
   - Verify test account is configured
   - Check internet connection

3. **Premium Status Not Persisting**:
   - Check SharedPreferences are being saved
   - Verify purchase acknowledgment
   - Review billing query on app start
   - Check for clearing app data

4. **Ads Showing for Premium Users**:
   - Verify premium status check in AdManager
   - Check billing client initialization
   - Review purchase verification flow
   - Ensure StateFlow is being collected

## Revenue Optimization

### Strategies:

1. **Ad Optimization**:
   - Use mediation for better fill rates
   - Test different ad placements
   - Optimize ad refresh rates
   - Implement rewarded video ads

2. **Pricing Strategy**:
   - Test different price points
   - Offer limited-time discounts
   - Consider regional pricing
   - Analyze competitor pricing

3. **Conversion Optimization**:
   - A/B test premium dialog design
   - Highlight premium benefits
   - Show premium features in action
   - Reduce friction in purchase flow

4. **Retention**:
   - Provide value in free version
   - Regular feature updates
   - Engage with user feedback
   - Build community

## Future Enhancements

### Potential Additions:

1. **Subscription Model**:
   - Monthly/yearly subscriptions
   - Trial period for new users
   - Upgrade/downgrade flows
   - Grace period handling

2. **Rewarded Video Ads**:
   - Reward users with premium features temporarily
   - Unlock advanced stats for watching ad
   - Extra time limits after video view

3. **Advanced Analytics**:
   - Firebase Analytics integration
   - Custom event tracking
   - Cohort analysis
   - Funnel analysis

4. **Additional Premium Features**:
   - Cloud sync
   - Advanced filtering rules
   - Activity reports export
   - White-labeling options

## Support

### Resources:
- [AdMob Documentation](https://developers.google.com/admob/android/quick-start)
- [Google Play Billing Documentation](https://developer.android.com/google/play/billing)
- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [AdMob Policy Center](https://support.google.com/admob/answer/6128543)

### Getting Help:
- Check AdMob community forums
- Review Play Console support
- Contact Google AdMob support
- Review billing library samples on GitHub

## Conclusion

The monetization implementation provides a solid foundation for generating revenue while maintaining user experience. The modular architecture allows for easy updates and additions of new monetization strategies in the future.

Remember to:
- Always test thoroughly before release
- Monitor metrics regularly
- Listen to user feedback
- Stay compliant with policies
- Keep libraries updated
- Optimize based on data

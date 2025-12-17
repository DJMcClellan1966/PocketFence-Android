# Monetization Architecture Flow

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     PocketFenceApplication                       │
│  - Initializes AdManager                                         │
│  - Initializes BillingManager                                    │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     │ Hilt DI
                     ▼
        ┌────────────────────────────┐
        │     AppModule (Hilt)       │
        │  Provides:                 │
        │  - MonetizationRepository  │
        │  - BillingManager          │
        │  - AdManager               │
        └─────────┬──────────────────┘
                  │
        ┌─────────┴──────────┐
        │                    │
        ▼                    ▼
┌─────────────────┐  ┌─────────────────┐
│  BillingManager │  │    AdManager    │
│                 │  │                 │
│ - Purchase flow │  │ - Banner ads    │
│ - Verification  │  │ - Interstitial  │
│ - Restore       │  │ - Frequency     │
└────────┬────────┘  └────────┬────────┘
         │                    │
         └──────────┬─────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │ MonetizationRepository│
        │                       │
        │ - Premium status      │
        │ - SharedPreferences   │
        │ - StateFlow           │
        └───────────┬───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │   SharedPreferences   │
        │                       │
        │ - is_premium          │
        │ - purchase_date       │
        │ - ad_metrics          │
        └───────────────────────┘
```

## Data Flow

### 1. Ad Display Flow

```
┌──────────────┐
│ DashboardFrag│
└──────┬───────┘
       │ @Inject
       │ lateinit var adManager: AdManager
       │
       ▼
┌──────────────┐     ┌────────────────────┐
│  AdManager   │────▶│ MonetizationRepo   │
│  loadAd()    │     │ getPremiumStatus() │
└──────┬───────┘     └────────────────────┘
       │                      │
       │                      ▼
       │              ┌───────────────┐
       │              │ Premium? NO   │
       │              └───────┬───────┘
       │                      │
       ▼                      ▼
┌──────────────┐      ┌──────────────┐
│  Load Ad     │◀─────│  Show Ad     │
│  from AdMob  │      │              │
└──────────────┘      └──────────────┘
```

### 2. Purchase Flow

```
┌──────────────┐
│     User     │
│ Clicks "Go   │
│  Premium"    │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│  PremiumDialog   │
│  - Show features │
│  - Purchase btn  │
└──────┬───────────┘
       │ Click Purchase
       ▼
┌──────────────────┐     ┌────────────────────┐
│  BillingManager  │────▶│  Google Play       │
│  launchPurchase()│     │  Billing Service   │
└──────┬───────────┘     └────────┬───────────┘
       │                          │
       │ Purchase callback        │
       │◀─────────────────────────┘
       │
       ▼
┌──────────────────┐
│  Verify Purchase │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│  Acknowledge     │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐     ┌────────────────────┐
│  BillingManager  │────▶│ MonetizationRepo   │
│  setPremium(true)│     │ setPremiumStatus() │
└──────────────────┘     └────────┬───────────┘
                                  │
                                  ▼
                         ┌────────────────────┐
                         │ Save to SharedPrefs│
                         │ Emit StateFlow     │
                         └────────┬───────────┘
                                  │
                                  ▼
                         ┌────────────────────┐
                         │ UI Components      │
                         │ - Hide ads         │
                         │ - Hide premium card│
                         │ - Show premium badge
                         └────────────────────┘
```

### 3. Restore Purchases Flow

```
┌──────────────┐
│     User     │
│ Clicks       │
│ "Restore"    │
└──────┬───────┘
       │
       ▼
┌──────────────────┐     ┌────────────────────┐
│  BillingManager  │────▶│  Google Play       │
│  restore()       │     │  Query Purchases   │
└──────┬───────────┘     └────────┬───────────┘
       │                          │
       │ Purchase list            │
       │◀─────────────────────────┘
       │
       ▼
┌──────────────────┐
│  Check for       │
│  premium_version │
└──────┬───────────┘
       │
       ├──Found────▶┌────────────────────┐
       │            │ setPremium(true)   │
       │            └────────────────────┘
       │
       └──Not Found─▶┌────────────────────┐
                     │ Show "No purchases"│
                     └────────────────────┘
```

## State Management

### Premium Status StateFlow

```
┌────────────────────┐
│ MonetizationRepo   │
│                    │
│ _isPremium         │
│ MutableStateFlow   │
│     (private)      │
└────────┬───────────┘
         │
         │ Exposed as
         ▼
┌────────────────────┐
│ isPremium          │
│ StateFlow          │
│   (public)         │
└────────┬───────────┘
         │
         │ Collected by UI
         ▼
┌────────────────────────────┐
│ UI Components              │
│ lifecycleScope.launch {    │
│   repo.isPremium.collect { │
│     updateUI(it)           │
│   }                        │
│ }                          │
└────────────────────────────┘
```

### Purchase State StateFlow

```
┌────────────────────┐
│  BillingManager    │
│                    │
│ _purchaseState     │
│ MutableStateFlow   │
└────────┬───────────┘
         │
         │ Emits states:
         │ - Idle
         │ - Loading
         │ - Success(msg)
         │ - Error(msg)
         ▼
┌────────────────────┐
│ purchaseState      │
│ StateFlow          │
└────────┬───────────┘
         │
         │ Observed by
         ▼
┌────────────────────┐
│  PremiumDialog     │
│                    │
│  when (state) {    │
│    Loading -> show │
│    Success -> toast│
│    Error -> toast  │
│  }                 │
└────────────────────┘
```

## Initialization Sequence

```
App Launch
    │
    ▼
PocketFenceApplication.onCreate()
    │
    ├─▶ adManager.initialize() ──▶ MobileAds.initialize()
    │                                    │
    │                                    ▼
    │                            Load first interstitial
    │
    └─▶ billingManager.initialize() ──▶ BillingClient.startConnection()
                                               │
                                               ▼
                                         Query purchases
                                               │
                                               ▼
                                         Restore premium status
```

## Lifecycle Management

### Fragment Lifecycle

```
DashboardFragment.onViewCreated()
    │
    ├─▶ setupUI()
    │      │
    │      ├─▶ Set click listeners
    │      └─▶ Initialize switches
    │
    ├─▶ observeViewModel()
    │      │
    │      └─▶ Collect protection status
    │
    ├─▶ setupAds()
    │      │
    │      └─▶ adManager.loadBannerAd()
    │
    └─▶ Observe premium status
           │
           └─▶ Update UI based on premium
```

### Activity Lifecycle

```
MainActivity.onCreate()
    │
    ├─▶ setupViewPager()
    │      │
    │      └─▶ Add tab selection listener
    │             │
    │             └─▶ Show interstitial every N tabs
    │
    └─▶ requestPermissions()
```

## Error Handling

### Ad Loading Failure

```
AdManager.loadInterstitialAd()
    │
    ▼
InterstitialAd.load(callback)
    │
    ├─▶ onAdLoaded() ──▶ Store ad reference
    │
    └─▶ onAdFailedToLoad() ──▶ Log error
                              │
                              └─▶ Set ad reference to null
```

### Purchase Failure

```
BillingManager.launchPurchaseFlow()
    │
    ▼
BillingClient.launchBillingFlow()
    │
    ├─▶ Success ──▶ handlePurchasesUpdate()
    │                    │
    │                    └─▶ Verify & acknowledge
    │
    ├─▶ User canceled ──▶ Set state to Idle
    │
    └─▶ Error ──▶ Set state to Error(message)
                      │
                      └─▶ Show error toast
```

## Thread Safety

All monetization operations use proper coroutine scopes:

- **BillingManager**: Uses CoroutineScope(Dispatchers.IO)
- **UI Updates**: Use Dispatchers.Main
- **StateFlow**: Thread-safe by design
- **SharedPreferences**: All writes use apply() (async)

## Testing Strategy

```
Unit Tests
    │
    ├─▶ MonetizationRepository
    │      │
    │      ├─▶ Test premium status
    │      ├─▶ Test ad metrics
    │      └─▶ Test persistence
    │
    └─▶ Future: BillingManager & AdManager
           (Require more complex mocking)

Integration Tests
    │
    └─▶ Manual testing with test accounts
```

## Security Considerations

```
Purchase Verification
    │
    ├─▶ Server-side verification (not implemented)
    │      Use Google Play Developer API
    │
    └─▶ Client-side (current)
           Purchase state from BillingClient
           Acknowledge after verification
```

## Performance Optimizations

```
Ad Loading
    │
    ├─▶ Preload interstitials in advance
    │
    ├─▶ Frequency capping (3 min minimum)
    │
    └─▶ Check premium status before loading

StateFlow
    │
    └─▶ Hot flows - latest value always available
        No unnecessary re-emission

Coroutines
    │
    ├─▶ Non-blocking billing operations
    │
    └─▶ Proper lifecycle-aware collection
```

This architecture ensures:
✅ Clean separation of concerns
✅ Testable components
✅ Reactive premium status updates
✅ Proper error handling
✅ Good user experience
✅ Production-ready code

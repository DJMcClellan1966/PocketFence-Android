# PocketFence - Android Parental Control App

PocketFence is a powerful yet simple Android parental control application that allows parents to monitor and control their children's internet access through their Android device's hotspot.

## Features

### 🛡️ Core Protection Features
- **VPN-Based Traffic Filtering**: Routes all traffic through a local VPN to monitor and block harmful content
- **Hotspot Device Management**: Automatically detects and manages devices connected to your Android hotspot
- **Website Blocking**: Block specific websites or entire categories of harmful content
- **Time Limits**: Set daily internet usage limits for individual devices
- **Quiet Hours**: Schedule times when internet access is automatically blocked
- **Real-time Monitoring**: Track connected devices and blocked sites in real-time

### 📱 User Interface
- **Dashboard**: Overview of protection status, connected devices, and statistics
- **Devices Tab**: View and manage all connected devices, set individual time limits
- **Blocked Sites Tab**: Add/remove websites from the blocklist with preset categories
- **Time Limits Tab**: Configure daily limits and quiet hours

### 🔒 Security & Privacy
- All data stored locally on the device
- No cloud services or external data sharing
- VPN traffic stays on the device
- Encrypted settings storage

## Requirements

- Android 8.0 (API 26) or higher
- Active Android hotspot
- VPN permission (prompted during setup)
- Location permission (for WiFi device scanning)

## Installation

1. Clone this repository:
```bash
git clone https://github.com/DJMcClellan1966/PocketFence-Android.git
```

2. Open the project in Android Studio

3. Build and run the app on your device

## Usage

### Getting Started

1. **Enable Hotspot**: Turn on your Android device's WiFi hotspot
2. **Launch PocketFence**: Open the app and grant required permissions
3. **Start Protection**: Tap "Start Protection" on the dashboard
4. **Grant VPN Permission**: Allow PocketFence to create a VPN connection
5. **Connect Child Devices**: Connect your child's devices to your hotspot

### Managing Devices

1. Navigate to the "Devices" tab
2. View all connected devices with their MAC and IP addresses
3. Set time limits for specific devices
4. Block/unblock devices as needed
5. Rename devices for easy identification

### Blocking Websites

**Manual Blocking:**
1. Go to "Blocked Sites" tab
2. Enter the website URL (e.g., "facebook.com")
3. Tap "Add Website"

**Category Blocking:**
1. In "Blocked Sites" tab, tap on preset categories:
   - Social Media
   - Adult Content
   - Gambling
   - Gaming
2. All sites in that category are automatically added

### Setting Time Limits

**Daily Time Limit:**
1. Go to "Time Limits" tab
2. Set hours and minutes for daily internet usage
3. Tap "Set Limit"

**Quiet Hours:**
1. Enable "Quiet Hours" toggle
2. Set start and end times
3. Internet will be blocked during these hours

## Architecture

PocketFence uses modern Android architecture components:

- **MVVM Pattern**: Separation of concerns with ViewModel and LiveData
- **Repository Pattern**: Centralized data management
- **VPN Service**: Custom VpnService for packet filtering
- **Background Services**: Continuous device monitoring
- **SharedPreferences**: Local data persistence
- **Coroutines**: Asynchronous operations
- **View Binding**: Type-safe view access

### Project Structure

```
app/
├── model/              # Data models (Device, Website, TimeLimit)
├── repository/         # Data layer and business logic
├── service/            # VPN and monitoring services
├── ui/                 # Activities and Fragments
│   ├── adapter/        # RecyclerView adapters
│   └── fragments/      # UI fragments
├── util/               # Utility classes
└── viewmodel/          # ViewModels for UI state
```

## Permissions

PocketFence requires the following permissions:

- `INTERNET` - For VPN functionality
- `ACCESS_NETWORK_STATE` - To monitor network connections
- `BIND_VPN_SERVICE` - To create VPN connection
- `ACCESS_WIFI_STATE` - To detect WiFi connections
- `CHANGE_WIFI_STATE` - To manage WiFi settings
- `ACCESS_FINE_LOCATION` - To scan for connected devices
- `FOREGROUND_SERVICE` - For background monitoring
- `POST_NOTIFICATIONS` - To show protection status

## Technical Details

### VPN Implementation

PocketFence creates a local VPN service that:
1. Intercepts all network packets
2. Analyzes packet destinations
3. Blocks packets to blacklisted domains
4. Forwards allowed packets

### Device Detection

The app uses multiple methods to detect devices:
1. ARP table scanning (`/proc/net/arp`)
2. WiFi hotspot client enumeration
3. MAC address to device name mapping

### Website Filtering

Filtering is performed at the IP level:
1. Domain names are normalized and stored
2. DNS requests are monitored
3. Matching domains trigger packet blocking
4. Statistics are tracked and displayed

## Limitations

- VPN filtering works best with DNS-based blocking
- Some apps may bypass VPN using direct IP connections
- Device scanning accuracy depends on Android version
- Hotspot management APIs are limited on newer Android versions

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.

## Disclaimer

This app is designed for parental control purposes. It should be used responsibly and with the knowledge of device users. The developers are not responsible for misuse of this application.

## Support

For issues, questions, or feature requests, please open an issue on GitHub.

## Monetization

PocketFence includes optional monetization features to support development:

### Free Version
- Full core protection features
- Website blocking and time limits
- Device management
- Ad-supported (non-intrusive banner and occasional interstitial ads)

### Premium Version
- **Ad-free experience**: Remove all advertisements
- **Advanced statistics**: Detailed analytics and reports (coming soon)
- **Multiple profiles**: Support for multiple user profiles (coming soon)
- **Priority support**: Get help faster

### Purchasing Premium
1. Open the app and navigate to the Dashboard
2. Tap the "Go Premium" button on the premium card
3. Review the premium features
4. Complete the purchase through Google Play
5. Ads will be automatically removed

### Restore Purchases
If you reinstall the app or switch devices:
1. Open the Premium dialog
2. Tap "Restore Purchases"
3. Your premium status will be restored

For detailed monetization setup and configuration, see [MONETIZATION_GUIDE.md](MONETIZATION_GUIDE.md).

## Future Enhancements

- [ ] App-specific blocking
- [ ] Content filtering levels (strict, moderate, light)
- [ ] Activity reports and statistics
- [ ] Multiple user profiles
- [ ] Remote management via web interface
- [ ] Schedule templates (school days, weekends)
- [ ] Emergency bypass codes

---

**Note**: This app requires root-level network access through VPN permissions. It does not require device rooting.


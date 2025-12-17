# PocketFence Features Overview

## 🎯 Core Functionality

### 1. VPN-Based Traffic Filtering
- **Local VPN Service**: Creates a VPN on the device to intercept all network traffic
- **Packet Analysis**: Examines each packet to determine destination
- **Domain Blocking**: Blocks packets destined for blacklisted domains
- **Real-time Processing**: Minimal latency with efficient packet handling

### 2. Hotspot Device Management
- **Automatic Detection**: Discovers devices connected to the hotspot
- **Device Identification**: Shows MAC address, IP address, and device name
- **Individual Control**: Set different rules for each device
- **Block/Unblock**: Instantly block or allow specific devices

### 3. Website Blocking System

#### Manual Blocking
- Add individual websites to blocklist
- Support for domain names (e.g., facebook.com)
- URL normalization for consistent matching
- Instant activation once added

#### Category-Based Blocking
Pre-configured categories with popular sites:
- **Social Media**: Facebook, Instagram, Twitter, TikTok, Snapchat, Reddit, Pinterest
- **Adult Content**: Major adult websites
- **Gambling**: Online casino and betting sites
- **Gaming**: Game platforms and streaming sites
- **Violence**: Sites with violent content
- **Drugs**: Drug-related content sites

### 4. Time Management

#### Daily Time Limits
- Set maximum daily internet usage per device
- Automatic tracking of time used
- Visual display of remaining time
- Automatic blocking when limit reached
- Daily reset at midnight

#### Quiet Hours
- Schedule internet blackout periods
- Configurable start and end times
- Can span midnight (e.g., 22:00 to 07:00)
- Automatic enforcement
- Independent of time limits

### 5. Device Monitoring

#### Real-time Statistics
- Count of connected devices
- Number of sites blocked today
- Protection status indicator
- VPN connection status

#### Device Information
- Device name (auto-detected or custom)
- MAC address
- IP address
- Last seen timestamp
- First connection time
- Current time usage
- Time remaining

### 6. User Interface

#### Dashboard Tab
- Protection on/off toggle
- Protection status (Active/Inactive)
- VPN connection status
- Device count
- Blocked sites count
- Quick settings (notifications, block unknown, strict mode)

#### Devices Tab
- List of all connected devices
- Device details cards
- Set time limit button per device
- Block/Unblock button per device
- Empty state when no devices

#### Blocked Sites Tab
- Add website input field
- Preset category chips
- List of blocked websites
- Delete button per website
- Category labels
- Empty state message

#### Time Limits Tab
- Daily limit input (hours and minutes)
- Set limit button
- Quiet hours toggle
- Start/End time picker buttons
- Time picker dialogs

## 🔐 Security Features

### VPN Security
- Local VPN only (no external connections)
- All traffic stays on device
- No logging of traffic content
- Secure packet handling

### Data Privacy
- All settings stored locally using SharedPreferences
- No cloud synchronization
- No external API calls
- No analytics or tracking
- No personal data collection

### Access Control
- VPN permission required
- Location permission for WiFi scanning
- Notification permission for alerts
- All permissions clearly requested

## 📊 Data Management

### Persistence
- Settings saved to SharedPreferences
- JSON serialization for complex objects
- Automatic daily statistics reset
- Device history maintained

### Data Models
- `ConnectedDevice`: Device information and limits
- `BlockedWebsite`: URL and category
- `TimeLimit`: Daily limits and quiet hours
- `ProtectionStatus`: Current system state

## 🔔 Notifications

### Foreground Service Notification
- Always visible when protection is active
- Shows number of protected devices
- Tappable to open app
- Cannot be dismissed while active

### Alert Notifications
- Site blocked notification (optional)
- Time limit reached notification
- Customizable through settings
- Auto-dismissible

## ⚙️ Settings

### Configurable Options
- **Enable Notifications**: Control alert notifications
- **Block Unknown Devices**: Automatically block new connections
- **Strict Mode**: Enhanced filtering (future use)

### Automatic Features
- Daily statistics reset
- Time usage tracking
- Device discovery
- Connection monitoring

## 🏗️ Technical Architecture

### Services
- `VpnFilterService`: Handles VPN and packet filtering
- `MonitoringService`: Tracks devices and time usage

### Background Processing
- Coroutines for async operations
- 10-second monitoring interval
- Efficient ARP table scanning
- Minimal battery impact

### UI Architecture
- MVVM pattern
- ViewBinding for type-safe views
- StateFlow for reactive updates
- FragmentStateAdapter for tabs

## 📱 Compatibility

### Minimum Requirements
- Android 8.0 (API 26)
- WiFi hotspot capability
- VPN support

### Tested On
- Android 8.0 - 13.0
- Various device manufacturers
- Different screen sizes

## 🚀 Performance

### Optimizations
- Efficient packet processing
- Minimal memory footprint
- Battery-friendly monitoring
- Fast UI rendering
- Lazy loading where possible

### Resource Usage
- Low CPU usage (< 5% on average)
- Moderate memory (50-100 MB)
- Minimal battery drain
- No network data usage

## 🔮 Future Enhancements

### Planned Features
- App-specific blocking
- Content filtering levels
- Detailed activity reports
- Usage statistics graphs
- Multiple user profiles
- Remote management
- Schedule templates
- Export/import settings
- Backup and restore
- Enhanced device naming
- Custom categories
- Regex pattern matching
- DNS caching
- IPv6 support

### Under Consideration
- Web dashboard
- Parent mobile app companion
- Multi-device synchronization
- Cloud backup (optional)
- Advanced analytics
- Machine learning content detection
- Geo-fencing
- Screen time tracking
- App usage monitoring

---

**Note**: This document describes the current implementation and planned features. Some features may require additional development and testing.

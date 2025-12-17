# PocketFence Quick Start Guide

Get PocketFence up and running in 5 minutes!

## Prerequisites

✅ Android device running Android 8.0 or higher  
✅ Device can create WiFi hotspot  
✅ Child devices to monitor  

## Step 1: Enable Hotspot

1. Open your Android device settings
2. Navigate to "Network & Internet" → "Hotspot & tethering"
3. Enable "WiFi hotspot"
4. Note the network name and password
5. Keep hotspot enabled while using PocketFence

## Step 2: Install PocketFence

### Option A: Build from Source
```bash
git clone https://github.com/DJMcClellan1966/PocketFence-Android.git
cd PocketFence-Android
./gradlew installDebug
```

### Option B: Install APK
1. Download the APK from releases
2. Enable "Install from Unknown Sources"
3. Install the APK
4. Open PocketFence

## Step 3: Grant Permissions

When you first open PocketFence, you'll be asked for permissions:

1. **Location Permission** → Tap "Allow"
   - Required to scan for connected devices
   
2. **Notification Permission** → Tap "Allow" (Android 13+)
   - Shows protection status and alerts

## Step 4: Start Protection

1. Open PocketFence
2. You'll see the Dashboard tab
3. Tap the **"Start Protection"** button
4. A dialog will appear requesting VPN permission
5. Tap **"OK"** to grant permission
6. Protection will start immediately
7. Status will change to "Active" with green color

✅ **Protection is now active!**

## Step 5: Connect Child Devices

1. On child's device, open WiFi settings
2. Find and connect to your hotspot network
3. Enter the hotspot password
4. Wait 10-15 seconds

Return to PocketFence:
1. Tap the **"Devices"** tab
2. You should see the connected device listed
3. Note: Initial name may be generic (e.g., "Device-XX:XX:XX")

## Step 6: Block Websites

### Quick Block - Use Categories

1. Go to **"Blocked Sites"** tab
2. Tap on preset category chips:
   - **Social Media** (Facebook, Instagram, TikTok, etc.)
   - **Adult Content** (adult websites)
   - **Gambling** (casino sites)
   - **Gaming** (game platforms)
3. Sites are immediately blocked

### Manual Block - Add Specific Sites

1. In **"Blocked Sites"** tab
2. Type website in the text field
   - Example: `youtube.com`
   - Example: `fortnite.com`
3. Tap **"Add Website"**
4. Site is immediately blocked

## Step 7: Set Time Limits

### Daily Time Limit

1. Go to **"Time Limits"** tab
2. Enter hours (e.g., 2)
3. Enter minutes (e.g., 30)
4. Tap **"Set Limit"**
5. Device will be blocked after 2h 30m of usage

### Quiet Hours (Bedtime Mode)

1. In **"Time Limits"** tab
2. Toggle **"Enable Quiet Hours"** ON
3. Tap **"Start Time"** button (e.g., 22:00)
4. Tap **"End Time"** button (e.g., 07:00)
5. Internet blocked during these hours

## Common Tasks

### Block a Device Completely

1. Go to **"Devices"** tab
2. Find the device card
3. Tap **"Block Device"** button
4. Device loses internet immediately

### Unblock a Device

1. Go to **"Devices"** tab
2. Find the blocked device (shows "BLOCKED" badge)
3. Tap **"Unblock Device"** button

### Remove a Blocked Website

1. Go to **"Blocked Sites"** tab
2. Find the website in the list
3. Tap **"Delete"** button
4. Confirm deletion

### Stop Protection

1. Go to **"Dashboard"** tab
2. Tap **"Stop Protection"** button
3. All filtering stops immediately
4. Devices retain full internet access

## Troubleshooting

### Device Not Appearing

**Problem**: Child device connected but not showing in app

**Solution**:
1. Ensure hotspot is active
2. Wait 15-20 seconds for scan
3. Pull down to refresh (if implemented)
4. Check location permission is granted
5. Try disconnecting and reconnecting device

### Website Not Blocked

**Problem**: Blocked website still accessible

**Solution**:
1. Verify protection is "Active" (green)
2. Check VPN icon in status bar
3. Ensure website is in blocked list
4. Try adding "www." prefix if needed
5. Some apps bypass VPN (normal)

### VPN Won't Start

**Problem**: Protection fails to start

**Solution**:
1. Ensure another VPN isn't running
2. Disable any existing VPN apps
3. Restart PocketFence
4. Restart device if needed
5. Re-grant VPN permission

### Time Limit Not Working

**Problem**: Device exceeds time limit

**Solution**:
1. Check time limit is set correctly
2. Verify protection is active
3. Note: Time resets at midnight
4. Manual reset: tap device → Set Time Limit → 0h 0m

### Battery Drain

**Problem**: Battery draining quickly

**Solution**:
1. Battery usage is normal (VPN processing)
2. Disable if not needed
3. Charge device while monitoring
4. Consider reducing monitoring (future update)

## Tips for Best Results

### ✨ Pro Tips

1. **Test First**: Test blocking on your own device before applying to child
2. **Start Broad**: Use category blocks first, then add specific sites
3. **Be Consistent**: Keep protection running during internet hours
4. **Check Regularly**: Review devices tab daily for new connections
5. **Communicate**: Tell children about monitoring (builds trust)

### ⚠️ Important Notes

- VPN must stay active for filtering to work
- Hotspot must remain enabled
- Some apps/games may detect VPN
- Time limits reset at midnight
- Statistics reset daily
- Offline apps work normally

### 🔐 Privacy Notes

- All data stays on your device
- No cloud services involved
- No tracking or analytics
- No personal data collected
- Settings stored locally only

## Quick Reference

| Task | Tab | Action |
|------|-----|--------|
| Start/Stop Protection | Dashboard | Tap toggle button |
| View Devices | Devices | See connected devices |
| Block Website | Blocked Sites | Enter URL, tap Add |
| Set Time Limit | Time Limits | Enter hours/minutes |
| Enable Quiet Hours | Time Limits | Toggle switch ON |
| Block Device | Devices | Tap Block button |

## Need More Help?

- 📖 Read the full [README.md](README.md)
- 🔍 Check [FEATURES.md](FEATURES.md) for details
- 🐛 Report issues on GitHub
- 💬 Ask questions in Discussions

## Next Steps

Now that PocketFence is running:

1. ✅ Add commonly used websites to blocklist
2. ✅ Set appropriate time limits for age
3. ✅ Configure quiet hours for sleep time
4. ✅ Monitor device connections regularly
5. ✅ Adjust settings based on behavior

---

**Congratulations!** 🎉 You're now protecting your child's internet access with PocketFence.

For advanced features and customization, see the full documentation.

**Stay Safe!** 🛡️

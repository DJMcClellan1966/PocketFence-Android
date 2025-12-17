package com.pocketfence.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketfence.android.R
import com.pocketfence.android.databinding.ItemChildDeviceBinding
import com.pocketfence.android.model.ChildDevice

class ChildDeviceAdapter(
    private val onWifiToggle: (ChildDevice, Boolean) -> Unit,
    private val onCellularToggle: (ChildDevice, Boolean) -> Unit,
    private val onRemove: (ChildDevice) -> Unit
) : ListAdapter<ChildDevice, ChildDeviceAdapter.ChildDeviceViewHolder>(ChildDeviceDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildDeviceViewHolder {
        val binding = ItemChildDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChildDeviceViewHolder(binding, onWifiToggle, onCellularToggle, onRemove)
    }
    
    override fun onBindViewHolder(holder: ChildDeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ChildDeviceViewHolder(
        private val binding: ItemChildDeviceBinding,
        private val onWifiToggle: (ChildDevice, Boolean) -> Unit,
        private val onCellularToggle: (ChildDevice, Boolean) -> Unit,
        private val onRemove: (ChildDevice) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(device: ChildDevice) {
            binding.childNameText.text = device.childName
            binding.deviceNameText.text = device.deviceName
            
            // Show device ID if available
            if (device.macAddress.isNotEmpty()) {
                binding.deviceIdText.text = device.macAddress
                binding.deviceIdText.visibility = View.VISIBLE
            } else {
                binding.deviceIdText.visibility = View.GONE
            }
            
            // Set access status badge
            binding.accessStatusBadge.text = device.getAccessStatusText()
            binding.accessStatusBadge.setBackgroundColor(
                when {
                    !device.isWifiEnabled && !device.isCellularEnabled -> 
                        binding.root.context.getColor(R.color.status_inactive)
                    device.hasRestrictions -> 
                        binding.root.context.getColor(R.color.status_warning)
                    else -> 
                        binding.root.context.getColor(R.color.status_active)
                }
            )
            
            // Set online/offline status
            binding.onlineStatusText.text = if (device.isOnline) 
                binding.root.context.getString(R.string.status_online) 
            else 
                binding.root.context.getString(R.string.status_offline)
            binding.onlineStatusText.setTextColor(
                if (device.isOnline)
                    binding.root.context.getColor(R.color.status_active)
                else
                    binding.root.context.getColor(R.color.text_secondary)
            )
            
            // Network type
            binding.networkTypeText.text = when (device.networkType.name) {
                "WIFI" -> "WiFi"
                "CELLULAR" -> "Cellular"
                "OFFLINE" -> "Offline"
                else -> "Unknown"
            }
            
            // WiFi toggle
            binding.wifiSwitch.setOnCheckedChangeListener(null)
            binding.wifiSwitch.isChecked = device.isWifiEnabled
            binding.wifiSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != device.isWifiEnabled) {
                    onWifiToggle(device, isChecked)
                }
            }
            
            // Cellular toggle
            binding.cellularSwitch.setOnCheckedChangeListener(null)
            binding.cellularSwitch.isChecked = device.isCellularEnabled
            binding.cellularSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != device.isCellularEnabled) {
                    onCellularToggle(device, isChecked)
                }
            }
            
            // Remove button
            binding.removeButton.setOnClickListener {
                onRemove(device)
            }
        }
    }
    
    private class ChildDeviceDiffCallback : DiffUtil.ItemCallback<ChildDevice>() {
        override fun areItemsTheSame(oldItem: ChildDevice, newItem: ChildDevice): Boolean {
            return oldItem.deviceId == newItem.deviceId
        }
        
        override fun areContentsTheSame(oldItem: ChildDevice, newItem: ChildDevice): Boolean {
            return oldItem == newItem
        }
    }
}

package com.pocketfence.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketfence.android.R
import com.pocketfence.android.databinding.ItemDeviceBinding
import com.pocketfence.android.model.ConnectedDevice

class DeviceAdapter(
    private val onBlockClick: (ConnectedDevice) -> Unit,
    private val onSetTimeLimitClick: (ConnectedDevice) -> Unit
) : ListAdapter<ConnectedDevice, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onBlockClick, onSetTimeLimitClick)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onBlockClick: (ConnectedDevice) -> Unit,
        private val onSetTimeLimitClick: (ConnectedDevice) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(device: ConnectedDevice) {
            binding.deviceNameText.text = device.deviceName
            binding.deviceMacText.text = device.macAddress
            binding.deviceIpText.text = device.ipAddress
            
            if (device.isBlocked) {
                binding.blockedBadge.visibility = View.VISIBLE
                binding.blockButton.text = binding.root.context.getString(R.string.unblock_device)
            } else {
                binding.blockedBadge.visibility = View.GONE
                binding.blockButton.text = binding.root.context.getString(R.string.block_device)
            }
            
            binding.timeRemainingText.text = device.getFormattedTimeRemaining()
            
            binding.blockButton.setOnClickListener {
                onBlockClick(device)
            }
            
            binding.setTimeLimitButton.setOnClickListener {
                onSetTimeLimitClick(device)
            }
        }
    }
    
    private class DeviceDiffCallback : DiffUtil.ItemCallback<ConnectedDevice>() {
        override fun areItemsTheSame(oldItem: ConnectedDevice, newItem: ConnectedDevice): Boolean {
            return oldItem.macAddress == newItem.macAddress
        }
        
        override fun areContentsTheSame(oldItem: ConnectedDevice, newItem: ConnectedDevice): Boolean {
            return oldItem == newItem
        }
    }
}

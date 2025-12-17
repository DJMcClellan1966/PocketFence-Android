package com.pocketfence.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pocketfence.android.R
import com.pocketfence.android.databinding.FragmentChildDevicesBinding
import com.pocketfence.android.model.ChildDevice
import com.pocketfence.android.ui.adapter.ChildDeviceAdapter
import com.pocketfence.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Fragment for managing child devices.
 */
@AndroidEntryPoint
class ChildDevicesFragment : Fragment() {
    
    private var _binding: FragmentChildDevicesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: ChildDeviceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = ChildDeviceAdapter(
            onWifiToggle = { device, enabled ->
                showConfirmDialog(
                    title = if (enabled) "Enable WiFi Access?" else "Block WiFi Access?",
                    message = "This will ${if (enabled) "allow" else "block"} WiFi access for ${device.childName}'s ${device.deviceName}",
                    onConfirm = {
                        viewModel.setChildDeviceWifiAccess(device.deviceId, enabled)
                    }
                )
            },
            onCellularToggle = { device, enabled ->
                showConfirmDialog(
                    title = if (enabled) "Enable Cellular Access?" else "Block Cellular Access?",
                    message = "This will ${if (enabled) "allow" else "block"} cellular data for ${device.childName}'s ${device.deviceName}",
                    onConfirm = {
                        viewModel.setChildDeviceCellularAccess(device.deviceId, enabled)
                    }
                )
            },
            onRemove = { device ->
                showConfirmDialog(
                    title = "Remove Device?",
                    message = "Remove ${device.childName}'s ${device.deviceName} from managed devices?",
                    onConfirm = {
                        viewModel.removeChildDevice(device.deviceId)
                    }
                )
            }
        )
        
        binding.childDevicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChildDevicesFragment.adapter
        }
    }
    
    private fun setupFab() {
        binding.addDeviceFab.setOnClickListener {
            showAddDeviceDialog()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.childDevices.collect { devices ->
                adapter.submitList(devices)
                
                if (devices.isEmpty()) {
                    binding.childDevicesRecyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.childDevicesRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
            }
        }
    }
    
    private fun showAddDeviceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_child_device, null)
        val childNameInput = dialogView.findViewById<EditText>(R.id.childNameInput)
        val deviceNameInput = dialogView.findViewById<EditText>(R.id.deviceNameInput)
        val deviceIdInput = dialogView.findViewById<EditText>(R.id.deviceIdInput)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_child_device)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val childName = childNameInput.text.toString().trim()
                val deviceName = deviceNameInput.text.toString().trim()
                val deviceId = deviceIdInput.text.toString().trim()
                
                if (childName.isNotEmpty() && deviceName.isNotEmpty()) {
                    val device = ChildDevice(
                        deviceId = if (deviceId.isNotEmpty()) deviceId else UUID.randomUUID().toString(),
                        deviceName = deviceName,
                        childName = childName,
                        macAddress = deviceId.takeIf { it.isNotEmpty() } ?: "",
                        isWifiEnabled = true,
                        isCellularEnabled = true
                    )
                    viewModel.addChildDevice(device)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

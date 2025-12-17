package com.pocketfence.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketfence.android.databinding.FragmentDevicesBinding
import com.pocketfence.android.ui.adapter.DeviceAdapter
import com.pocketfence.android.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class DevicesFragment : Fragment() {
    
    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DeviceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = DeviceAdapter(
            onBlockClick = { device ->
                viewModel.blockDevice(device.macAddress, !device.isBlocked)
            },
            onSetTimeLimitClick = { device ->
                showTimeLimitDialog(device.macAddress)
            }
        )
        
        binding.devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DevicesFragment.adapter
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectedDevices.collect { devices ->
                adapter.submitList(devices)
                
                if (devices.isEmpty()) {
                    binding.devicesRecyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.devicesRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
            }
        }
    }
    
    private fun showTimeLimitDialog(macAddress: String) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        
        // Simple time limit dialog (can be enhanced with custom layout)
        val hours = arrayOf("No Limit", "1 Hour", "2 Hours", "3 Hours", "4 Hours", "6 Hours", "8 Hours")
        
        dialog.setTitle("Set Time Limit")
            .setItems(hours) { _, which ->
                val hourValue = when (which) {
                    0 -> 0
                    else -> which
                }
                viewModel.setDeviceTimeLimit(macAddress, hourValue, 0)
            }
            .setNegativeButton("Cancel", null)
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

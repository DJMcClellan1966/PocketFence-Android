package com.pocketfence.android.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pocketfence.android.databinding.FragmentTimeLimitsBinding
import com.pocketfence.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for managing time limits and quiet hours.
 */
@AndroidEntryPoint
class TimeLimitsFragment : Fragment() {
    
    private var _binding: FragmentTimeLimitsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()
    
    private var startHour = 22
    private var startMinute = 0
    private var endHour = 7
    private var endMinute = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeLimitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.setTimeLimitButton.setOnClickListener {
            val hoursText = binding.hoursInput.text.toString()
            val minutesText = binding.minutesInput.text.toString()
            
            val hours = hoursText.toIntOrNull() ?: 0
            val minutes = minutesText.toIntOrNull() ?: 0
            
            // Validate hours (0-23) and minutes (0-59)
            if (hours < 0 || hours > 23) {
                Toast.makeText(requireContext(), "Hours must be between 0 and 23", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (minutes < 0 || minutes > 59) {
                Toast.makeText(requireContext(), "Minutes must be between 0 and 59", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (hours == 0 && minutes == 0) {
                Toast.makeText(requireContext(), "Time limit set to unlimited", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Daily limit set to ${hours}h ${minutes}m", Toast.LENGTH_SHORT).show()
            }
            
            viewModel.setDailyTimeLimit(hours, minutes)
        }
        
        binding.quietHoursSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.quietHoursSettings.visibility = if (isChecked) View.VISIBLE else View.GONE
            
            if (isChecked) {
                viewModel.setQuietHours(true, startHour, startMinute, endHour, endMinute)
            } else {
                viewModel.setQuietHours(false, startHour, startMinute, endHour, endMinute)
            }
        }
        
        binding.startTimeButton.setOnClickListener {
            showTimePicker(true)
        }
        
        binding.endTimeButton.setOnClickListener {
            showTimePicker(false)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timeLimit.collect { timeLimit ->
                val hours = timeLimit.dailyLimitMs / (1000 * 60 * 60)
                val minutes = (timeLimit.dailyLimitMs % (1000 * 60 * 60)) / (1000 * 60)
                
                binding.hoursInput.setText(hours.toString())
                binding.minutesInput.setText(minutes.toString())
                
                binding.quietHoursSwitch.isChecked = timeLimit.quietHoursEnabled
                binding.quietHoursSettings.visibility = 
                    if (timeLimit.quietHoursEnabled) View.VISIBLE else View.GONE
                
                startHour = timeLimit.quietHoursStart / 60
                startMinute = timeLimit.quietHoursStart % 60
                endHour = timeLimit.quietHoursEnd / 60
                endMinute = timeLimit.quietHoursEnd % 60
                
                updateTimeButtons()
            }
        }
    }
    
    private fun showTimePicker(isStartTime: Boolean) {
        val hour = if (isStartTime) startHour else endHour
        val minute = if (isStartTime) startMinute else endMinute
        
        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                if (isStartTime) {
                    startHour = selectedHour
                    startMinute = selectedMinute
                } else {
                    endHour = selectedHour
                    endMinute = selectedMinute
                }
                
                updateTimeButtons()
                
                if (binding.quietHoursSwitch.isChecked) {
                    viewModel.setQuietHours(true, startHour, startMinute, endHour, endMinute)
                }
            },
            hour,
            minute,
            true
        ).show()
    }
    
    private fun updateTimeButtons() {
        binding.startTimeButton.text = String.format("%02d:%02d", startHour, startMinute)
        binding.endTimeButton.text = String.format("%02d:%02d", endHour, endMinute)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.pocketfence.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketfence.android.R
import com.pocketfence.android.databinding.FragmentBlockedSitesBinding
import com.pocketfence.android.model.WebsiteCategory
import com.pocketfence.android.ui.adapter.BlockedWebsiteAdapter
import com.pocketfence.android.util.NetworkUtils
import com.pocketfence.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for managing blocked websites.
 */
@AndroidEntryPoint
class BlockedSitesFragment : Fragment() {
    
    private var _binding: FragmentBlockedSitesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: BlockedWebsiteAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedSitesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.addWebsiteButton.setOnClickListener {
            val url = binding.websiteUrlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!NetworkUtils.isValidUrl(url)) {
                Toast.makeText(requireContext(), R.string.error_invalid_url, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val normalizedUrl = NetworkUtils.normalizeUrl(url)
            viewModel.addBlockedWebsite(normalizedUrl)
            binding.websiteUrlInput.text?.clear()
            Toast.makeText(requireContext(), "Added $normalizedUrl", Toast.LENGTH_SHORT).show()
        }
        
        binding.chipSocialMedia.setOnClickListener {
            viewModel.addPresetCategory(WebsiteCategory.SOCIAL_MEDIA)
            Toast.makeText(requireContext(), "Added social media sites", Toast.LENGTH_SHORT).show()
        }
        
        binding.chipAdultContent.setOnClickListener {
            viewModel.addPresetCategory(WebsiteCategory.ADULT_CONTENT)
            Toast.makeText(requireContext(), "Added adult content sites", Toast.LENGTH_SHORT).show()
        }
        
        binding.chipGambling.setOnClickListener {
            viewModel.addPresetCategory(WebsiteCategory.GAMBLING)
            Toast.makeText(requireContext(), "Added gambling sites", Toast.LENGTH_SHORT).show()
        }
        
        binding.chipGaming.setOnClickListener {
            viewModel.addPresetCategory(WebsiteCategory.GAMING)
            Toast.makeText(requireContext(), "Added gaming sites", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = BlockedWebsiteAdapter { website ->
            showDeleteConfirmation(website.url)
        }
        
        binding.blockedSitesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BlockedSitesFragment.adapter
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.blockedWebsites.collect { websites ->
                adapter.submitList(websites)
                
                if (websites.isEmpty()) {
                    binding.blockedSitesRecyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.blockedSitesRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
            }
        }
    }
    
    private fun showDeleteConfirmation(url: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_website_title)
            .setMessage(getString(R.string.delete_website_message, url))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.removeBlockedWebsite(url)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

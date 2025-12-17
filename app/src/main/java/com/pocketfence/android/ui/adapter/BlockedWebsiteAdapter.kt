package com.pocketfence.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketfence.android.databinding.ItemBlockedWebsiteBinding
import com.pocketfence.android.model.BlockedWebsite

class BlockedWebsiteAdapter(
    private val onDeleteClick: (BlockedWebsite) -> Unit
) : ListAdapter<BlockedWebsite, BlockedWebsiteAdapter.WebsiteViewHolder>(WebsiteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebsiteViewHolder {
        val binding = ItemBlockedWebsiteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WebsiteViewHolder(binding, onDeleteClick)
    }
    
    override fun onBindViewHolder(holder: WebsiteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class WebsiteViewHolder(
        private val binding: ItemBlockedWebsiteBinding,
        private val onDeleteClick: (BlockedWebsite) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(website: BlockedWebsite) {
            binding.websiteUrlText.text = website.url
            // Safe category name handling with proper formatting
            val categoryName = website.category?.name ?: "CUSTOM"
            binding.websiteCategoryText.text = categoryName.replace('_', ' ').lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            
            binding.deleteButton.setOnClickListener {
                onDeleteClick(website)
            }
        }
    }
    
    private class WebsiteDiffCallback : DiffUtil.ItemCallback<BlockedWebsite>() {
        override fun areItemsTheSame(oldItem: BlockedWebsite, newItem: BlockedWebsite): Boolean {
            return oldItem.url == newItem.url
        }
        
        override fun areContentsTheSame(oldItem: BlockedWebsite, newItem: BlockedWebsite): Boolean {
            return oldItem == newItem
        }
    }
}

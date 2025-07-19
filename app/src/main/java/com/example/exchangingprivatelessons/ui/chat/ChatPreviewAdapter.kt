package com.example.exchangingprivatelessons.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exchangingprivatelessons.databinding.ItemChatPreviewBinding
import com.example.exchangingprivatelessons.domain.model.ViewChatPreview

class ChatPreviewAdapter(
    private val onClick: (String, String) -> Unit
) : ListAdapter<ViewChatPreview, ChatPreviewAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemChatPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    inner class VH(private val b: ItemChatPreviewBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: ViewChatPreview) = with(b) {
            displayName.text = item.peerName
            lastMessage.text = item.lastMessage
            time.text        = item.formattedTime
            root.setOnClickListener { onClick(item.chatId, item.peerName) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ViewChatPreview>() {
            override fun areItemsTheSame(o: ViewChatPreview, n: ViewChatPreview) =
                o.chatId == n.chatId
            override fun areContentsTheSame(o: ViewChatPreview, n: ViewChatPreview) = o == n
        }
    }
}

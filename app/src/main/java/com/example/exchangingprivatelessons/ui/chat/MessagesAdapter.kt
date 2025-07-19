package com.example.exchangingprivatelessons.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exchangingprivatelessons.databinding.ItemMessageLeftBinding
import com.example.exchangingprivatelessons.databinding.ItemMessageRightBinding
import com.example.exchangingprivatelessons.domain.model.ViewMessage

/**
 * ▪ LEFT  – הודעות של המשתמש האחר
 * ▪ RIGHT – ההודעות שלי (currentUid)
 */
class MessagesAdapter(
    private val currentUid: String
) : ListAdapter<ViewMessage, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val LEFT  = 0
        private const val RIGHT = 1

        private val DIFF = object : DiffUtil.ItemCallback<ViewMessage>() {
            override fun areItemsTheSame(o: ViewMessage, n: ViewMessage) = o.id == n.id
            override fun areContentsTheSame(o: ViewMessage, n: ViewMessage) = o == n
        }
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).senderId == currentUid) RIGHT else LEFT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == RIGHT) {
            RightVH(
                ItemMessageRightBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            LeftVH(
                ItemMessageLeftBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LeftVH  -> holder.bind(getItem(position))
            is RightVH -> holder.bind(getItem(position))
        }
    }

    /* ---------------- ViewHolders ---------------- */

    private class LeftVH(
        private val b: ItemMessageLeftBinding
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(msg: ViewMessage) = with(b) {
            textMsg.text  = msg.text   // ← ids מה‑XML: textMsg / textTime
            textTime.text = msg.time
        }
    }

    private class RightVH(
        private val b: ItemMessageRightBinding
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(msg: ViewMessage) = with(b) {
            text.text  = msg.text      // ← ids מה‑XML: text / time
            time.text  = msg.time
        }
    }
}

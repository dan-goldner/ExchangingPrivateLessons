package com.example.exchangingprivatelessons.ui.request

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.common.util.asClockOrDate
import com.example.exchangingprivatelessons.databinding.ItemRequestCardBinding

class RequestAdapter(
    private val approve: (String) -> Unit,
    private val decline: (String) -> Unit,
    private val cancel : (String) -> Unit
) : ListAdapter<ViewRequestItem, RequestAdapter.VH>(Diff) {

    /* ---------- Diff ---------- */
    private companion object Diff : DiffUtil.ItemCallback<ViewRequestItem>() {
        override fun areItemsTheSame(a: ViewRequestItem, b: ViewRequestItem) = a.id == b.id
        override fun areContentsTheSame(a: ViewRequestItem, b: ViewRequestItem) = a == b
    }

    /* ---------- onCreate / onBind ---------- */
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        ItemRequestCardBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    /* ---------- View‑Holder ---------- */
    inner class VH(private val b: ItemRequestCardBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ViewRequestItem) = with(b) {

            titleTv.text = item.lessonTitle

            /* -------- תת‑כותרת -------- */
            subtitleTv.text = if (item.canRespond) {
                "Requested by: ${item.requesterName} · ${item.requestedAt.asClockOrDate()}"
            } else {
                "${item.ownerName} · ${item.requestedAt.asClockOrDate()}"
            }

            /* -------- אוואטר -------- */
            val photoUrl = if (item.canRespond) item.requesterPhotoUrl else item.ownerPhotoUrl
            avatarIv.load(photoUrl) {
                placeholder(R.drawable.ic_profile_placeholder)
                error      (R.drawable.ic_profile_placeholder)
            }

            /* -------- סטטוס -------- */
            statusChip.text       = item.status.name
            statusChip.isClickable = false       // לא להיראות ככפתור
            statusChip.isCheckable = false

            /* -------- כפתורים -------- */
            approveBtn.isVisible = item.canRespond
            declineBtn.isVisible = item.canRespond
            cancelBtn .isVisible = item.canCancel
            actionsRow.isVisible = approveBtn.isVisible || cancelBtn.isVisible

            if (item.canRespond) {
                approveBtn.setOnClickListener { approve(item.id) }
                declineBtn.setOnClickListener { decline(item.id) }
            }
            if (item.canCancel) {
                cancelBtn.setOnClickListener { cancel(item.id) }
            }
        }
    }
}

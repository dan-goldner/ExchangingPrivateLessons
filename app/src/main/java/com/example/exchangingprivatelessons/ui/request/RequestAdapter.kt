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
import com.example.exchangingprivatelessons.common.util.asFullDateTime
import com.example.exchangingprivatelessons.databinding.ItemRequestCardBinding
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.model.ViewRequestItem

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
    /* ---------- View‑Holder ---------- */
    inner class VH(private val b: ItemRequestCardBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ViewRequestItem) = with(b) {

            /* --- Avatar (ללא שינוי) --- */
            val photoUrl = if (item.viewMode == RequestsViewModel.Mode.RECEIVED)
                item.requesterPhotoUrl
            else item.ownerPhotoUrl
            avatarIv.load(photoUrl) {
                placeholder(R.drawable.ic_profile_placeholder)
                error      (R.drawable.ic_profile_placeholder)
            }

            /* --- Title (שם השיעור) --- */
            titleTv.text = item.lessonTitle

            /* -------- Subtitle (2 שורות גמישות) -------- */
            val rqTime = item.requestedAt.asFullDateTime()
            val rsTime = item.respondedAt
                ?.takeIf { it > 0 }        // ← מסנן 0
                ?.asFullDateTime()


            val line1 = if (item.viewMode == RequestsViewModel.Mode.RECEIVED)
                "Requested by ${item.requesterName} at $rqTime"
            else
                "Requested to ${item.ownerName} at $rqTime"

            val line2 = when {
                rsTime == null -> null                                         // עדיין בהמתנה
                item.viewMode == RequestsViewModel.Mode.RECEIVED ->
                    if (item.status == RequestStatus.Approved)
                        "You approved at $rsTime"
                    else
                        "You declined at $rsTime"
                else /* SENT */ ->
                    if (item.status == RequestStatus.Approved)
                        "He approved at $rsTime"
                    else
                        "He declined at $rsTime"
            }

            subtitleTv.text = buildString {
                append(line1)
                line2?.let { append('\n'); append(it) }
            }

            /* --- Status chip (ללא שינוי) --- */
            statusChip.text        = item.status.name
            statusChip.isClickable = false
            statusChip.isCheckable = false

            /* --- Action buttons (ללא שינוי) --- */
            approveBtn.isVisible = item.canRespond
            declineBtn.isVisible = item.canRespond
            cancelBtn .isVisible = item.canCancel
            actionsRow.isVisible = approveBtn.isVisible || cancelBtn.isVisible

            if (item.canRespond) {
                approveBtn.setOnClickListener { approve(item.id) }
                declineBtn.setOnClickListener { decline(item.id) }
            }
            if (item.canCancel)   cancelBtn.setOnClickListener { cancel(item.id) }
        }
    }
}

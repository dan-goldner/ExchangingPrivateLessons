package com.example.exchangingprivatelessons.ui.lesson

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.RowLessonBinding
import com.example.exchangingprivatelessons.domain.model.ViewLesson

class LessonAdapter(
    private val click:   (String) -> Unit,
    private val archive: (String, Boolean) -> Unit
) : ListAdapter<ViewLesson, LessonAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        RowLessonBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    /* -------- View‑Holder -------- */
    inner class VH(private val row: RowLessonBinding) : RecyclerView.ViewHolder(row.root) {
        fun bind(v: ViewLesson) = with(row) {

            /* טקסטים */
            titleTv.text       = v.title
            descPreviewTv.text = v.description
            dateTv.text        = "Listed at: ${formatDate(v.createdAt)}"
            ratingTv.text      = "Lesson Rating: %.1f".format(v.ratingAvg)

            /* ⬇️ תמונת בעל‑השיעור */
            lessonImg.load(v.ownerPhotoUrl?.takeIf { it.isNotBlank() }) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_placeholder)
                error      (R.drawable.ic_profile_placeholder)
                fallback   (R.drawable.ic_profile_placeholder)
            }

            /* ארכוב */
            archiveBtn.run {
                setIconResource(
                    if (v.archived) R.drawable.ic_unarchive else R.drawable.ic_archive
                )
                setOnClickListener { archive(v.id, !v.archived) }
                isVisible = v.canArchive
            }

            root.setOnClickListener { click(v.id) }
        }
    }

    private class Diff : DiffUtil.ItemCallback<ViewLesson>() {
        override fun areItemsTheSame(a: ViewLesson, b: ViewLesson) = a.id == b.id
        override fun areContentsTheSame(a: ViewLesson, b: ViewLesson) = a == b
    }
}

package com.example.exchangingprivatelessons.ui.lesson

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.RowLessonBinding

class LessonAdapter(
    private val click   : (String) -> Unit,
    private val archive : (String, Boolean) -> Unit
) : ListAdapter<LessonItem, LessonAdapter.VH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        RowLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    inner class VH(private val row: RowLessonBinding) : RecyclerView.ViewHolder(row.root) {
        fun bind(item: LessonItem) = with(row) {
            titleTv.text       = item.title
            descPreviewTv.text = item.description
            dateTv.text        = "Listed at: ${item.date}"
            ratingTv.text      = "Lesson Rating: ${item.rating}"

            // ⬅️  הנה השורה/בלוק החדש
            lessonImg.load(item.imageUrl?.takeIf { it.isNotBlank() }) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_placeholder)
                error      (R.drawable.ic_profile_placeholder)
                fallback   (R.drawable.ic_profile_placeholder)
            }

            archiveBtn.run {
                setIconResource(
                    if (item.archived) R.drawable.ic_unarchive else R.drawable.ic_archive
                )
                setOnClickListener { archive(item.id, !item.archived) }
                isVisible = item.canArchive
            }

            root.setOnClickListener { click(item.id) }
        }
    }



    private class DiffCallback : DiffUtil.ItemCallback<LessonItem>() {
        override fun areItemsTheSame(o: LessonItem, n: LessonItem) = o.id == n.id
        override fun areContentsTheSame(o: LessonItem, n: LessonItem) = o == n
    }

    override fun submitList(list: List<LessonItem>?) {
        Log.d("LessonAdapter", "Submitting list with ${list?.size ?: 0} items")
        super.submitList(list)
    }
}

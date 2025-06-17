package com.example.exchangingprivatelessons.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exchangingprivatelessons.databinding.LessonListRowBinding
import com.example.exchangingprivatelessons.model.Lesson

class LessonsAdapter(
    private var lessons: List<Lesson>,
    private val onClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonsAdapter.VH>() {

    inner class VH(private val b: LessonListRowBinding)
        : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Lesson) {
            b.textViewTitle.text = item.title
            b.textViewDescription.text = item.description
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = LessonListRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount() = lessons.size

    fun updateLessons(newList: List<Lesson>) {
        lessons = newList
        notifyDataSetChanged()
    }
}

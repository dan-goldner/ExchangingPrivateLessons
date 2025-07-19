/* ───────────────────────────  LessonRequest  ───────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable
import com.example.exchangingprivatelessons.domain.model.RequestStatus

@Serializable
data class LessonRequestDto(
    val id: String = "",
    val lessonId: String = "",
    val ownerId: String = "",
    val requesterId: String = "",
    val status: RequestStatus = RequestStatus.Pending,
    val requestedAt: Long? = null,
    val respondedAt: Long? = null        // מתמלא כש-Approved / Declined
)

package com.example.exchangingprivatelessons.domain.model

import com.example.exchangingprivatelessons.ui.request.RequestsViewModel

data class ViewRequestItem(
    val id: String,

    /* Lesson */
    val lessonTitle     : String,

    /* Owner (= המורה) */
    val ownerName       : String,
    val ownerPhotoUrl   : String?,

    /* Requester (= התלמיד) */
    val requesterName   : String,
    val requesterPhotoUrl: String?,

    /* Meta */
    val requestedAt : Long,
    val status      : RequestStatus,

    val respondedAt: Long?,

    /* Permissions (מחושבים ב‑Use‑Case) */
    val canRespond : Boolean,
    val canCancel  : Boolean,

    /* For adapter */
    val viewMode   : RequestsViewModel.Mode
)

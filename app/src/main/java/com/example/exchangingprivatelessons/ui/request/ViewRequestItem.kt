package com.example.exchangingprivatelessons.ui.request

import com.example.exchangingprivatelessons.domain.model.RequestStatus

data class ViewRequestItem(
    val id: String,

    /* Lesson */
    val lessonTitle     : String,

    /* Owner (= המורה) */
    val ownerName     : String,
    val ownerPhotoUrl : String?,

    /* Requester (= התלמיד) */
    val requesterName     : String,
    val requesterPhotoUrl : String?,

    /* Meta */
    val requestedAt : Long,
    val status      : RequestStatus,

    /* Permissions */
    val canRespond : Boolean,   // אני בעל‑השיעור
    val canCancel  : Boolean,    // אני המבקש

)

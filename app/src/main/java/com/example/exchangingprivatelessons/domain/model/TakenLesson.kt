package com.example.exchangingprivatelessons.domain.model

/**
 * Lesson that the **current** user קיבל אישור להצטרף אליו.
 *
 * ‑ ה‐ Lesson המלא (לצרכי UI)
 * ‑ פרטי המורה להצגה ברשימה
 * ‑ יכולת דירוג (business‑rule – נקבעה ע״י השרת)
 */
data class TakenLesson(
    val lesson        : Lesson,
    val ownerName     : String,
    val ownerPhotoUrl : String?,
    val takenAt       : Long,     // ✅ Long
    val canRate       : Boolean
)

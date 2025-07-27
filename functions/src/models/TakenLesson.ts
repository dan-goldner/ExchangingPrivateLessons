// src/models/TakenLesson.ts
import { Lesson } from "./Lesson";   // יבוא נכון

export interface TakenLesson {
  lessonId      : string
  ownerId       : string
  lesson        : Lesson               // עובד כי Lesson מיוצא מה‑model
  ownerName     : string
  ownerPhotoUrl : string
  canRate       : boolean
  takenAt       : FirebaseFirestore.FieldValue
}

import type { FirestoreDataConverter } from "firebase-admin/firestore";
import { TS } from "./common";

export enum LessonStatus {
  Active   = "Active",
  Archived = "Archived",
}

export interface Lesson {
  ownerId: string;
  title: string;
  description: string;
  status: LessonStatus;
  ratingSum: number;
  ratingCount: number;
  createdAt: TS;
  lastUpdatedAt: TS;
}

export const lessonConverter: FirestoreDataConverter<Lesson> = {
  toFirestore: (l) => l,
  fromFirestore: (snap) => snap.data() as Lesson,
};


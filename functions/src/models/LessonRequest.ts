import type { FirestoreDataConverter } from "firebase-admin/firestore";
import { TS } from "./common";

export enum RequestStatus {
  Pending  = "Pending",
  Approved = "Approved",
  Declined = "Declined",
}

export interface LessonRequest {
  lessonId: string;
  ownerId: string;
  requesterId: string;
  status: RequestStatus;
  requestedAt: TS;
  respondedAt: TS | null;
}

export const lessonRequestConverter: FirestoreDataConverter<LessonRequest> = {
  toFirestore: (r) => r,
  fromFirestore: (snap) => snap.data() as LessonRequest,
};


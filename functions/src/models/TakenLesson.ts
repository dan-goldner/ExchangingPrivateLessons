import { TS } from "./common";

export interface TakenLesson {
  lessonId: string;
  ownerId: string;
  takenAt: TS;
}
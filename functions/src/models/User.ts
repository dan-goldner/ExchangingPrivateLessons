import type { FirestoreDataConverter } from "firebase-admin/firestore";
import { TS } from "./common";

export interface User {
  displayName: string;
  email: string;
  photoUrl: string;
  bio: string;
  score: number;
  createdAt: TS;
  lastLoginAt: TS;
  lastUpdatedAt?: TS;
}

export const userConverter: FirestoreDataConverter<User> = {
  toFirestore: (u) => u,
  fromFirestore: (snap) => snap.data() as User,
};

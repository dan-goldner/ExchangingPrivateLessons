import type { FirestoreDataConverter } from "firebase-admin/firestore";
import { TS } from "./common";

export interface Message {
  senderId: string;
  text: string;
  sentAt: TS;
}

export const messageConverter: FirestoreDataConverter<Message> = {
  toFirestore: (m) => m,
  fromFirestore: (snap) => snap.data() as Message,
};

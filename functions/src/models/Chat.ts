import { TS } from "./common";

export interface Chat {
  participantIds: [string, string];
  lastMessage: string;
  lastMessageAt: TS;
}
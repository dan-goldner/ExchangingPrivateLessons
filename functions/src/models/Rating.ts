import { TS } from "./common";

export interface Rating {
  numericValue: 1 | 2 | 3 | 4 | 5;
  comment?: string;
  ratedAt: TS;
}

//import * as functionsV1 from "firebase-functions/v1";

import { setGlobalOptions } from "firebase-functions/v2";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import {
  onDocumentCreated,
  onDocumentDeleted,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";

import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";

import { User } from "./models/User";
import { Lesson, LessonStatus } from "./models/Lesson";
import { LessonRequest, RequestStatus } from "./models/LessonRequest";
import { TakenLesson } from "./models/TakenLesson";
import { Rating } from "./models/Rating";
import { Chat } from "./models/Chat";
import { Message, messageConverter } from "./models/Message";


setGlobalOptions({ region: "me-west1", maxInstances: 10 });

/* ────────────────────────────────────────────────────────────────────────── */
/* Init                                    */
/* ────────────────────────────────────────────────────────────────────────── */
admin.initializeApp();
const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue;

/* ────────────────────────────────────────────────────────────────────────── */
/* Utils                                   */
/* ────────────────────────────────────────────────────────────────────────── */
type FBError = Error & { code?: string };
export const clampScore = (s: number) => Math.max(s, -3);

export const assertAuth = (uid?: string): string => {
  if (!uid) throw new HttpsError("unauthenticated", "Login required");
  return uid;
};

export const chatIdFor = (a: string, b: string) => [a, b].sort().join("_");

/* ────────────────────────────────────────────────────────────────────────── */
/* Firestore Typed Converters                         */
/* ────────────────────────────────────────────────────────────────────────── */
function conv<T>() {
  return {
    toFirestore: (data: T) => data as FirebaseFirestore.DocumentData,
    fromFirestore: (snap: FirebaseFirestore.QueryDocumentSnapshot) =>
      snap.data() as T,
  };
}

// Top-level collections
const usersCol   = db.collection("users").withConverter(conv<User>());
const lessonsCol = db.collection("lessons").withConverter(conv<Lesson>());
const lessonReqCol = db.collection("lessonRequests").withConverter(
  conv<LessonRequest>()
);
const chatsCol   = db.collection("chats").withConverter(conv<Chat>());

// Sub-collections
const takenLessonsCol = (uid: string) =>
  usersCol.doc(uid).collection("takenLessons").withConverter(conv<TakenLesson>());

const ratingsCol = (lessonId: string) =>
  lessonsCol.doc(lessonId).collection("ratings").withConverter(conv<Rating>());


const messagesCol = (chatId: string) =>
   chatsCol.doc(chatId).collection("messages").withConverter(messageConverter);


/* ────────────────────────────────────────────────────────────────────────── */
/* TRIGGERS                                  */
/* ────────────────────────────────────────────────────────────────────────── */

export const signInOrUp = onCall<{
  email: string;
  password: string;
  displayName?: string;
  bio?: string;
}>(async ({ data }) => {
  const email       = data.email?.trim().toLowerCase() ?? "";
  const password    = data.password?.trim()            ?? "";
  const displayName = data.displayName?.trim()         ?? "";
  const bio         = data.bio?.trim()                 ?? "";

  if (!email || !password) {
    throw new HttpsError("invalid-argument", "Missing email / password");
  }

  /* ---------- 1.  Auth ---------- */
  let userRec: admin.auth.UserRecord | null = null;

  try {
    // 👈 ל‑TS ברור שהשמה תתבצע כאן אם לא נזרק Exception
    userRec = await admin.auth().getUserByEmail(email);
  } catch (err) {
    const e = err as { code?: string };
    if (e.code === "auth/user-not-found") {
      if (displayName || bio) {
        userRec = await admin.auth().createUser({ email, password, displayName });
      } else {
        throw new HttpsError("not-found", "User does not exist");
      }
    } else {
      throw err;
    }
  }


  if (!userRec) throw new HttpsError("internal", "Auth failed");

  /* ---------- 2.  Firestore user‑doc ---------- */
  const userRef = usersCol.doc(userRec.uid);

  const initData = {
    displayName,
    email,
    photoUrl   : userRec.photoURL ?? "",
    bio,
    score      : 0,
    createdAt  : FieldValue.serverTimestamp(),
    lastLoginAt: FieldValue.serverTimestamp(),
  };

  await userRef.set(initData, { merge: true });

  const doc = (await userRef.get()).data() as User;

    const toMillis = (ts?: FirebaseFirestore.Timestamp | null): number =>
      ts ? ts.toMillis() : 0;

    return {
      uid         : userRec.uid,
      displayName : doc.displayName,
      email       : doc.email,
      photoUrl    : doc.photoUrl,
      bio         : doc.bio,
      score       : doc.score,
      createdAt   : toMillis(doc.createdAt  as FirebaseFirestore.Timestamp | null),
      lastLoginAt : toMillis(doc.lastLoginAt as FirebaseFirestore.Timestamp | null),
    };
});



/** 2️⃣ Firestore user deleted → remove Auth */
export const onUserDocDelete = onDocumentDeleted(
  { document: "users/{uid}" },
  async ({ params: { uid } }) => {
    try {
      await admin.auth().deleteUser(uid);
      logger.info(`🗑️ Auth user ${uid} deleted`);
    } catch (e) {
      const err = e as FBError;
      if (err.code === "auth/user-not-found")
        logger.warn(`Auth user ${uid} not found`);
      else logger.error("❌ Delete Auth failed", e);
    }
  }
);

/** 3️⃣ takenLessons ⇄ score */
async function adjustUserScore(uid: string, delta: 1 | -1) {
  const userRef = usersCol.doc(uid);
  await db.runTransaction(async (tx) => {
    const userSnap = await tx.get(userRef);
    const currentScore = userSnap.data()?.score ?? 0;
    tx.update(userRef, { score: clampScore(currentScore + delta) });
  });
  logger.info(`ℹ️ Score ${delta > 0 ? "++" : "--"} for ${uid}`);
}

export const onTakenLessonCreated = onDocumentCreated(
  { document: "users/{uid}/takenLessons/{lessonId}" },
  ({ params }) => adjustUserScore(params.uid, 1)
);

export const onTakenLessonDeleted = onDocumentDeleted(
  { document: "users/{uid}/takenLessons/{lessonId}" },
  ({ params }) => adjustUserScore(params.uid, -1)
);


/** 4️⃣ Rating aggregation */
export const onRatingCreated = onDocumentCreated(
  { document: "lessons/{lessonId}/ratings/{uid}" },
  async ({ params: { lessonId }, data }) => {
    const val = (data?.data() as Rating | undefined)?.numericValue ?? 0;
    await lessonsCol.doc(lessonId).update({
      ratingSum: FieldValue.increment(val),
      ratingCount: FieldValue.increment(1),
    });
  }
);

export const onRatingDeleted = onDocumentDeleted(
  { document: "lessons/{lessonId}/ratings/{uid}" },
  async ({ params: { lessonId }, data }) => {
    const val = (data?.data() as Rating | undefined)?.numericValue ?? 0;
    await lessonsCol.doc(lessonId).update({
      ratingSum: FieldValue.increment(-val),
      ratingCount: FieldValue.increment(-1),
    });
  }
);

export const onRatingUpdated = onDocumentUpdated(
  { document: "lessons/{lessonId}/ratings/{uid}" },
  async ({ params: { lessonId }, data }) => {
    if (!data) return;                      // <-- guard
    const before = (data.before.data() as Rating | undefined)?.numericValue ?? 0;
    const after  = (data.after.data()  as Rating | undefined)?.numericValue ?? 0;

    const diff = after - before;
    if (diff) {
      await lessonsCol.doc(lessonId).update({ ratingSum: FieldValue.increment(diff) });
    }
  }
);



/** 5️⃣ lastMessage sync */
export const onMessageCreated = onDocumentCreated(
  { document: "chats/{chatId}/messages/{messageId}" },
  async ({ params: { chatId }, data }) => {
    const message = data?.data() as Message;
    await chatsCol.doc(chatId).update({
      lastMessage: message.text,
      lastMessageAt: FieldValue.serverTimestamp(),
    });
  }
);


/** 6️⃣ Firestore cleanup when a lesson is deleted */
export const onLessonDeleted = onDocumentDeleted(
  { document: "lessons/{lessonId}" },
  async ({ params: { lessonId }, data }) => {
    const lesson = data?.data() as Lesson | undefined;
    const bw = db.bulkWriter();

    const deleteCollection = async (snap: FirebaseFirestore.QuerySnapshot) => {
      for (const [i, doc] of snap.docs.entries()) {
        bw.delete(doc.ref);
        if ((i + 1) % 450 === 0) await bw.flush(); // avoid >500 ops/commit
      }
      await bw.flush(); // flush tail
    };

    // 🔥 Delete image from Storage
    if (lesson?.imageUrl) {
      const match = lesson.imageUrl.match(/\/([^/]+\/[^/]+\.(png|jpe?g|webp))$/i);
      if (match) {
        try {
          await admin.storage().bucket().file(match[1]).delete();
          logger.info(`🗑️ Lesson image ${match[1]} deleted`);
        } catch {
          logger.warn(`Lesson image ${match[1]} not found`);
        }
      }
    }

    // 🧹 Delete related sub-collections and docs
    const ratings = await ratingsCol(lessonId).get();
    const requests = await lessonReqCol.where("lessonId", "==", lessonId).get();
    const taken = await db
      .collectionGroup("takenLessons")
      .where("lessonId", "==", lessonId)
      .get();

    await deleteCollection(ratings);
    await deleteCollection(requests);
    await deleteCollection(taken);

    try {
      await bw.close();
    } catch (e) {
      logger.error("❌ BulkWriter failed", e);
    }

    logger.info(`♻️ Lesson ${lessonId}: related docs cleaned`);
  }
);


/** 7️⃣ Delete profile picture on user delete */
export const onUserDocDeleteStorage = onDocumentDeleted(
    { document: "users/{uid}" },
    async ({ data }) => {
      const user = data?.data();
      if (!user?.photoUrl) return;

      const match = user.photoUrl.match(/\/([^/]+\/[^/]+\.(png|jpe?g|webp))$/i);
      if (!match) return;

      try {
        await admin.storage().bucket().file(match[1]).delete();
        logger.info(`🗑️ Profile image ${match[1]} deleted`);
      } catch {
        logger.warn(`Profile image ${match[1]} not found`);
      }
    }
);

/** 8️⃣ Delete chat when last participant's account is deleted */
export const onUserDocDeleteChatCleanup = onDocumentDeleted(
  { document: "users/{uid}" },
  async ({ params: { uid } }) => {
    const userChats = await chatsCol.where("participantIds", "array-contains", uid).get();

    await Promise.all(
      userChats.docs.map(async (chatDoc) => {
        const chat = chatDoc.data();
        const otherParticipantId = chat.participantIds.find((id) => id !== uid);

        if (otherParticipantId) {
            const otherUserSnap = await usersCol.doc(otherParticipantId).get();
            if (otherUserSnap.exists) return; // The other user still exists, do nothing
        }

        // If we reach here, the other user doesn't exist or there was no other user.
        const messagesSnap = await messagesCol(chatDoc.id).get();
        const bw = db.bulkWriter();
        messagesSnap.docs.forEach((doc) => bw.delete(doc.ref));
        bw.delete(chatDoc.ref);
        await bw.flush();
        await bw.close();

        logger.info(`🗑️ Chat ${chatDoc.id} deleted (last participant removed)`);
      })
    );
  }
);


/* ────────────────────────────────────────────────────────────────────────── */
/* CALLABLE FUNCTIONS                            */
/* ────────────────────────────────────────────────────────────────────────── */

/** 9️⃣ Delete-my-account */
export const deleteMyAccount = onCall(async ({ auth }) => {
  const uid = assertAuth(auth?.uid);
  await usersCol.doc(uid).delete(); // Triggers onUserDocDelete and related cleanup
  return { status: "ok" };
});

/** 🔟 Lesson requests life-cycle */
type LessonRequestInput = { lessonId: string; ownerId: string };
export const createLessonRequest = onCall<LessonRequestInput>(
  async ({ data, auth }) => {
    const requesterId = assertAuth(auth?.uid);
    const { lessonId, ownerId } = data;

    if (!lessonId?.trim() || !ownerId?.trim())
      throw new HttpsError("invalid-argument", "Invalid lessonId or ownerId");

    const existing = await lessonReqCol
      .where("requesterId", "==", requesterId)
      .where("lessonId", "==", lessonId)
      .where("status", "==", RequestStatus.Pending)
      .limit(1)
      .get();

    if (!existing.empty)
      throw new HttpsError("already-exists", "Request already pending");

    const ref = lessonReqCol.doc();
    await ref.set({
      lessonId,
      ownerId,
      requesterId,
      status: RequestStatus.Pending,
      requestedAt: FieldValue.serverTimestamp(),
      respondedAt: null,
    });

    return { success: true, id: ref.id };
  }
);

type ApprovalInput = { requestId: string };
export const approveLessonRequest = onCall<ApprovalInput>(async ({ data, auth }) => {
  const ownerId = assertAuth(auth?.uid);
  const { requestId } = data;

  if (!requestId?.trim())
    throw new HttpsError("invalid-argument", "Missing requestId");

  const reqRef = lessonReqCol.doc(requestId);

  await db.runTransaction(async (tx) => {
    const reqSnap = await tx.get(reqRef);
    if (!reqSnap.exists)
      throw new HttpsError("not-found", "Request not found");

    const req = reqSnap.data() as LessonRequest;
    if (req.ownerId !== ownerId)
      throw new HttpsError("permission-denied", "Not your request");
    if (req.status !== RequestStatus.Pending)
      throw new HttpsError("failed-precondition", "Request already handled");

    // Update request status
    tx.update(reqRef, { status: RequestStatus.Approved, respondedAt: FieldValue.serverTimestamp() });

    // Create takenLessons entry for the requester
    const takenLessonRef = takenLessonsCol(req.requesterId).doc(req.lessonId);
    tx.set(takenLessonRef, {
        lessonId: req.lessonId,
        ownerId: ownerId,
        takenAt: FieldValue.serverTimestamp(),
    });

    // Create chat between users if it doesn't exist
    const chatId = chatIdFor(ownerId, req.requesterId);
    const chatRef = chatsCol.doc(chatId);
    const chatSnap = await tx.get(chatRef);
    if (!chatSnap.exists) {
      tx.set(chatRef, {
        participantIds: [ownerId, req.requesterId],
        lastMessage: "",
        lastMessageAt: FieldValue.serverTimestamp(),
      });
    }
  });

  return { success: true };
});

export const declineLessonRequest = onCall<ApprovalInput>(async ({ data, auth }) => {
    const ownerId = assertAuth(auth?.uid);
    const { requestId } = data;

    if (!requestId?.trim())
      throw new HttpsError("invalid-argument", "Missing requestId");

    const reqRef = lessonReqCol.doc(requestId);

    await db.runTransaction(async (tx) => {
      const reqSnap = await tx.get(reqRef);
      if (!reqSnap.exists)
        throw new HttpsError("not-found", "Request not found");

      const req = reqSnap.data() as LessonRequest;
      if (req.ownerId !== ownerId)
        throw new HttpsError("permission-denied", "Not your request");
      if (req.status !== RequestStatus.Pending)
        throw new HttpsError("failed-precondition", "Request already handled");

      tx.update(reqRef, { status: RequestStatus.Declined, respondedAt: FieldValue.serverTimestamp() });
    });

    return { success: true };
});

export const cancelLessonRequest = onCall<ApprovalInput>(async ({ data, auth }) => {
  const requesterId = assertAuth(auth?.uid);
  const { requestId } = data;

  if (!requestId?.trim())
    throw new HttpsError("invalid-argument", "Missing requestId");

  const reqRef = lessonReqCol.doc(requestId);

  await db.runTransaction(async (tx) => {
    const reqSnap = await tx.get(reqRef);
    if (!reqSnap.exists)
      throw new HttpsError("not-found", "Request not found");

    const req = reqSnap.data() as LessonRequest;
    if (req.requesterId !== requesterId)
      throw new HttpsError("permission-denied", "Not your request");
    if (req.status !== RequestStatus.Pending)
      throw new HttpsError("failed-precondition", "Already handled by owner");

    tx.delete(reqRef);
  });

  return { success: true };
});

/** 1️⃣1️⃣ Chat functions */
async function getOrCreateChat(uidA: string, uidB: string) {
    const chatId = chatIdFor(uidA, uidB);
    const chatRef = chatsCol.doc(chatId);
    const chatSnap = await chatRef.get();

    if (!chatSnap.exists) {
        await chatRef.set({
            participantIds: [uidA, uidB],
            lastMessage: "",
            lastMessageAt: FieldValue.serverTimestamp(),
        });
    }
    return chatId;
}

export const createChat = onCall<{ peerUid: string }>(async ({ data, auth }) => {
  const uid = assertAuth(auth?.uid);
  const peerUid = data.peerUid?.trim();
  if (!peerUid || peerUid === uid)
    throw new HttpsError("invalid-argument", "Invalid peer user ID");

  const chatId = await getOrCreateChat(uid, peerUid);
  return { chatId };
});

export const sendMessage = onCall<{ chatId: string; text: string }>(
  async ({ data, auth }) => {
    const uid = assertAuth(auth?.uid);
    const { chatId, text } = data;

    const trimmedText = text.trim();
    if (!trimmedText) {
      throw new HttpsError("invalid-argument", "Message text cannot be empty");
    }

    const chatSnap = await chatsCol.doc(chatId).get();
    if (!chatSnap.exists) throw new HttpsError("not-found", "Chat not found");

    const chatData = chatSnap.data() as Chat;
    if (!chatData.participantIds?.includes(uid)) {
      throw new HttpsError("permission-denied", "Not a member of this chat");
    }

    await messagesCol(chatId).add({
      senderId: uid,
      text: trimmedText,
      sentAt: FieldValue.serverTimestamp(),
    });

    return { success: true };
  }
);


/** 1️⃣2️⃣ Lesson management */
type UpdLessonInput = {
  lessonId: string;
  title?: string;
  description?: string;
  imageUrl?: string;
};

export const updateLesson = onCall<UpdLessonInput>(async ({ data, auth }) => {
  const uid = assertAuth(auth?.uid);
  const { lessonId, title, description, imageUrl } = data;

  const lessonRef = lessonsCol.doc(lessonId);
  const lessonSnap = await lessonRef.get();

  if (!lessonSnap.exists)
    throw new HttpsError("not-found", "Lesson not found");
  const lessonData = lessonSnap.data() as Lesson;   // assert once
  if (lessonData.ownerId !== uid)
    throw new HttpsError("permission-denied", "Not your lesson");

  const patch: Partial<Lesson> = {
    lastUpdated: FieldValue.serverTimestamp(),
  };

  if (title !== undefined) {
    const t = title.trim();
    if (!t) throw new HttpsError("invalid-argument", "Title cannot be empty");
    patch.title = t;
  }

  if (description !== undefined) {
    const d = description.trim();
    if (!d)
      throw new HttpsError("invalid-argument", "Description cannot be empty");
    patch.description = d;
  }

  if (imageUrl !== undefined) {
    patch.imageUrl = imageUrl;
  }

  const keys = Object.keys(patch).filter((k) => k !== "lastUpdated");
  if (!keys.length)
    throw new HttpsError("invalid-argument", "Nothing to update");

  await lessonRef.update(patch);
  return { success: true };
});


/** 1️⃣3️⃣ Create / archive lesson */
type CreateLessonInput = { title: string; description: string; imageUrl?: string; };
export const createLesson = onCall<CreateLessonInput>(async ({ data, auth }) => {
  const ownerId = assertAuth(auth?.uid);
  const { title, description, imageUrl = "" } = data;

  const trimmedTitle = title.trim();
  const trimmedDesc = description.trim();
  if (!trimmedTitle || !trimmedDesc)
    throw new HttpsError("invalid-argument", "Title and description are required");

  const newLessonRef = lessonsCol.doc();
  await newLessonRef.set({
    ownerId,
    title: trimmedTitle,
    description: trimmedDesc,
    imageUrl,
    status: LessonStatus.Active,
    ratingSum: 0,
    ratingCount: 0,
    createdAt: FieldValue.serverTimestamp(),
    lastUpdated: FieldValue.serverTimestamp(),
  });

  return { success: true, lessonId: newLessonRef.id };
});

type ArchiveLessonInput = { lessonId: string; archived: boolean };
export const archiveLesson = onCall<ArchiveLessonInput>(async ({ data, auth }) => {
  const uid = assertAuth(auth?.uid);
  const { lessonId, archived } = data;
  const lessonRef = lessonsCol.doc(lessonId);
  const lessonSnap = await lessonRef.get();

  if (!lessonSnap.exists)
    throw new HttpsError("not-found", "Lesson not found");
    const lessonData = lessonSnap.data() as Lesson;   // assert once
    if (lessonData.ownerId !== uid)
    throw new HttpsError("permission-denied", "Not your lesson");

  await lessonRef.update({
    status: archived ? LessonStatus.Archived : LessonStatus.Active,
    lastUpdated: FieldValue.serverTimestamp(),
  });

  if (archived) {
    const takenSnaps = await db.collectionGroup("takenLessons").where("lessonId", "==", lessonId).get();
    const bw = db.bulkWriter();
    takenSnaps.docs.forEach((d) => bw.delete(d.ref));
    await bw.close();
  }

  return { success: true };
});

/** 1️⃣4️⃣ Update profile */
type UpdateProfileInput = { displayName?: string; bio?: string; photoUrl?: string; };
export const updateProfile = onCall<UpdateProfileInput>(async ({ data, auth }) => {
  const uid = assertAuth(auth?.uid);
  const { displayName, bio, photoUrl } = data;

  if (displayName === undefined && bio === undefined && photoUrl === undefined)
    throw new HttpsError("invalid-argument", "Nothing to update");
  if ((displayName?.length ?? 0) > 40)
    throw new HttpsError("invalid-argument", "displayName too long");
  if ((bio?.length ?? 0) > 250)
    throw new HttpsError("invalid-argument", "bio too long");

  const patch: Partial<User> = { lastUpdated: FieldValue.serverTimestamp() };
  if (displayName !== undefined) patch.displayName = displayName.trim();
  if (bio !== undefined) patch.bio = bio.trim();
  if (photoUrl !== undefined) patch.photoUrl = photoUrl.trim();

  await usersCol.doc(uid).update(patch);
  return { success: true };
});

/** 1️⃣5️⃣ Rate lesson */
export const rateLesson = onCall<{ lessonId: string; numericValue: number; comment?: string; }>(
    async ({ data, auth }) => {
      const uid = assertAuth(auth?.uid);
      const { lessonId, numericValue, comment = "" } = data;

      if (!Number.isInteger(numericValue) || numericValue < 1 || numericValue > 5)
        throw new HttpsError("invalid-argument", "rating must be 1-5");

      const lessonSnap = await lessonsCol.doc(lessonId).get();
      if (!lessonSnap.exists)
        throw new HttpsError("not-found", "Lesson not found");
      if (!lessonSnap.exists)
        throw new HttpsError("not-found", "Lesson not found");

      const lessonData = lessonSnap.data() as Lesson;   // assert once
      if (lessonData.ownerId === uid)
        throw new HttpsError("failed-precondition", "Cannot rate your own lesson");


      const ratingRef = ratingsCol(lessonId).doc(uid);

      await db.runTransaction(async (tx) => {
        const prevSnap = await tx.get(ratingRef);
        const trimmedComment = comment.trim();

        if (prevSnap.exists) {
            const prevData = prevSnap.data();
            if (
              prevData &&
              prevData.numericValue === numericValue &&
              (prevData.comment ?? "") === trimmedComment
            ) {
              return; // NO-OP if nothing changed
            }

        }

        const payload: Rating = {
          numericValue: numericValue as Rating["numericValue"],  // 1-5
          ratedAt: FieldValue.serverTimestamp(),
          ...(trimmedComment && { comment: trimmedComment }),
        };

        tx.set(ratingRef, payload, { merge: true });
      });

      return { success: true };
    }
);

/** 1️⃣6️⃣ Touch lastLoginAt */
export const touchLogin = onCall(async ({ auth }) => {
  const uid = assertAuth(auth?.uid);
  await usersCol.doc(uid).update({ lastLoginAt: FieldValue.serverTimestamp() });
  return { ok: true };
});


/** 1️⃣7️⃣ Get my requests by status */
export const getRequestsByStatus = onCall<{ status: string }>(
  async ({ data, auth }) => {
    const uid = assertAuth(auth?.uid);
    const { status } = data;

    // Validate status value using the enum values
    if (!Object.values(RequestStatus).includes(status as RequestStatus)) {
         throw new HttpsError("invalid-argument", "Invalid request status");
    }

    // Query for requests where the user is the requester and status matches
    const snapshot = await lessonReqCol
      .where("requesterId", "==", uid)
      .where("status", "==", status)
      .orderBy("requestedAt", "desc")
      .get();

    const results = snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }));
    return { requests: results };
  }
);


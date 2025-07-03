import * as admin from "firebase-admin";
import * as functions from "firebase-functions/v1";
import * as logger from "firebase-functions/logger";

admin.initializeApp();

/** יצירת מסמך משתמש */
export const createUserDoc = functions.auth.user().onCreate(async (user) => {
  const {uid, email, displayName, photoURL} = user;

  await admin.firestore().doc(`users/${uid}`).set({
    email: email ?? "",
    displayName: displayName ?? "",
    photoUrl: photoURL ?? "",
    bio: "",
    score: 0,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  logger.info(`User ${uid} created`);
});

/** מחיקת מסמך משתמש */
export const deleteUserDoc = functions.auth.user().onDelete(async (user) => {
  await admin.firestore().doc(`users/${user.uid}`).delete();
  logger.info(`User ${user.uid} deleted`);
});

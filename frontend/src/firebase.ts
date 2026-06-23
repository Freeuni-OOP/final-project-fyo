import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyDbpoE0d2N-gaiPx_Lop0U_2rSo_ObCj9w",
  authDomain: "findyouropponent-1e269.firebaseapp.com",
  projectId: "findyouropponent-1e269",
  storageBucket: "findyouropponent-1e269.firebasestorage.app",
  messagingSenderId: "1067196971744",
  appId: "1:1067196971744:web:f6eacf03bcc2682aff6e85",
  measurementId: "G-RCR5HF9X2R"
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
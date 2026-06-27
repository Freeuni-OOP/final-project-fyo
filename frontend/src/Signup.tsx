import { useState } from "react";
import { createUserWithEmailAndPassword } from "firebase/auth"; //function from firebase that creates account.
import { auth } from "./firebase";

const SIGNUP_ENDPOINT = "http://localhost:8081/api/auth/signup";
export default function Signup(){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try{
            const userCredential = await createUserWithEmailAndPassword(
                auth,
                email,
                password
            );

            const tokenId = await userCredential.user.getIdToken();

            //HTTP request to SPRINGBOOT
            const response = await fetch(SIGNUP_ENDPOINT, {
                method: "POST", //we are posting
                headers: {
                    "Content-Type": "application/json", //we are sending JSON file
                    Authorization: `Bearer ${tokenId}`, //attach auth token to request
                },
                body: JSON.stringify({email}), //sending actual data
            });
            
            if(!response.ok){
                throw new Error("Signup failed");
            }
            console.log("Signup Successful");
        }
        catch (err){
            if(err instanceof Error){
                setError(err.message);
            }
            else{
                setError("something went wrong during signup");
            }
        }
        finally{
            setLoading(false);
        }
    }

    return (
    <form onSubmit={handleSubmit}>
      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </div>

      <div>
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
        />
      </div>

      {error && <p style={{ color: "red" }}>{error}</p>}

      <button type="submit" disabled={loading}>
        {loading ? "Signing up..." : "Sign up"}
      </button>
    </form>
  );
}


import React, { HtmlHTMLAttributes, useState } from "react";
import { createUserWithEmailAndPassword } from "firebase/auth"; //function from firebase that creates account.
import { auth } from "./firebase";

const LOGIN_ENDPOINT = "http://localhost:8081/api/auth/login"

export default function Login(){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function  handleSubmit(e:React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try{
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
            const tokenId = await userCredential.user.getIdToken();

            const responce = await fetch(LOGIN_ENDPOINT, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: 'Bearer ${tokenId}',
                },
                body: JSON.stringify({email}),
            });

            if(!responce.ok){
                throw new Error("Login failed");
            }
            console.log("Login Successful");

        }
        catch(er){
            if(er instanceof Error){
                setError(er.message);
            }
            else{
                setError("Something went wrong during login")
            }
        }
        finally{
            setLoading(false);
        }
    }

    return(
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
                />
            </div>
        
            {error && <p style={{ color: "red" }}>{error}</p>}
        
            <button type="submit" disabled={loading}>
                {loading ? "Logging in..." : "Log in"}
            </button>
        </form>
    );
}
"use client";
import React, { useEffect, useState } from "react";
import axios from "axios";

const backHost = "localhost:8080" 

export default function App() {

  const [isLoggedIn, setLoggedIn] = useState(false);
  const [role, setRole] = useState("patient");

  let content;

   content = (!isLoggedIn) ? 
    <Login setLoggedIn = {setLoggedIn} /> :
    role == "patient" && <Patient /> ||
    role == "physician" && <Physician />

  return (
    <div>
      {content}
    </div>
  );

}

function Login({ setLoggedIn }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  function handleLoginRequest() {
    axios
      .post(`http://${backHost}/api/login`,
      {
        username,
        password
      })
      .then((response) => {
        localStorage.setItem("token", response.data)
        setLoggedIn(true)
      })
      .catch((error) => {
        console.error(error);
      });
  }

  return (
    <form className="centered">
      <input
        type="text"
        name="username"
        value={username}
        onChange={(event) => {
          setUsername(event.target.value);
        }}
      />
      <input
        type="password"
        name="password"
        value={password}
        onChange={(event) => {
          setPassword(event.target.value);
        }}
      />
      <button type="button" onClick={handleLoginRequest}>
        Login
      </button>
    </form>
  );
}


function Patient()
{

  return (
    <div>pas</div>
  )
}


function Physician()
{

  return (
    <div>pasfas</div>
  )
}

"use client";
import React, { ReactNode, useEffect, useState } from "react";
import axios from "axios";
import { backHost } from "./page";

export function Login({ setLoggedIn }) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
  
    function handleLoginRequest() {
      axios
        .post(`http://${backHost}/api/login`, {
          username,
          password,
        })
        .then((response) => {
          localStorage.setItem("authToken", response.data);
          setLoggedIn(true);
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
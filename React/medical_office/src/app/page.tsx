"use client";
import React, { ReactNode, useEffect, useState } from "react";
import axios from "axios";

const backHost = "localhost:8080" 

export default function App() {

  const [isLoggedIn, setLoggedIn] = useState(false);
  // const [role, setRole] = useState("patient");

  let content;

  useEffect(()=>{
    setLoggedIn(localStorage.getItem("authToken") != null)
    }, [isLoggedIn])


   content = (!isLoggedIn) ? 
    <Login setLoggedIn = {setLoggedIn} /> :
    <Home setLoggedIn = {setLoggedIn}/>

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
        localStorage.setItem("authToken", response.data)
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

function Home({ setLoggedIn })
{
  const [userData, setUserData] = useState({});
  const [links, setLinks] = useState({});

  useEffect(()=>{
    axios
    .get(`http://${backHost}/api/medical_office/self`, {
      headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
      }})
    .then((response) => {
      setLinks(response.data._links)
      delete response.data._links
      setUserData(response.data)
    })
    .catch((error) => {
      console.error(error);
    });
    
    }, [])
  

    function readOnlyKeyFilter(key: string): boolean
    {
      return (key == "userID" || key == "cnp" || key == "physicianId" || key == "isActive");
    }

    function updateUserData()
    {

      const formData = new FormData(document.getElementById("userDataForm") as HTMLFormElement);
      const newData = (Object.fromEntries(formData));

      delete newData.cnp
      delete newData.physicianID
      

      console.log(newData)

      axios
      .put(`http://${backHost}/api/medical_office/self`, newData, {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("authToken"),
        }})
      .then((response) => {
        alert("Succesful")
      })
      .catch((error) => {
         alert(error.response.status + " : " + error.response.data)
      });
    }
    
  return (
    <div> 
         <LogoutButton setLoggedIn = {setLoggedIn}/>

      <form className="centered" id="userDataForm">
        { Object.entries(userData).map(([key,value],i) => 
        <React.Fragment key={i}>
          <label>{key}</label>
          <input type={(key == "birthDay") ? "date" : "text"} name={key} key={i} defaultValue={value as string} readOnly={ readOnlyKeyFilter(key) }></input>
        </React.Fragment>

        )}       
        <button type="button" onClick={updateUserData}>Update</button>
      </form>
    </div>
  )
}


function LogoutButton({ setLoggedIn })
{
  
  function performLogout()
  {
    axios
    .put(`http://${backHost}/api/logout`, null, {
      headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
      }})
    .then((response) => {
      localStorage.removeItem("authToken")
      setLoggedIn(false)
    })
    .catch((error) => {
      console.error(error);
    });
  }

  return(
    <button id="logout" onClick={performLogout}>Logout</button>
  )
}

"use client";
import React, { ReactNode, useEffect, useState } from "react";
import axios from "axios";
import { link } from "fs";
import { ListPhysicians } from "./listPhysicians";
import { Login } from "./login";
import { ListAppointments } from "./listAppointments";


export const backHost = "localhost:8080";

export default function App() {
  const [isLoggedIn, setLoggedIn] = useState(false);

  let content;

  useEffect(() => {
    setLoggedIn(localStorage.getItem("authToken") != null);
  }, [isLoggedIn]);

  content = !isLoggedIn ? (
    <Login setLoggedIn={setLoggedIn} />
  ) : (
    <Home setLoggedIn={setLoggedIn} />
  );

  return <div>{content}</div>;
}



function Home({ setLoggedIn }) {
  const [userData, setUserData] = useState({});
  const [links, setLinks] = useState({});

  useEffect(() => {
    axios
      .get(`http://${backHost}/api/medical_office/self`, {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
        },
      })
      .then((response) => {
        console.log(response)

        setLinks(response.data._links);
        delete response.data._links;
        setUserData(response.data);
      })
      .catch((error) => {
        console.error(error);
      });
  }, []);

  function readOnlyKeyFilter(key: string): boolean {
    return (
      key == "userID" ||
      key == "cnp" ||
      key == "physicianId" ||
      key == "isActive"
    );
  }

  function updateUserData() {
    const formData = new FormData(
      document.getElementById("userDataForm") as HTMLFormElement
    );
    const newData = Object.fromEntries(formData);

    delete newData.cnp;
    delete newData.physicianID;

    axios
      .put(`http://${backHost}/api/medical_office/self`, newData, {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
        },
      })
      .then((response) => {
        alert("Succesful");
      })
      .catch((error) => {
        alert(error.response.status + " : " + error.response.data);
      });
  }

  return (
    <div>
      <LogoutButton setLoggedIn={setLoggedIn} />

      <form className="centered" id="userDataForm">
        {Object.entries(userData).map(([key, value], i) => (
          <React.Fragment key={i}>
            <label>{key}</label>
            <input
              type={key == "birthDay" ? "date" : "text"}
              name={key}
              key={i}
              defaultValue={value as string}
              readOnly={readOnlyKeyFilter(key)}
            ></input>
          </React.Fragment>
        ))}
        <button type="button" onClick={updateUserData}>
          Update
        </button>
      </form>

      {/* patient can see doctors */}
      {links.listPhysicians != undefined && (
        <>
          <ListPhysicians link={links.listPhysicians} />
        </>
      )}

      {links.appointments != undefined && (
        <>
        <ListAppointments link={links.appointments} />
        </>
      )}
    </div>
  );
}


function LogoutButton({ setLoggedIn }) {
  function performLogout() {
    axios
      .put(`http://${backHost}/api/logout`, null, {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
        },
      })
      .then((response) => {
        localStorage.removeItem("authToken");
        setLoggedIn(false);
      })
      .catch((error) => {
        console.error(error);
      });
  }

  return (
    <button id="logout" onClick={performLogout}>
      Logout
    </button>
  );
}

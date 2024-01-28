"use client";
import React, { ReactNode, useEffect, useState } from "react";
import axios from "axios";

const backHost = "localhost:8080";

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

function Login({ setLoggedIn }) {
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
    </div>
  );
}

function ListPhysicians({ link }) {
  const [physicianPage, setPhysicianPage] = useState(0);
  const [selectedPhysician, setSelectedPhysician] = useState(0);
  const [nameFilter, setNameFilter] = useState("");
  const [specializationFilter, setSpecializationFilter] = useState("");

  const [showPhysicianMode, setShowPhysicianMode] = useState(false);
  const [physicianList, setPhysicianList] = useState([]);
  const [pageLinks, setPageLinks] = useState({});

  useEffect(() => {
    listPhysicians();
  }, [nameFilter, specializationFilter, physicianPage]);

  function showPhysicians() {
    if (!showPhysicianMode) {
      setShowPhysicianMode(true);
    } else {
      setShowPhysicianMode(false);
      return;
    }
    listPhysicians();
  }

  function listPhysicians() {
    //construct link from template
    let uri = link.href as string;
    uri = uri
      .replace("{", "")
      .replace("}", "")
      .replace(RegExp(",", "g"), "&")
      .replace("page", "page=" + physicianPage)
      .replace("name", "name=" + nameFilter)
      .replace("specialization", "specialization=" + specializationFilter);

    axios
      .get(uri, {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
        },
      })
      .then((response) => {
        if (response.data._embedded == undefined) {
          setShowPhysicianMode(false);
          alert("Cannot find");
        } else {
          setPhysicianList(response.data._embedded.physicianResponseDTOList);
          setPageLinks(response.data._links);
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }

  function selectPhysician(event) {
    setSelectedPhysician(event.currentTarget.id);
  }

  function onPrev() {
    setPhysicianPage(physicianPage - 1);
  }

  function onNext() {
    setPhysicianPage(physicianPage + 1);
  }

  return (
    <>
      <div
        id="listPhysicians"
        className="centered"
        style={{ margin: "100px", borderColor: "white" }}
      >
        <button type="button" onClick={showPhysicians}>
          ToggleListPhysicians
        </button>
        <label>Filter by name</label>
        <input
          type="text"
          value={nameFilter}
          onChange={(event) => {
            setNameFilter(event.target.value);
          }}
        ></input>
        <label>Filter by specialization</label>
        <input
          type="text"
          value={specializationFilter}
          onChange={(event) => {
            setSpecializationFilter(event.target.value);
          }}
        ></input>
        <div
          className="physicians"
          style={{
            flexDirection: "row",
            display: "flex",
            gap: "5px",
            alignItems: "flex-start",
          }}
        >
          {showPhysicianMode &&
            physicianList.map(
              (obj: any, i) =>
                delete obj._links && (
                  <React.Fragment key={i}>
                    <div
                      className="physician"
                      id={obj.physicianId}
                      onClick={selectPhysician}
                      style={{
                        flexDirection: "column",
                        display: "flex",
                        borderWidth: "3px",
                        borderColor: "white",
                      }}
                    >
                      {Object.entries(obj).map(([key, value], i) => (
                        <React.Fragment key={i}>
                          <span style={{ fontStyle: "italic" }}>{key}</span>
                          <span
                            style={{
                              borderColor: "rgba(255,255,255,0.5)",
                              borderWidth: "2px",
                              color: "white",
                            }}
                          >
                            {value as string}
                          </span>
                        </React.Fragment>
                      ))}
                    </div>
                  </React.Fragment>
                )
            )}

          {showPhysicianMode && (
            <div>
              {pageLinks.prev != undefined && (
                <button onClick={onPrev}>Previous</button>
              )}
              {pageLinks.next != undefined && (
                <button onClick={onNext}>Next</button>
              )}
            </div>
          )}
        </div>
      </div>
    </>
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

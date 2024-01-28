"use client";
import React, { ReactNode, useEffect, useState } from "react";
import axios from "axios";

export function ListAppointments({ link }) {
  const [typeFilter, setTypeFilter] = useState("");
  const [dateFilter, setDateFilter] = useState("");
  const [appointmentsList, setAppointmentsList] = useState([]);

  useEffect(() => {
    // listAppointments()
  }, [typeFilter, dateFilter]);

  function listAppointments() {
    //construct link from template
    let uri = link.href as string;

    uri = uri
      .replace("{", "")
      .replace("}", "")
      .replace(RegExp(",", "g"), "&")
      .replace("type", (typeFilter == "") ? "" : "type=" + typeFilter)
      .replace("date", "date=" + dateFilter);

    axios
      .get(uri, {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("authToken"),
        },
      })
      .then((response) => {
        if (response.data._embedded == undefined) {
          alert("Cannot find");
        } else {
            if(response.data._embedded.appointmentPatientDTOList != undefined)
            {
                setAppointmentsList(
                    response.data._embedded.appointmentPatientDTOList
                  );
            }
            if(response.data._embedded.appointmentPhysicianDTOList != undefined)
            {
                setAppointmentsList(
                    response.data._embedded.appointmentPhysicianDTOList
                  );
            }

        }
      })
      .catch((error) => {
        alert(error.response.status + ":" + "Invalid parameters");
      });
  }

  return (
    <div className="centered">
      <select
        name="typeFilter"
        id="typeFilter"
        value={typeFilter}
        onChange={(event) => {
          setTypeFilter(event.target.value);
        }}
      >
        <option value="">Empty</option>
        <option value="day">Day</option>
        <option value="month">Month</option>
      </select>
      <input
        type="text"
        value={dateFilter}
        onChange={(event) => {
          setDateFilter(event.target.value);
        }}
      ></input>
      <button onClick={listAppointments}>ListAppointments</button>

      {appointmentsList.map(
        (obj: any, i) =>
          delete obj._links && (
            <React.Fragment key={i}>
              <div
                id={"appointment" + i}
                style={{ flexDirection: "column", display: "flex" }}
              >
                <label>Date</label>
                <input type="text" value={obj.date} readOnly></input>
                <label>Status</label>
                <input type="text" value={obj.status} readOnly></input>
                <div
                  style={{
                    borderWidth: "3px",
                    flexDirection: "column",
                    display: "flex",
                  }}
                >
                  {Object.entries((obj.patient != undefined) ? obj.patient : obj.physician ).map(([key, value], i) => (
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
              </div>
            </React.Fragment>
          )
      )}
    </div>
  );
}

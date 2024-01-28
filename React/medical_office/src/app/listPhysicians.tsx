
"use client";
import React, { ReactNode, useEffect, useState } from "react";
import axios from "axios";


export function ListPhysicians({ link }) {
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
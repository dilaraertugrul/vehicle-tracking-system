package com.example.vehicletrackingbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// Kullanmadığımız JSON alanlarını görmeden gelmek için

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmResponse {

    private String code; // OSRM isteğinin başarılı olup olmadığını belirtir

    private List<OsrmRoute> routes;

    public OsrmResponse() {

    }
    public String getCode(){
        return code;
    }

    public void setCode(String code){
        this.code = code;
    }

    public List<OsrmRoute> getRoutes(){
        return routes;
    }

    public void setRoutes(List<OsrmRoute> routes){
        this.routes = routes;
    }


}
